package com.subtracks.playback

import com.subtracks.data.source.ServerActionSink
import com.subtracks.data.source.StarType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ScrobblerTest {
    private sealed interface Call {
        data class NowPlaying(
            val songId: String,
        ) : Call

        data class Scrobble(
            val songId: String,
            val time: Long,
        ) : Call
    }

    private class RecordingSink : ServerActionSink {
        val calls = mutableListOf<Call>()
        var failScrobble = false

        override suspend fun nowPlaying(songId: String) {
            calls += Call.NowPlaying(songId)
        }

        override suspend fun scrobble(
            songId: String,
            time: Long,
        ) {
            if (failScrobble) throw IOException("scrobble failed")
            calls += Call.Scrobble(songId, time)
        }

        override suspend fun setStar(
            type: StarType,
            id: String,
            starred: Boolean,
        ) = Unit
    }

    private fun TestScope.testScope() = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + SupervisorJob())

    @Test
    fun sendsNowPlayingThenScrobbleOnce() =
        runTest {
            val sink = RecordingSink()
            val scrobbler = Scrobbler(sink = sink, enabled = flowOf(true), scope = testScope(), policy = ScrobblePolicy(now = { 1_000L }))
            val state = MutableStateFlow(PlaybackState())
            val position = MutableStateFlow(0L)
            scrobbler.attach(state, position)
            val item = QueueItem("s1", "Title", "Artist", "Album", null, durationMs = 30_000)

            state.value = PlaybackState(item = item, isPlaying = true)
            assertEquals(listOf(Call.NowPlaying("s1")), sink.calls)

            var tick = 500L
            while (tick <= 15_000L) {
                position.value = tick
                tick += 500
            }

            assertEquals(
                listOf(
                    Call.NowPlaying("s1"),
                    Call.Scrobble("s1", 1_000L),
                ),
                sink.calls,
            )

            position.value = 25_000L
            assertEquals(2, sink.calls.size)
        }

    @Test
    fun fallsBackToThePlayerDurationWhenTheQueueItemHasNone() =
        runTest {
            val sink = RecordingSink()
            val scrobbler = Scrobbler(sink = sink, enabled = flowOf(true), scope = testScope(), policy = ScrobblePolicy(now = { 1_000L }))
            val state = MutableStateFlow(PlaybackState())
            val position = MutableStateFlow(0L)
            scrobbler.attach(state, position)
            val item = QueueItem("s1", "Title", "Artist", "Album", null, durationMs = null)

            state.value = PlaybackState(item = item, isPlaying = true, durationMs = 30_000)
            var tick = 500L
            while (tick <= 15_000L) {
                position.value = tick
                tick += 500
            }

            assertEquals(
                listOf(
                    Call.NowPlaying("s1"),
                    Call.Scrobble("s1", 1_000L),
                ),
                sink.calls,
            )
        }

    @Test
    fun aSubmissionRecordsThePlayLocally() =
        runTest {
            val sink = RecordingSink()
            val recorded = mutableListOf<Pair<String, Long>>()
            val scrobbler =
                Scrobbler(
                    sink = sink,
                    enabled = flowOf(true),
                    scope = testScope(),
                    policy = ScrobblePolicy(now = { 1_000L }),
                    recordPlay = { songId, at -> recorded += songId to at },
                )
            val state = MutableStateFlow(PlaybackState())
            val position = MutableStateFlow(0L)
            scrobbler.attach(state, position)
            val item = QueueItem("s1", "Title", "Artist", "Album", null, durationMs = 30_000)

            state.value = PlaybackState(item = item, isPlaying = true)
            assertEquals(emptyList<Pair<String, Long>>(), recorded)

            var tick = 500L
            while (tick <= 15_000L) {
                position.value = tick
                tick += 500
            }

            assertEquals(listOf("s1" to 1L), recorded)
        }

    @Test
    fun aFailedSubmissionStillRecordsThePlayLocally() =
        runTest {
            val sink = RecordingSink().apply { failScrobble = true }
            val recorded = mutableListOf<Pair<String, Long>>()
            val scrobbler =
                Scrobbler(
                    sink = sink,
                    enabled = flowOf(true),
                    scope = testScope(),
                    policy = ScrobblePolicy(now = { 1_000L }),
                    recordPlay = { songId, at -> recorded += songId to at },
                )
            val state = MutableStateFlow(PlaybackState())
            val position = MutableStateFlow(0L)
            scrobbler.attach(state, position)
            val item = QueueItem("s1", "Title", "Artist", "Album", null, durationMs = 30_000)

            state.value = PlaybackState(item = item, isPlaying = true)
            var tick = 500L
            while (tick <= 15_000L) {
                position.value = tick
                tick += 500
            }

            assertEquals(emptyList<Call>(), sink.calls.filterIsInstance<Call.Scrobble>())
            assertEquals(listOf("s1" to 1L), recorded)
        }

    @Test
    fun disabledSendsNothing() =
        runTest {
            val sink = RecordingSink()
            val enabled = MutableStateFlow(false)
            val scrobbler = Scrobbler(sink = sink, enabled = enabled, scope = testScope(), policy = ScrobblePolicy(now = { 1_000L }))
            val state = MutableStateFlow(PlaybackState())
            val position = MutableStateFlow(0L)
            scrobbler.attach(state, position)
            val item = QueueItem("s1", "Title", "Artist", "Album", null, durationMs = 30_000)

            state.value = PlaybackState(item = item, isPlaying = true)
            var tick = 500L
            while (tick <= 20_000L) {
                position.value = tick
                tick += 500
            }
            assertEquals(emptyList<Call>(), sink.calls)

            enabled.value = true
            assertEquals(listOf(Call.NowPlaying("s1")), sink.calls)
        }

    @Test
    fun aTransitionSendsNowPlayingOnceForTheNewTrack() =
        runTest {
            val sink = RecordingSink()
            val scrobbler = Scrobbler(sink = sink, enabled = flowOf(true), scope = testScope(), policy = ScrobblePolicy(now = { 1_000L }))
            val state = MutableStateFlow(PlaybackState())
            val position = MutableStateFlow(0L)
            scrobbler.attach(state, position)
            val first = QueueItem("s1", "One", "Artist", "Album", null, durationMs = 30_000)
            val second = QueueItem("s2", "Two", "Artist", "Album", null, durationMs = 30_000)

            state.value = PlaybackState(item = first, isPlaying = true)
            var tick = 500L
            while (tick <= 15_000L) {
                position.value = tick
                tick += 500
            }
            position.value = 0L
            state.value = PlaybackState(item = second, isPlaying = true)

            assertEquals(
                listOf(
                    Call.NowPlaying("s1"),
                    Call.Scrobble("s1", 1_000L),
                    Call.NowPlaying("s2"),
                ),
                sink.calls,
            )
        }
}
