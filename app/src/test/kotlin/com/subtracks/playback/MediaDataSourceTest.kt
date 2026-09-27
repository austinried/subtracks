package com.subtracks.playback

import android.content.Context
import android.net.Uri
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MediaDataSourceTest {
    @Test
    fun aDownloadedFileIsReadThroughTheStreamingChain() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.cacheDir, "downloaded-${System.nanoTime()}").apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }
        val dataSource = mediaDataSourceFactory(context, OkHttpDataSource.Factory(OkHttpClient())).createDataSource()

        val length = dataSource.open(DataSpec(Uri.fromFile(file)))
        val buffer = ByteArray(4)
        val read = dataSource.read(buffer, 0, buffer.size)
        dataSource.close()

        assertEquals(4L, length)
        assertEquals(4, read)
        assertEquals(listOf<Byte>(1, 2, 3, 4), buffer.toList())
    }
}
