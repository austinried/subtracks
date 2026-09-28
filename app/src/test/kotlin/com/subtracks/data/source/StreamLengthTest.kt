package com.subtracks.data.source

import com.subtracks.data.prefs.StreamQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamLengthTest {
    @Test
    fun directPlayHasNoDeclaredLength() {
        assertNull(declaredStreamLength(180_000, StreamQuality()))
    }

    @Test
    fun missingDurationHasNoDeclaredLength() {
        assertNull(declaredStreamLength(null, StreamQuality(96, "mp3")))
        assertNull(declaredStreamLength(0, StreamQuality(96, "mp3")))
    }

    @Test
    fun transcodedLengthIsAboveTheRequestedBitrate() {
        val length = declaredStreamLength(180_000, StreamQuality(96, "mp3"))!!

        assertTrue(length > 180 * 96_000 / 8)
    }

    @Test
    fun aTinyBitrateStillClearsTheEncodersFloor() {
        assertEquals(2_062_144L, declaredStreamLength(180_000, StreamQuality(24, "mp3")))
    }

    @Test
    fun losslessFormatsIgnoreTheBitrateAndAllowForTheLargestStream() {
        val cdQuality = declaredStreamLength(320_000, StreamQuality(64, "flac"))!!
        val highBitrate = declaredStreamLength(320_000, StreamQuality(320, "flac"))!!

        assertEquals(cdQuality, highBitrate)
        assertTrue(cdQuality >= 320 * 1_411_200 / 8)
    }

    @Test
    fun oggBasedFormatsGetNoEstimateBecauseSeekingReadsFromTheEnd() {
        assertNull(declaredStreamLength(180_000, StreamQuality(128, "opus")))
        assertNull(declaredStreamLength(180_000, StreamQuality(128, "ogg")))
        assertNull(declaredStreamLength(180_000, StreamQuality(128, "vorbis")))
    }

    @Test
    fun theLengthSuffixRoundTripsThroughTheFragment() {
        val uri = "http://host/rest/stream.view?id=s1" + streamLengthSuffix(1_234_567)

        assertEquals(1_234_567L, declaredLengthFromFragment(uri.substringAfter('#')))
        assertNull(declaredLengthFromFragment(null))
        assertNull(declaredLengthFromFragment("somethingElse=1"))
    }
}
