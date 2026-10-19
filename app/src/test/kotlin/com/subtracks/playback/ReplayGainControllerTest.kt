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
    fun appliesTheItemsGain() {
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 0f))
        controller.applyItem(item("a", trackGain = -6.0206f))

        assertEquals(0.5f, applied.last(), TOLERANCE)
    }

    @Test
    fun recomputesWhenTheSettingsChange() {
        controller.applyItem(item("a", trackGain = -6.0206f))
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 6.0206f))

        assertEquals(1f, applied.last(), TOLERANCE)
    }

    @Test
    fun switchingOffRestoresUnityGain() {
        controller.applyItem(item("a", trackGain = -6.0206f))
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 0f))
        assertEquals(0.5f, applied.last(), TOLERANCE)

        controller.setSettings(ReplayGainSettings(ReplayGainMode.Off, 0f))

        assertEquals(1f, applied.last(), TOLERANCE)
    }

    @Test
    fun dynamicModeFollowsTheItemsGainContext() {
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Dynamic, 0f))

        controller.applyItem(item("a", trackGain = -6.0206f, albumGain = -1f))
        assertEquals(0.5f, applied.last(), TOLERANCE)

        controller.applyItem(item("b", trackGain = -1f, albumGain = -6.0206f, albumContext = true))
        assertEquals(0.5f, applied.last(), TOLERANCE)
    }

    @Test
    fun anItemWithoutGainPlaysAtUnity() {
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 0f))
        controller.applyItem(item("a", trackGain = -6.0206f))
        assertEquals(0.5f, applied.last(), TOLERANCE)

        controller.applyItem(item("b", trackGain = null))

        assertEquals(1f, applied.last(), TOLERANCE)
    }

    @Test
    fun theLatestItemLeavesItsGainApplied() {
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 0f))

        controller.applyItem(item("a", trackGain = 6.0206f, trackPeak = 0.5f))
        assertEquals(2f, applied.last(), TOLERANCE)

        controller.applyItem(item("b", trackGain = -6.0206f))

        assertEquals(0.5f, applied.last(), TOLERANCE)
    }

    private fun item(
        id: String,
        trackGain: Float?,
        trackPeak: Float? = null,
        albumGain: Float? = null,
        albumContext: Boolean = false,
    ) = queueMediaItem(
        QueueItem(
            id = id,
            title = id,
            artist = null,
            album = null,
            coverArtId = null,
            replayGain = ReplayGain(trackGain = trackGain, albumGain = albumGain, trackPeak = trackPeak),
            albumContext = albumContext,
        ),
        local = null,
        stream = "http://server/$id",
        transcode = false,
    )

    private companion object {
        const val TOLERANCE = 0.001f
    }
}
