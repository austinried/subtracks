package com.subtracks

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class UserAgentTest {
    @Test
    fun addsTheSubtracksUserAgent() {
        val server = MockWebServer()
        server.enqueue(MockResponse())
        server.start()
        val client = OkHttpClient.Builder().addInterceptor(UserAgent.interceptor()).build()

        client.newCall(Request.Builder().url(server.url("/")).build()).execute().close()
        val header = server.takeRequest().getHeader("User-Agent")
        server.shutdown()

        assertEquals(UserAgent.value, header)
    }
}
