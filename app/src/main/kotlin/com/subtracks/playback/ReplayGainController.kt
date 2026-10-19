package com.subtracks.playback

import androidx.media3.common.MediaItem
import com.subtracks.data.prefs.ReplayGainMode

internal class ReplayGainController(
    private val apply: (Float) -> Unit,
) {
    private var track = ReplayGain()
    private var albumContext = false
    private var settings = ReplayGainSettings(ReplayGainMode.Auto, 0f)

    @Synchronized
    fun applyItem(item: MediaItem) {
        track = item.replayGain()
        albumContext = item.albumContext()
        applyGain()
    }

    @Synchronized
    fun setSettings(value: ReplayGainSettings) {
        settings = value
        applyGain()
    }

    private fun applyGain() {
        apply(replayGainLinearGain(track, settings, albumContext))
    }
}
