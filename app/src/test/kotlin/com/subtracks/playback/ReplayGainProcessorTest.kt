package com.subtracks.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.audio.AudioProcessor.StreamMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.SinglePeriodTimeline
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.prefs.ReplayGainMode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder

@OptIn(UnstableApi::class)
@RunWith(AndroidJUnit4::class)
class ReplayGainProcessorTest {
    @Test
    fun scalesSixteenBitSamples() {
        val processor = processor(gain = 0.5f)

        processor.queueInput(sixteenBit(100, -200, 1000, -1000))

        assertEquals(listOf(50, -100, 500, -500), readShorts(processor.getOutput()))
    }

    @Test
    fun passesSamplesThroughAtUnityGain() {
        val processor = processor(gain = 1f)

        processor.queueInput(sixteenBit(1234, -4321))

        assertEquals(listOf(1234, -4321), readShorts(processor.getOutput()))
    }

    @Test
    fun saturatesInsteadOfWrappingWhenTheGainOverflows() {
        val processor = processor(gain = 4f)

        processor.queueInput(sixteenBit(20000, -20000))

        assertEquals(listOf(32767, -32768), readShorts(processor.getOutput()))
    }

    @Test
    fun scalesFloatSamples() {
        val processor = processor(gain = 0.5f, encoding = C.ENCODING_PCM_FLOAT)

        processor.queueInput(floatBuffer(1f, -0.5f))

        assertEquals(listOf(0.5f, -0.25f), readFloats(processor.getOutput()))
    }

    @Test
    fun passesAnUnsupportedEncodingThroughUnchanged() {
        val processor = processor(gain = 2f, encoding = C.ENCODING_PCM_24BIT)

        processor.queueInput(ByteBuffer.wrap(byteArrayOf(1, 2, 3)))

        assertEquals(listOf<Byte>(1, 2, 3), readBytes(processor.getOutput()))
    }

    @Test
    fun passesSamplesThroughWhenTheGainIsNaN() {
        val processor = processor(gain = Float.NaN)

        processor.queueInput(sixteenBit(1234, -4321))

        assertEquals(listOf(1234, -4321), readShorts(processor.getOutput()))
    }

    @Test
    fun passesSamplesThroughWhenTheGainIsInfinite() {
        val processor = processor(gain = Float.POSITIVE_INFINITY)

        processor.queueInput(sixteenBit(1234, -4321))

        assertEquals(listOf(1234, -4321), readShorts(processor.getOutput()))
    }

    @Test
    fun keepsTheInputFormatSoTheSinkKeepsItInThePipeline() {
        val processor = ReplayGainProcessor()

        val configured = processor.configure(AudioFormat(SAMPLE_RATE, CHANNELS, C.ENCODING_PCM_16BIT))

        assertEquals(C.ENCODING_PCM_16BIT, configured.encoding)
        assertEquals(true, processor.isActive())
    }

    @Test
    fun aFlushReportsTheItemBeingFlushed() {
        val reported = mutableListOf<MediaItem?>()
        val item = item("a", trackGain = -6.0206f)

        ReplayGainProcessor { reported += it }.flush(streamMetadata(item))

        assertEquals(listOf(item), reported)
    }

    @Test
    fun aFlushWithoutAPeriodReportsNoItem() {
        val reported = mutableListOf<MediaItem?>()

        ReplayGainProcessor { reported += it }.flush(StreamMetadata.DEFAULT)

        assertEquals(listOf(null), reported)
    }

    @Test
    fun aFlushMovesTheGainToTheNextItem() {
        lateinit var processor: ReplayGainProcessor
        val controller = ReplayGainController { processor.setLinearGain(it) }
        processor = ReplayGainProcessor { controller.applyItem(it) }
        controller.setSettings(ReplayGainSettings(ReplayGainMode.Track, 0f))
        processor.configure(AudioFormat(SAMPLE_RATE, CHANNELS, C.ENCODING_PCM_16BIT))

        processor.flush(streamMetadata(item("a", trackGain = -6.0206f)))
        processor.queueInput(sixteenBit(1000))
        assertEquals(listOf(500), readShorts(processor.getOutput()))

        processor.flush(streamMetadata(item("b", trackGain = 6.0206f)))
        processor.queueInput(sixteenBit(1000))
        assertEquals(listOf(2000), readShorts(processor.getOutput()))
    }

    private fun processor(
        gain: Float,
        encoding: Int = C.ENCODING_PCM_16BIT,
    ): ReplayGainProcessor =
        ReplayGainProcessor().apply {
            setLinearGain(gain)
            configure(AudioFormat(SAMPLE_RATE, CHANNELS, encoding))
            flush(StreamMetadata.DEFAULT)
        }

    private fun item(
        id: String,
        trackGain: Float,
    ): MediaItem =
        queueMediaItem(
            QueueItem(
                id = id,
                title = id,
                artist = null,
                album = null,
                coverArtId = null,
                replayGain = ReplayGain(trackGain = trackGain),
            ),
            local = null,
            stream = "http://server/$id",
            transcode = false,
        )

    private fun streamMetadata(item: MediaItem): StreamMetadata {
        val timeline = SinglePeriodTimeline(DURATION_US, false, false, false, null, item)
        return StreamMetadata
            .Builder()
            .setTimeline(timeline)
            .setPeriodUid(timeline.getUidOfPeriod(0))
            .build()
    }

    private fun sixteenBit(vararg samples: Int): ByteBuffer =
        ByteBuffer.allocate(samples.size * 2).order(ByteOrder.nativeOrder()).apply {
            samples.forEach { putShort(it.toShort()) }
            flip()
        }

    private fun floatBuffer(vararg samples: Float): ByteBuffer =
        ByteBuffer.allocate(samples.size * 4).order(ByteOrder.nativeOrder()).apply {
            samples.forEach { putFloat(it) }
            flip()
        }

    private fun readShorts(buffer: ByteBuffer): List<Int> =
        buildList {
            while (buffer.remaining() >= 2) add(buffer.short.toInt())
        }

    private fun readFloats(buffer: ByteBuffer): List<Float> =
        buildList {
            while (buffer.remaining() >= 4) add(buffer.float)
        }

    private fun readBytes(buffer: ByteBuffer): List<Byte> =
        buildList {
            while (buffer.hasRemaining()) add(buffer.get())
        }

    private companion object {
        const val SAMPLE_RATE = 44_100
        const val CHANNELS = 2
        const val DURATION_US = 1_000_000L
    }
}
