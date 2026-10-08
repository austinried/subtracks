package com.subtracks.metadata

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

class MetadataConstraintsTest {
    @Test
    fun titlesFitStoreLimit() {
        localeFiles("title.txt").forEach { assertAtMost(it, TITLE_LIMIT) }
    }

    @Test
    fun shortDescriptionsFitStoreLimit() {
        localeFiles("short_description.txt").forEach { assertAtMost(it, SHORT_DESCRIPTION_LIMIT) }
    }

    @Test
    fun fullDescriptionsFitStoreLimit() {
        localeFiles("full_description.txt").forEach { assertAtMost(it, FULL_DESCRIPTION_LIMIT) }
    }

    @Test
    fun iconsAre512OpaqueAlphaPngsWithinOneMegabyte() {
        localeFiles("images/icon.png").forEach { file ->
            val image = readImage(file)
            assertEquals("${file.path} must be 512x512", 512, image.width)
            assertEquals("${file.path} must be 512x512", 512, image.height)
            assertEquals(
                "${file.path} must be a 32-bit PNG with alpha (Play); colorType ${image.colorType}",
                RGBA_COLOR_TYPE,
                image.colorType,
            )
            assertTrue("${file.path} must be at most 1 MiB", file.length() <= 1024 * 1024)
        }
    }

    @Test
    fun featureGraphicsAre1024x500WithoutAlpha() {
        localeFiles("images/featureGraphic.png").forEach { file ->
            val image = readImage(file)
            assertEquals("${file.path} must be 1024x500", 1024, image.width)
            assertEquals("${file.path} must be 1024x500", 500, image.height)
            assertNoAlpha(file, image)
        }
    }

    @Test
    fun phoneScreenshotsMeetStoreRules() {
        localeFiles("images/phoneScreenshots").forEach { dir ->
            val screenshots =
                dir
                    .listFiles()
                    .orEmpty()
                    .filter { it.isFile && it.extension.lowercase() in IMAGE_EXTENSIONS }
                    .sortedBy { it.name }
            assertTrue(
                "${dir.path} needs 2-8 phone screenshots, found ${screenshots.size}",
                screenshots.size in 2..8,
            )
            screenshots.forEach { file ->
                val image = readImage(file)
                val short = minOf(image.width, image.height)
                val long = maxOf(image.width, image.height)
                assertTrue("${file.path} shortest side $short must be at least 320", short >= 320)
                assertTrue("${file.path} longest side $long must be at most 3840", long <= 3840)
                assertTrue("${file.path} aspect ratio exceeds 2:1", long <= 2 * short)
                assertNoAlpha(file, image)
            }
        }
    }

    private fun assertNoAlpha(
        file: File,
        image: ImageInfo,
    ) {
        if (image.isPng) {
            assertEquals(
                "${file.path} must be a 24-bit PNG without alpha (colorType $RGB_COLOR_TYPE); colorType ${image.colorType}",
                RGB_COLOR_TYPE,
                image.colorType,
            )
        }
    }

    private fun assertAtMost(
        file: File,
        limit: Int,
    ) {
        val text = file.readText().replace("\r\n", "\n")
        val length = text.codePointCount(0, text.length)
        assertTrue("${file.path} is $length characters, over the $limit limit", length <= limit)
    }

    private fun localeFiles(relativePath: String): List<File> {
        val locales = File(repoRoot(), "metadata").listFiles().orEmpty().filter { it.isDirectory }
        assertTrue("no locales found under metadata/", locales.isNotEmpty())
        return locales.map { File(it, relativePath) }.onEach { assertTrue("missing ${it.path}", it.exists()) }
    }

    private fun repoRoot(): File =
        generateSequence(File(".").canonicalFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").isFile }

    private data class ImageInfo(
        val width: Int,
        val height: Int,
        val colorType: Int?,
        val isPng: Boolean,
    )

    private fun readImage(file: File): ImageInfo {
        val bytes = file.readBytes()
        if (bytes.isPng()) {
            return ImageInfo(
                width = bytes.beInt(PNG_WIDTH_OFFSET),
                height = bytes.beInt(PNG_HEIGHT_OFFSET),
                colorType = bytes[PNG_COLOR_TYPE_OFFSET].toInt() and 0xFF,
                isPng = true,
            )
        }
        val image = ImageIO.read(file)
        assertTrue("unreadable image ${file.path}", image != null)
        return ImageInfo(image.width, image.height, colorType = null, isPng = false)
    }

    private fun ByteArray.isPng(): Boolean =
        size > PNG_COLOR_TYPE_OFFSET &&
            this[0] == 0x89.toByte() &&
            this[1] == 'P'.code.toByte() &&
            this[2] == 'N'.code.toByte() &&
            this[3] == 'G'.code.toByte()

    private fun ByteArray.beInt(offset: Int): Int =
        ((this[offset].toInt() and 0xFF) shl 24) or
            ((this[offset + 1].toInt() and 0xFF) shl 16) or
            ((this[offset + 2].toInt() and 0xFF) shl 8) or
            (this[offset + 3].toInt() and 0xFF)

    private companion object {
        // Play allows 30 and F-Droid 50; the stricter wins.
        const val TITLE_LIMIT = 30

        // Play and F-Droid both allow 80.
        const val SHORT_DESCRIPTION_LIMIT = 80

        // Play and F-Droid both allow 4000.
        const val FULL_DESCRIPTION_LIMIT = 4000

        const val RGB_COLOR_TYPE = 2
        const val RGBA_COLOR_TYPE = 6
        val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg")

        const val PNG_WIDTH_OFFSET = 16
        const val PNG_HEIGHT_OFFSET = 20
        const val PNG_COLOR_TYPE_OFFSET = 25
    }
}
