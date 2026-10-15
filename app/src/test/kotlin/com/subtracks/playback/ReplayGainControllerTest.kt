package com.subtracks.playback

import com.subtracks.data.prefs.ReplayGainMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ReplayGainControllerTest {
    private val applied = mutableListOf<Float>()
    private val controller = ReplayGainController { applied += it }

    @Test
    fun appliesTheGainForTheCurrentTrack() {
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 0f))
        controller.setTrack(ReplayGain(trackGain = -6.0206f))

        assertEquals(0.5f, applied.last(), TOLERANCE)
    }

    @Test
    fun recomputesWhenTheSettingsChange() {
        controller.setTrack(ReplayGain(trackGain = -6.0206f))
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 6.0206f))

        assertEquals(1f, applied.last(), TOLERANCE)
    }

    @Test
    fun switchingOffRestoresUnityGain() {
        controller.setTrack(ReplayGain(trackGain = -6.0206f))
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 0f))
        assertEquals(0.5f, applied.last(), TOLERANCE)

        controller.setSettings(ReplayGainSettings(ReplayGainMode.Off, 0f))

        assertEquals(1f, applied.last(), TOLERANCE)
    }

    @Test
    fun aTrackWithoutGainPlaysAtUnity() {
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 0f))
        controller.setTrack(null)

        assertEquals(1f, applied.last(), TOLERANCE)
    }

    private companion object {
        const val TOLERANCE = 0.001f
    }
}
