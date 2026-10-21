package com.subtracks.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.Song
import com.subtracks.ui.library.SongRow
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SongArtistDisplayTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val song =
        Song(
            sourceId = 1,
            id = "s1",
            albumId = "al1",
            artistId = "ar2",
            title = "Song",
            album = "Album",
            artist = "Two",
            duration = 100,
            track = 1,
            disc = 1,
            starred = null,
            artists = listOf(ArtistRef("ar1", "One"), ArtistRef("ar2", "Two"), ArtistRef("ar3", "Three")),
        )

    @Test
    fun aSongRowListsThePluralArtistsWithTheSingularArtistFirst() {
        composeRule.setContent {
            SubtracksTheme {
                SongRow(song = song)
            }
        }

        composeRule.onNodeWithText("Two · One · Three").assertIsDisplayed()
    }

    @Test
    fun theInfoDialogListsThePluralArtistsWithTheSingularArtistFirst() {
        composeRule.setContent {
            SubtracksTheme {
                SongInfoDialog(
                    song = song,
                    download = null,
                    localFile = null,
                    streamEncoding = null,
                    onDismiss = {},
                    genres = emptyList(),
                )
            }
        }

        composeRule.onNodeWithText("Two · One · Three").assertIsDisplayed()
    }
}
