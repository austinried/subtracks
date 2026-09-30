package com.subtracks.data.source.subsonic

import com.subtracks.data.source.StarType
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SubsonicWriteIntegrationTest(
    private val server: TestServer,
) {
    @Test
    fun starAndUnstarAlbumRoundTrips() =
        runBlocking {
            val source = TestServers.source(server)
            val album =
                source
                    .albums()
                    .toList()
                    .flatten()
                    .first { it.name == "Retroconnaissance EP" }

            source.setStar(StarType.Album, album.id, true)
            try {
                assertNotNull(
                    "$server",
                    source
                        .albums()
                        .toList()
                        .flatten()
                        .first { it.id == album.id }
                        .starred,
                )
            } finally {
                source.setStar(StarType.Album, album.id, false)
            }
            assertNull(
                "$server",
                source
                    .albums()
                    .toList()
                    .flatten()
                    .first { it.id == album.id }
                    .starred,
            )
        }

    @Test
    fun starAndUnstarArtistRoundTrips() =
        runBlocking {
            val source = TestServers.source(server)
            val artist =
                source
                    .artists()
                    .toList()
                    .flatten()
                    .first { it.name == "Brad Sucks" }

            source.setStar(StarType.Artist, artist.id, true)
            try {
                assertNotNull(
                    "$server",
                    source
                        .artists()
                        .toList()
                        .flatten()
                        .first { it.id == artist.id }
                        .starred,
                )
            } finally {
                source.setStar(StarType.Artist, artist.id, false)
            }
            assertNull(
                "$server",
                source
                    .artists()
                    .toList()
                    .flatten()
                    .first { it.id == artist.id }
                    .starred,
            )
        }

    @Test
    fun starAndUnstarSongRoundTrips() =
        runBlocking {
            val source = TestServers.source(server)
            val song =
                source
                    .songs()
                    .toList()
                    .flatten()
                    .first { it.album == "Retroconnaissance EP" }

            source.setStar(StarType.Song, song.id, true)
            try {
                assertNotNull(
                    "$server",
                    source
                        .songs()
                        .toList()
                        .flatten()
                        .first { it.id == song.id }
                        .starred,
                )
            } finally {
                source.setStar(StarType.Song, song.id, false)
            }
            assertNull(
                "$server",
                source
                    .songs()
                    .toList()
                    .flatten()
                    .first { it.id == song.id }
                    .starred,
            )
        }

    @Test
    fun scrobbleAcceptsNowPlayingAndSubmission() =
        runBlocking {
            val source = TestServers.source(server)
            val song =
                source
                    .songs()
                    .toList()
                    .flatten()
                    .first()

            source.scrobble(song.id, submission = false)
            source.scrobble(song.id, submission = true, time = System.currentTimeMillis())
        }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun servers(): List<Array<Any>> = TestServers.all.map { arrayOf<Any>(it) }
    }
}
