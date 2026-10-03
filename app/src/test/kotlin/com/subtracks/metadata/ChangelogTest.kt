package com.subtracks.metadata

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ChangelogTest {
    @Test
    fun changelogsFitStoreLimit() {
        val root =
            generateSequence(File(".").canonicalFile) { it.parentFile }
                .first { File(it, "settings.gradle.kts").isFile }
        val files =
            File(root, "metadata")
                .listFiles()
                .orEmpty()
                .flatMap { locale -> File(locale, "changelogs").listFiles().orEmpty().toList() }
                .filter { it.isFile && it.extension == "txt" }
        assertTrue("no changelog files found under metadata/", files.isNotEmpty())
        files.forEach { file ->
            val text = file.readText().replace("\r\n", "\n")
            val length = text.codePointCount(0, text.length)
            assertTrue(
                "metadata changelogs must be at most $LIMIT characters; ${file.name} is $length",
                length <= LIMIT,
            )
        }
    }

    private companion object {
        const val LIMIT = 500
    }
}
