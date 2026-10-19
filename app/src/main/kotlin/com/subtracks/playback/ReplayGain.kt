package com.subtracks.playback

import com.subtracks.data.model.Song
import com.subtracks.data.prefs.ReplayGainMode
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow

data class ReplayGain(
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
    albumContext: Boolean = false,
): Float {
    if (settings.mode == ReplayGainMode.Off) return 1f
    // gonic sends a peak of 0 when it only has album gain, so a non-positive or non-finite peak means
    // "no peak data" and is ignored rather than treated as silence. A boost cannot be checked for
    // headroom without a peak, so a peakless track is only ever attenuated.
    val trackPeak = gain.trackPeak?.takeIf { it.isFinite() && it > 0f }
    val albumPeak = gain.albumPeak?.takeIf { it.isFinite() && it > 0f }
    val album = settings.mode == ReplayGainMode.Album || (settings.mode == ReplayGainMode.Auto && albumContext)
    val (requestedDb, peak) =
        if (album) {
            (gain.albumGain ?: gain.trackGain) to (albumPeak ?: trackPeak)
        } else {
            (gain.trackGain ?: gain.albumGain) to (trackPeak ?: albumPeak)
        }
    val gainDb = requestedDb?.takeIf { it.isFinite() } ?: return 1f
    val requested = gainDb + settings.preampDb
    val maxGainDb = peak?.let(::maxSafeGainDb) ?: 0f
    val applied = min(requested, maxGainDb)
    return 10f.pow(applied / 20f).takeIf { it.isFinite() && it > 0f } ?: 1f
}

private fun maxSafeGainDb(peak: Float): Float = -20f * log10(peak)
