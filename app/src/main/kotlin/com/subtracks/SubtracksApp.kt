package com.subtracks

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.repo.ArtworkSeedStore
import com.subtracks.di.appModule
import com.subtracks.ui.theme.ArtworkSeedCache
import okhttp3.OkHttpClient
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class SubtracksApp : Application() {
    private val http = OkHttpClient.Builder().addInterceptor(UserAgent.interceptor()).build()

    @OptIn(DelicateCoilApi::class)
    override fun onCreate() {
        super.onCreate()
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
