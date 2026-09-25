package com.subtracks.data.source.subsonic

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SubsonicSourceTest {
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

    @Test
    fun getSongsFallsBackToAlbumsWhenEmptySearchUnsupported() =
        runBlocking {
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                        when {
                            request.path?.startsWith("/rest/search3.view") == true -> {
                                MockResponse().setBody(failed(10, "Required parameter is missing"))
                            }

                            request.path?.startsWith("/rest/getAlbumList2.view") == true -> {
                                MockResponse().setBody(ALBUM_LIST)
                            }

                            request.path?.startsWith("/rest/getAlbum.view") == true -> {
                                MockResponse().setBody(ALBUM)
                            }

                            else -> {
                                MockResponse().setResponseCode(404)
                            }
                        }
                }
            val source = SubsonicSource(1, client())

            assertEquals(
                listOf("s1", "s2"),
                source
                    .songs()
                    .toList()
                    .flatten()
                    .map { it.id },
            )
        }

    @Test
    fun artistsAreEmittedInBoundedBatches() =
        runBlocking {
            val count = 1200
            val body =
                buildString {
                    append("<subsonic-response status=\"ok\"><artists>")
                    repeat(count) { index ->
                        append("<artist id=\"ar$index\" name=\"Artist $index\" albumCount=\"1\"/>")
                    }
                    append("</artists></subsonic-response>")
                }
            server.enqueue(MockResponse().setBody(body))

            val sizes = SubsonicSource(1, client()).artists().toList().map { it.size }

            assertEquals(listOf(500, 500, 200), sizes)
        }

    @Test
    fun streamUriRequestsTranscodingWhenABitrateIsSet() {
        val uri = SubsonicSource(1, client(), maxBitrate = 128).streamUri("s1").toString()

        assertTrue(uri, uri.contains("maxBitRate=128"))
        assertTrue(uri, uri.contains("estimateContentLength=true"))
    }

    @Test
    fun streamUriLeavesTheOriginalStreamAloneByDefault() {
        val uri = SubsonicSource(1, client()).streamUri("s1").toString()

        assertFalse(uri, uri.contains("maxBitRate"))
        assertFalse(uri, uri.contains("format="))
        assertFalse(uri, uri.contains("estimateContentLength"))
    }

    @Test
    fun streamUriRequestsAPreferredFormat() {
        val uri = SubsonicSource(1, client(), streamFormat = "opus").streamUri("s1").toString()

        assertTrue(uri, uri.contains("format=opus"))
        assertTrue(uri, uri.contains("estimateContentLength=true"))
    }

    private fun client() =
        SubsonicClient(
            baseUrl = server.url("/"),
            username = "u",
            password = "p",
            useTokenAuth = false,
            http = OkHttpClient(),
        )

    private fun failed(
        code: Int,
        message: String,
    ) = "<subsonic-response status=\"failed\"><error code=\"$code\" message=\"$message\"/></subsonic-response>"

    private companion object {
        const val ALBUM_LIST =
            "<subsonic-response status=\"ok\" version=\"1.16.1\"><albumList2>" +
                "<album id=\"al1\" name=\"Album\" artist=\"Artist\" artistId=\"ar1\" songCount=\"2\"/>" +
                "</albumList2></subsonic-response>"

        const val ALBUM =
            "<subsonic-response status=\"ok\" version=\"1.16.1\"><album id=\"al1\" name=\"Album\" artist=\"Artist\" artistId=\"ar1\">" +
                "<song id=\"s1\" title=\"One\" albumId=\"al1\" artistId=\"ar1\" track=\"1\"/>" +
                "<song id=\"s2\" title=\"Two\" albumId=\"al1\" artistId=\"ar1\" track=\"2\"/>" +
                "</album></subsonic-response>"
    }
}
