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
    // Mirrors the stock DefaultRenderersFactory.buildAudioSink, plus the processor. setAudioProcessors
    // keeps the default chain and prepends ours, but DefaultAudioSink drops the whole chain when
    // float output is enabled, so enabling that would silently disable replay gain.
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
