package com.subtracks.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AudioEncodingTest {
    @Test
    fun commonTypesGetReadableNames() {
        assertEquals("MP3", AudioEncoding("audio/mpeg").format)
        assertEquals("FLAC", AudioEncoding("audio/flac").format)
        assertEquals("AAC", AudioEncoding("audio/mp4").format)
        assertEquals("Opus", AudioEncoding("audio/opus").format)
    }

    @Test
    fun anUnknownTypeFallsBackToItsSubtype() {
        assertEquals("X-CUSTOM", AudioEncoding("audio/x-custom").format)
    }

    @Test
    fun noTypeMeansNoFormat() {
        assertNull(AudioEncoding(null).format)
    }
}
