package com.subtracks.playback

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import com.subtracks.log.Log

@OptIn(UnstableApi::class)
internal class ReplayGainRenderersFactory(
    context: Context,
    private val processor: ReplayGainProcessor,
) : DefaultRenderersFactory(context) {
    // Mirrors the stock DefaultRenderersFactory.buildAudioSink. setAudioProcessors keeps the default
    // chain and prepends ours; DefaultAudioSink omits the whole chain when float output is enabled,
    // and AudioSink.setOffloadMode bypasses it entirely.
    public override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioOutputPlaybackParameters: Boolean,
    ): AudioSink {
        if (enableFloatOutput) {
            Log.w(FLOAT_OUTPUT_TAG, "float output is on, so replay gain is not applied to high-resolution tracks")
        }
        return DefaultAudioSink
            .Builder(context)
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioOutputPlaybackParameters(enableAudioOutputPlaybackParameters)
            .setAudioProcessors(arrayOf(processor))
            .build()
    }

    private companion object {
        const val FLOAT_OUTPUT_TAG = "replaygain"
    }
}
