package com.subtracks.data.source.subsonic

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.security.MessageDigest
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

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

    fun get(
        method: String,
        params: Map<String, String> = emptyMap(),
    ): Document {
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
            val stream = response.body.byteStream()
            val document = secureDocumentBuilderFactory().newDocumentBuilder().parse(stream)
            val root = document.documentElement
            if (root == null || root.tagName != "subsonic-response") {
                throw SubsonicException(-1, "Unexpected response from the server")
            }
            if (root.getAttribute("status") == "failed") {
                val error = root.getElementsByTagName("error").item(0) as? Element
                throw SubsonicException(
                    error?.getAttribute("code")?.toIntOrNull() ?: -1,
                    error?.getAttribute("message") ?: "Unknown error",
                )
            }
            return document
        }
    }

    companion object {
        const val API_VERSION = "1.13.0"
        const val CLIENT = "subtracks"

        private fun secureDocumentBuilderFactory(): DocumentBuilderFactory =
            DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = false
                isExpandEntityReferences = false
                setFeatureQuietly("http://apache.org/xml/features/disallow-doctype-decl", true)
                setFeatureQuietly("http://xml.org/sax/features/external-general-entities", false)
                setFeatureQuietly("http://xml.org/sax/features/external-parameter-entities", false)
                setFeatureQuietly("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
            }

        private fun DocumentBuilderFactory.setFeatureQuietly(
            name: String,
            enabled: Boolean,
        ) {
            try {
                setFeature(name, enabled)
            } catch (_: ParserConfigurationException) {
                // Android's parser does not implement every hardening flag; the root-tag check in get() is the backstop.
            }
        }

        private fun randomSalt(): String = (1..4).map { ('a'..'z').random() }.joinToString("")

        private fun md5Hex(input: String): String =
            MessageDigest
                .getInstance("MD5")
                .digest(input.toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
    }
}
