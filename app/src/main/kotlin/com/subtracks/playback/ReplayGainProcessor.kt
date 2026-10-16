package com.subtracks.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.audio.AudioProcessor.StreamMetadata
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
internal class ReplayGainProcessor(
    private val onItem: (MediaItem) -> Unit = {},
) : BaseAudioProcessor() {
    @Volatile
    private var linearGain = 1f

    fun setLinearGain(value: Float) {
        linearGain = value
    }

    override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat = inputAudioFormat

    // DefaultAudioSink flushes the processors on every configuration, which happens at each media
    // item transition even when the format is unchanged, and hands over the timeline and the period
    // being flushed (androidx/media#418). That is early enough to set the new item's gain before any
    // of its buffers are processed, unlike Player.Listener.onMediaItemTransition, which the app
    // thread receives afterwards. A flush with no period comes from a DefaultAudioSink reset rather
    // than an item boundary, hence the ignore.
    override fun onFlush(streamMetadata: StreamMetadata) {
        streamMetadata.currentItem()?.let(onItem)
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val output = replaceOutputBuffer(inputBuffer.remaining())
        val gain = linearGain.takeIf { it.isFinite() } ?: 1f
        when (inputAudioFormat.encoding) {
            C.ENCODING_PCM_16BIT -> {
                while (inputBuffer.remaining() >= 2) {
                    output.putShort((inputBuffer.short * gain).roundToInt().coerceIn(MIN_SAMPLE, MAX_SAMPLE).toShort())
                }
            }

            C.ENCODING_PCM_FLOAT -> {
                while (inputBuffer.remaining() >= 4) output.putFloat(inputBuffer.float * gain)
            }

            else -> {
                if (inputBuffer.hasRemaining()) output.put(inputBuffer)
            }
        }
        inputBuffer.position(inputBuffer.limit())
        output.flip()
    }

    private companion object {
        const val MIN_SAMPLE = -32768
        const val MAX_SAMPLE = 32767
    }
}

@OptIn(UnstableApi::class)
internal fun StreamMetadata.currentItem(): MediaItem? {
    val uid = periodUid ?: return null
    val index = timeline.getIndexOfPeriod(uid)
    if (index == C.INDEX_UNSET) return null
    val period = timeline.getPeriod(index, Timeline.Period())
    return timeline.getWindow(period.windowIndex, Timeline.Window()).mediaItem
}
