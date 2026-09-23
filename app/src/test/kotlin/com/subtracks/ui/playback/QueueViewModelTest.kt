package com.subtracks.ui.playback

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Song
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
        controller.playSongs(1, 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }

        assertEquals((100L..159L).toList(), viewModel.rows.map { it.position })

        viewModel.loadNewer()
        await { viewModel.rows.lastOrNull()?.position == 199L }
        assertEquals((100L..199L).toList(), viewModel.rows.map { it.position })

        viewModel.loadOlder()
        await { viewModel.rows.firstOrNull()?.position == 40L }
        assertEquals((40L..199L).toList(), viewModel.rows.map { it.position })
    }

    @Test
    fun reorderingMovesTheRowLocally() {
        runBlocking { seedSongs(200) }
        controller.playSongs(1, 100)
        await { controller.state.value.position == 100L }

        viewModel.open()
        await { viewModel.ready }

        viewModel.reorder(fromIndex = 0, toIndex = 2)

        val positions = viewModel.rows.map { it.position }
        assertEquals(listOf(101L, 102L, 100L), positions.take(3))
    }

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
        val deadline = System.nanoTime() + 5_000_000_000L
        while (!predicate() && System.nanoTime() < deadline) Thread.sleep(10)
        assertTrue("Timed out waiting for the expected state", predicate())
    }
}
