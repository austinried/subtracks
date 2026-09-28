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
                "audio/flac", "audio/x-flac" -> "FLAC"
                "audio/ogg" -> "Ogg"
                "audio/opus" -> "Opus"
                "audio/webm" -> "WebM"
                "audio/wav", "audio/x-wav" -> "WAV"
                else -> mimeType.substringAfterLast('/').uppercase()
            }
}
