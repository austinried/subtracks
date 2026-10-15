package com.subtracks.playback

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.source.MediaSource.MediaPeriodId
import androidx.media3.exoplayer.source.SinglePeriodTimeline
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder

// Pins the media3 behaviour the whole feature rests on: DefaultAudioSink hands the processor the
// timeline and the period being configured, on every configuration, which is what lets the gain be
// set before a new item's buffers. See androidx/media#418.
@OptIn(UnstableApi::class)
@RunWith(AndroidJUnit4::class)
class ReplayGainSinkTest {
    @Test
    fun theSinkHandsTheItemBeingConfiguredToTheProcessor() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val reported = mutableListOf<MediaItem>()
        val processor = ReplayGainProcessor { reported += it }
        val sink = ReplayGainRenderersFactory(context, processor).buildAudioSink(context, false, false)

        val item = MediaItem.Builder().setMediaId("b").build()
        val timeline = SinglePeriodTimeline(DURATION_US, false, false, false, null, item)
        sink.configure(
            AudioSink.AudioSinkConfig
                .Builder(pcmFormat())
                .setTimeline(timeline)
                .setMediaPeriodId(MediaPeriodId(timeline.getUidOfPeriod(0)))
                .build(),
        )
        sink.handleBuffer(sixteenBit(1000), 0, 1)

        assertEquals(listOf(item), reported)
    }

    private fun pcmFormat() =
        Format
            .Builder()
            .setSampleMimeType(MimeTypes.AUDIO_RAW)
            .setPcmEncoding(C.ENCODING_PCM_16BIT)
            .setChannelCount(2)
            .setSampleRate(44_100)
            .build()

    private fun sixteenBit(vararg samples: Int): ByteBuffer =
        ByteBuffer.allocate(samples.size * 2).order(ByteOrder.nativeOrder()).apply {
            samples.forEach { putShort(it.toShort()) }
            flip()
        }

    private companion object {
        const val DURATION_US = 1_000_000L
    }
}
