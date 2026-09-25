package com.subtracks.data.source.subsonic

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
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
    private val server: Server,
) {
    private lateinit var source: SubsonicSource

    @Before
    fun setUp() {
        val client =
            SubsonicClient(
                baseUrl = server.baseUrl.toHttpUrl(),
                username = server.username,
                password = server.password,
                useTokenAuth = false,
                http = OkHttpClient(),
            )
        source = SubsonicSource(1, client)
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
            assertTrue(kosmo.frequentRank != null || kosmo.recentRank != null)

            assertNull(albums.first { it.name == "Retroconnaissance EP" }.starred)
            assertNull(albums.first { it.name == "I Don't Know What I'm Doing" }.starred)
        }

    @Test
    fun songs() =
        runBlocking {
            assertEquals(20, source.songs().collectAll().size)
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

    data class Server(
        val name: String,
        val baseUrl: String,
        val username: String,
        val password: String,
    )

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun servers(): List<Array<Any>> =
            listOf(
                arrayOf(Server("navidrome", "http://localhost:4533/", "admin", "password")),
                arrayOf(Server("gonic", "http://localhost:4747/", "admin", "admin")),
            )
    }
}
