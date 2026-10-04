package com.subtracks.ui.library

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Artist
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
        isAlbumArtist: Boolean,
    ) {
        composeRule.setContent {
            SubtracksTheme {
                ArtistDetailScreen(
                    artist = artist,
                    artistName = artistName,
                    isAlbumArtist = isAlbumArtist,
                    albums = emptyList(),
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
        render(artist = null, artistName = "Guest", isAlbumArtist = false)

        composeRule.onAllNodesWithText("Guest")[0].assertExists()
        composeRule.onNodeWithText("Artist").assertExists()
    }

    @Test
    fun anAlbumArtistIsLabelledAlbumArtist() {
        render(
            artist = Artist(sourceId = 1, id = "ar-1", name = "Radiohead", albumCount = 1, starred = null),
            artistName = "Radiohead",
            isAlbumArtist = true,
        )

        composeRule.onNodeWithText("Album artist").assertExists()
    }
}
