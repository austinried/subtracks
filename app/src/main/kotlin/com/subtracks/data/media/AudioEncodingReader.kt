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
                    mimeType = format.stringOrNull(MediaFormat.KEY_MIME).orGeneric(file),
                    bitrate = format.intOrNull(MediaFormat.KEY_BIT_RATE),
                    sampleRate = format.intOrNull(MediaFormat.KEY_SAMPLE_RATE),
                    channels = format.intOrNull(MediaFormat.KEY_CHANNEL_COUNT),
                )
            } finally {
                extractor.release()
            }
        }.getOrNull()
    }

// The extractor reports audio/raw for some containers it decodes itself (a FLAC, say), which reads
// as "Raw" and hides the codec; the file's own header names it.
private fun String?.orGeneric(file: File): String? =
    if (isNullOrBlank() ||
        this in GENERIC_MIME_TYPES
    ) {
        sniffAudioMime(file) ?: this
    } else {
        this
    }

internal fun sniffAudioMime(file: File): String? =
    runCatching {
        val head = ByteArray(SNIFF_BYTES)
        val read = file.inputStream().use { it.read(head) }
        val text = String(head, 0, read.coerceAtLeast(0), Charsets.ISO_8859_1)
        when {
            text.startsWith("fLaC") -> "audio/flac"
            text.startsWith("OggS") && text.contains("OpusHead") -> "audio/opus"
            text.startsWith("OggS") && text.contains("vorbis") -> "audio/vorbis"
            text.startsWith("OggS") -> "audio/ogg"
            text.startsWith("RIFF") -> "audio/wav"
            text.startsWith("ID3") -> "audio/mpeg"
            read >= 2 && head[0] == 0xFF.toByte() && (head[1].toInt() and 0xE0) == 0xE0 -> "audio/mpeg"
            read >= 8 && text.regionMatches(4, "ftyp", 0, 4) -> if (text.contains("alac")) "audio/alac" else "audio/mp4"
            text.startsWith(EBML_MAGIC) -> webmCodec(text)
            else -> null
        }
    }.getOrNull()

// WebM and MP4 are containers; the codec they carry is named in the track description near the
// start, and media3 reports that inner codec for a stream, so match it here.
private fun webmCodec(text: String): String =
    when {
        text.contains("A_OPUS") -> "audio/opus"
        text.contains("A_VORBIS") -> "audio/vorbis"
        text.contains("A_FLAC") -> "audio/flac"
        else -> "audio/webm"
    }

private fun MediaFormat.stringOrNull(key: String): String? = if (containsKey(key)) getString(key) else null

private fun MediaFormat.intOrNull(key: String): Int? = if (containsKey(key)) getInteger(key) else null

private const val SNIFF_BYTES = 1024
private const val EBML_MAGIC = "\u001A\u0045\u00DF\u00A3"
private val GENERIC_MIME_TYPES = setOf("audio/raw", "audio/x-raw", "application/octet-stream", "audio/pcm")
