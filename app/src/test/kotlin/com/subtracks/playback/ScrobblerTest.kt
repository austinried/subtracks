package com.subtracks.playback

import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.source.MusicSource
import com.subtracks.data.source.StarType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScrobblerTest {
    private data class Call(
        val songId: String,
        val submission: Boolean,
        val time: Long?,
    )

    private class RecordingSource : MusicSource {
        override val id: Long = 1
        val calls = mutableListOf<Call>()

        override suspend fun ping() = Unit

        override suspend fun setStar(
            type: StarType,
            id: String,
            starred: Boolean,
        ) = Unit

        override suspend fun scrobble(
            songId: String,
            submission: Boolean,
            time: Long?,
        ) {
            calls += Call(songId, submission, time)
        }

        override fun artists(): Flow<List<Artist>> = emptyFlow()

        override fun albums(): Flow<List<Album>> = emptyFlow()

        override fun songs(): Flow<List<Song>> = emptyFlow()

        override fun playlists(): Flow<List<Playlist>> = emptyFlow()

        override fun playlistSongs(playlistIds: List<String>): Flow<List<PlaylistSong>> = emptyFlow()
    }

    @Test
    fun sendsNowPlayingThenScrobbleOnce() =
        runTest {
            val source = RecordingSource()
            val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler) + SupervisorJob())
            val scrobbler = Scrobbler(source = { source }, scope = scope, policy = ScrobblePolicy(now = { 1_000L }))
            val state = MutableStateFlow(PlaybackState())
            val position = MutableStateFlow(0L)
            scrobbler.attach(state, position)
            val item = QueueItem("s1", "Title", "Artist", "Album", null, durationMs = 30_000)

            state.value = PlaybackState(item = item, isPlaying = true)
            assertEquals(listOf(Call("s1", submission = false, time = null)), source.calls)

            var tick = 500L
            while (tick <= 15_000L) {
                position.value = tick
                tick += 500
            }

            assertEquals(
                listOf(
                    Call("s1", submission = false, time = null),
                    Call("s1", submission = true, time = 1_000L),
                ),
                source.calls,
            )

            position.value = 25_000L
            assertEquals(2, source.calls.size)
        }
}
