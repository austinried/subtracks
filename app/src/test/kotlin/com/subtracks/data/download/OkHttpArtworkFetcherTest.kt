package com.subtracks.data.download

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class OkHttpArtworkFetcherTest {
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

    private fun url() = server.url("/rest/getCoverArt?id=art-1").toString()

    @Test
    fun theResponseBodyComesBack() {
        server.enqueue(MockResponse().setBody("artwork"))

        val bytes = runBlocking { OkHttpArtworkFetcher(OkHttpClient()).fetch(url()) }

        assertArrayEquals("artwork".toByteArray(), bytes)
    }

    @Test
    fun aBodyLargerThanTheLimitIsRefused() {
        server.enqueue(MockResponse().setBody("0123456789"))

        val fetch =
            runBlocking {
                runCatching { OkHttpArtworkFetcher(OkHttpClient(), maxBytes = 4).fetch(url()) }
            }

        assertTrue("expected a refusal, got $fetch", fetch.exceptionOrNull() is IOException)
    }

    @Test
    fun anUnsuccessfulResponseIsRefused() {
        server.enqueue(MockResponse().setResponseCode(500))

        val fetch = runBlocking { runCatching { OkHttpArtworkFetcher(OkHttpClient()).fetch(url()) } }

        assertTrue("expected a refusal, got $fetch", fetch.exceptionOrNull() is IOException)
    }
}
