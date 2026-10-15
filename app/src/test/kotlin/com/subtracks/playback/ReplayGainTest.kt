package com.subtracks.playback

import com.subtracks.data.prefs.ReplayGainMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ReplayGainTest {
    @Test
    fun trackModeAppliesTheTrackGain() {
        assertEquals(
            0.5f,
            replayGainLinearGain(
                ReplayGain(trackGain = -6.0206f, albumGain = -1f, trackPeak = 1f, albumPeak = 1f),
                ReplayGainSettings(ReplayGainMode.Track, 0f),
            ),
            TOLERANCE,
        )
    }

    @Test
    fun trackModeFallsBackToTheAlbumGain() {
        assertEquals(
            0.5f,
            replayGainLinearGain(
                ReplayGain(albumGain = -6.0206f),
                ReplayGainSettings(ReplayGainMode.Track, 0f),
            ),
            TOLERANCE,
        )
    }

    @Test
    fun albumModeAppliesTheAlbumGain() {
        assertEquals(
            0.5f,
            replayGainLinearGain(
                ReplayGain(trackGain = -1f, albumGain = -6.0206f, trackPeak = 1f, albumPeak = 1f),
                ReplayGainSettings(ReplayGainMode.Album, 0f),
            ),
            TOLERANCE,
        )
    }

    @Test
    fun albumModeFallsBackToTheTrackGain() {
        assertEquals(
            0.5f,
            replayGainLinearGain(
                ReplayGain(trackGain = -6.0206f),
                ReplayGainSettings(ReplayGainMode.Album, 0f),
            ),
            TOLERANCE,
        )
    }

    @Test
    fun offAppliesUnityGain() {
        assertEquals(
            1f,
            replayGainLinearGain(
                ReplayGain(trackGain = -6.0206f, albumGain = -6.0206f),
                ReplayGainSettings(ReplayGainMode.Off, 0f),
            ),
            TOLERANCE,
        )
    }

    @Test
    fun aSongWithoutGainPlaysAtUnity() {
        assertEquals(
            1f,
            replayGainLinearGain(ReplayGain(), ReplayGainSettings(ReplayGainMode.Track, 3f)),
            TOLERANCE,
        )
    }

    @Test
    fun thePreampShiftsTheAppliedGain() {
        assertEquals(
            1f,
            replayGainLinearGain(
                ReplayGain(trackGain = -6.0206f),
                ReplayGainSettings(ReplayGainMode.Track, 6.0206f),
            ),
            TOLERANCE,
        )
    }

    @Test
    fun thePeakCapsAPositiveGain() {
        assertEquals(
            2f,
            replayGainLinearGain(
                ReplayGain(trackGain = 12f, trackPeak = 0.5f),
                ReplayGainSettings(ReplayGainMode.Track, 0f),
            ),
            TOLERANCE,
        )
    }

    @Test
    fun thePeakCapsUsingTheAlbumPeakWhenTheTrackPeakIsMissing() {
        assertEquals(
            2f,
            replayGainLinearGain(
                ReplayGain(trackGain = 12f, albumPeak = 0.5f),
                ReplayGainSettings(ReplayGainMode.Track, 0f),
            ),
            TOLERANCE,
        )
    }

    @Test
    fun aMissingOrZeroPeakDoesNotCap() {
        val settings = ReplayGainSettings(ReplayGainMode.Track, 0f)
        val expected = 3.9810717f

        assertEquals(expected, replayGainLinearGain(ReplayGain(trackGain = 12f), settings), TOLERANCE)
        assertEquals(expected, replayGainLinearGain(ReplayGain(trackGain = 12f, trackPeak = 0f), settings), TOLERANCE)
    }

    @Test
    fun aTruePeakAboveFullScaleCapsBelowUnity() {
        assertEquals(
            0.5f,
            replayGainLinearGain(
                ReplayGain(trackGain = 6f, trackPeak = 2f),
                ReplayGainSettings(ReplayGainMode.Track, 0f),
            ),
            TOLERANCE,
        )
    }

    @Test
    fun thePeakNeverLowersAnAttenuation() {
        assertEquals(
            0.31622776f,
            replayGainLinearGain(
                ReplayGain(trackGain = -10f, trackPeak = 0.9f),
                ReplayGainSettings(ReplayGainMode.Track, 0f),
            ),
            TOLERANCE,
        )
    }

    @Test
    fun aNonFinitePeakDoesNotCap() {
        assertEquals(
            3.9810717f,
            replayGainLinearGain(
                ReplayGain(trackGain = 12f, trackPeak = Float.POSITIVE_INFINITY),
                ReplayGainSettings(ReplayGainMode.Track, 0f),
            ),
            TOLERANCE,
        )
    }

    @Test
    fun playsAtUnityWhenTheGainIsNotFinite() {
        val settings = ReplayGainSettings(ReplayGainMode.Track, 0f)

        assertEquals(1f, replayGainLinearGain(ReplayGain(trackGain = Float.POSITIVE_INFINITY), settings), TOLERANCE)
        assertEquals(1f, replayGainLinearGain(ReplayGain(trackGain = Float.NaN), settings), TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.001f
    }
}
