package com.subtracks.ui.search

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.subtracks.data.model.Album
import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.Song
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelXL)
class SearchScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun resultsAreGroupedByTypeAndTappingASongOrAlbumFiresItsCallback() {
        var played: Song? = null
        var opened: Album? = null
        composeRule.setContent {
            SubtracksTheme {
                SearchScreen(
                    query = "road",
                    onQueryChange = {},
                    results =
                        SearchResults(
                            albums = listOf(album("al-1", "Road Album")),
                            artists = listOf(artist("ar-1", "Road Artist")),
                            playlists = listOf(playlist("pl-1", "Road Trip")),
                            songs = listOf(AlbumSongItem(song("s1", "Road Song"), coverArt = null)),
                        ),
                    onSongClick = { played = it },
                    onAlbumClick = { opened = it },
                )
            }
        }

        composeRule.onNodeWithText("Songs").assertIsDisplayed()
        composeRule.onNodeWithText("Albums").assertIsDisplayed()
        composeRule.onNodeWithText("Artists").assertIsDisplayed()
        composeRule.onNodeWithText("Playlists").assertIsDisplayed()

        composeRule.onNodeWithText("Road Song").performClick()
        composeRule.onNodeWithText("Road Album").performClick()

        assertEquals("s1", played?.id)
        assertEquals("al-1", opened?.id)
    }

    @Test
    fun aQueryShorterThanThreeCharactersPromptsForMoreInput() {
        composeRule.setContent {
            SubtracksTheme {
                SearchScreen(query = "ro", onQueryChange = {}, results = SearchResults())
            }
        }

        composeRule.onNodeWithText("Type at least 3 characters to search.").assertIsDisplayed()
    }

    @Test
    fun aQueryWithNoResultsSaysSo() {
        composeRule.setContent {
            SubtracksTheme {
                SearchScreen(query = "zzz", onQueryChange = {}, results = SearchResults())
            }
        }

        composeRule.onNodeWithText("No results", substring = true).assertIsDisplayed()
    }

    @Test
    fun idsSharedAcrossTypesDoNotCollide() {
        composeRule.setContent {
            SubtracksTheme {
                SearchScreen(
                    query = "road",
                    onQueryChange = {},
                    results =
                        SearchResults(
                            albums = listOf(album("1", "Road Album")),
                            artists = listOf(artist("1", "Road Artist")),
                            playlists = listOf(playlist("1", "Road Trip")),
                            songs = listOf(AlbumSongItem(song("1", "Road Song"), coverArt = null)),
                        ),
                )
            }
        }

        composeRule.onNodeWithText("Road Song").assertIsDisplayed()
        composeRule.onNodeWithText("Road Album").assertIsDisplayed()
        composeRule.onNodeWithText("Road Artist").assertIsDisplayed()
        composeRule.onNodeWithText("Road Trip").assertIsDisplayed()
    }

    private fun album(
        id: String,
        name: String,
    ) = Album(
        sourceId = 1,
        id = id,
        artistId = "ar-1",
        name = name,
        albumArtist = "Artist",
        created = 0,
        coverArt = null,
        genre = null,
        year = null,
        starred = null,
        songCount = 1,
    )

    private fun artist(
        id: String,
        name: String,
    ) = Artist(
        sourceId = 1,
        id = id,
        name = name,
        albumCount = 1,
        starred = null,
        coverArt = null,
    )

    private fun playlist(
        id: String,
        name: String,
    ) = Playlist(
        sourceId = 1,
        id = id,
        name = name,
        comment = null,
        coverArt = null,
        songCount = 1,
        created = 0,
        changed = 0,
        duration = 100,
    )

    private fun song(
        id: String,
        title: String,
    ) = Song(
        sourceId = 1,
        id = id,
        albumId = "al-1",
        artistId = "ar-1",
        title = title,
        album = "Album",
        artist = "Artist",
        duration = 100,
        track = 1,
        disc = 1,
        starred = null,
    )
}
