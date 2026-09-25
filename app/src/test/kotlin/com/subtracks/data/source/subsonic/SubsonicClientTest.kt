package com.subtracks.data.source.subsonic

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class SubsonicClientTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun client(tokenAuth: Boolean) =
        SubsonicClient(
            baseUrl = server.url("/"),
            username = "guest",
            password = "secret",
            useTokenAuth = tokenAuth,
            http = OkHttpClient(),
        )

    @Test
    fun tokenAuthSendsSaltAndHash() {
        server.enqueue(MockResponse().setBody("<subsonic-response status=\"ok\" version=\"1.16.1\"/>"))

        client(tokenAuth = true).check("ping")

        val url = server.takeRequest().requestUrl!!
        assertEquals("/rest/ping.view", url.encodedPath)
        assertEquals("guest", url.queryParameter("u"))
        assertEquals("subtracks", url.queryParameter("c"))
        assertNotNull(url.queryParameter("s"))
        assertNotNull(url.queryParameter("t"))
        assertNull(url.queryParameter("p"))
    }

    @Test
    fun plaintextAuthSendsPassword() {
        server.enqueue(MockResponse().setBody("<subsonic-response status=\"ok\" version=\"1.16.1\"/>"))

        client(tokenAuth = false).check("ping")

        val url = server.takeRequest().requestUrl!!
        assertEquals("secret", url.queryParameter("p"))
        assertNull(url.queryParameter("t"))
    }

    @Test
    fun paramsAreForwarded() {
        server.enqueue(MockResponse().setBody("<subsonic-response status=\"ok\" version=\"1.16.1\"/>"))

        client(tokenAuth = true).check("getCoverArt", mapOf("id" to "cov", "size" to "256"))

        val url = server.takeRequest().requestUrl!!
        assertEquals("cov", url.queryParameter("id"))
        assertEquals("256", url.queryParameter("size"))
    }

    @Test
    fun failedStatusThrowsWithCode() {
        server.enqueue(
            MockResponse().setBody(
                "<subsonic-response status=\"failed\"><error code=\"40\" message=\"Wrong username\"/></subsonic-response>",
            ),
        )

        val error = assertThrows(SubsonicException::class.java) { client(tokenAuth = true).check("ping") }
        assertEquals(40, error.code)
        assertEquals("Wrong username", error.message)
    }

    @Test
    fun nonSubsonicResponseThrows() {
        server.enqueue(MockResponse().setBody("<html><body>Sign in</body></html>"))

        assertThrows(SubsonicException::class.java) { client(tokenAuth = true).check("ping") }
    }
}
