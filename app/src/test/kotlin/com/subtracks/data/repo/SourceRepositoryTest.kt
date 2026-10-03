package com.subtracks.data.repo

import android.content.Context
import android.content.res.Resources
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.TEST_TIMEOUT_MS
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.coverArtKey
import com.subtracks.data.prefs.StreamQuality
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.prefs.fakeUserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
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
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class SourceRepositoryTest {
    private lateinit var db: SubtracksDatabase
    private lateinit var prefs: UserPreferences
    private lateinit var repository: SourceRepository
    private lateinit var artwork: ArtworkStore
    private lateinit var scope: CoroutineScope
    private val messages = ArrayList<String>()
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resources = context.resources
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        artwork = ArtworkStore(File(context.cacheDir, "art-${System.nanoTime()}"))
        prefs = fakeUserPreferences()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        repository =
            SourceRepository(db, OkHttpClient(), prefs, artwork, showMessage = { messages += it.resolve(resources) }, scope = scope)
    }

    @After
    fun tearDown() {
        cancelAndJoinBlocking(scope)
        db.close()
    }

    @Test
    fun aTranscodedDownloadQualityDownloadsTheStreamInstead() =
        runBlocking {
            repository.addSource("nav", "http://a.example/", "u", "p", false)
            val sourceId = repository.activeSourceId().first()!!
            withTimeout(TEST_TIMEOUT_MS) { while (repository.downloadUri(sourceId, "s1") == null) delay(10) }

            assertTrue(repository.downloadUri(sourceId, "s1")!!.contains("/rest/download"))

            prefs.setDownloadQuality(StreamQuality(maxBitrate = 128, format = "opus"))

            var transcoded: String? = null
            withTimeout(TEST_TIMEOUT_MS) {
                while (transcoded == null) {
                    delay(10)
                    transcoded = repository.downloadUri(sourceId, "s1")?.takeIf { it.contains("/rest/stream") }
                }
            }

            assertTrue(transcoded!!.contains("maxBitRate=128"))
            assertTrue(transcoded.contains("format=opus"))
        }

    @Test
    fun addingASourceMakesItTheSingleActiveOne() =
        runTest {
            repository.addSource("first", "http://a.example", "u", "p", true)
            val firstId = repository.activeSourceId().first()

            repository.addSource("second", "http://b.example", "u", "p", true)

            assertEquals(1, repository.sources().first().count { it.isActive })
            assertEquals("second", repository.activeConfig().first()?.name)
            assertEquals(
                "http://a.example/",
                repository
                    .sources()
                    .first()
                    .first { it.id == firstId }
                    .address,
            )
        }

    @Test
    fun editingASourceRewritesItsDetailsWithoutAddingOne() =
        runTest {
            repository.addSource("first", "http://a.example", "u1", "p1", true)
            val id = repository.activeSourceId().first()!!
            repository.addSource("second", "http://b.example", "u2", "p2", true)

            repository.updateSource(id, "renamed", "c.example", "u3", "p3", false)

            val sources = repository.sources().first()
            assertEquals(2, sources.size)
            val edited = sources.first { it.id == id }
            assertEquals("renamed", edited.name)
            assertEquals("http://c.example/", edited.address)
            assertFalse("editing must not change which source is active", edited.isActive)
            val config = repository.sourceConfigOnce(id)!!
            assertEquals("u3", config.username)
            assertEquals("p3", config.password)
            assertFalse(config.useTokenAuth)
        }

    @Test
    fun theActiveSourceCannotBeDeleted() =
        runTest {
            repository.addSource("first", "http://a.example", "u", "p", true)
            val firstId = repository.activeSourceId().first()!!
            repository.addSource("second", "http://b.example", "u", "p", true)
            val secondId = repository.activeSourceId().first()!!

            assertFalse("the active source must survive deletion", repository.deleteSource(secondId))
            assertEquals(2, repository.sources().first().size)

            repository.selectSource(firstId)

            assertTrue(repository.deleteSource(secondId))
            assertEquals(listOf("first"), repository.sources().first().map { it.name })
            assertEquals("first", repository.activeConfig().first()?.name)
        }

    @Test
    fun aTokenAuthSourceIsProbedAndSwitchedToPassword() =
        runBlocking {
            withServer { server ->
                server.enqueue(MockResponse().setBody(failed(41)))
                server.enqueue(MockResponse().setBody(OK))

                repository.addSource("nav", server.url("/").toString(), "u", "s3cret", true)

                val config = withTimeout(TEST_TIMEOUT_MS) { repository.activeConfig().first { it != null && !it.useTokenAuth } }
                assertFalse(config!!.useTokenAuth)
                withTimeout(TEST_TIMEOUT_MS) { while (messages.isEmpty()) delay(10) }
                assertEquals(1, messages.size)
            }
        }

    @Test
    fun theActiveSourceFetchConcurrencyComesFromThePreference() =
        runBlocking {
            withServer { server ->
                val dispatches = AtomicInteger()
                val inFlight = AtomicInteger()
                val maxInFlight = AtomicInteger()
                val secondStarted = CountDownLatch(1)
                val release = CountDownLatch(1)
                server.dispatcher =
                    object : Dispatcher() {
                        override fun dispatch(request: RecordedRequest): MockResponse {
                            val url = request.requestUrl!!
                            if (url.encodedPath != "/rest/getPlaylist.view") return MockResponse().setResponseCode(404)
                            if (dispatches.incrementAndGet() == 2) secondStarted.countDown()
                            val current = inFlight.incrementAndGet()
                            maxInFlight.updateAndGet { maxOf(it, current) }
                            release.await(5, TimeUnit.SECONDS)
                            inFlight.decrementAndGet()
                            val id = url.queryParameter("id")!!
                            return MockResponse().setBody(
                                "<subsonic-response status=\"ok\"><playlist id=\"$id\">" +
                                    (1..3).joinToString("") { "<entry id=\"$id-s$it\" title=\"t$it\"/>" } +
                                    "</playlist></subsonic-response>",
                            )
                        }
                    }

                prefs.setSyncConcurrency(1)
                repository.addSource("nav", server.url("/").toString(), "u", "p", false)

                val source = repository.activeMusicSource()!!
                val fetching =
                    async(Dispatchers.IO) {
                        source.playlistSongs(listOf("p1", "p2", "p3")).toList().flatten()
                    }

                // With concurrency 1 the second fetch must not start while the first is in flight;
                // if the preference is not wired the source defaults to a higher bound and it does.
                assertFalse(secondStarted.await(500, TimeUnit.MILLISECONDS))
                release.countDown()
                assertEquals(9, fetching.await().size)
                assertEquals("only one fetch may be in flight at a time", 1, maxInFlight.get())
            }
        }

    @Test
    fun pingReportsWhetherItFellBackToThePassword() =
        runBlocking {
            withServer { server ->
                server.enqueue(MockResponse().setBody(failed(41)))
                server.enqueue(MockResponse().setBody(OK))
                server.enqueue(MockResponse().setBody(OK))

                assertTrue(repository.ping(server.url("/").toString(), "u", "s3cret", true).getOrThrow())
                assertFalse(repository.ping(server.url("/").toString(), "u", "s3cret", false).getOrThrow())
            }
        }

    @Test
    fun downloadedArtworkIsPreferredOverTheServer() =
        runBlocking {
            repository.addSource("server", "http://a.example/", "u", "p", true)
            val sourceId = awaitActiveSourceId()
            artwork.write(sourceId, coverArtKey(sourceId, "art-1", false), byteArrayOf(1, 2, 3))

            val ref = awaitCoverArt("art-1")

            assertTrue("expected the stored file, was ${ref.url}", ref.url.startsWith("file:"))
            assertEquals(coverArtKey(sourceId, "art-1", false), ref.cacheKey)
        }

    @Test
    fun coverArtFallsBackToTheServerWithoutDownloadedArtwork() =
        runBlocking {
            repository.addSource("server", "http://a.example/", "u", "p", true)

            val ref = awaitCoverArt("art-1")

            assertTrue("expected the server url, was ${ref.url}", ref.url.startsWith("http"))
        }

    @Test
    fun offlineModeDisablesNetworkAndStreaming() =
        runBlocking {
            repository.addSource("server", "http://a.example/", "u", "p", true)
            val sourceId = awaitActiveSourceId()
            repository.setOfflineMode(true)
            withTimeout(TEST_TIMEOUT_MS) { repository.offline.first { it } }

            assertEquals(null, repository.streamUri("s1", 1_000L, StreamQuality()))
            assertEquals(null, repository.networkCoverArt(sourceId, "art-1", false))
            assertEquals(null, repository.downloadUri(sourceId, "s1"))
            assertEquals(null, repository.activeMusicSource())
            assertEquals(null, repository.coverArt("art-1"))
        }

    @Test
    fun offlineModeStillServesDownloadedArtwork() =
        runBlocking {
            repository.addSource("server", "http://a.example/", "u", "p", true)
            val sourceId = awaitActiveSourceId()
            artwork.write(sourceId, coverArtKey(sourceId, "art-1", false), byteArrayOf(1, 2, 3))
            repository.setOfflineMode(true)
            withTimeout(TEST_TIMEOUT_MS) { repository.offline.first { it } }

            val ref = awaitCoverArt("art-1")

            assertTrue("expected the stored file, was ${ref.url}", ref.url.startsWith("file:"))
        }

    private suspend fun awaitActiveSourceId(): Long = withTimeout(TEST_TIMEOUT_MS) { repository.activeSourceId().first { it != null }!! }

    private suspend fun awaitCoverArt(coverArt: String): CoverArtRef =
        withTimeout(TEST_TIMEOUT_MS) {
            var ref = repository.coverArt(coverArt)
            while (ref == null) {
                delay(10)
                ref = repository.coverArt(coverArt)
            }
            ref
        }

    private suspend fun withServer(block: suspend (MockWebServer) -> Unit) {
        val server = MockWebServer()
        server.start()
        try {
            block(server)
        } finally {
            server.shutdown()
        }
    }

    private fun failed(code: Int) = "<subsonic-response status=\"failed\"><error code=\"$code\" message=\"nope\"/></subsonic-response>"

    private companion object {
        const val OK = "<subsonic-response status=\"ok\" version=\"1.16.1\"/>"
    }
}
