package com.subtracks.playback

import android.content.Context
import androidx.media3.common.PlaybackException
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.Song
import com.subtracks.data.model.Source
import com.subtracks.data.net.NetworkMode
import com.subtracks.data.prefs.StreamQuality
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class PlaybackControllerTest {
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private lateinit var db: SubtracksDatabase
    private lateinit var prefs: UserPreferences
    private lateinit var networkMode: MutableStateFlow<NetworkMode>
    private lateinit var sources: SourceRepository
    private lateinit var queues: QueueRepository
    private lateinit var handle: FakePlayerHandle
    private lateinit var controller: PlaybackController
    private val messages = CopyOnWriteArrayList<String>()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        prefs = fakeUserPreferences()
        networkMode = MutableStateFlow(NetworkMode.Wifi)
        sources = SourceRepository(db, OkHttpClient(), prefs, networkMode = networkMode)
        queues = QueueRepository(db)
        handle = FakePlayerHandle()
        controller =
            PlaybackController(sources, queues, FakePlayerConnection(handle), showMessage = { messages += it }, dispatcher = dispatcher)
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
        await { handle.operations.contains("play") }

        assertEquals(51, handle.itemCount)
        assertEquals(25, handle.currentIndex)
        assertEquals("s31", handle.currentItem?.id)
        assertEquals(listOf("prepare", "play"), handle.operations.takeLast(2))
    }

    @Test
    fun upcomingItemIsTheNextQueueItemWithItsCoverArt() {
        seedAlbum(3, sourceId = 1)
        runBlocking {
            db.libraryDao().upsertAlbums(
                listOf(
                    Album(
                        sourceId = 1,
                        id = "al1",
                        artistId = "ar1",
                        name = "Album",
                        albumArtist = "Artist",
                        created = 0,
                        coverArt = "cover-al1",
                        genre = null,
                        year = null,
                        starred = null,
                        songCount = 3,
                    ),
                ),
            )
        }
        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        assertEquals("cover-al1", runBlocking { controller.upcomingItem()?.coverArtId })
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
    fun theReadyStateResolvesWithTheRestoredItemAlreadyInPlace() {
        seedAlbum(5, sourceId = 1)
        runBlocking { queues.replace(listOf(queues.albumEntry(1, "al1"))) }

        assertFalse(controller.ready.value)

        controller.connect()
        await { controller.ready.value }

        assertNotNull("the mini player must be drawable once ready", controller.state.value.item)
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
    fun theDurationUsesTheNextTracksMetadataWhileItLoads() {
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

        assertEquals(100_000L, controller.state.value.durationMs)
    }

    @Test
    fun theLastTrackStillEnablesNext() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 2)
        await {
            controller.state.value.item
                ?.id == "s3"
        }

        assertTrue(controller.state.value.hasNext)
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
    fun playbackErrorsAreShownAsAToast() {
        seedAlbum(3, sourceId = 1)
        controller.playAlbum(1, "al1", 0)
        await { handle.operations.contains("play") }

        handle.fail("Can't reach the server. Check your connection.")
        await { messages.isNotEmpty() }

        assertEquals(listOf("Can't reach the server. Check your connection."), messages.toList())
    }

    @Test
    fun networkErrorsAreReportedWithAFriendlyMessage() {
        val failed = PlaybackException("net", null, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
        val timeout = PlaybackException("net", null, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT)
        val refused = PlaybackException("http", null, PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS)

        assertEquals("Can't reach the server. Check your connection.", playbackErrorMessage(failed))
        assertEquals("Can't reach the server. Check your connection.", playbackErrorMessage(timeout))
        assertEquals("The server refused to stream this track.", playbackErrorMessage(refused))
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

        assertEquals(QueueContext(QueueKind.Album, 1, "al1"), controller.state.value.context)
        assertTrue(controller.state.value.isPlaying)
        assertTrue(controller.state.value.hasNext)
        assertTrue(controller.state.value.hasPrevious)
    }

    @Test
    fun jumpingToAVisibleItemSeeksWithinTheWindow() {
        seedAlbum(60, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        controller.playAt(10)
        await { controller.state.value.position == 10L }

        assertTrue(handle.operations.contains("seekToIndex(10)"))
        assertEquals(
            "s11",
            controller.state.value.item
                ?.id,
        )
    }

    @Test
    fun jumpingOutsideTheWindowReloadsAroundTheTarget() {
        seedAlbum(60, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        controller.playAt(40)
        await {
            controller.state.value.item
                ?.id == "s41"
        }

        assertEquals(40L, controller.state.value.position)
        assertTrue(handle.operations.any { it.startsWith("setWindow") })
        assertTrue(controller.state.value.hasPrevious)
    }

    @Test
    fun removingTheCurrentTrackPlaysTheNextOne() {
        seedAlbum(5, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        runBlocking { controller.removeAt(0) }
        await {
            controller.state.value.item
                ?.id == "s2"
        }

        assertEquals(0L, controller.state.value.position)
        assertTrue(handle.operations.any { it.startsWith("setWindow") })
        assertTrue(controller.state.value.hasPrevious)
    }

    @Test
    fun removingALaterTrackKeepsTheCurrentOne() {
        seedAlbum(5, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        runBlocking { controller.removeAt(3) }
        await { handle.itemCount == 4 }

        assertEquals(
            "s1",
            controller.state.value.item
                ?.id,
        )
        assertEquals(listOf("s1", "s2", "s3", "s5"), handle.items.map { it.id })
    }

    @Test
    fun movingATrackReordersTheWindow() {
        seedAlbum(4, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        runBlocking { controller.move(0, 2) }
        await { controller.state.value.position == 2L }

        assertEquals(listOf("s2", "s3", "s1", "s4"), handle.items.map { it.id })
        assertEquals(
            "s1",
            controller.state.value.item
                ?.id,
        )
    }

    @Test
    fun movingALaterTrackBeforeTheCurrentShiftsTheCursor() {
        seedAlbum(4, sourceId = 1)

        controller.playAlbum(1, "al1", 2)
        await {
            controller.state.value.item
                ?.id == "s3"
        }

        runBlocking { controller.move(3, 0) }
        await { controller.state.value.position == 3L }

        assertEquals(listOf("s4", "s1", "s2", "s3"), handle.items.map { it.id })
        assertEquals(
            "s3",
            controller.state.value.item
                ?.id,
        )
    }

    @Test
    fun movingTracksFarFromThePlayingOneDoesNotReloadTheWindow() {
        seedAlbum(100, sourceId = 1)

        controller.playAlbum(1, "al1", 50)
        await {
            controller.state.value.item
                ?.id == "s51"
        }
        handle.operations.clear()

        runBlocking { controller.move(80, 90) }

        assertEquals(
            "s51",
            controller.state.value.item
                ?.id,
        )
        assertEquals(50L, controller.state.value.position)
        assertFalse(handle.operations.any { it.startsWith("setWindow") })
    }

    @Test
    fun movingATrackAcrossTheWindowKeepsPlaying() {
        seedAlbum(100, sourceId = 1)

        controller.playAlbum(1, "al1", 50)
        await {
            controller.state.value.item
                ?.id == "s51"
        }
        handle.operations.clear()

        runBlocking { controller.move(10, 90) }

        assertEquals(
            "s51",
            controller.state.value.item
                ?.id,
        )
        assertFalse(handle.operations.any { it.startsWith("setWindow") })
    }

    @Test
    fun undoRestoresARemovedTrack() {
        seedAlbum(5, sourceId = 1)

        controller.playAlbum(1, "al1", 2)
        await {
            controller.state.value.item
                ?.id == "s3"
        }

        runBlocking { controller.removeAt(3) }
        await { handle.itemCount == 4 }

        runBlocking { controller.undo() }
        await { handle.itemCount == 5 }

        assertEquals(listOf("s1", "s2", "s3", "s4", "s5"), handle.items.map { it.id })
        assertEquals(
            "s3",
            controller.state.value.item
                ?.id,
        )
        assertEquals(2L, controller.state.value.position)
    }

    @Test
    fun undoRestoresAReorderedQueue() {
        seedAlbum(4, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        runBlocking { controller.move(0, 2) }
        await { handle.items.map { it.id } == listOf("s2", "s3", "s1", "s4") }

        runBlocking { controller.undo() }
        await { handle.items.map { it.id } == listOf("s1", "s2", "s3", "s4") }

        assertEquals(
            "s1",
            controller.state.value.item
                ?.id,
        )
        assertEquals(0L, controller.state.value.position)
    }

    @Test
    fun undoOfARemovalDoesNotReloadTheWindow() {
        seedAlbum(5, sourceId = 1)

        controller.playAlbum(1, "al1", 2)
        await {
            controller.state.value.item
                ?.id == "s3"
        }

        runBlocking { controller.removeAt(3) }
        await { handle.itemCount == 4 }
        handle.operations.clear()

        runBlocking { controller.undo() }
        await { handle.itemCount == 5 }

        assertTrue(handle.operations.contains("insertAt(3, s4)"))
        assertFalse(handle.operations.any { it.startsWith("setWindow") })
    }

    @Test
    fun undoOfAReorderDoesNotReloadTheWindow() {
        seedAlbum(4, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        runBlocking { controller.move(0, 2) }
        await { handle.items.map { it.id } == listOf("s2", "s3", "s1", "s4") }
        handle.operations.clear()

        runBlocking { controller.undo() }
        await { handle.items.map { it.id } == listOf("s1", "s2", "s3", "s4") }

        assertTrue(handle.operations.contains("move(2, 0)"))
        assertFalse(handle.operations.any { it.startsWith("setWindow") })
    }

    @Test
    fun startingANewQueueClearsTheUndo() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        runBlocking { controller.removeAt(1) }
        await { handle.itemCount == 2 }

        controller.playAlbum(1, "al1", 2)
        await {
            controller.state.value.item
                ?.id == "s3"
        }

        runBlocking { controller.undo() }

        assertEquals(listOf("s1", "s2", "s3"), handle.items.map { it.id })
    }

    @Test
    fun shuffleKeepsTheCurrentTrackAndRestoresTheOriginalOrder() {
        seedAlbum(5, sourceId = 1)

        controller.playAlbum(1, "al1", 2)
        await {
            controller.state.value.item
                ?.id == "s3"
        }

        controller.toggleShuffle()
        await { controller.state.value.shuffle }

        assertEquals(
            "s3",
            controller.state.value.item
                ?.id,
        )
        assertEquals(0L, controller.state.value.position)
        assertEquals("s3", handle.items.first().id)

        controller.toggleShuffle()
        await { !controller.state.value.shuffle }

        assertEquals(listOf("s1", "s2", "s3", "s4", "s5"), handle.items.map { it.id })
        assertEquals(
            "s3",
            controller.state.value.item
                ?.id,
        )
    }

    @Test
    fun nextWrapsToTheStartWhenRepeating() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 2)
        await {
            controller.state.value.item
                ?.id == "s3"
        }

        controller.cycleRepeat()
        assertEquals(RepeatMode.All, controller.state.value.repeat)

        controller.next()
        await {
            controller.state.value.item
                ?.id == "s1"
        }
    }

    @Test
    fun nextOnTheLastTrackDoesNothingWithoutRepeat() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 2)
        await {
            controller.state.value.item
                ?.id == "s3"
        }

        controller.next()
        Thread.sleep(100)

        assertEquals(
            "s3",
            controller.state.value.item
                ?.id,
        )
    }

    @Test
    fun previousRestartsTheTrackWhenPastTheThreshold() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 1)
        await {
            controller.state.value.item
                ?.id == "s2"
        }

        handle.positionMs = 5_000L
        controller.previous()

        await { handle.operations.contains("seekTo(0)") }
        assertEquals(
            "s2",
            controller.state.value.item
                ?.id,
        )
    }

    @Test
    fun previousFromTheStartRestartsWithoutRepeat() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        controller.previous()

        assertTrue(handle.operations.contains("seekTo(0)"))
        assertEquals(
            "s1",
            controller.state.value.item
                ?.id,
        )
    }

    @Test
    fun previousFromTheStartWrapsWithRepeatAll() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        controller.cycleRepeat()
        controller.previous()
        await {
            controller.state.value.item
                ?.id == "s3"
        }
    }

    @Test
    fun repeatAllRestartsTheQueueWhenItEnds() {
        seedAlbum(3, sourceId = 1)

        controller.playAlbum(1, "al1", 2)
        await {
            controller.state.value.item
                ?.id == "s3"
        }

        controller.cycleRepeat()
        assertEquals(RepeatMode.All, controller.state.value.repeat)

        handle.finish()
        handle.emitEvents()
        await {
            controller.state.value.item
                ?.id == "s1"
        }
    }

    @Test
    fun tappingASongWhileShuffledPlaysThatSong() {
        seedAlbum(5, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        controller.toggleShuffle()
        await { controller.state.value.shuffle }

        controller.playAlbum(1, "al1", 3)
        await {
            controller.state.value.item
                ?.id == "s4"
        }

        assertEquals(0L, controller.state.value.position)
        assertEquals("s4", handle.items.first().id)
    }

    @Test
    fun jumpingInTheQueueWhileShuffledKeepsTheOrder() {
        seedAlbum(5, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        controller.toggleShuffle()
        await { controller.state.value.shuffle }
        val order = handle.items.map { it.id }

        controller.playAt(2)
        await { controller.state.value.position == 2L }

        assertEquals(order, handle.items.map { it.id })
        assertEquals(
            order[2],
            controller.state.value.item
                ?.id,
        )
    }

    @Test
    fun repeatAllLoopsASingleTrackQueue() {
        seedAlbum(1, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1" && controller.state.value.isPlaying
        }

        controller.cycleRepeat()
        assertEquals(RepeatMode.All, controller.state.value.repeat)

        handle.finish()
        handle.emitEvents()
        await { controller.state.value.isPlaying }

        assertEquals(
            "s1",
            controller.state.value.item
                ?.id,
        )
    }

    @Test
    fun repeatAllLoopsAfterEndingWithRepeatOff() {
        seedAlbum(2, sourceId = 1)

        controller.playAlbum(1, "al1", 0)
        await { controller.state.value.isPlaying }

        handle.finish()
        handle.emitEvents()
        await { !controller.state.value.isPlaying }

        controller.togglePlayPause()
        await { controller.state.value.isPlaying }
        controller.cycleRepeat()
        assertEquals(RepeatMode.All, controller.state.value.repeat)

        handle.finish()
        handle.emitEvents()
        await { controller.state.value.isPlaying }
    }

    @Test
    fun shufflePlayStartsShuffledOnARandomTrack() {
        seedAlbum(5, sourceId = 1)

        controller.shuffleAlbum(1, "al1")
        await { controller.state.value.item != null && controller.state.value.shuffle }

        assertTrue(controller.state.value.shuffle)
        assertEquals(0L, controller.state.value.position)
        assertTrue(handle.items.first().id in listOf("s1", "s2", "s3", "s4", "s5"))
    }

    @Test
    fun shufflingTheSameAlbumAgainPicksADifferentTrack() {
        seedAlbum(5, sourceId = 1)

        controller.shuffleAlbum(1, "al1")
        await { controller.state.value.item != null }
        val first =
            controller.state.value.item
                ?.id

        controller.shuffleAlbum(1, "al1")
        await {
            controller.state.value.item
                ?.id != first
        }

        assertTrue(
            controller.state.value.item
                ?.id != first,
        )
    }

    @Test
    fun playInOrderTurnsOffShuffle() {
        seedAlbum(3, sourceId = 1)

        controller.shuffleAlbum(1, "al1")
        await { controller.state.value.shuffle }

        controller.playAlbumInOrder(1, "al1")
        await { !controller.state.value.shuffle }

        assertEquals(
            "s1",
            controller.state.value.item
                ?.id,
        )
        assertEquals(0L, controller.state.value.position)
    }

    @Test
    fun shufflePlayOnASingleTrackAlbumPlaysIt() {
        seedAlbum(1, sourceId = 1)

        controller.shuffleAlbum(1, "al1")
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        assertTrue(controller.state.value.shuffle)
    }

    @Test
    fun restoringResumesFromTheSavedPosition() {
        seedAlbum(3, sourceId = 1)
        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        runBlocking { queues.setPosition(12_000L) }

        val restoredHandle = FakePlayerHandle()
        val restored = PlaybackController(sources, queues, FakePlayerConnection(restoredHandle), dispatcher = dispatcher)
        restored.connect()
        await {
            restored.state.value.item
                ?.id == "s1"
        }

        assertEquals(12_000L, restored.positionMs.value)
        assertEquals(100_000L, restored.state.value.durationMs)
        assertEquals(
            "setWindow(size=3, start=0, position=12000)",
            restoredHandle.operations.first { it.startsWith("setWindow") },
        )
        restored.close()
    }

    @Test
    fun changingTrackResetsTheSavedPosition() {
        seedAlbum(3, sourceId = 1)
        controller.playAlbum(1, "al1", 0)
        await {
            controller.state.value.item
                ?.id == "s1"
        }

        runBlocking { queues.setPosition(60_000L) }

        controller.next()
        await {
            controller.state.value.item
                ?.id == "s2"
        }

        assertEquals(0L, runBlocking { queues.cursorPositionMs() })
    }

    @Test
    fun positionTickerAdvancesPositionWithoutEmittingPlaybackState() =
        runBlocking {
            seedAlbum(3, sourceId = 1)
            controller.playAlbum(1, "al1", 0)
            await { controller.state.value.isPlaying }

            val emissions = CopyOnWriteArrayList<PlaybackState>()
            val collector = launch(Dispatchers.Default) { controller.state.collect { emissions += it } }
            await { emissions.isNotEmpty() }
            val baseline = emissions.size

            handle.positionMs = 7_000L
            delay(1_200L)

            assertEquals(7_000L, controller.positionMs.value)
            assertEquals(baseline, emissions.size)
            collector.cancel()
        }

    @Test
    fun positionTickerStopsWhenPaused() =
        runBlocking {
            seedAlbum(3, sourceId = 1)
            controller.playAlbum(1, "al1", 0)
            await { controller.state.value.isPlaying }

            handle.positionMs = 7_000L
            delay(700L)
            controller.togglePlayPause()
            await { !controller.state.value.isPlaying }
            val paused = controller.positionMs.value

            handle.positionMs = 9_000L
            delay(700L)

            assertEquals(paused, controller.positionMs.value)
        }

    @Test
    fun changingStreamQualityReloadsTheCurrentItemAtTheSamePosition() {
        seedAlbum(60, sourceId = 1)
        runBlocking { prefs.setStreamQuality(NetworkMode.Wifi, StreamQuality(64, null)) }
        await { sources.quality.value == StreamQuality(64, null) }

        controller.playAlbum(1, "al1", 10)
        await { handle.operations.contains("play") }
        handle.positionMs = 42_000L
        val windows = handle.operations.count { it.startsWith("setWindow") }

        runBlocking { prefs.setStreamQuality(NetworkMode.Wifi, StreamQuality(128, "opus")) }
        await { handle.operations.count { it.startsWith("setWindow") } > windows }

        assertEquals("s11", handle.currentItem?.id)
        assertEquals(42_000L, handle.positionMs)
        assertEquals(listOf("prepare", "play"), handle.operations.takeLast(2))
    }

    @Test
    fun changingStreamQualityWhilePausedReloadsWithoutResuming() {
        seedAlbum(60, sourceId = 1)
        runBlocking { prefs.setStreamQuality(NetworkMode.Wifi, StreamQuality(64, null)) }
        await { sources.quality.value == StreamQuality(64, null) }

        controller.playAlbum(1, "al1", 10)
        await { handle.playWhenReady }
        controller.togglePlayPause()
        await { !handle.playWhenReady }
        handle.positionMs = 33_000L
        val before = handle.operations.size
        val windows = handle.operations.count { it.startsWith("setWindow") }

        runBlocking { prefs.setStreamQuality(NetworkMode.Wifi, StreamQuality(128, "opus")) }
        await { handle.operations.count { it.startsWith("setWindow") } > windows }

        assertEquals(33_000L, handle.positionMs)
        assertFalse(handle.playWhenReady)
        assertTrue(handle.operations.drop(before).none { it.startsWith("prepare") || it.startsWith("play") })
    }

    @Test
    fun switchingToTheOtherModeReloadsWithItsQuality() {
        seedAlbum(60, sourceId = 1)
        runBlocking {
            prefs.setStreamQuality(NetworkMode.Wifi, StreamQuality(320, null))
            prefs.setStreamQuality(NetworkMode.Mobile, StreamQuality(96, "opus"))
        }
        await { sources.quality.value == StreamQuality(320, null) }

        controller.playAlbum(1, "al1", 10)
        await { handle.operations.contains("play") }
        handle.positionMs = 12_000L
        val windows = handle.operations.count { it.startsWith("setWindow") }

        networkMode.value = NetworkMode.Mobile
        await { sources.quality.value == StreamQuality(96, "opus") }
        await { handle.operations.count { it.startsWith("setWindow") } > windows }

        assertEquals("s11", handle.currentItem?.id)
        assertEquals(12_000L, handle.positionMs)
    }

    @Test
    fun switchingToTheOtherModeWithEqualQualityDoesNotReload() {
        seedAlbum(60, sourceId = 1)
        runBlocking {
            prefs.setStreamQuality(NetworkMode.Wifi, StreamQuality(96, "opus"))
            prefs.setStreamQuality(NetworkMode.Mobile, StreamQuality(96, "opus"))
        }
        await { sources.quality.value == StreamQuality(96, "opus") }

        controller.playAlbum(1, "al1", 10)
        await { handle.operations.contains("play") }
        val windows = handle.operations.count { it.startsWith("setWindow") }

        networkMode.value = NetworkMode.Mobile
        runBlocking { delay(300) }

        assertEquals(windows, handle.operations.count { it.startsWith("setWindow") })
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
