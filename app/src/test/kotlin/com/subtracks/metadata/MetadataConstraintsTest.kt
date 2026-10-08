package com.subtracks.metadata

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.imageio.ImageIO

/**
 * Checks the committed store metadata against the stricter of the two stores' rules.
 *
 * Google Play, "Add preview assets to showcase your app":
 * https://support.google.com/googleplay/android-developer/answer/9866151
 * - Screenshots (Requirements): "JPEG or 24-bit PNG (no alpha)", "Minimum dimension: 320px",
 *   "Maximum dimension: 3840px", "The maximum dimension of your screenshot can't be more than
 *   twice as long as the minimum dimension"; "a minimum of two screenshots" and "up to 8".
 * - Screenshots (Highly recommended, not enforced here): "16:9 for landscape ... and 9:16 for
 *   portrait screenshots (minimum 1080x1920px)" for recommendation eligibility.
 * - App icon (Requirements): "32-bit PNG (with alpha)", "512px by 512px", "Maximum file size: 1024KB".
 * - Feature graphic (Requirements): "JPEG or 24-bit PNG (no alpha)", "1024px by 500px".
 * - Short description (Requirements): "80 character limit".
 *
 * F-Droid, "All About Descriptions, Graphics, and Screenshots":
 * https://f-droid.org/docs/All_About_Descriptions_Graphics_and_Screenshots/
 * - Fastlane structure: "title.txt (app name, max 50 chars)",
 *   "short_description.txt (short description, max 80 chars)",
 *   "full_description.txt (full app description, max 4000 chars)", "changelogs/... (max 500 chars)".
 */
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
    fun changelogsFitStoreLimit() {
        localeFiles("changelogs").forEach { dir ->
            dir
                .listFiles()
                .orEmpty()
                .filter { it.isFile && it.extension == "txt" }
                .forEach { assertAtMost(it, CHANGELOG_LIMIT) }
        }
    }

    // Play (Requirements): "32-bit PNG (with alpha)", "512px by 512px", "Maximum file size: 1024KB".
    @Test
    fun iconsAre512AlphaPngsWithinOneMegabyte() {
        localeFiles("images/icon.png").forEach { file ->
            val image = readImage(file)
            assertEquals("${file.path} must be a PNG", ImageFormat.PNG, image.format)
            assertEquals("${file.path} must be 512x512", 512, image.width)
            assertEquals("${file.path} must be 512x512", 512, image.height)
            assertEquals(
                "${file.path} must be a 32-bit PNG with alpha (Play); colorType ${image.colorType}",
                RGBA_COLOR_TYPE,
                image.colorType,
            )
            assertTrue("${file.path} must be an 8-bit PNG; bitDepth ${image.bitDepth}", image.bitDepth == 8)
            assertTrue("${file.path} must be at most 1 MiB", file.length() <= 1024 * 1024)
        }
    }

    // Play (Requirements): "JPEG or 24-bit PNG (no alpha)", "1024px by 500px".
    @Test
    fun featureGraphicsAre1024x500WithoutAlpha() {
        localeFiles("images/featureGraphic.png").forEach { file ->
            val image = readImage(file)
            assertEquals("${file.path} must be 1024x500", 1024, image.width)
            assertEquals("${file.path} must be 1024x500", 500, image.height)
            assertOpaquePngOrJpeg(file, image)
        }
    }

    // Play (Requirements): 2-8 screenshots, sides 320-3840px, and "The maximum dimension of your
    // screenshot can't be more than twice as long as the minimum dimension" (the 2:1 ratio).
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
                assertOpaquePngOrJpeg(file, image)
            }
        }
    }

    private fun assertOpaquePngOrJpeg(
        file: File,
        image: ImageInfo,
    ) {
        when (image.format) {
            ImageFormat.PNG -> {
                assertEquals(
                    "${file.path} must be a 24-bit PNG without alpha (colorType $RGB_COLOR_TYPE); colorType ${image.colorType}",
                    RGB_COLOR_TYPE,
                    image.colorType,
                )
                assertTrue("${file.path} must be an 8-bit PNG; bitDepth ${image.bitDepth}", image.bitDepth == 8)
                assertTrue("${file.path} must not carry a tRNS (transparency) chunk", !image.hasTrns)
            }

            ImageFormat.JPEG -> {
                Unit
            }

            ImageFormat.OTHER -> {
                assertTrue("${file.path} must be a PNG or JPEG", false)
            }
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

    private enum class ImageFormat { PNG, JPEG, OTHER }

    private data class ImageInfo(
        val width: Int,
        val height: Int,
        val colorType: Int?,
        val bitDepth: Int?,
        val hasTrns: Boolean,
        val format: ImageFormat,
    )

    private fun readImage(file: File): ImageInfo {
        val bytes = file.readBytes()
        if (bytes.isPng()) {
            return ImageInfo(
                width = bytes.beInt(PNG_WIDTH_OFFSET),
                height = bytes.beInt(PNG_HEIGHT_OFFSET),
                colorType = bytes[PNG_COLOR_TYPE_OFFSET].toInt() and 0xFF,
                bitDepth = bytes[PNG_BIT_DEPTH_OFFSET].toInt() and 0xFF,
                hasTrns = bytes.hasTrnsChunk(),
                format = ImageFormat.PNG,
            )
        }
        val image = ImageIO.read(file)
        assertTrue("unreadable image ${file.path}", image != null)
        return ImageInfo(
            image.width,
            image.height,
            null,
            null,
            hasTrns = false,
            format = if (bytes.isJpeg()) ImageFormat.JPEG else ImageFormat.OTHER,
        )
    }

    private fun ByteArray.hasTrnsChunk(): Boolean {
        var offset = PNG_SIGNATURE_SIZE
        while (offset + 8 <= size) {
            val length = beInt(offset)
            val type = String(this, offset + 4, 4, Charsets.US_ASCII)
            if (type == "tRNS") return true
            if (type == "IEND" || length < 0) return false
            offset += PNG_CHUNK_OVERHEAD + length
        }
        return false
    }

    private fun ByteArray.isPng(): Boolean =
        size > PNG_COLOR_TYPE_OFFSET &&
            this[0] == 0x89.toByte() &&
            this[1] == 'P'.code.toByte() &&
            this[2] == 'N'.code.toByte() &&
            this[3] == 'G'.code.toByte()

    private fun ByteArray.isJpeg(): Boolean = size > 2 && this[0] == 0xFF.toByte() && this[1] == 0xD8.toByte()

    private fun ByteArray.beInt(offset: Int): Int =
        ((this[offset].toInt() and 0xFF) shl 24) or
            ((this[offset + 1].toInt() and 0xFF) shl 16) or
            ((this[offset + 2].toInt() and 0xFF) shl 8) or
            (this[offset + 3].toInt() and 0xFF)

    private companion object {
        // Play (Requirements): 30; F-Droid title.txt: 50. Stricter wins.
        const val TITLE_LIMIT = 30

        // Play short description and F-Droid short_description.txt: 80.
        const val SHORT_DESCRIPTION_LIMIT = 80

        // F-Droid full_description.txt: 4000.
        const val FULL_DESCRIPTION_LIMIT = 4000

        // Play "What's new" and F-Droid changelogs: 500.
        const val CHANGELOG_LIMIT = 500

        const val RGB_COLOR_TYPE = 2
        const val RGBA_COLOR_TYPE = 6
        val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg")

        const val PNG_WIDTH_OFFSET = 16
        const val PNG_HEIGHT_OFFSET = 20
        const val PNG_BIT_DEPTH_OFFSET = 24
        const val PNG_COLOR_TYPE_OFFSET = 25
        const val PNG_SIGNATURE_SIZE = 8
        const val PNG_CHUNK_OVERHEAD = 12
    }
}
