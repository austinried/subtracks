package com.subtracks.playback

import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSourceException
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy

/**
 * A transcoded stream is chunked with no length, so the app declares an upper-bound length, and on
 * the first request the server is still producing the stream: a seek near that bound lands past the
 * data and comes back as POSITION_OUT_OF_RANGE (Navidrome answers 416 until it has cached the
 * transcode). That first request is what warms the cache, so one retry lands on a response with a
 * real Content-Length and the stream plays and seeks properly.
 */
@UnstableApi
internal class RetryOutOfRangePolicy(
    private val delayMs: Long = RETRY_DELAY_MS,
) : DefaultLoadErrorHandlingPolicy() {
    override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long =
        when {
            loadErrorInfo.exception.isOutOfRange() -> {
                if (loadErrorInfo.errorCount <= MAX_RETRIES) delayMs else C.TIME_UNSET
            }

            else -> {
                super.getRetryDelayMsFor(loadErrorInfo)
            }
        }

    private fun Throwable.isOutOfRange(): Boolean =
        generateSequence(this as Throwable?) { it.cause }.any {
            it is DataSourceException && it.reason == DataSourceException.POSITION_OUT_OF_RANGE
        }
}

private const val MAX_RETRIES = 1
private const val RETRY_DELAY_MS = 1_000L
