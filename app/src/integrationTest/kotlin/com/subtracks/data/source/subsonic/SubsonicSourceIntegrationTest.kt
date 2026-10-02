package com.subtracks.data.source.subsonic

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class SubsonicSourceIntegrationTest(
    private val server: TestServer,
) {
    private lateinit var source: SubsonicSource

    @Before
    fun setUp() {
        source = TestServers.source(server)
    }

    @Test
    fun ping() =
        runBlocking {
            source.ping()
        }

    @Test
    fun artists() =
        runBlocking {
            val artists = source.artists().collectAll()

            assertEquals(2, artists.size)
            assertNotNull(artists.first { it.name == "Ugress" }.starred)
            assertNull(artists.first { it.name == "Brad Sucks" }.starred)
        }

    @Test
    fun albums() =
        runBlocking {
            val albums = source.albums().collectAll()

            assertEquals(3, albums.size)

            val kosmo = albums.first { it.name == "Kosmonaut" }
            assertTrue(kosmo.id.isNotEmpty())
            assertTrue(kosmo.artistId!!.isNotEmpty())
            assertEquals("Ugress", kosmo.albumArtist)
            assertEquals(2006L, kosmo.year)
            assertEquals("Electronic", kosmo.genre)
            assertNotNull(kosmo.coverArt)
            assertNotNull(kosmo.starred)
            assertTrue(kosmo.created <= System.currentTimeMillis() / 1000)

            assertNull(albums.first { it.name == "Retroconnaissance EP" }.starred)
            assertNull(albums.first { it.name == "I Don't Know What I'm Doing" }.starred)
        }

    @Test
    fun songs() =
        runBlocking {
            val songs = source.songs().collectAll()

            assertEquals(20, songs.size)
            assertTrue(songs.all { song -> song.genre == null || song.genres.isEmpty() || song.genres.contains(song.genre) })
            // Nextcloud Music does not populate the OpenSubsonic genres array.
            if (server.name != "nextcloud") {
                assertTrue(songs.any { it.genres.isNotEmpty() })
            }
        }

    @Test
    fun playlists() =
        runBlocking {
            val playlists = source.playlists().collectAll()

            assertEquals(1, playlists.size)
            assertEquals(7, source.playlistSongs(playlists.map { it.id }).collectAll().size)
        }

    @Test
    fun albumArtistRelation() =
        runBlocking {
            val artists = source.artists().collectAll()
            val albums = source.albums().collectAll()

            val ugressAlbums =
                albums
                    .filter { it.artistId == artists.first { a -> a.name == "Ugress" }.id }
                    .map { it.name }
                    .sorted()
            assertEquals(listOf("Kosmonaut", "Retroconnaissance EP"), ugressAlbums)

            val bradAlbums =
                albums
                    .filter { it.artistId == artists.first { a -> a.name == "Brad Sucks" }.id }
                    .map { it.name }
            assertEquals(listOf("I Don't Know What I'm Doing"), bradAlbums)
        }

    private suspend fun <T> Flow<List<T>>.collectAll(): List<T> = toList().flatten()

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun servers(): List<Array<Any>> = TestServers.all.map { arrayOf<Any>(it) }
    }
}
