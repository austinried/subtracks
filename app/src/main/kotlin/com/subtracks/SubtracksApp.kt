package com.subtracks

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.subtracks.data.LegacyDataCleanup
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.repo.ArtworkSeedStore
import com.subtracks.di.appModule
import com.subtracks.log.Log
import com.subtracks.ui.theme.ArtworkSeedCache
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class SubtracksApp : Application() {
    private val http =
        UserAgent
            .httpClient()
            .newBuilder()
            .addInterceptor(Log.interceptor())
            .build()

    @OptIn(DelicateCoilApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.init(this)
        LegacyDataCleanup.run(this)
        stopKoin()
        SingletonImageLoader.reset()
        val koin =
            startKoin {
                modules(appModule(this@SubtracksApp, http))
            }.koin
        ArtworkSeedCache.install(koin.get<ArtworkSeedStore>(), koin.get<UserPreferences>())
        SingletonImageLoader.setSafe { context ->
            ImageLoader
                .Builder(context)
                .components { add(OkHttpNetworkFetcherFactory(callFactory = { http })) }
                .build()
        }
    }
}
