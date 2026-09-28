package com.subtracks.data.download

import java.io.File

/**
 * A Subsonic server answers a bad or refused download with HTTP 200 and an XML error document, which
 * the platform downloader stores like any other payload, so a "successful" download can be an error
 * page. No audio container starts with markup, so the leading bytes tell them apart.
 */
internal fun File.looksLikeAudio(): Boolean =
    runCatching {
        val head = ByteArray(HEAD_BYTES)
        val read = inputStream().use { it.read(head) }
        var start = 0
        if (read >= 3 && head[0] == 0xEF.toByte() && head[1] == 0xBB.toByte() && head[2] == 0xBF.toByte()) start = 3
        while (start < read && head[start].toInt().toChar().isWhitespace()) start++
        start < read && head[start] != MARKUP_START && head[start] != JSON_START
    }.getOrDefault(false)

private const val HEAD_BYTES = 16
private val MARKUP_START = '<'.code.toByte()
private val JSON_START = '{'.code.toByte()
