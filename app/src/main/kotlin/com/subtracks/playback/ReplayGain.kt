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
    val cap = peak?.takeIf { it.isFinite() && it > 0f }?.let(::maxSafeGainDb)
    val applied = if (cap != null) min(requested, cap) else requested
    return 10f.pow(applied / 20f).takeIf { it.isFinite() } ?: 1f
}

// gonic sends a peak of 0 when it only has album gain, so a non-positive or non-finite peak means
// "no peak data" and is ignored rather than treated as silence.
private fun maxSafeGainDb(peak: Float): Float = -20f * log10(peak)
