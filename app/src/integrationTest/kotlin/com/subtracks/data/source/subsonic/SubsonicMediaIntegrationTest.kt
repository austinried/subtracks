package com.subtracks.data.source.subsonic

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SubsonicMediaIntegrationTest(
    private val server: TestServer,
) {
    private val http = OkHttpClient()

    @Test
    fun streamsOriginalAudio() =
        runBlocking {
            val source = TestServers.source(server)
            val song =
                source
                    .songs()
                    .toList()
                    .flatten()
                    .first()

            get(source.streamUri(song.id).toString()).use { response ->
                assertTrue("$server: HTTP ${response.code}", response.isSuccessful)
                val bytes = response.body.bytes()
                assertTrue("$server: ${bytes.size} bytes", bytes.size > 1000)
                assertFalse("$server: stream returned a text body", bytes.startsWithText())
            }
        }

    @Test
    fun downloadsOriginalAudio() =
        runBlocking {
            val source = TestServers.source(server)
            val song =
                source
                    .songs()
                    .toList()
                    .flatten()
                    .first()

            get(source.downloadUri(song.id).toString()).use { response ->
                assertTrue("$server: HTTP ${response.code}", response.isSuccessful)
                val bytes = response.body.bytes()
                assertTrue("$server: ${bytes.size} bytes", bytes.size > 1000)
            }
        }

    @Test
    fun fetchesCoverArtImage() =
        runBlocking {
            val source = TestServers.source(server)
            val album =
                source
                    .albums()
                    .toList()
                    .flatten()
                    .first { it.coverArt != null }

            get(source.coverArtUri(album.coverArt)!!.toString()).use { response ->
                assertTrue("$server: HTTP ${response.code}", response.isSuccessful)
                val bytes = response.body.bytes()
                assertTrue("$server: ${bytes.size} bytes", bytes.size > 100)
                assertTrue("$server: not a JPEG or PNG", bytes.isJpegOrPng())
            }
        }

    private fun get(url: String): Response = http.newCall(Request.Builder().url(url).build()).execute()

    private fun ByteArray.startsWithText(): Boolean = decodeToString(0, minOf(8, size)).trimStart().startsWith("<")

    private fun ByteArray.isJpegOrPng(): Boolean {
        val jpeg = size >= 2 && this[0] == 0xFF.toByte() && this[1] == 0xD8.toByte()
        val png =
            size >= 4 &&
                this[0] == 0x89.toByte() &&
                this[1] == 0x50.toByte() &&
                this[2] == 0x4E.toByte() &&
                this[3] == 0x47.toByte()
        return jpeg || png
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun servers(): List<Array<Any>> = TestServers.all.map { arrayOf<Any>(it) }
    }
}
