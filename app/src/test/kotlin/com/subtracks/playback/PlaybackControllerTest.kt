package com.subtracks.playback

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.Song
import com.subtracks.data.model.Source
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class PlaybackControllerTest {
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private lateinit var db: SubtracksDatabase
    private lateinit var sources: SourceRepository
    private lateinit var queues: QueueRepository
    private lateinit var handle: FakePlayerHandle
    private lateinit var controller: PlaybackController

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        sources = SourceRepository(db, OkHttpClient())
        queues = QueueRepository(db)
        handle = FakePlayerHandle()
        controller = PlaybackController(sources, queues, FakePlayerConnection(handle), dispatcher = dispatcher)
    }

    @After
    fun tearDown() {
        controller.close()
        sources.close()
        db.close()
        dispatcher.close()
    }

    @Test
    fun playingAnAlbumLoadsAWindowAroundTheStart() {
        seedAlbum(60, sourceId = 1)

        controller.playAlbum(1, "al1", 30)
        await { handle.items.isNotEmpty() }

        assertEquals(51, handle.itemCount)
        assertEquals(25, handle.currentIndex)
        assertEquals("s31", handle.currentItem?.id)
        assertEquals(listOf("prepare", "play"), handle.operations.takeLast(2))
    }

    @Test
    fun advancingShiftsTheWindowAndKeepsItBounded() {
        seedAlbum(100, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        repeat(60) { step ->
            handle.advanceTo(handle.currentIndex + 1)
            await {
                controller.state.value.item
                    ?.id == "s${step + 2}"
            }
        }

        assertTrue(handle.itemCount <= 51)
        assertEquals(
            "s61",
            controller.state.value.item
                ?.id,
        )
    }

    @Test
    fun togglingPlayOnARestoredQueuePreparesBeforePlaying() {
        seedAlbum(5, sourceId = 1)
        runBlocking { queues.replace(listOf(queues.albumEntry(1, "al1"))) }

        controller.connect()
        await { handle.items.isNotEmpty() }
        handle.operations.clear()

        controller.togglePlayPause()
        await { handle.operations.contains("play") }

        assertEquals(listOf("prepare", "play"), handle.operations)
    }

    @Test
    fun nextAdvancesTheCursor() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        controller.next()
        await {
            controller.state.value.item
                ?.id == "s2"
        }

        assertTrue(handle.operations.contains("seekToIndex(1)"))
        assertTrue(controller.state.value.hasPrevious)
    }

    @Test
    fun theDurationDoesNotDropToZeroWhenTheNextTrackIsStillLoading() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        handle.duration = 284_000
        handle.emitEvents()
        await { controller.state.value.durationMs == 284_000L }

        handle.duration = 0
        controller.next()
        await {
            controller.state.value.item
                ?.id == "s2"
        }

        assertEquals(284_000L, controller.state.value.durationMs)
    }

    @Test
    fun theLastTrackReportsNoNext() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 2)
        await {
            controller.state.value.item
                ?.id == "s3"
        }

        assertFalse(controller.state.value.hasNext)
        assertTrue(controller.state.value.hasPrevious)
    }

    @Test
    fun thePlayerReportsStoppedWhenTheQueueEnds() {
        seedAlbum(1, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await { controller.state.value.isPlaying }

        handle.finish()
        handle.emitEvents()
        await { !controller.state.value.isPlaying }

        assertFalse(controller.state.value.isPlaying)
    }

    @Test
    fun togglingPlayAfterTheQueueEndedRestartsTheCurrentTrack() {
        seedAlbum(1, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await { controller.state.value.isPlaying }

        handle.finish()
        await { !controller.state.value.isPlaying }

        handle.operations.clear()
        controller.togglePlayPause()
        await { controller.state.value.isPlaying }

        assertEquals(listOf("seekToIndex(0)", "play"), handle.operations)
    }

    @Test
    fun tappingPauseWhileBufferingPauses() {
        seedAlbum(1, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await { controller.state.value.isPlaying }

        handle.startBuffering()
        handle.operations.clear()
        controller.togglePlayPause()
        await { handle.operations.contains("pause") }

        assertEquals(listOf("pause"), handle.operations)
    }

    @Test
    fun bufferingIsOnlyReportedAfterItPersists() {
        seedAlbum(1, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await { controller.state.value.isPlaying }

        handle.startBuffering()
        Thread.sleep(300)
        assertFalse(controller.state.value.isBuffering)

        Thread.sleep(1_200)
        assertTrue(controller.state.value.isBuffering)

        handle.becomeReady()
        await { !controller.state.value.isBuffering }
    }

    @Test
    fun playbackFailuresAreSurfacedUntilTheNextTrack() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        handle.fail("ERROR_CODE_IO_UNSPECIFIED")
        await { controller.state.value.error == "ERROR_CODE_IO_UNSPECIFIED" }

        handle.advanceTo(handle.currentIndex + 1)
        await {
            controller.state.value.item
                ?.id == "s2"
        }

        assertEquals(null, controller.state.value.error)
    }

    @Test
    fun startingAnEmptyQueueStopsThePlayer() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await { handle.items.isNotEmpty() }

        controller.playAlbum(1, "missing", 0)
        await { handle.operations.contains("clear") }

        assertTrue(handle.items.isEmpty())
        assertEquals(null, controller.state.value.item)
    }

    @Test
    fun restoringAQueueFromAnotherSourceStopsIt() {
        seedAlbum(3, sourceId = 1)
        seedAlbum(3, sourceId = 2)
        runBlocking {
            queues.replace(listOf(QueueEntry(position = 0, sourceId = 2, kind = QueueKind.Album, refId = "al1")))
        }

        controller.connect()
        await { handle.operations.contains("clear") }

        assertFalse(handle.operations.any { it.startsWith("setWindow") })
        assertTrue(handle.items.isEmpty())
        assertEquals(null, controller.state.value.item)
    }

    @Test
    fun stateCarriesTheQueueContext() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await { controller.state.value.item != null }

        assertEquals(QueueContext(QueueKind.Album, "al1"), controller.state.value.context)
        assertTrue(controller.state.value.isPlaying)
        assertTrue(controller.state.value.hasNext)
        assertFalse(controller.state.value.hasPrevious)
    }

    private fun seedAlbum(
        count: Int,
        sourceId: Long,
    ) {
        runBlocking {
            db.sourcesDao().upsertSource(
                Source(sourceId, "source $sourceId", "http://localhost:$sourceId", sourceId == 1L, sourceId),
            )
            db.libraryDao().upsertSongs(
                (1..count).map { track ->
                    Song(
                        sourceId = sourceId,
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
    }

    private fun await(predicate: () -> Boolean) {
        val deadline = System.nanoTime() + 5_000_000_000L
        while (!predicate() && System.nanoTime() < deadline) Thread.sleep(10)
        assertTrue("Timed out waiting for the expected playback state", predicate())
    }
}
