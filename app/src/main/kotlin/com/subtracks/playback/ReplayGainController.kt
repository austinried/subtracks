package com.subtracks.playback

import com.subtracks.data.prefs.ReplayGainMode

internal class ReplayGainController(
    private val apply: (Float) -> Unit,
) {
    private var track = ReplayGain()
    private var settings = ReplayGainSettings(ReplayGainMode.Track, 0f)

    fun setTrack(value: ReplayGain?) {
        track = value ?: ReplayGain()
        applyGain()
    }

    fun setSettings(value: ReplayGainSettings) {
        settings = value
        applyGain()
    }

    private fun applyGain() {
        apply(replayGainLinearGain(track, settings))
    }
}
