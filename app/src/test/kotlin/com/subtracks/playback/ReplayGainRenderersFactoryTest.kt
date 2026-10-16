package com.subtracks.playback

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.audio.AudioProcessorChain
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(UnstableApi::class)
@RunWith(AndroidJUnit4::class)
class ReplayGainRenderersFactoryTest {
    @Test
    fun installsTheGainProcessorInTheAudioSink() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val processor = ReplayGainProcessor()

        val sink = ReplayGainRenderersFactory(context, processor).buildAudioSink(context, false, false)

        // DefaultAudioSink exposes no accessor for the chain it was built with.
        val chain =
            DefaultAudioSink::class.java
                .getDeclaredField("audioProcessorChain")
                .apply { isAccessible = true }
                .get(sink) as AudioProcessorChain

        assertEquals(true, chain.audioProcessors.any { it === processor })
    }
}
