package com.subtracks.data.download

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.Base64
import java.util.concurrent.TimeUnit

/**
 * Cover art downloaded alongside the media, stored per source under `downloads/<sourceId>/art`.
 *
 * Cover art ids come from the server, so keys are base64url encoded rather than used as file
 * names: a key can contain separators or spaces and must never reach outside the art directory.
 */
class ArtworkStore(
    private val downloadsDir: File,
) {
    fun file(
        sourceId: Long,
        cacheKey: String,
    ): File = artDir(sourceId).resolve(fileName(cacheKey))

    fun uri(
        sourceId: Long,
        cacheKey: String,
    ): String? = file(sourceId, cacheKey).takeIf { it.exists() }?.let { Uri.fromFile(it).toString() }

    fun write(
        sourceId: Long,
        cacheKey: String,
        bytes: ByteArray,
    ): Boolean {
        val target = file(sourceId, cacheKey)
        return runCatching {
            target.parentFile?.mkdirs()
            val partial = File.createTempFile(target.name, PARTIAL_SUFFIX, target.parentFile)
            try {
                partial.writeBytes(bytes)
                partial.renameTo(target)
            } finally {
                partial.delete()
            }
        }.getOrDefault(false)
    }

    fun sweep(
        sourceId: Long,
        keep: Set<String>,
    ) {
        val keepNames = keep.mapTo(HashSet(), ::fileName)
        artDir(sourceId).listFiles().orEmpty().forEach { file ->
            if (file.isFile && file.name !in keepNames) file.delete()
        }
    }

    private fun artDir(sourceId: Long): File = downloadsDir.resolve(sourceId.toString()).resolve(ART_DIR)

    private fun fileName(cacheKey: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(cacheKey.toByteArray(Charsets.UTF_8))

    private companion object {
        const val ART_DIR = "art"
        const val PARTIAL_SUFFIX = ".part"
    }
}

fun interface ArtworkFetcher {
    suspend fun fetch(url: String): ByteArray
}

class OkHttpArtworkFetcher(
    http: OkHttpClient,
    private val maxBytes: Long = MAX_BYTES,
) : ArtworkFetcher {
    // A blocking OkHttp call is not interrupted when the coroutine around it is cancelled, so the
    // per-call timeout is what actually bounds a stuck artwork request.
    private val http = http.newBuilder().callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS).build()

    override suspend fun fetch(url: String): ByteArray =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()
            http.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Artwork request failed with ${response.code}")
                val body = response.body ?: throw IOException("Artwork response had no body")
                if (body.contentLength() > maxBytes) throw IOException("Artwork response is larger than $maxBytes bytes")
                val source = body.source()
                source.request(maxBytes + 1)
                if (source.buffer.size > maxBytes) throw IOException("Artwork response is larger than $maxBytes bytes")
                source.readByteArray()
            }
        }

    private companion object {
        const val CALL_TIMEOUT_SECONDS = 15L
        const val MAX_BYTES = 8L * 1024 * 1024
    }
}
