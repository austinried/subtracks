package com.subtracks.data.source.subsonic

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient

data class TestServer(
    val name: String,
    val baseUrl: String,
    val username: String,
    val password: String,
    val supportsTokenAuth: Boolean,
) {
    override fun toString(): String = name
}

object TestServers {
    val all =
        listOf(
            TestServer("navidrome", "http://localhost:4533/", "admin", "password", supportsTokenAuth = true),
            TestServer("gonic", "http://localhost:4747/", "admin", "admin", supportsTokenAuth = true),
            TestServer("lms", "http://localhost:5082/", "admin", "subtracks-lms", supportsTokenAuth = false),
            TestServer(
                "nextcloud",
                "http://localhost:8090/index.php/apps/music/subsonic/",
                "admin",
                "subtracks-nextcloud",
                supportsTokenAuth = false,
            ),
        )

    fun client(
        server: TestServer,
        useTokenAuth: Boolean = false,
        onTokenAuthUnsupported: () -> Unit = {},
    ): SubsonicClient =
        SubsonicClient(
            baseUrl = server.baseUrl.toHttpUrl(),
            username = server.username,
            password = server.password,
            useTokenAuth = useTokenAuth,
            http = OkHttpClient(),
            onTokenAuthUnsupported = onTokenAuthUnsupported,
        )

    fun source(
        server: TestServer,
        id: Long = 1,
        useTokenAuth: Boolean = false,
    ): SubsonicSource = SubsonicSource(id, client(server, useTokenAuth))
}
