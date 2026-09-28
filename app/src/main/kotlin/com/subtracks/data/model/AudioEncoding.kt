package com.subtracks.data.model

data class AudioEncoding(
    val mimeType: String?,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
    val channels: Int? = null,
) {
    val format: String?
        get() =
            when (mimeType?.lowercase()) {
                null -> null
                "audio/mpeg" -> "MP3"
                "audio/mp4", "audio/m4a", "audio/x-m4a", "audio/aac" -> "AAC"
                "audio/mp4a-latm", "audio/aac-adts", "audio/aac-latm" -> "AAC"
                "audio/alac" -> "ALAC"
                "audio/flac", "audio/x-flac" -> "FLAC"
                "audio/ogg" -> "Ogg"
                "audio/vorbis" -> "Vorbis"
                "audio/opus" -> "Opus"
                "audio/webm" -> "WebM"
                "audio/wav", "audio/x-wav" -> "WAV"
                else -> mimeType.substringAfterLast('/').uppercase()
            }
}
