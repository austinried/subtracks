package com.subtracks.ui.library

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.ArtistAlbums
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ArtistDetailScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun render(
        artist: Artist?,
        artistName: String,
        albums: ArtistAlbums = ArtistAlbums(),
        albumsLoaded: Boolean = true,
    ) {
        composeRule.setContent {
            SubtracksTheme {
                ArtistDetailScreen(
                    artist = artist,
                    artistName = artistName,
                    albums = albums,
                    albumsLoaded = albumsLoaded,
                    art = null,
                    coverArt = { _, _ -> null },
                    onBack = {},
                    onAlbumClick = {},
                )
            }
        }
    }

    @Test
    fun aCreditedTrackArtistShowsItsDerivedNameAndIsLabelledArtist() {
        render(
            artist = null,
            artistName = "Guest",
            albums = ArtistAlbums(appearances = listOf(album("al-1", "Guest Album"))),
        )

        composeRule.onAllNodesWithText("Guest")[0].assertExists()
        composeRule.onNodeWithText("Artist · 1 album").assertExists()
        composeRule.onAllNodesWithContentDescription("More options").assertCountEquals(0)
    }

    @Test
    fun anAlbumArtistIsLabelledAlbumArtist() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 1, starred = null),
            artistName = "Radiohead",
            albums = ArtistAlbums(albumCredits = listOf(album("al-1", "Kid A"))),
        )

        composeRule.onNodeWithText("Album artist · 1 album").assertExists()
        composeRule.onNodeWithContentDescription("More options").assertExists()
    }

    @Test
    fun mixedCreditsShowAlbumCreditsAndAppearancesInSeparateSections() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 2, starred = null),
            artistName = "Radiohead",
            albums =
                ArtistAlbums(
                    albumCredits = listOf(album("al-1", "Kid A")),
                    appearances = listOf(album("al-2", "Guest Album")),
                ),
        )

        composeRule.onNodeWithText("Album artist · 2 albums").assertExists()

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("ALBUMS"))
        composeRule.onNodeWithText("ALBUMS").assertExists()
        composeRule.onNodeWithText("Kid A").assertExists()

        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("APPEARS ON"))
        composeRule.onNodeWithText("APPEARS ON").assertExists()
        composeRule.onNodeWithText("Guest Album").assertExists()
    }

    @Test
    fun theTypeAndCountStayBlankUntilTheAlbumsLoad() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 1, starred = null),
            artistName = "Radiohead",
            albumsLoaded = false,
        )

        composeRule.onNodeWithText("Artist · 0 albums").assertDoesNotExist()
        composeRule.onNodeWithText("Album artist · 0 albums").assertDoesNotExist()
    }

    private fun album(
        id: String,
        name: String,
    ) = Album(
        sourceId = 1,
        id = id,
        artistId = "ar-1",
        name = name,
        albumArtist = "Radiohead",
        created = 0,
        coverArt = null,
        genre = null,
        year = null,
        starred = null,
        songCount = 1,
    )
}
