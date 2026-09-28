package com.subtracks.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.download.DownloadNotifier
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class AppModuleTest {
    private lateinit var koin: Koin

    @Before
    fun setUp() = stopKoin()

    @After
    fun tearDown() {
        if (::koin.isInitialized) {
            runCatching {
                koin.get<DownloadNotifier>().close()
                koin.get<PlaybackController>().close()
                koin.get<DownloadRepository>().close()
                koin.get<SourceRepository>().close()
            }
        }
        stopKoin()
    }

    @Test
    fun theModuleResolvesThePlaybackGraph() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        koin = startKoin { modules(appModule(context, OkHttpClient())) }.koin

        val downloads = koin.get<DownloadRepository>()
        val controller = koin.get<PlaybackController>()

        assertSame(downloads, koin.get<DownloadRepository>())
        assertSame(controller, koin.get<PlaybackController>())
    }
}
