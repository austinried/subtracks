package com.subtracks.ui.playback

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongListItem
import com.subtracks.data.model.Source
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.FakePlayerConnection
import com.subtracks.playback.FakePlayerHandle
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class QueueViewModelTest {
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private lateinit var db: SubtracksDatabase
    private lateinit var queues: QueueRepository
    private lateinit var controller: PlaybackController
    private lateinit var viewModel: QueueViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        val sources = SourceRepository(db, OkHttpClient(), fakeUserPreferences())
        queues = QueueRepository(db)
        controller = PlaybackController(sources, queues, FakePlayerConnection(FakePlayerHandle()), dispatcher)
        viewModel = QueueViewModel(queues, controller)
    }

    @After
    fun tearDown() {
        controller.close()
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

        assertEquals((70L..129L).toList(), viewModel.rows.map { it.position })
        assertEquals(30, viewModel.initialIndex)

        viewModel.loadNewer()
        await { viewModel.rows.lastOrNull()?.position == 189L }
        assertEquals((70L..189L).toList(), viewModel.rows.map { it.position })

        viewModel.loadOlder()
        await { viewModel.rows.firstOrNull()?.position == 10L }
        assertEquals((10L..189L).toList(), viewModel.rows.map { it.position })
    }

    @Test
    fun openingAtTheEndLoadsAWindowAndExtendsOlder() {
        runBlocking { seedSongs(200) }
        controller.playAlbum(1, "al1", 199)
        await { controller.state.value.position == 199L }

        viewModel.open()
        await { viewModel.ready }

        assertEquals((169L..199L).toList(), viewModel.rows.map { it.position })
        assertEquals(30, viewModel.initialIndex)

        viewModel.loadOlder()
        await { viewModel.rows.firstOrNull()?.position == 109L }
        assertEquals((109L..199L).toList(), viewModel.rows.map { it.position })
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
        val firstIds = viewModel.rows.map { it.id }
        val firstGeneration = viewModel.generation

        viewModel.open()
        await { viewModel.generation > firstGeneration }

        assertEquals(firstIds, viewModel.rows.map { it.id })
    }

    @Test
    fun reorderingMovesTheRowLocally() {
        runBlocking { seedSongs(200) }
        controller.playAlbum(1, "al1", 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }

        viewModel.reorder(fromIndex = 0, toIndex = 2)

        val positions = viewModel.rows.map { it.position }
        assertEquals(listOf(71L, 72L, 70L), positions.take(3))
    }

    @Test
    fun scrollingNewerEvictsOlderRowsToBoundTheWindow() {
        runBlocking { seedSongs(400) }
        controller.playAlbum(1, "al1", 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }
        assertEquals((70L..129L).toList(), viewModel.rows.map { it.position })

        extendToEnd()

        assertEquals(QUEUE_WINDOW_ROWS, viewModel.rows.size)
        assertEquals((220L..399L).toList(), viewModel.rows.map { it.position })
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

        assertEquals(QUEUE_WINDOW_ROWS, viewModel.rows.size)
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
            viewModel.rows[10]
                .song.song.id
        val from = viewModel.rows[10].position
        val to = viewModel.rows[13].position
        viewModel.reorder(10, 13)
        viewModel.move(from, to)
        await {
            viewModel.rows.size == QUEUE_WINDOW_ROWS &&
                viewModel.rows.getOrNull(13)?.position == 233L
        }

        assertEquals(
            movedSongId,
            viewModel.rows[13]
                .song.song.id,
        )
        assertEquals((220L..399L).toList(), viewModel.rows.map { it.position })
    }

    @Test
    fun movingTheTopRowDownRenumbersFromTheWindowStart() {
        runBlocking { seedSongs(400) }
        controller.playAlbum(1, "al1", 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }
        extendToEnd()
        assertEquals((220L..399L).toList(), viewModel.rows.map { it.position })

        val from = viewModel.rows.first().position
        viewModel.reorder(0, 5)
        val to = dropTarget(viewModel.rows, index = 5, from = from)
        viewModel.move(from, to)
        await {
            viewModel.rows.firstOrNull()?.position == 220L &&
                viewModel.rows.lastOrNull()?.position == 399L
        }

        assertEquals((220L..399L).toList(), viewModel.rows.map { it.position })
    }

    @Test
    fun removeAndUndoKeepTheBoundedWindowConsistent() {
        runBlocking { seedSongs(400) }
        controller.playAlbum(1, "al1", 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }
        val removedSongId =
            viewModel.rows[30]
                .song.song.id

        viewModel.remove(viewModel.rows[30].position)
        await {
            viewModel.rows.size == 60 &&
                viewModel.rows.firstOrNull()?.position == 70L &&
                viewModel.rows
                    .getOrNull(30)
                    ?.song
                    ?.song
                    ?.id != removedSongId
        }
        assertEquals((70L..129L).toList(), viewModel.rows.map { it.position })

        viewModel.undo()
        await {
            viewModel.rows.size == 60 &&
                viewModel.rows.firstOrNull()?.position == 70L &&
                viewModel.rows
                    .getOrNull(30)
                    ?.song
                    ?.song
                    ?.id == removedSongId
        }
        assertContiguous()
    }

    private fun assertContiguous() = assertTrue(isContiguous())

    private fun isContiguous(): Boolean {
        val positions = viewModel.rows.map { it.position }
        return positions.isNotEmpty() && positions == (positions.first()..positions.last()).toList()
    }

    private fun awaitWindow(
        first: Long,
        last: Long,
    ) = await {
        viewModel.rows.size == (last - first + 1).toInt() &&
            viewModel.rows.firstOrNull()?.position == first &&
            viewModel.rows.lastOrNull()?.position == last
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

    private fun row(position: Int) =
        QueueRow(
            position.toLong(),
            position.toLong(),
            SongListItem(
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

    private fun await(predicate: () -> Boolean) {
        repeat(500) {
            if (predicate()) return
            Thread.sleep(10)
        }
        assertTrue("Timed out waiting for the expected state", predicate())
    }
}
