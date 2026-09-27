package com.subtracks.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.playback.PlaybackController
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
class AppModuleTest {
    @Before
    fun setUp() = stopKoin()

    @After
    fun tearDown() = stopKoin()

    @Test
    fun theModuleResolvesThePlaybackGraph() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val koin = startKoin { modules(appModule(context, OkHttpClient())) }.koin

        assertNotNull(koin.get<DownloadRepository>())
        assertNotNull(koin.get<PlaybackController>())
    }
}
