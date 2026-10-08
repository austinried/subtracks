package com.subtracks

import okhttp3.Interceptor
import okhttp3.OkHttpClient

object UserAgent {
    val value: String = "Subtracks (Android; ${BuildConfig.VERSION_NAME})"

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
