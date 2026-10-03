package com.subtracks.ui.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Album
import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {
    @Test
    fun playRowsAreKeptWhenTheServerHasPlayData() {
        val feed =
            buildHomeFeed(
                recentAlbums = listOf(album("a")),
                recentArtists = listOf(artist("r")),
                frequentAlbums = listOf(album("b")),
                frequentArtists = listOf(artist("s")),
                genres = listOf("Rock"),
                decades = listOf(1970L),
                starredSongs = listOf(starred("song")),
                addedAlbums = listOf(album("c")),
                rediscover = listOf(album("d")),
                hasPlayData = true,
            )

        assertEquals(listOf("a"), feed.recentlyPlayedAlbums.map { it.id })
        assertEquals(listOf("r"), feed.recentlyPlayedArtists.map { it.id })
        assertEquals(listOf("b"), feed.mostPlayedAlbums.map { it.id })
        assertEquals(listOf("s"), feed.mostPlayedArtists.map { it.id })
        assertEquals(listOf("Rock"), feed.genres)
        assertEquals(listOf(1970L), feed.decades)
        assertEquals(listOf("song"), feed.recentlyStarredSongs.map { it.song.id })
        assertEquals(listOf("c"), feed.recentlyAddedAlbums.map { it.id })
        assertEquals(listOf("d"), feed.rediscoverAlbums.map { it.id })
    }

    @Test
    fun aServerWithNoPlayDataHidesThePlayRowsButKeepsTheRest() {
        val feed =
            buildHomeFeed(
                recentAlbums = listOf(album("a")),
                recentArtists = listOf(artist("r")),
                frequentAlbums = listOf(album("b")),
                frequentArtists = listOf(artist("s")),
                genres = listOf("Rock"),
                decades = listOf(1970L),
                starredSongs = listOf(starred("song")),
                addedAlbums = listOf(album("c")),
                rediscover = listOf(album("d")),
                hasPlayData = false,
            )

        assertTrue(feed.recentlyPlayedAlbums.isEmpty())
        assertTrue(feed.recentlyPlayedArtists.isEmpty())
        assertTrue(feed.mostPlayedAlbums.isEmpty())
        assertTrue(feed.mostPlayedArtists.isEmpty())
        assertEquals(listOf("c"), feed.recentlyAddedAlbums.map { it.id })
        assertEquals(listOf("Rock"), feed.genres)
        assertEquals(listOf("song"), feed.recentlyStarredSongs.map { it.song.id })
    }

    private fun album(id: String) =
        Album(
            sourceId = 1,
            id = id,
            artistId = "ar",
            name = id,
            albumArtist = "Artist",
            created = 0,
            coverArt = null,
            genre = null,
            year = null,
            starred = null,
            songCount = 1,
        )

    private fun artist(id: String) =
        Artist(
            sourceId = 1,
            id = id,
            name = id,
            albumCount = 1,
            starred = null,
            coverArt = null,
        )

    private fun starred(id: String) =
        AlbumSongItem(
            song =
                Song(
                    sourceId = 1,
                    id = id,
                    albumId = "al",
                    artistId = "ar",
                    title = id,
                    album = "Album",
                    artist = "Artist",
                    duration = 100,
                    track = 1,
                    disc = 1,
                    starred = 1,
                    genre = null,
                ),
            coverArt = null,
        )
}
