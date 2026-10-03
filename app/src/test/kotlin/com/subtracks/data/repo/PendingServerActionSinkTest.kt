package com.subtracks.data.repo

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.PendingActionKind
import com.subtracks.data.model.Source
import com.subtracks.data.source.ServerActionSink
import com.subtracks.data.source.StarType
import com.subtracks.data.source.subsonic.SubsonicException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
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

        override suspend fun flush() = Unit
    }

    private lateinit var db: SubtracksDatabase
    private lateinit var scope: CoroutineScope
    private lateinit var delegate: RecordingSink
    private lateinit var sink: PendingServerActionSink
    private val offline = MutableStateFlow(true)
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
        sink = PendingServerActionSink(delegate, db, activeSourceId = { sourceId }, offline = offline, scope = scope)
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
}
