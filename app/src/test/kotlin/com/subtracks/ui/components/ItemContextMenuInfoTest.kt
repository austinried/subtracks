package com.subtracks.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ItemContextMenuInfoTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val album =
        Album(
            sourceId = 1,
            id = "al1",
            artistId = "ar1",
            name = "Album",
            albumArtist = "Artist",
            created = 0,
            coverArt = null,
            genre = null,
            year = null,
            starred = null,
            songCount = 1,
        )

    @Test
    fun anAlbumMenuOpensItsInfo() {
        var opened: MenuTarget? = null
        composeRule.setContent {
            SubtracksTheme {
                ItemContextMenu(
                    target = MenuTarget.Album(album),
                    actions = ItemActions(),
                    onDismiss = {},
                    onInfo = { opened = it },
                )
            }
        }

        composeRule.onNodeWithText("Info").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)

        assertEquals(album, (opened as? MenuTarget.Album)?.album)
    }

    @Test
    fun anArtistMenuOpensItsInfo() {
        var opened: MenuTarget? = null
        composeRule.setContent {
            SubtracksTheme {
                ItemContextMenu(
                    target =
                        MenuTarget.Artist(
                            Artist(
                                sourceId = 1,
                                id = "ar1",
                                name = "Artist",
                                albumCount = 1,
                                starred = null,
                            ),
                        ),
                    actions = ItemActions(),
                    onDismiss = {},
                    onInfo = { opened = it },
                )
            }
        }

        composeRule.onNodeWithText("Info").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)

        assertEquals("ar1", (opened as? MenuTarget.Artist)?.artist?.id)
    }
}
