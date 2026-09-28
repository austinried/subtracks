package com.subtracks.data.source

import com.subtracks.data.prefs.StreamQuality

internal const val STREAM_LENGTH_FRAGMENT = "streamLength"

private const val MIN_TRANSCODE_BPS = 64_000L
private const val UNCAPPED_BPS = 2_000_000L
private const val LOSSLESS_CEILING_BPS = 9_216_000L
private const val OVERHEAD_BYTES = 256L * 1024L
private val LOSSLESS_FORMATS = setOf("flac", "wav", "alac")

// Ogg-based streams seek by binary-searching for the last page from the end of the file, so a
// length estimate that overshoots makes the player read past the real data and the server answers
// 416 (POSITION_OUT_OF_RANGE). These get no estimate and seek only within what is buffered.
private val END_SEEKING_FORMATS = setOf("opus", "ogg", "vorbis")

fun declaredStreamLength(
    durationMs: Long?,
    quality: StreamQuality,
): Long? {
    if (durationMs == null || durationMs <= 0) return null
    if (!quality.transcodes) return null
    if (quality.format?.lowercase() in END_SEEKING_FORMATS) return null
    val bps =
        when {
            quality.format?.lowercase() in LOSSLESS_FORMATS -> LOSSLESS_CEILING_BPS
            quality.maxBitrate > 0 -> maxOf(quality.maxBitrate * 1000L, MIN_TRANSCODE_BPS)
            else -> UNCAPPED_BPS
        }
    return durationMs / 1000 * bps / 8 * 5 / 4 + OVERHEAD_BYTES
}

internal fun streamLengthSuffix(length: Long): String = "#$STREAM_LENGTH_FRAGMENT=$length"

internal fun declaredLengthFromFragment(fragment: String?): Long? {
    val marker = "$STREAM_LENGTH_FRAGMENT="
    if (fragment == null || !fragment.startsWith(marker)) return null
    return fragment.substring(marker.length).toLongOrNull()?.takeIf { it > 0 }
}
