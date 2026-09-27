package com.subtracks.data.source.subsonic

import com.subtracks.data.source.DEFAULT_FETCH_CONCURRENCY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

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
    fun albumsPaginateUntilAShortPage() =
        runBlocking {
            val requestedOffsets = CopyOnWriteArrayList<Int>()
            val requestedTypes = CopyOnWriteArrayList<String>()
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val url = request.requestUrl!!
                        if (url.encodedPath != "/rest/getAlbumList2.view") return MockResponse().setResponseCode(404)
                        val type = url.queryParameter("type")!!
                        requestedTypes += type
                        if (type != "newest") {
                            return MockResponse().setBody(emptyAlbumList())
                        }
                        val offset = url.queryParameter("offset")!!.toInt()
                        requestedOffsets += offset
                        val count = if (offset < 1000) 500 else 3
                        return MockResponse().setBody(albumPage(offset, count))
                    }
                }

            val albums = SubsonicSource(1, client()).albums().toList().flatten()

            assertEquals(1003, albums.size)
            assertEquals(listOf(0, 500, 1000), requestedOffsets.toList())
            assertEquals(listOf("newest"), requestedTypes.distinct())
            assertEquals("al-0", albums.first().id)
            assertEquals("al-1002", albums.last().id)
        }

    @Test
    fun songsUseEmptySearchAndPaginate() =
        runBlocking {
            val probeCount = AtomicInteger()
            val requestedOffsets = CopyOnWriteArrayList<Int>()
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val url = request.requestUrl!!
                        if (url.encodedPath != "/rest/search3.view") return MockResponse().setResponseCode(404)
                        if (url.queryParameter("songCount") == "1") {
                            probeCount.incrementAndGet()
                            return MockResponse().setBody(songPage("probe", 0, 1))
                        }
                        val offset = url.queryParameter("songOffset")!!.toInt()
                        requestedOffsets += offset
                        val count = if (offset < 1000) 500 else 3
                        return MockResponse().setBody(songPage("s", offset, count))
                    }
                }
            val source = SubsonicSource(1, client())

            val first = source.songs().toList().flatten()
            val firstOffsets = requestedOffsets.toList()
            val second = source.songs().toList().flatten()

            assertEquals(1003, first.size)
            assertEquals(1003, second.size)
            assertEquals(1, probeCount.get())
            assertEquals(listOf(0, 500, 1000), firstOffsets)
        }

    @Test
    fun entitiesWithEmptyIdsAreDropped() =
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    "<subsonic-response status=\"ok\"><artists>" +
                        "<artist id=\"\" name=\"No id\"/>" +
                        "<artist id=\"ar1\" name=\"Real\"/>" +
                        "</artists></subsonic-response>",
                ),
            )

            val artists = SubsonicSource(1, client()).artists().toList().flatten()

            assertEquals(listOf("ar1"), artists.map { it.id })
        }

    @Test
    fun cancellingAfterTheFirstBatchDoesNotHang() =
        runBlocking {
            val body =
                buildString {
                    append("<subsonic-response status=\"ok\"><artists>")
                    repeat(100_000) { append("<artist id=\"ar$it\" name=\"A$it\" albumCount=\"1\"/>") }
                    append("</artists></subsonic-response>")
                }
            server.enqueue(MockResponse().setBody(body))

            val firstBatch = withTimeout(15_000) { SubsonicSource(1, client()).artists().first() }

            assertEquals(500, firstBatch.size)
        }

    @Test
    fun albumsAbortWhenThePageCapIsHit() =
        runBlocking {
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val url = request.requestUrl!!
                        if (url.encodedPath != "/rest/getAlbumList2.view") return MockResponse().setResponseCode(404)
                        return if (url.queryParameter("type") == "newest") {
                            MockResponse().setBody(albumPage(0, 500))
                        } else {
                            MockResponse().setBody(emptyAlbumList())
                        }
                    }
                }

            val failure =
                runCatching {
                    SubsonicSource(1, client(), maxPages = 2).albums().toList()
                }

            val thrown = failure.exceptionOrNull()
            assertEquals(true, thrown is SubsonicException)
            assertTrue(thrown?.message.orEmpty(), thrown?.message.orEmpty().contains("too large"))
            assertTrue(thrown?.message.orEmpty(), thrown?.message.orEmpty().contains("1000 rows"))
        }

    @Test
    fun albumsAtThePageCapSucceedWhenTheNextPageIsEmpty() =
        runBlocking {
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val url = request.requestUrl!!
                        if (url.encodedPath != "/rest/getAlbumList2.view") return MockResponse().setResponseCode(404)
                        if (url.queryParameter("type") != "newest") return MockResponse().setBody(emptyAlbumList())
                        return when (url.queryParameter("offset")) {
                            "0", "500" -> MockResponse().setBody(albumPage(url.queryParameter("offset")!!.toInt(), 500))
                            else -> MockResponse().setBody(emptyAlbumList())
                        }
                    }
                }

            val albums = SubsonicSource(1, client(), maxPages = 2).albums().toList().flatten()

            assertEquals(1000, albums.size)
        }

    @Test
    fun albumsAbortWhenThePageJustOverTheCapIsNotEmpty() =
        runBlocking {
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val url = request.requestUrl!!
                        if (url.encodedPath != "/rest/getAlbumList2.view") return MockResponse().setResponseCode(404)
                        if (url.queryParameter("type") != "newest") return MockResponse().setBody(emptyAlbumList())
                        return when (url.queryParameter("offset")) {
                            "0", "500" -> MockResponse().setBody(albumPage(url.queryParameter("offset")!!.toInt(), 500))
                            else -> MockResponse().setBody(albumPage(1000, 1))
                        }
                    }
                }

            val failure =
                runCatching {
                    SubsonicSource(1, client(), maxPages = 2).albums().toList()
                }

            assertEquals(true, failure.exceptionOrNull() is SubsonicException)
        }

    @Test
    fun emptyIdRowsDoNotEndPaginationEarly() =
        runBlocking {
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val url = request.requestUrl!!
                        if (url.encodedPath != "/rest/getAlbumList2.view") return MockResponse().setResponseCode(404)
                        if (url.queryParameter("type") != "newest") return MockResponse().setBody(emptyAlbumList())
                        return when (url.queryParameter("offset")) {
                            "0" -> MockResponse().setBody(albumPageWithEmptyId(0, 500))
                            else -> MockResponse().setBody(albumPage(500, 3))
                        }
                    }
                }

            val albums = SubsonicSource(1, client()).albums().toList().flatten()

            assertEquals(502, albums.size)
            assertEquals("al-502", albums.last().id)
        }

    @Test
    fun playlistSongsFetchesPlaylistsConcurrently() =
        runBlocking {
            val inFlight = AtomicInteger()
            val maxInFlight = AtomicInteger()
            val secondStarted = CountDownLatch(1)
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val url = request.requestUrl!!
                        if (url.encodedPath != "/rest/getPlaylist.view") return MockResponse().setResponseCode(404)
                        val current = inFlight.incrementAndGet()
                        maxInFlight.updateAndGet { maxOf(it, current) }
                        if (current >= 2) secondStarted.countDown()
                        secondStarted.await(2, TimeUnit.SECONDS)
                        inFlight.decrementAndGet()
                        val playlistId = url.queryParameter("id")!!
                        return MockResponse().setBody(
                            "<subsonic-response status=\"ok\"><playlist id=\"$playlistId\">" +
                                (1..3).joinToString("") { "<entry id=\"$playlistId-s$it\" title=\"Title $it\"/>" } +
                                "</playlist></subsonic-response>",
                        )
                    }
                }

            val entries = SubsonicSource(1, client()).playlistSongs(listOf("p1", "p2", "p3", "p4")).toList().flatten()

            assertEquals(12, entries.size)
            assertEquals(4, entries.map { it.playlistId }.distinct().size)
            entries.groupBy { it.playlistId }.forEach { (playlistId, batch) ->
                assertEquals(listOf(0L, 1L, 2L), batch.sortedBy { it.position }.map { it.position })
                assertEquals((1..3).map { "$playlistId-s$it" }, batch.sortedBy { it.position }.map { it.songId })
            }
            assertTrue(maxInFlight.get() in 2..DEFAULT_FETCH_CONCURRENCY)
        }

    @Test
    fun playlistSongsFetchesSequentiallyWhenTheBoundIsOne() =
        runBlocking {
            val inFlight = AtomicInteger()
            val maxInFlight = AtomicInteger()
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val url = request.requestUrl!!
                        if (url.encodedPath != "/rest/getPlaylist.view") return MockResponse().setResponseCode(404)
                        val current = inFlight.incrementAndGet()
                        maxInFlight.updateAndGet { maxOf(it, current) }
                        Thread.sleep(50)
                        inFlight.decrementAndGet()
                        return MockResponse().setBody(playlistResponse(url.queryParameter("id")!!))
                    }
                }

            val entries =
                SubsonicSource(1, client(), maxConcurrentFetches = 1)
                    .playlistSongs(listOf("p1", "p2", "p3", "p4"))
                    .toList()
                    .flatten()

            assertEquals(12, entries.size)
            assertEquals(1, maxInFlight.get())
        }

    @Test
    fun playlistSongsRespectsASmallerFetchBound() =
        runBlocking {
            val bound = 2
            val started = CountDownLatch(bound)
            val beyondBound = CountDownLatch(bound + 1)
            val release = CountDownLatch(1)
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val url = request.requestUrl!!
                        if (url.encodedPath != "/rest/getPlaylist.view") return MockResponse().setResponseCode(404)
                        beyondBound.countDown()
                        started.countDown()
                        release.await(5, TimeUnit.SECONDS)
                        return MockResponse().setBody(playlistResponse(url.queryParameter("id")!!))
                    }
                }

            val fetching =
                async(Dispatchers.IO) {
                    SubsonicSource(1, client(), maxConcurrentFetches = bound)
                        .playlistSongs(listOf("p1", "p2", "p3", "p4"))
                        .toList()
                        .flatten()
                }

            assertTrue("$bound fetches should have started together", started.await(5, TimeUnit.SECONDS))
            // While those are held, the semaphore must block any further fetch.
            assertFalse(
                "a ${bound + 1}th fetch started past the bound",
                beyondBound.await(500, TimeUnit.MILLISECONDS),
            )
            release.countDown()
            val entries = fetching.await()

            assertEquals(12, entries.size)
        }

    @Test
    fun albumSongsFetchesSequentiallyWhenTheBoundIsOne() =
        runBlocking {
            val inFlight = AtomicInteger()
            val maxInFlight = AtomicInteger()
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val url = request.requestUrl!!
                        return when {
                            url.encodedPath == "/rest/search3.view" -> {
                                MockResponse().setBody(failed(10, "Required parameter is missing"))
                            }

                            url.encodedPath == "/rest/getAlbumList2.view" -> {
                                MockResponse().setBody(albumListOf(4))
                            }

                            url.encodedPath == "/rest/getAlbum.view" -> {
                                val current = inFlight.incrementAndGet()
                                maxInFlight.updateAndGet { maxOf(it, current) }
                                Thread.sleep(50)
                                inFlight.decrementAndGet()
                                MockResponse().setBody(albumWithSong(url.queryParameter("id")!!))
                            }

                            else -> {
                                MockResponse().setResponseCode(404)
                            }
                        }
                    }
                }

            val songs = SubsonicSource(1, client(), maxConcurrentFetches = 1).songs().toList().flatten()

            assertEquals(4, songs.size)
            assertEquals(1, maxInFlight.get())
        }

    @Test
    fun playlistSongsPropagatesFailuresFromAnyPlaylist() =
        runBlocking {
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val url = request.requestUrl!!
                        if (url.encodedPath != "/rest/getPlaylist.view") return MockResponse().setResponseCode(404)
                        return if (url.queryParameter("id") == "p2") {
                            MockResponse().setBody(failed(70, "Playlist not found"))
                        } else {
                            MockResponse().setBody(
                                "<subsonic-response status=\"ok\"><playlist id=\"p1\">" +
                                    "<entry id=\"s1\" title=\"One\"/>" +
                                    "</playlist></subsonic-response>",
                            )
                        }
                    }
                }

            val failure = runCatching { SubsonicSource(1, client()).playlistSongs(listOf("p1", "p2", "p3")).toList() }

            assertEquals(true, failure.exceptionOrNull() is SubsonicException)
        }

    @Test
    fun albumSongsFetchesAlbumsConcurrentlyWhenSearchIsUnsupported() =
        runBlocking {
            val inFlight = AtomicInteger()
            val maxInFlight = AtomicInteger()
            val secondStarted = CountDownLatch(1)
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val url = request.requestUrl!!
                        return when {
                            url.encodedPath == "/rest/search3.view" -> {
                                MockResponse().setBody(failed(10, "Required parameter is missing"))
                            }

                            url.encodedPath == "/rest/getAlbumList2.view" -> {
                                MockResponse().setBody(
                                    "<subsonic-response status=\"ok\"><albumList2>" +
                                        (0 until 4).joinToString("") {
                                            "<album id=\"al$it\" name=\"Album $it\" artist=\"Artist\" artistId=\"ar1\" songCount=\"1\"/>"
                                        } +
                                        "</albumList2></subsonic-response>",
                                )
                            }

                            url.encodedPath == "/rest/getAlbum.view" -> {
                                val current = inFlight.incrementAndGet()
                                maxInFlight.updateAndGet { maxOf(it, current) }
                                if (current >= 2) secondStarted.countDown()
                                secondStarted.await(2, TimeUnit.SECONDS)
                                inFlight.decrementAndGet()
                                val albumId = url.queryParameter("id")!!
                                MockResponse().setBody(
                                    "<subsonic-response status=\"ok\"><album id=\"$albumId\">" +
                                        "<song id=\"$albumId-s1\" title=\"One\"/>" +
                                        "</album></subsonic-response>",
                                )
                            }

                            else -> {
                                MockResponse().setResponseCode(404)
                            }
                        }
                    }
                }

            val songs = SubsonicSource(1, client()).songs().toList().flatten()

            assertEquals(4, songs.size)
            assertTrue(maxInFlight.get() in 2..DEFAULT_FETCH_CONCURRENCY)
        }

    @Test
    fun playlistEntriesWithoutIdsAreSkipped() =
        runBlocking {
            server.enqueue(
                MockResponse().setBody(
                    "<subsonic-response status=\"ok\"><playlist id=\"p1\">" +
                        "<entry id=\"s1\" title=\"One\"/>" +
                        "<entry title=\"No id\"/>" +
                        "<entry id=\"s2\" title=\"Two\"/>" +
                        "</playlist></subsonic-response>",
                ),
            )

            val entries = SubsonicSource(1, client()).playlistSongs(listOf("p1")).toList().flatten()

            assertEquals(listOf("s1", "s2"), entries.map { it.songId })
            assertEquals(listOf(0L, 1L), entries.map { it.position })
        }

    @Test
    fun streamUriRequestsTranscodingWhenABitrateIsSet() {
        val uri = SubsonicSource(1, client(), maxBitrate = 128).streamUri("s1").toString()

        assertTrue(uri, uri.contains("maxBitRate=128"))
        assertFalse(uri, uri.contains("estimateContentLength"))
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
        assertFalse(uri, uri.contains("estimateContentLength"))
    }

    private fun client() =
        SubsonicClient(
            baseUrl = server.url("/"),
            username = "u",
            password = "p",
            useTokenAuth = false,
            http = OkHttpClient(),
        )

    private fun playlistResponse(playlistId: String) =
        "<subsonic-response status=\"ok\"><playlist id=\"$playlistId\">" +
            (1..3).joinToString("") { "<entry id=\"$playlistId-s$it\" title=\"Title $it\"/>" } +
            "</playlist></subsonic-response>"

    private fun albumListOf(count: Int) =
        "<subsonic-response status=\"ok\"><albumList2>" +
            (0 until count).joinToString("") {
                "<album id=\"al$it\" name=\"Album $it\" artist=\"Artist\" artistId=\"ar1\" songCount=\"1\"/>"
            } +
            "</albumList2></subsonic-response>"

    private fun albumWithSong(albumId: String) =
        "<subsonic-response status=\"ok\"><album id=\"$albumId\">" +
            "<song id=\"$albumId-s1\" title=\"One\"/>" +
            "</album></subsonic-response>"

    private fun failed(
        code: Int,
        message: String,
    ) = "<subsonic-response status=\"failed\"><error code=\"$code\" message=\"$message\"/></subsonic-response>"

    private fun emptyAlbumList() = "<subsonic-response status=\"ok\"><albumList2></albumList2></subsonic-response>"

    private fun albumPage(
        offset: Int,
        count: Int,
    ) = "<subsonic-response status=\"ok\"><albumList2>" +
        (offset until offset + count).joinToString("") {
            "<album id=\"al-$it\" name=\"Album $it\" artist=\"Artist\" artistId=\"ar1\" songCount=\"1\"/>"
        } +
        "</albumList2></subsonic-response>"

    private fun albumPageWithEmptyId(
        offset: Int,
        count: Int,
    ) = "<subsonic-response status=\"ok\"><albumList2>" +
        "<album id=\"\" name=\"No id\" artist=\"Artist\" artistId=\"ar1\" songCount=\"1\"/>" +
        (offset until offset + count - 1).joinToString("") {
            "<album id=\"al-$it\" name=\"Album $it\" artist=\"Artist\" artistId=\"ar1\" songCount=\"1\"/>"
        } +
        "</albumList2></subsonic-response>"

    private fun songPage(
        prefix: String,
        offset: Int,
        count: Int,
    ) = "<subsonic-response status=\"ok\"><searchResult3>" +
        (offset until offset + count).joinToString("") {
            "<song id=\"$prefix$it\" title=\"Song $it\"/>"
        } +
        "</searchResult3></subsonic-response>"

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
