package com.subtracks.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AppModuleTest {
    private lateinit var koin: Koin
    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        stopKoin()
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        if (::koin.isInitialized) {
            runBlocking {
                koin.get<CoroutineScope>().coroutineContext[Job]?.cancelAndJoin()
                koin.get<CoroutineScope>(named("io")).coroutineContext[Job]?.cancelAndJoin()
                koin.get<CoroutineScope>(named("playback")).coroutineContext[Job]?.cancelAndJoin()
            }
        }
        stopKoin()
        Dispatchers.resetMain()
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
