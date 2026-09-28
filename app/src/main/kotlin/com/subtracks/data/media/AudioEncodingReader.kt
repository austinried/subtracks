package com.subtracks.data.media

import android.media.MediaExtractor
import android.media.MediaFormat
import com.subtracks.data.model.AudioEncoding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun readAudioEncoding(file: File): AudioEncoding? =
    withContext(Dispatchers.IO) {
        runCatching {
            val extractor = MediaExtractor()
            try {
                extractor.setDataSource(file.absolutePath)
                if (extractor.trackCount == 0) return@runCatching null
                val format = extractor.getTrackFormat(0)
                AudioEncoding(
                    mimeType = format.stringOrNull(MediaFormat.KEY_MIME),
                    bitrate = format.intOrNull(MediaFormat.KEY_BIT_RATE),
                    sampleRate = format.intOrNull(MediaFormat.KEY_SAMPLE_RATE),
                    channels = format.intOrNull(MediaFormat.KEY_CHANNEL_COUNT),
                )
            } finally {
                extractor.release()
            }
        }.getOrNull()
    }

private fun MediaFormat.stringOrNull(key: String): String? = if (containsKey(key)) getString(key) else null

private fun MediaFormat.intOrNull(key: String): Int? = if (containsKey(key)) getInteger(key) else null
