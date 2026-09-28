package com.subtracks.data.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class AudioEncodingReaderTest {
    @Test
    fun containerHeadersNameTheCommonCodecs() {
        assertEquals("audio/flac", sniff("fLaC\u0000\u0000\u0000\u0022"))
        assertEquals("audio/mpeg", sniff("ID3\u0004\u0000\u0000"))
        assertEquals("audio/mpeg", sniff("\u00FF\u00FB\u0090\u0000"))
        assertEquals("audio/wav", sniff("RIFF\u0000\u0000\u0000\u0000WAVE"))
        assertEquals("audio/mp4", sniff("\u0000\u0000\u0000\u001cftypM4A "))
        assertEquals("audio/webm", sniff("\u001A\u0045\u00DF\u00A3"))
    }

    @Test
    fun aContainerIsNotTheCodecInsideIt() {
        assertEquals("audio/opus", sniff("\u001A\u0045\u00DF\u00A3webm\u0000A_OPUS\u0000"))
        assertEquals("audio/vorbis", sniff("\u001A\u0045\u00DF\u00A3webm\u0000A_VORBIS\u0000"))
        assertEquals("audio/webm", sniff("\u001A\u0045\u00DF\u00A3webm\u0000"))
        assertEquals("audio/alac", sniff("\u0000\u0000\u0000\u001cftypM4A \u0000\u0000\u0000\u0000alac"))
    }

    @Test
    fun anOggPageNamesTheCodecInsideIt() {
        assertEquals("audio/opus", sniff("OggS\u0000\u0002\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000OpusHead"))
        assertEquals("audio/vorbis", sniff("OggS\u0000\u0002\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001vorbis"))
        assertEquals("audio/ogg", sniff("OggS\u0000\u0002\u0000\u0000"))
    }

    @Test
    fun anythingElseIsUnknown() {
        assertNull(sniff("<?xml version=\"1.0\"?><subsonic-response/>"))
        assertNull(sniff(""))
    }

    private fun sniff(contents: String): String? =
        File.createTempFile("audio", ".tmp").apply { writeBytes(contents.toByteArray(Charsets.ISO_8859_1)) }.let {
            try {
                sniffAudioMime(it)
            } finally {
                it.delete()
            }
        }
}
