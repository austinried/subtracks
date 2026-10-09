package com.subtracks

import android.os.Build
import okhttp3.Interceptor
import okhttp3.OkHttpClient

object UserAgent {
    val value: String = "subtracks (Android ${Build.VERSION.RELEASE}; ${BuildConfig.VERSION_NAME})"

    fun interceptor(): Interceptor =
        Interceptor { chain ->
            chain.proceed(
                chain
                    .request()
                    .newBuilder()
                    .header("User-Agent", value)
                    .build(),
            )
        }

    fun httpClient(): OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor()).build()
}
