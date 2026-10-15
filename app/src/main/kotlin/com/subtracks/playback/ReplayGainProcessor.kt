package com.subtracks.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
internal class ReplayGainProcessor : BaseAudioProcessor() {
    @Volatile
    private var linearGain = 1f

    fun setLinearGain(value: Float) {
        linearGain = value
    }

    override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat = inputAudioFormat

    override fun queueInput(inputBuffer: ByteBuffer) {
        val output = replaceOutputBuffer(inputBuffer.remaining())
        val gain = linearGain
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
                output.put(inputBuffer)
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
