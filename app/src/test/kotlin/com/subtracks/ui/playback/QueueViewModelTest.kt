package com.subtracks.ui.playback

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.TEST_TIMEOUT_MS
import com.subtracks.awaitUntil
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.download.FakeDownloadEngine
import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.Source
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.FakePlayerConnection
import com.subtracks.playback.FakePlayerHandle
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class QueueViewModelTest {
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private lateinit var db: SubtracksDatabase
    private lateinit var sources: SourceRepository
    private lateinit var queues: QueueRepository
    private lateinit var downloads: DownloadRepository
    private lateinit var downloadsDir: File
    private lateinit var controller: PlaybackController
    private lateinit var viewModel: QueueViewModel
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val controllerScope = CoroutineScope(SupervisorJob() + dispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        sources =
            SourceRepository(db, OkHttpClient(), fakeUserPreferences(), ArtworkStore(File(context.cacheDir, "art")), scope = repoScope)
        queues = QueueRepository(db)
        val artwork = ArtworkStore(File(context.cacheDir, "art"))
        downloadsDir = File(context.cacheDir, "downloads-${System.nanoTime()}")
        downloads =
            DownloadRepository(
                db,
                sources,
                FakeDownloadEngine(),
                downloadsDir,
                artworkStore = artwork,
                artworkFetcher = { ByteArray(0) },
                scope = repoScope,
            ).also { it.start() }
        controller = PlaybackController(sources, queues, FakePlayerConnection(FakePlayerHandle()), downloads, scope = controllerScope)
        viewModel = QueueViewModel(queues, controller, sources, downloads)
    }

    @After
    fun tearDown() {
        runBlocking { viewModel.viewModelScope.coroutineContext[Job]?.cancelAndJoin() }
        cancelAndJoinBlocking(controllerScope, repoScope)
        db.close()
        Dispatchers.resetMain()
        dispatcher.close()
    }

    @Test
    fun openingLoadsAWindowAtTheCursorAndExtendingGrowsIt() {
        runBlocking { seedSongs(200) }
        controller.playAlbum(1, "al1", 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }

        assertEquals((70L..129L).toList(), rowsSnapshot().map { it.position })
        assertEquals(30, viewModel.initialIndex)

        viewModel.loadNewer()
        await { rowsSnapshot().lastOrNull()?.position == 189L }
        assertEquals((70L..189L).toList(), rowsSnapshot().map { it.position })

        viewModel.loadOlder()
        await { rowsSnapshot().firstOrNull()?.position == 10L }
        assertEquals((10L..189L).toList(), rowsSnapshot().map { it.position })
    }

    @Test
    fun thePlayingHighlightNeedsBothPositionAndSong() {
        val playing = row(5)

        assertTrue(playing.isPlaying(5L, "s5"))
        assertFalse("position lagged while the list reconciled", playing.isPlaying(6L, "s5"))
        assertFalse("duplicate song elsewhere in the queue", playing.isPlaying(5L, "s6"))
        assertFalse(playing.isPlaying(null, "s5"))
    }

    @Test
    fun openingAShuffledQueueStartsAtTheTop() {
        runBlocking { seedSongs(20) }
        runBlocking {
            queues.replace(listOf(queues.albumEntry(1, "al1")))
            queues.setShuffle(12345L)
            queues.setCursor(0)
        }
        controller.connect()
        await { controller.state.value.item != null && controller.state.value.shuffle }

        viewModel.open()
        await { viewModel.ready }

        assertEquals(0, viewModel.initialIndex)
        assertEquals(0L, rowsSnapshot().first().position)
        assertTrue(
            rowsSnapshot().first().isPlaying(
                0L,
                controller.state.value.item
                    ?.id,
            ),
        )
    }

    @Test
    fun removingWhileShuffledReconcilesTheRows() {
        runBlocking { seedSongs(20) }
        runBlocking {
            queues.replace(listOf(queues.albumEntry(1, "al1")))
            queues.setShuffle(12345L)
            queues.setCursor(0)
        }
        controller.connect()
        await { controller.state.value.item != null && controller.state.value.shuffle }
        viewModel.open()
        await { viewModel.ready }

        val before = runBlocking { queues.snapshot() }
        val currentPosition = controller.state.value.position!!
        val removePosition = (0 until before.size).first { it != currentPosition }

        viewModel.remove(removePosition)

        await {
            val snapshot = runBlocking { queues.snapshot() }
            val rows = rowsSnapshot()
            rows.isNotEmpty() &&
                runBlocking { rows.all { queues.itemAt(snapshot, it.position)?.song?.id == it.song.song.id } }
        }
    }

    @Test
    fun openingAtTheEndLoadsAWindowAndExtendsOlder() {
        runBlocking { seedSongs(200) }
        controller.playAlbum(1, "al1", 199)
        await { controller.state.value.position == 199L }

        viewModel.open()
        await { viewModel.ready }

        assertEquals((169L..199L).toList(), rowsSnapshot().map { it.position })
        assertEquals(30, viewModel.initialIndex)

        viewModel.loadOlder()
        await { rowsSnapshot().firstOrNull()?.position == 109L }
        assertEquals((109L..199L).toList(), rowsSnapshot().map { it.position })
    }

    @Test
    fun dropTargetUsesStablePositionsAcrossDirectionChanges() {
        assertEquals(1L, dropTarget(listOf(row(1), row(0), row(2), row(3)), index = 1, from = 0))
        assertEquals(3L, dropTarget(listOf(row(1), row(2), row(3), row(0)), index = 3, from = 0))
        assertEquals(1L, dropTarget(listOf(row(0), row(3), row(1), row(2)), index = 1, from = 3))
    }

    @Test
    fun reopeningReusesRowIdentitySoComposeDoesNotCrossFadeTwoLists() {
        runBlocking { seedSongs(200) }
        controller.playAlbum(1, "al1", 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }
        val firstIds = rowsSnapshot().map { it.id }
        val firstGeneration = viewModel.generation

        viewModel.open()
        await { viewModel.generation > firstGeneration }

        assertEquals(firstIds, rowsSnapshot().map { it.id })
    }

    @Test
    fun reorderingMovesTheRowLocally() {
        runBlocking { seedSongs(200) }
        controller.playAlbum(1, "al1", 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }

        runOnMain { viewModel.reorder(fromIndex = 0, toIndex = 2) }

        val positions = rowsSnapshot().map { it.position }
        assertEquals(listOf(71L, 72L, 70L), positions.take(3))
    }

    @Test
    fun scrollingNewerEvictsOlderRowsToBoundTheWindow() {
        runBlocking { seedSongs(400) }
        controller.playAlbum(1, "al1", 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }
        assertEquals((70L..129L).toList(), rowsSnapshot().map { it.position })

        extendToEnd()

        assertEquals(QUEUE_WINDOW_ROWS, rowsSnapshot().size)
        assertEquals((220L..399L).toList(), rowsSnapshot().map { it.position })
    }

    @Test
    fun scrollingOlderAfterEvictionDropsNewerRows() {
        runBlocking { seedSongs(400) }
        controller.playAlbum(1, "al1", 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }
        extendToEnd()

        viewModel.loadOlder()
        awaitWindow(first = 160, last = 339)

        assertEquals(QUEUE_WINDOW_ROWS, rowsSnapshot().size)
    }

    @Test
    fun movingARowAfterEvictionKeepsPositionsContiguous() {
        runBlocking { seedSongs(400) }
        controller.playAlbum(1, "al1", 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }
        extendToEnd()

        val movedSongId =
            rowsSnapshot()[10]
                .song.song.id
        val from = rowsSnapshot()[10].position
        val to = rowsSnapshot()[13].position
        runOnMain { viewModel.reorder(10, 13) }
        viewModel.move(from, to)
        await {
            rowsSnapshot().size == QUEUE_WINDOW_ROWS &&
                rowsSnapshot().getOrNull(13)?.position == 233L
        }

        assertEquals(
            movedSongId,
            rowsSnapshot()[13]
                .song.song.id,
        )
        assertEquals((220L..399L).toList(), rowsSnapshot().map { it.position })
    }

    @Test
    fun movingTheTopRowDownRenumbersFromTheWindowStart() {
        runBlocking { seedSongs(400) }
        controller.playAlbum(1, "al1", 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }
        extendToEnd()
        assertEquals((220L..399L).toList(), rowsSnapshot().map { it.position })

        val from = rowsSnapshot().first().position
        runOnMain { viewModel.reorder(0, 5) }
        val to = dropTarget(rowsSnapshot(), index = 5, from = from)
        viewModel.move(from, to)
        await {
            rowsSnapshot().firstOrNull()?.position == 220L &&
                rowsSnapshot().lastOrNull()?.position == 399L
        }

        assertEquals((220L..399L).toList(), rowsSnapshot().map { it.position })
    }

    @Test
    fun removingATrackUnderTheBlockReloadsRows() {
        runBlocking { seedSongs(10) }
        runBlocking {
            db.libraryDao().upsertSongs(
                listOf(Song(1, "x1", "al2", "ar1", "X", "Other Album", "Artist", 100, 1, 1, null, null)),
            )
        }
        controller.playAlbum(1, "al1", 2)
        await { controller.state.value.position == 2L }
        controller.addToQueue(1, QueueKind.Song, "x1")
        await { runBlocking { controller.upcomingItem()?.id } == "x1" }

        viewModel.open()
        await { viewModel.ready }

        viewModel.remove(2)
        await {
            val positions = rowsSnapshot().map { it.position }
            positions.isNotEmpty() && positions == (positions.first()..positions.last()).toList()
        }

        val snapshot = runBlocking { queues.snapshot() }
        val rows = rowsSnapshot()
        val expected =
            runBlocking {
                (rows.first().position..rows.last().position).mapNotNull { queues.itemAt(snapshot, it)?.song?.id }
            }
        assertEquals(expected, rows.map { it.song.song.id })
    }

    @Test
    fun removeAndUndoKeepTheBoundedWindowConsistent() {
        runBlocking { seedSongs(400) }
        controller.playAlbum(1, "al1", 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }
        val removedSongId =
            rowsSnapshot()[30]
                .song.song.id

        viewModel.remove(rowsSnapshot()[30].position)
        await {
            rowsSnapshot().size == 60 &&
                rowsSnapshot().firstOrNull()?.position == 70L &&
                rowsSnapshot()
                    .getOrNull(30)
                    ?.song
                    ?.song
                    ?.id != removedSongId
        }
        assertEquals((70L..129L).toList(), rowsSnapshot().map { it.position })

        viewModel.undo()
        await {
            rowsSnapshot().size == 60 &&
                rowsSnapshot().firstOrNull()?.position == 70L &&
                rowsSnapshot()
                    .getOrNull(30)
                    ?.song
                    ?.song
                    ?.id == removedSongId
        }
        assertContiguous()
    }

    private fun assertContiguous() = assertTrue(isContiguous())

    private fun isContiguous(): Boolean {
        val positions = rowsSnapshot().map { it.position }
        return positions.isNotEmpty() && positions == (positions.first()..positions.last()).toList()
    }

    private fun awaitWindow(
        first: Long,
        last: Long,
    ) = await {
        rowsSnapshot().size == (last - first + 1).toInt() &&
            rowsSnapshot().firstOrNull()?.position == first &&
            rowsSnapshot().lastOrNull()?.position == last
    }

    private fun extendTo(
        position: Long,
        first: Long,
    ) {
        viewModel.loadNewer()
        awaitWindow(first = first, last = position)
    }

    private fun extendToEnd() {
        extendTo(189, first = 70)
        extendTo(249, first = 70)
        extendTo(309, first = 130)
        extendTo(369, first = 190)
        extendTo(399, first = 220)
    }

    @Test
    fun offlineHidesRowsThatAreNotDownloaded() {
        runBlocking { seedSongs(6) }
        markDownloaded(1, "s2")
        markDownloaded(1, "s4")
        setOffline()
        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s2"
        }

        viewModel.open()
        await { viewModel.ready }

        assertEquals(listOf("s2", "s4"), rowsSnapshot().map { it.song.song.id })
    }

    private fun markDownloaded(
        sourceId: Long,
        songId: String,
    ) {
        val file = File(downloadsDir, "$sourceId/$songId")
        runBlocking {
            downloads.reconcile()
            db.downloadDao().upsert(SongDownload(sourceId, songId, DownloadStatus.Completed))
            file.parentFile?.mkdirs()
            file.writeBytes(byteArrayOf(1))
            downloads.reconcile()
        }
        await { downloads.localUri(songId) != null }
    }

    private fun setOffline() {
        runBlocking {
            sources.setOfflineMode(true)
            withTimeout(TEST_TIMEOUT_MS) { sources.offline.first { it } }
        }
    }

    private fun row(position: Int) =
        QueueRow(
            position.toLong(),
            position.toLong(),
            AlbumSongItem(
                song =
                    Song(
                        sourceId = 1,
                        id = "s$position",
                        albumId = "al1",
                        artistId = "ar1",
                        title = "Song $position",
                        album = "Album",
                        artist = "Artist",
                        duration = 100,
                        track = 1,
                        disc = 1,
                        starred = null,
                        genre = null,
                    ),
                coverArt = null,
            ),
        )

    private suspend fun seedSongs(count: Int) {
        db.sourcesDao().upsertSource(
            Source(id = 1, name = "test", address = "http://localhost", isActive = true, createdAt = 0),
        )
        db.libraryDao().upsertSongs(
            (1..count).map { track ->
                Song(
                    sourceId = 1,
                    id = "s$track",
                    albumId = "al1",
                    artistId = "ar1",
                    title = "Song $track",
                    album = "Album",
                    artist = "Artist",
                    duration = 100,
                    track = track.toLong(),
                    disc = 1,
                    starred = null,
                    genre = null,
                )
            },
        )
    }

    // `rows` is a snapshot list mutated on the main dispatcher, so read it there rather than from
    // the test thread (otherwise iterating it races the view model and throws or reads torn state).
    private fun rowsSnapshot(): List<QueueRow> = runBlocking { withContext(dispatcher) { viewModel.rows.toList() } }

    private fun runOnMain(block: () -> Unit) = runBlocking { withContext(dispatcher) { block() } }

    private fun await(predicate: () -> Boolean) = awaitUntil("waiting for the expected state", predicate)
}
