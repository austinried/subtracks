package com.subtracks.data.repo

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.awaitUntil
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.PendingActionKind
import com.subtracks.data.model.Source
import com.subtracks.data.source.ServerActionSink
import com.subtracks.data.source.StarType
import com.subtracks.data.source.subsonic.SubsonicException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(AndroidJUnit4::class)
class PendingServerActionSinkTest {
    private sealed interface Call {
        data class Star(
            val type: StarType,
            val id: String,
            val starred: Boolean,
        ) : Call

        data class Scrobble(
            val songId: String,
            val time: Long,
        ) : Call
    }

    private class RecordingSink : ServerActionSink {
        val calls = CopyOnWriteArrayList<Call>()
        var starFailure: Exception? = null
        var scrobbleFailure: Exception? = null

        override suspend fun nowPlaying(songId: String) = Unit

        override suspend fun scrobble(
            songId: String,
            time: Long,
        ) {
            scrobbleFailure?.let { throw it }
            calls += Call.Scrobble(songId, time)
        }

        override suspend fun setStar(
            type: StarType,
            id: String,
            starred: Boolean,
        ) {
            starFailure?.let { throw it }
            calls += Call.Star(type, id, starred)
        }
    }

    private lateinit var db: SubtracksDatabase
    private lateinit var scope: CoroutineScope
    private lateinit var delegate: RecordingSink
    private lateinit var sink: PendingServerActionSink
    private val offline = MutableStateFlow(true)
    private val offlinePreferences = MutableStateFlow(true)
    private val networkAvailable = MutableStateFlow(true)
    private var sourceId = 0L

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        sourceId = runBlocking { db.sourcesDao().insertSource(Source(name = "server", address = "http://x/", createdAt = 0)) }
        delegate = RecordingSink()
        sink =
            PendingServerActionSink(
                delegate = delegate,
                db = db,
                activeSourceId = { sourceId },
                offline = offline,
                offlinePreferences = offlinePreferences,
                networkAvailable = networkAvailable,
                scope = scope,
            )
    }

    @After
    fun tearDown() {
        cancelAndJoinBlocking(scope)
        db.close()
    }

    @Test
    fun anOfflineStarIsQueuedAndReplayedWhenOnline() =
        runBlocking {
            sink.setStar(StarType.Song, "s1", true)

            assertEquals(emptyList<Call>(), delegate.calls)
            assertEquals(1, db.pendingActionDao().pending(sourceId).size)

            offline.value = false
            sink.flush()

            assertEquals(listOf(Call.Star(StarType.Song, "s1", true)), delegate.calls)
            assertEquals(0, db.pendingActionDao().pending(sourceId).size)
        }

    @Test
    fun turningOfflineOffReplaysThroughTheCollector() =
        runBlocking {
            sink.setStar(StarType.Song, "s1", true)

            offline.value = false
            offlinePreferences.value = false

            awaitUntil("the queued star to replay") { delegate.calls.isNotEmpty() }
            assertEquals(listOf(Call.Star(StarType.Song, "s1", true)), delegate.calls)
            assertEquals(0, db.pendingActionDao().pending(sourceId).size)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun aRestoredConnectionReplaysThroughTheCollector() =
        runBlocking {
            offline.value = false
            networkAvailable.value = false
            val reconnectScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())
            try {
                val reconnectable =
                    PendingServerActionSink(
                        delegate = delegate,
                        db = db,
                        activeSourceId = { sourceId },
                        offline = offline,
                        offlinePreferences = flowOf(false),
                        networkAvailable = networkAvailable,
                        scope = reconnectScope,
                    )
                delegate.scrobbleFailure = IOException("down")

                reconnectable.scrobble("s1", 1_000)
                assertEquals(1, db.pendingActionDao().pending(sourceId).size)

                delegate.scrobbleFailure = null
                networkAvailable.value = true

                awaitUntil("the queued scrobble to replay") { delegate.calls.isNotEmpty() }
                assertEquals(listOf(Call.Scrobble("s1", 1_000)), delegate.calls)
                assertEquals(0, db.pendingActionDao().pending(sourceId).size)
            } finally {
                cancelAndJoinBlocking(reconnectScope)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun aNetworkSwitchThatStaysAvailableReplaysAgain() =
        runBlocking {
            offline.value = false
            val network = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)
            network.tryEmit(false)
            val reconnectScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())
            try {
                val reconnectable =
                    PendingServerActionSink(
                        delegate = delegate,
                        db = db,
                        activeSourceId = { sourceId },
                        offline = offline,
                        offlinePreferences = flowOf(false),
                        networkAvailable = network,
                        scope = reconnectScope,
                    )
                delegate.scrobbleFailure = IOException("down")
                reconnectable.scrobble("s1", 1_000)
                delegate.scrobbleFailure = null
                network.tryEmit(true)
                awaitUntil("the first replay") { delegate.calls.size == 1 }

                delegate.scrobbleFailure = IOException("down")
                reconnectable.scrobble("s2", 2_000)
                delegate.scrobbleFailure = null
                // No false in between: an available-network switch must still re-notify.
                network.tryEmit(true)

                awaitUntil("the second replay") { delegate.calls.size == 2 }
                assertEquals(listOf(Call.Scrobble("s1", 1_000), Call.Scrobble("s2", 2_000)), delegate.calls)
            } finally {
                cancelAndJoinBlocking(reconnectScope)
            }
        }

    @Test
    fun anOfflineUnstarCoalescesAwayAnEarlierStar() =
        runBlocking {
            sink.setStar(StarType.Song, "s1", true)
            sink.setStar(StarType.Song, "s1", false)

            val queued = db.pendingActionDao().pending(sourceId)
            assertEquals(1, queued.size)
            assertEquals(PendingActionKind.Unstar, queued.single().kind)

            offline.value = false
            sink.flush()

            assertEquals(listOf(Call.Star(StarType.Song, "s1", false)), delegate.calls)
        }

    @Test
    fun aQueuedStarCannotOverwriteANewerOnlineUnstar() =
        runBlocking {
            offline.value = false
            delegate.starFailure = IOException("down")
            sink.setStar(StarType.Song, "s1", true)
            assertEquals(1, db.pendingActionDao().pending(sourceId).size)

            delegate.starFailure = null
            sink.setStar(StarType.Song, "s1", false)

            assertEquals(listOf(Call.Star(StarType.Song, "s1", false)), delegate.calls)
            assertEquals(0, db.pendingActionDao().pending(sourceId).size)
        }

    @Test
    fun offlineScrobblesReplayInOrderWithTheirTimes() =
        runBlocking {
            sink.scrobble("s1", 1_000)
            sink.scrobble("s2", 2_000)

            offline.value = false
            sink.flush()

            assertEquals(listOf(Call.Scrobble("s1", 1_000), Call.Scrobble("s2", 2_000)), delegate.calls)
        }

    @Test
    fun aConnectionFailureQueuesTheScrobbleAndFlushRetries() =
        runBlocking {
            offline.value = false
            delegate.scrobbleFailure = IOException("down")

            sink.scrobble("s1", 1_000)

            assertEquals(emptyList<Call>(), delegate.calls)
            assertEquals(1, db.pendingActionDao().pending(sourceId).size)

            delegate.scrobbleFailure = null
            sink.flush()

            assertEquals(listOf(Call.Scrobble("s1", 1_000)), delegate.calls)
            assertEquals(0, db.pendingActionDao().pending(sourceId).size)
        }

    @Test
    fun aStarTheServerRejectsIsNotQueued() =
        runBlocking {
            offline.value = false
            delegate.starFailure = SubsonicException(40, "nope")

            assertThrows(SubsonicException::class.java) {
                runBlocking { sink.setStar(StarType.Song, "s1", true) }
            }

            assertEquals(0, db.pendingActionDao().pending(sourceId).size)
        }

    @Test
    fun aTransportFailureIsQueuedRatherThanRejected() =
        runBlocking {
            offline.value = false
            delegate.starFailure = SubsonicException(-1, "HTTP 503")

            sink.setStar(StarType.Song, "s1", true)

            assertEquals(1, db.pendingActionDao().pending(sourceId).size)
        }

    @Test
    fun flushKeepsQueuedActionsWhileTheServerIsStillUnreachable() =
        runBlocking {
            sink.setStar(StarType.Song, "s1", true)

            offline.value = false
            delegate.starFailure = IOException("down")
            sink.flush()

            assertEquals(1, db.pendingActionDao().pending(sourceId).size)

            delegate.starFailure = null
            sink.flush()

            assertEquals(listOf(Call.Star(StarType.Song, "s1", true)), delegate.calls)
            assertEquals(0, db.pendingActionDao().pending(sourceId).size)
        }

    @Test
    fun flushKeepsQueuedActionsOnATransportFailure() =
        runBlocking {
            sink.scrobble("s1", 1_000)

            offline.value = false
            delegate.scrobbleFailure = SubsonicException(-1, "HTTP 503")
            sink.flush()

            assertEquals(1, db.pendingActionDao().pending(sourceId).size)

            delegate.scrobbleFailure = null
            sink.flush()

            assertEquals(listOf(Call.Scrobble("s1", 1_000)), delegate.calls)
            assertEquals(0, db.pendingActionDao().pending(sourceId).size)
        }
}
