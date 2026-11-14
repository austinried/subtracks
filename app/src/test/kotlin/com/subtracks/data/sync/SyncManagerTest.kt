package com.subtracks.data.sync

import android.content.Context
import android.content.res.Resources
import androidx.room3.useWriterConnection
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.TEST_TIMEOUT_MS
import com.subtracks.awaitUntil
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.db.createTestDatabase
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.download.FakeDownloadEngine
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.Source
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.NetworkServerActionSink
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.QueueResetPreparation
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.repo.replace
import com.subtracks.data.repo.setShuffle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
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
import org.junit.Assert.assertNull
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
        db = createTestDatabase()
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
    fun aNoOpSyncPreservesTheQueueResolutionCache() =
        runBlocking {
            val healthy = healthyDispatcher()
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                        when {
                            request.path?.startsWith("/rest/search3.view") == true -> MockResponse().setBody(TWO_SONGS)
                            request.path?.startsWith("/rest/getPlaylists.view") == true -> MockResponse().setBody(PLAYLISTS)
                            request.path?.startsWith("/rest/getPlaylist.view") == true -> MockResponse().setBody(PLAYLIST)
                            else -> healthy.dispatch(request)
                        }
                }
            sourceRepository.addSource("Local", server.url("/").toString(), "u", "p", useTokenAuth = false)
            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Success } }
            queueRepository.replace(listOf(queueRepository.playlistEntry(1, "p1")))
            val snapshot = queueRepository.snapshot()
            assertEquals(2L, snapshot.size)
            assertEquals(listOf("s1"), queueRepository.range(snapshot, 0, 0).map { it.item.song.id })
            assertEquals(listOf("s2"), queueRepository.range(snapshot, 1, 1).map { it.item.song.id })

            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.drop(1).first { it is SyncStatus.Success } }
            val offsetSeeks = queueRepository.offsetSeeks
            val keysetSeeks = queueRepository.keysetSeeks
            assertEquals(listOf("s2"), queueRepository.range(snapshot, 1, 1).map { it.item.song.id })

            assertEquals(offsetSeeks, queueRepository.offsetSeeks)
            assertEquals(keysetSeeks + 1, queueRepository.keysetSeeks)
        }

    @Test
    fun aFailedSyncDoesNotAdvanceTheQueueContentVersion() =
        runBlocking {
            val before = queueRepository.snapshot().version

            manager.requestSync()
            val status = withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Failed } }

            assertTrue(status is SyncStatus.Failed)
            assertEquals(before, queueRepository.snapshot().version)
        }

    @Test
    fun aFailedSyncDoesNotResetTheQueue() =
        runBlocking {
            db.sourcesDao().upsertSource(Source(1, "Local", "http://localhost", true, 0))
            queueRepository.replace(listOf(queueRepository.albumEntry(1, "al1").copy(rangeStart = 0, rangeEnd = 0)))
            val before = queueRepository.snapshot().entries.map { it.entry }

            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Failed } }

            assertEquals(before, queueRepository.snapshot().entries.map { it.entry })
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
    fun aFailedSyncCarriesCommittedOriginsIntoTheNextSuccessfulReset() =
        runBlocking {
            val healthy = healthyDispatcher()
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                        if (request.path?.startsWith("/rest/getPlaylists.view") == true) {
                            MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                        } else {
                            healthy.dispatch(request)
                        }
                }
            sourceRepository.addSource("Local", server.url("/").toString(), "u", "p", useTokenAuth = false)
            val edited = queueRepository.albumEntry(1, "al1").copy(rangeStart = 0, rangeEnd = 0)
            queueRepository.replace(listOf(edited))

            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Failed } }
            assertEquals(
                0L,
                queueRepository
                    .snapshot()
                    .entries
                    .single()
                    .entry.rangeStart,
            )

            assertTrue(db.queueDao().pendingSyncOrigins(1).isNotEmpty())
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
            server.dispatcher = healthy
            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Success } }

            assertNull(
                queueRepository
                    .snapshot()
                    .entries
                    .single()
                    .entry.rangeStart,
            )
            assertTrue(db.queueDao().pendingSyncOrigins(1).isEmpty())
        }

    @Test
    fun aPendingClearFailureRollsBackTheQueueResetAndCanBeRetried() =
        runBlocking {
            server.dispatcher = healthyDispatcher()
            sourceRepository.addSource("Local", server.url("/").toString(), "u", "p", useTokenAuth = false)
            queueRepository.replace(listOf(queueRepository.albumEntry(1, "al1").copy(rangeStart = 0, rangeEnd = 0)))
            val before = queueRepository.snapshot()
            val cacheVersion = queueRepository.libraryCacheVersion()
            val resetAttempts = AtomicInteger()
            queueRepository.setResetListener { reset ->
                resetAttempts.incrementAndGet()
                QueueResetPreparation(reset.snapshot)
            }
            failPendingOriginClear()

            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Failed } }

            assertEquals(
                0L,
                queueRepository
                    .snapshot()
                    .entries
                    .single()
                    .entry.rangeStart,
            )
            assertTrue(db.queueDao().pendingSyncOrigins(1).isNotEmpty())
            assertEquals(
                listOf(0L, before.version, before.resetVersion, cacheVersion),
                listOf(
                    resetAttempts.get().toLong(),
                    queueRepository.contentVersion(),
                    queueRepository.snapshot().resetVersion,
                    queueRepository.libraryCacheVersion(),
                ),
            )

            allowPendingOriginClear()
            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.drop(1).first { it is SyncStatus.Success } }

            assertNull(
                queueRepository
                    .snapshot()
                    .entries
                    .single()
                    .entry.rangeStart,
            )
            assertTrue(db.queueDao().pendingSyncOrigins(1).isEmpty())
            assertEquals(1, resetAttempts.get())
        }

    @Test
    fun aResetListenerFailureIsReportedAndCanBeRetried() =
        runBlocking {
            server.dispatcher = healthyDispatcher()
            sourceRepository.addSource("Local", server.url("/").toString(), "u", "p", useTokenAuth = false)
            queueRepository.replace(listOf(queueRepository.albumEntry(1, "al1").copy(rangeStart = 0, rangeEnd = 0)))
            val before = queueRepository.snapshot()
            val cacheVersion = queueRepository.libraryCacheVersion()
            val resetAttempted = CountDownLatch(1)
            val resetAttempts = AtomicInteger()
            queueRepository.setResetListener { reset ->
                queueRepository.setPosition(42_000L)
                resetAttempted.countDown()
                if (resetAttempts.incrementAndGet() == 1) throw IllegalStateException("reconciliation failed")
                QueueResetPreparation(reset.snapshot)
            }

            manager.requestSync()
            assertTrue(resetAttempted.await(10, TimeUnit.SECONDS))
            awaitUntil("waiting for the queue reset listener failure") { manager.status.value !is SyncStatus.Running }

            assertTrue(manager.status.value is SyncStatus.Failed)
            assertEquals(
                0L,
                queueRepository
                    .snapshot()
                    .entries
                    .single()
                    .entry.rangeStart,
            )
            assertTrue(db.queueDao().pendingSyncOrigins(1).isNotEmpty())
            assertEquals(
                listOf(before.version, before.resetVersion, cacheVersion, 0L),
                listOf(
                    queueRepository.contentVersion(),
                    queueRepository.snapshot().resetVersion,
                    queueRepository.libraryCacheVersion(),
                    queueRepository.cursorPositionMs(),
                ),
            )

            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.drop(1).first { it is SyncStatus.Success } }

            assertEquals(2, resetAttempts.get())
            assertNull(
                queueRepository
                    .snapshot()
                    .entries
                    .single()
                    .entry.rangeStart,
            )
            assertTrue(db.queueDao().pendingSyncOrigins(1).isEmpty())
        }

    @Test
    fun aQueueWriteFailureAfterPendingClearRollsBackWithoutPublishing() =
        runBlocking {
            server.dispatcher = healthyDispatcher()
            sourceRepository.addSource("Local", server.url("/").toString(), "u", "p", useTokenAuth = false)
            queueRepository.replace(listOf(queueRepository.albumEntry(1, "al1").copy(rangeStart = 0, rangeEnd = 0)))
            val before = queueRepository.snapshot()
            val cacheVersion = queueRepository.libraryCacheVersion()
            val resetAttempts = AtomicInteger()
            queueRepository.setResetListener { reset ->
                resetAttempts.incrementAndGet()
                QueueResetPreparation(reset.snapshot)
            }
            failQueueResetInsert()

            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Failed } }

            assertEquals(
                0L,
                queueRepository
                    .snapshot()
                    .entries
                    .single()
                    .entry.rangeStart,
            )
            assertTrue(db.queueDao().pendingSyncOrigins(1).isNotEmpty())
            assertEquals(
                listOf(0L, before.version, before.resetVersion, cacheVersion),
                listOf(
                    resetAttempts.get().toLong(),
                    queueRepository.contentVersion(),
                    queueRepository.snapshot().resetVersion,
                    queueRepository.libraryCacheVersion(),
                ),
            )

            allowQueueResetInsert()
            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.drop(1).first { it is SyncStatus.Success } }

            assertEquals(1, resetAttempts.get())
            assertNull(
                queueRepository
                    .snapshot()
                    .entries
                    .single()
                    .entry.rangeStart,
            )
            assertTrue(db.queueDao().pendingSyncOrigins(1).isEmpty())
        }

    @Test
    fun aFailedSyncPreservesResolvedQueueShuffle() =
        runBlocking {
            server.dispatcher = healthyDispatcher()
            sourceRepository.addSource("Local", server.url("/").toString(), "u", "p", useTokenAuth = false)
            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Success } }
            queueRepository.replace(listOf(queueRepository.albumEntry(1, "al1")))
            queueRepository.setShuffle(42L)
            assertTrue(queueRepository.snapshot().shuffled)
            val healthy = healthyDispatcher()
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                        when {
                            request.path?.startsWith("/rest/search3.view") == true -> {
                                MockResponse().setBody(TWO_SONGS)
                            }

                            request.path?.startsWith("/rest/getPlaylists.view") == true -> {
                                MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                            }

                            else -> {
                                healthy.dispatch(request)
                            }
                        }
                }

            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Failed } }

            val snapshot = queueRepository.snapshot()
            assertTrue(snapshot.shuffled)
            assertEquals(1L, snapshot.contextSize)
        }

    @Test
    fun aFailedSongPruneDoesNotResetAnEditedDownloadedQueue() =
        runBlocking {
            val healthy = healthyDispatcher()
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                        if (request.path?.startsWith("/rest/search3.view") == true) {
                            MockResponse().setBody(TWO_SONGS)
                        } else {
                            healthy.dispatch(request)
                        }
                }
            sourceRepository.addSource("Local", server.url("/").toString(), "u", "p", useTokenAuth = false)
            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Success } }
            val context = ApplicationProvider.getApplicationContext<Context>()
            val downloadsDir = File(context.cacheDir, "sync-downloads-${System.nanoTime()}")
            val downloads =
                DownloadRepository(
                    db,
                    sourceRepository,
                    FakeDownloadEngine(),
                    downloadsDir,
                    artworkStore = ArtworkStore(File(context.cacheDir, "art")),
                    artworkFetcher = { ByteArray(0) },
                    scope = scope,
                )
            File(downloadsDir, "1").mkdirs()
            File(downloadsDir, "1/s2").writeText("audio")
            db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Completed, downloadedAt = 1))
            downloads.start()
            downloads.awaitLoaded()
            awaitUntil("waiting for download baseline") { downloads.localUri("s2") != null }
            queueRepository.replace(listOf(queueRepository.downloadedEntry(1).copy(rangeStart = 1)))
            server.dispatcher =
                object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse =
                        when {
                            request.path?.startsWith("/rest/search3.view") == true -> {
                                MockResponse().setBody(SONG)
                            }

                            request.path?.startsWith("/rest/getPlaylists.view") == true -> {
                                MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
                            }

                            else -> {
                                healthy.dispatch(request)
                            }
                        }
                }

            manager.requestSync()
            withTimeout(TEST_TIMEOUT_MS) { manager.status.first { it is SyncStatus.Failed } }

            assertEquals(
                1L,
                queueRepository
                    .snapshot()
                    .entries
                    .single()
                    .entry.rangeStart,
            )
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

    private suspend fun failPendingOriginClear() {
        db.useWriterConnection { connection ->
            connection.usePrepared(
                "CREATE TRIGGER fail_pending_origin_clear BEFORE DELETE ON pending_sync_origins " +
                    "BEGIN SELECT RAISE(FAIL, 'pending-origin clear failed'); END",
            ) { it.step() }
        }
    }

    private suspend fun allowPendingOriginClear() {
        db.useWriterConnection { connection ->
            connection.usePrepared("DROP TRIGGER fail_pending_origin_clear") { it.step() }
        }
    }

    private suspend fun failQueueResetInsert() {
        db.useWriterConnection { connection ->
            connection.usePrepared(
                "CREATE TRIGGER fail_queue_reset_insert BEFORE INSERT ON queue_entries " +
                    "BEGIN SELECT RAISE(FAIL, 'queue reset insert failed'); END",
            ) { it.step() }
        }
    }

    private suspend fun allowQueueResetInsert() {
        db.useWriterConnection { connection ->
            connection.usePrepared("DROP TRIGGER fail_queue_reset_insert") { it.step() }
        }
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

        const val TWO_SONGS =
            "<subsonic-response status=\"ok\" version=\"1.16.1\"><searchResult3>" +
                "<song id=\"s1\" title=\"Song One\" albumId=\"al1\" artistId=\"ar1\" track=\"1\"/>" +
                "<song id=\"s2\" title=\"Song Two\" albumId=\"al1\" artistId=\"ar1\" track=\"2\"/>" +
                "</searchResult3></subsonic-response>"

        const val PLAYLISTS =
            "<subsonic-response status=\"ok\" version=\"1.16.1\"><playlists>" +
                "<playlist id=\"p1\" name=\"Playlist\" songCount=\"2\"/>" +
                "</playlists></subsonic-response>"

        const val PLAYLIST =
            "<subsonic-response status=\"ok\" version=\"1.16.1\">" +
                "<playlist id=\"p1\" name=\"Playlist\" songCount=\"2\">" +
                "<entry id=\"s1\" title=\"Song One\" albumId=\"al1\" artistId=\"ar1\" track=\"1\"/>" +
                "<entry id=\"s2\" title=\"Song Two\" albumId=\"al1\" artistId=\"ar1\" track=\"2\"/>" +
                "</playlist></subsonic-response>"
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
