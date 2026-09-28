package com.subtracks.data.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AudioFileTest {
    @Test
    fun anXmlErrorDocumentIsNotAudio() {
        assertFalse(file("<?xml version=\"1.0\"?><subsonic-response status=\"failed\"/>").looksLikeAudio())
    }

    @Test
    fun leadingWhitespaceAndABomDoNotHideMarkup() {
        assertFalse(file("\uFEFF\n  <html><body>Gateway timeout</body></html>").looksLikeAudio())
    }

    @Test
    fun aJsonErrorBodyIsNotAudio() {
        assertFalse(file("{\"subsonic-response\":{\"status\":\"failed\"}}").looksLikeAudio())
    }

    @Test
    fun anEmptyFileIsNotAudio() {
        assertFalse(file("").looksLikeAudio())
    }

    @Test
    fun aMissingFileIsNotAudio() {
        assertFalse(File.createTempFile("absent", ".tmp").apply { delete() }.looksLikeAudio())
    }

    @Test
    fun audioHeadersPass() {
        assertTrue(file("audio").looksLikeAudio())
        assertTrue(file("ID3\u0004\u0000audio").looksLikeAudio())
        assertTrue(file("fLaC\u0000\u0000\u0000\u0022").looksLikeAudio())
    }

    private fun file(contents: String): File = File.createTempFile("audio", ".tmp").apply { writeText(contents) }.also { it.deleteOnExit() }
}
