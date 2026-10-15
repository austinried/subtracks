package com.subtracks.playback

import com.subtracks.data.prefs.ReplayGainMode

internal class ReplayGainController(
    private val apply: (Float) -> Unit,
) {
    // Written from the playback thread by ReplayGainProcessor.onFlush and from the app thread by
    // the media item transition fallback.
    @Volatile
    private var track = ReplayGain()

    @Volatile
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
