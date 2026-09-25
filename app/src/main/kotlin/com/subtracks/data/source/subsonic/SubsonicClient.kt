package com.subtracks.data.source.subsonic

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.security.MessageDigest

class SubsonicException(
    val code: Int,
    message: String,
) : Exception(message)

class SubsonicClient(
    private val baseUrl: HttpUrl,
    private val username: String,
    private val password: String,
    private val useTokenAuth: Boolean,
    private val http: OkHttpClient,
    private val userAgent: String = "subtracks/android",
) {
    fun uri(
        method: String,
        params: Map<String, String> = emptyMap(),
    ): HttpUrl {
        val builder = baseUrl.newBuilder().addPathSegments("rest/$method.view")
        builder.addQueryParameter("v", API_VERSION)
        builder.addQueryParameter("c", CLIENT)
        builder.addQueryParameter("u", username)
        if (useTokenAuth) {
            val salt = randomSalt()
            builder.addQueryParameter("s", salt)
            builder.addQueryParameter("t", md5Hex(password + salt))
        } else {
            builder.addQueryParameter("p", password)
        }
        params.forEach { (key, value) -> builder.addQueryParameter(key, value) }
        return builder.build()
    }

    fun check(
        method: String,
        params: Map<String, String> = emptyMap(),
    ) {
        stream(method, params) { SubsonicXml.readStatus(it) }
    }

    fun <T> stream(
        method: String,
        params: Map<String, String> = emptyMap(),
        body: (InputStream) -> T,
    ): T {
        val request =
            Request
                .Builder()
                .url(uri(method, params))
                .header("User-Agent", userAgent)
                .build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw SubsonicException(-1, "HTTP ${response.code}")
            }
            return body(response.body.byteStream())
        }
    }

    companion object {
        const val API_VERSION = "1.13.0"
        const val CLIENT = "subtracks"

        private fun randomSalt(): String = (1..4).map { ('a'..'z').random() }.joinToString("")

        private fun md5Hex(input: String): String =
            MessageDigest
                .getInstance("MD5")
                .digest(input.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
    }
}
