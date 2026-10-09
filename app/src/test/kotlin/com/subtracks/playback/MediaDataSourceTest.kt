package com.subtracks.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.source.streamLengthSuffix
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets

@RunWith(AndroidJUnit4::class)
class MediaDataSourceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun dataSource(cache: SimpleCache? = null) =
        mediaDataSourceFactory(context, cache, OkHttpDataSource.Factory(OkHttpClient())).createDataSource()

    @Test
    fun aDownloadedFileIsReadThroughTheStreamingChain() {
        val file = File(context.cacheDir, "downloaded-${System.nanoTime()}").apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }
        val dataSource = dataSource()

        val length = dataSource.open(DataSpec(Uri.fromFile(file)))
        val buffer = ByteArray(4)
        val read = dataSource.read(buffer, 0, buffer.size)
        dataSource.close()

        assertEquals(4L, length)
        assertEquals(4, read)
        assertEquals(listOf<Byte>(1, 2, 3, 4), buffer.toList())
    }

    @Test
    fun aStreamWithoutAContentLengthGetsItsDeclaredLength() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setChunkedBody("0123456789", 1))
        server.start()
        val url = "http://${server.hostName}:${server.port}/rest/stream" + streamLengthSuffix(1_000_000)
        val dataSource = dataSource()

        val length = dataSource.open(DataSpec(Uri.parse(url)))
        dataSource.close()
        server.shutdown()

        assertEquals(1_000_000L, length)
    }

    @Test
    fun reopeningAStreamIsServedFromTheCache() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("0123456789"))
        server.start()
        val url = "http://${server.hostName}:${server.port}/rest/stream"
        val cache =
            SimpleCache(
                File(context.cacheDir, "stream-cache-${System.nanoTime()}"),
                NoOpCacheEvictor(),
                StandaloneDatabaseProvider(context),
            )

        val first = readAll(dataSource(cache), url)
        val second = readAll(dataSource(cache), url)
        val requests = server.requestCount
        server.shutdown()
        cache.release()

        assertEquals("0123456789", String(first, StandardCharsets.UTF_8))
        assertEquals(first.toList(), second.toList())
        assertEquals(1, requests)
    }

    private fun readAll(
        dataSource: DataSource,
        url: String,
    ): ByteArray {
        dataSource.open(DataSpec(Uri.parse(url)))
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        while (true) {
            val read = dataSource.read(buffer, 0, buffer.size)
            if (read == C.RESULT_END_OF_INPUT) break
            out.write(buffer, 0, read)
        }
        dataSource.close()
        return out.toByteArray()
    }
}
