package com.subtracks.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.Song
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ItemContextMenuArtistTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val song =
        Song(
            sourceId = 1,
            id = "s1",
            albumId = "al1",
            artistId = "ar1",
            title = "Song",
            album = "Album",
            artist = "A • B",
            duration = 100,
            track = 1,
            disc = 1,
            starred = null,
            genre = null,
            artists = listOf(ArtistRef("ar1", "A"), ArtistRef("ar2", "B")),
        )

    @Test
    fun showsOneEntryPerArtistAndOpensTheChosenOne() {
        var opened: String? = null
        composeRule.setContent {
            SubtracksTheme {
                var show by remember { mutableStateOf(true) }
                if (show) {
                    ItemContextMenu(
                        target = MenuTarget.Song(song),
                        actions = ItemActions(viewArtist = { opened = it }),
                        onDismiss = { show = false },
                    )
                }
            }
        }

        composeRule.onNodeWithText("A").assertIsDisplayed()
        composeRule.onNodeWithText("B").assertIsDisplayed()

        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithText("B").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)

        assertEquals("ar2", opened)
    }

    @Test
    fun longMenusScrollToTheLastArtist() {
        var opened: String? = null
        composeRule.setContent {
            SubtracksTheme {
                ItemContextMenu(
                    target =
                        MenuTarget.Song(
                            song.copy(artists = (1..20).map { ArtistRef("ar$it", "Artist $it") }),
                        ),
                    actions = ItemActions(viewArtist = { opened = it }),
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("Artist 20").performScrollTo().assertIsDisplayed()

        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithText("Artist 20").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)

        assertEquals("ar20", opened)
    }
}
