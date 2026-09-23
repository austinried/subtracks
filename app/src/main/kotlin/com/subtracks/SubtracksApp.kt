package com.subtracks

import android.app.Application
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.subtracks.di.appModule
import okhttp3.OkHttpClient
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class SubtracksApp : Application() {
    private val http = OkHttpClient()

    override fun onCreate() {
        super.onCreate()
        stopKoin()
        startKoin {
            modules(appModule(this@SubtracksApp, http))
        }
        SingletonImageLoader.setSafe { context ->
            ImageLoader
                .Builder(context)
                .components { add(OkHttpNetworkFetcherFactory(callFactory = { http })) }
                .crossfade(150)
                .build()
        }
    }
}
