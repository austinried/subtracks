package com.subtracks.data.sync

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
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.repo.NetworkServerActionSink
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@RunWith(AndroidJUnit4::class)
class SyncManagerTest {
    private lateinit var db: SubtracksDatabase
    private lateinit var sourceRepository: SourceRepository
    private lateinit var queueRepository: QueueRepository
    private lateinit var manager: SyncManager
    private lateinit var preferences: UserPreferences
    private lateinit var scope: CoroutineScope
    private val messages = CopyOnWriteArrayList<String>()
    private lateinit var server: MockWebServer
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
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        sourceRepository =
            SourceRepository(db, OkHttpClient(), fakeUserPreferences(), ArtworkStore(File(context.cacheDir, "art")), scope = scope)
        queueRepository = QueueRepository(db)
        preferences = fakeUserPreferences()
        manager =
            SyncManager(
                db,
                sourceRepository,
                queueRepository,
                NetworkServerActionSink(sourceRepository),
                preferences = preferences,
                showMessage = { messages += it.resolve(resources) },
                scope = scope,
            )
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        cancelAndJoinBlocking(scope)
        db.close()
        server.shutdown()
    }

    @Test
    fun aFailedSyncTellsTheUser() =
        runBlocking {
            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Failed } }

            assertEquals(listOf("No server configured"), messages)
            assertTrue(preferences.lastSyncAt().first() > 0)
        }

    @Test
    fun aSilentSyncDoesNotTellTheUser() =
        runBlocking {
            manager.requestSync(silent = true)
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Failed } }

            assertTrue(messages.isEmpty())
        }

    @Test
    fun aFailedSyncStillInvalidatesTheLibraryCache() =
        runBlocking {
            val before = queueRepository.snapshot().version

            manager.requestSync()
            val status = withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Failed } }

            assertTrue(status is SyncStatus.Failed)
            assertEquals(before + 1, queueRepository.snapshot().version)
        }

    @Test
    fun aSuccessfulSyncMirrorsTheLibraryIntoRoom() =
        runBlocking {
            server.dispatcher = healthyDispatcher()
            sourceRepository.addSource("Local", server.url("/").toString(), "u", "p", useTokenAuth = false)

            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Success } }

            assertEquals(listOf("ar1"), db.libraryDao().artistIds(1))
            assertEquals(listOf("al1"), db.libraryDao().albumIds(1))
            assertTrue(preferences.lastSyncAt().first() > 0)
        }

    @Test
    fun aMidSyncFailureKeepsCommittedRowsAndRecovers() =
        runBlocking {
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                        when {
                            request.path?.startsWith("/rest/getArtists.view") == true -> {
                                MockResponse().setBody(ARTISTS)
                            }

                            request.path?.startsWith("/rest/getAlbumList2.view") == true -> {
                                MockResponse().setBody(ALBUM_LIST)
                            }

                            request.path?.startsWith("/rest/search3.view") == true -> {
                                MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                            }

                            request.path?.startsWith("/rest/getAlbum.view") == true -> {
                                MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                            }

                            else -> {
                                MockResponse().setResponseCode(404)
                            }
                        }
                }
            sourceRepository.addSource("Local", server.url("/").toString(), "u", "p", useTokenAuth = false)

            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Failed } }

            assertEquals(listOf("ar1"), db.libraryDao().artistIds(1))

            server.dispatcher = healthyDispatcher()
            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Success } }

            assertEquals(listOf("ar1"), db.libraryDao().artistIds(1))
            assertEquals(listOf("al1"), db.libraryDao().albumIds(1))
        }

    @Test
    fun requestsWhileRunningCoalesceIntoOneSync() =
        runBlocking {
            val artistRequests = AtomicInteger()
            val firstArtistStarted = CountDownLatch(1)
            val secondArtistStarted = CountDownLatch(1)
            val releaseFirst = CountDownLatch(1)
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        if (request.path?.startsWith("/rest/getArtists.view") == true) {
                            when (artistRequests.incrementAndGet()) {
                                1 -> {
                                    firstArtistStarted.countDown()
                                    releaseFirst.await(10, TimeUnit.SECONDS)
                                }

                                2 -> {
                                    secondArtistStarted.countDown()
                                }
                            }
                        }
                        return healthyDispatcher().dispatch(request)
                    }
                }
            sourceRepository.addSource("Local", server.url("/").toString(), "u", "p", useTokenAuth = false)

            manager.requestSync()
            assertTrue(firstArtistStarted.await(10, TimeUnit.SECONDS))
            manager.requestSync()
            manager.requestSync()
            releaseFirst.countDown()

            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Success } }
            assertTrue("the coalesced sync should start", secondArtistStarted.await(10, TimeUnit.SECONDS))
            assertEquals(2, artistRequests.get())
        }

    private fun healthyDispatcher() =
        object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                when {
                    request.path?.startsWith("/rest/getArtists.view") == true -> {
                        MockResponse().setBody(ARTISTS)
                    }

                    request.path?.startsWith("/rest/getAlbumList2.view") == true -> {
                        val type = request.requestUrl?.queryParameter("type")
                        MockResponse().setBody(if (type == "newest") ALBUM_LIST else emptyAlbumList())
                    }

                    request.path?.startsWith("/rest/search3.view") == true -> {
                        MockResponse().setBody(SONG)
                    }

                    request.path?.startsWith("/rest/getPlaylists.view") == true -> {
                        MockResponse().setBody(emptyPlaylists())
                    }

                    else -> {
                        MockResponse().setResponseCode(404)
                    }
                }
        }

    private fun emptyAlbumList() = "<subsonic-response status=\"ok\"><albumList2></albumList2></subsonic-response>"

    private fun emptyPlaylists() = "<subsonic-response status=\"ok\"><playlists></playlists></subsonic-response>"

    private companion object {
        const val ARTISTS =
            "<subsonic-response status=\"ok\" version=\"1.16.1\"><artists>" +
                "<artist id=\"ar1\" name=\"Artist One\" albumCount=\"1\"/>" +
                "</artists></subsonic-response>"

        const val ALBUM_LIST =
            "<subsonic-response status=\"ok\" version=\"1.16.1\"><albumList2>" +
                "<album id=\"al1\" artistId=\"ar1\" name=\"Album One\" artist=\"Artist One\" songCount=\"1\"/>" +
                "</albumList2></subsonic-response>"

        const val SONG =
            "<subsonic-response status=\"ok\" version=\"1.16.1\"><searchResult3>" +
                "<song id=\"s1\" title=\"Song One\" albumId=\"al1\" artistId=\"ar1\" track=\"1\"/>" +
                "</searchResult3></subsonic-response>"
    }

    @Test
    fun aSyncRequestWhileOfflineIsRefusedWithoutTouchingTheNetwork() =
        runBlocking {
            sourceRepository.setOfflineMode(true)
            withTimeout(TEST_TIMEOUT_MS) { sourceRepository.offline.first { it } }

            manager.requestSync()
            val status = withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Failed } }

            assertEquals("Offline mode is on", (status as SyncStatus.Failed).message.resolve(resources))
        }
}
