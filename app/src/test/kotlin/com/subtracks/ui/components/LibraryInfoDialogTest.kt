package com.subtracks.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.ArtistRef
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LibraryInfoDialogTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun anAlbumShowsEveryArtistAndBothSortNames() {
        composeRule.setContent {
            SubtracksTheme {
                AlbumInfoDialog(
                    album =
                        Album(
                            sourceId = 1,
                            id = "al1",
                            artistId = "ar1",
                            name = "Album",
                            albumArtist = "A",
                            created = 0,
                            coverArt = null,
                            genre = "Rock",
                            year = 1999,
                            starred = null,
                            songCount = 12,
                            artists = listOf(ArtistRef("ar1", "A"), ArtistRef("ar2", "B")),
                            sortName = "Album, The",
                            albumArtistSort = "A, The",
                        ),
                    download = null,
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("A · B").assertIsDisplayed()
        composeRule.onNodeWithText("Album, The").assertIsDisplayed()
        composeRule.onNodeWithText("A, The").assertIsDisplayed()
        composeRule.onNodeWithText("1999").assertIsDisplayed()
    }

    @Test
    fun anArtistShowsItsSortNameAndCounts() {
        composeRule.setContent {
            SubtracksTheme {
                ArtistInfoDialog(
                    artist =
                        Artist(
                            sourceId = 1,
                            id = "ar1",
                            name = "The Artist",
                            albumCount = 4,
                            starred = null,
                            songCount = 40,
                            sortName = "Artist, The",
                        ),
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("Artist, The").assertIsDisplayed()
        composeRule.onNodeWithText("4").assertIsDisplayed()
        composeRule.onNodeWithText("40").assertIsDisplayed()
    }
}
