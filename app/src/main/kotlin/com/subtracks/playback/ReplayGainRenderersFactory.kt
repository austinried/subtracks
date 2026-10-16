package com.subtracks.playback

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink

@OptIn(UnstableApi::class)
internal class ReplayGainRenderersFactory(
    context: Context,
    private val processor: ReplayGainProcessor,
) : DefaultRenderersFactory(context) {
    // Mirrors the stock DefaultRenderersFactory.buildAudioSink. setAudioProcessors keeps the default
    // chain and prepends ours; DefaultAudioSink omits the whole chain when float output is enabled.
    public override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioOutputPlaybackParameters: Boolean,
    ): AudioSink =
        DefaultAudioSink
            .Builder(context)
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioOutputPlaybackParameters(enableAudioOutputPlaybackParameters)
            .setAudioProcessors(arrayOf(processor))
            .build()
}
