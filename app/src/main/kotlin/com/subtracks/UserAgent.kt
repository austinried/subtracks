package com.subtracks

import okhttp3.Interceptor

object UserAgent {
    val value: String = "subtracks/android (${BuildConfig.VERSION_NAME})"

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
}
