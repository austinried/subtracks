package com.subtracks.playback

import com.subtracks.data.model.Song
import com.subtracks.data.prefs.ReplayGainMode
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow

internal data class ReplayGain(
    val trackGain: Float? = null,
    val albumGain: Float? = null,
    val trackPeak: Float? = null,
    val albumPeak: Float? = null,
)

internal data class ReplayGainSettings(
    val mode: ReplayGainMode,
    val preampDb: Float,
)

internal fun Song.replayGain(): ReplayGain =
    ReplayGain(
        trackGain = trackGain,
        albumGain = albumGain,
        trackPeak = trackPeak,
        albumPeak = albumPeak,
    )

internal fun replayGainLinearGain(
    gain: ReplayGain,
    settings: ReplayGainSettings,
): Float {
    val (requestedDb, peak) =
        when (settings.mode) {
            ReplayGainMode.Off -> return 1f
            ReplayGainMode.Track -> (gain.trackGain ?: gain.albumGain) to (gain.trackPeak ?: gain.albumPeak)
            ReplayGainMode.Album -> (gain.albumGain ?: gain.trackGain) to (gain.albumPeak ?: gain.trackPeak)
        }
    val gainDb = requestedDb ?: return 1f
    val requested = gainDb + settings.preampDb
    val applied = if (peak != null && peak > 0f) min(requested, maxSafeGainDb(peak)) else requested
    return 10f.pow(applied / 20f)
}

// A peak of 1.0 is full scale, so anything above it leaves no headroom and this goes negative. A
// peak of zero or less carries no peak data (gonic sends 0 when only album gain is known), which
// is why the caller ignores it rather than treating it as silence.
private fun maxSafeGainDb(peak: Float): Float = -20f * log10(peak)
