package com.subtracks.playback

import androidx.media3.common.MediaItem
import com.subtracks.data.prefs.ReplayGainMode

internal class ReplayGainController(
    private val apply: (Float) -> Unit,
) {
    private var track = ReplayGain()
    private var settings = ReplayGainSettings(ReplayGainMode.Track, 0f)

    // Called from the playback thread, by ReplayGainProcessor.onFlush.
    @Synchronized
    fun applyItem(item: MediaItem) {
        track = item.replayGain()
        applyGain()
    }

    // Called from the app thread, when the preferences change.
    @Synchronized
    fun setSettings(value: ReplayGainSettings) {
        settings = value
        applyGain()
    }

    private fun applyGain() {
        apply(replayGainLinearGain(track, settings))
    }
}
