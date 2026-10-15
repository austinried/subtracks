package com.subtracks.playback

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.prefs.ReplayGainMode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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

    @Test
    fun theLatestItemLeavesItsGainApplied() {
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 0f))

        controller.applyItem(item("a", trackGain = 6.0206f))
        controller.applyItem(item("b", trackGain = -6.0206f))

        assertEquals(0.5f, applied.last(), TOLERANCE)
    }

    @Test
    fun anItemWithoutGainPlaysAtUnity() {
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 0f))

        controller.applyItem(item("a", trackGain = null))

        assertEquals(1f, applied.last(), TOLERANCE)
    }

    @Test
    fun noItemPlaysAtUnity() {
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 0f))
        controller.applyItem(item("a", trackGain = 6.0206f))

        controller.applyItem(null)

        assertEquals(1f, applied.last(), TOLERANCE)
    }

    private fun item(
        id: String,
        trackGain: Float?,
    ) = queueMediaItem(
        QueueItem(
            id = id,
            title = id,
            artist = null,
            album = null,
            coverArtId = null,
            replayGain = ReplayGain(trackGain = trackGain),
        ),
        local = null,
        stream = "http://server/$id",
        transcode = false,
    )

    private companion object {
        const val TOLERANCE = 0.001f
    }
}
