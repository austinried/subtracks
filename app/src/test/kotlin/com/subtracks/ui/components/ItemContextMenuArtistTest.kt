package com.subtracks.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.Song
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun pluralArtistsShareOneEntryThatOpensTheChooser() {
        var opened: List<ArtistRef>? = null
        composeRule.setContent {
            SubtracksTheme {
                var show by remember { mutableStateOf(true) }
                if (show) {
                    ItemContextMenu(
                        target = MenuTarget.Song(song),
                        actions = ItemActions(viewArtist = {}),
                        onDismiss = { show = false },
                        onChooseArtists = { opened = it },
                    )
                }
            }
        }

        val entry = composeRule.onAllNodesWithText("A · B").filterToOne(hasClickAction())
        entry.assertIsDisplayed()
        assertTrue(
            composeRule
                .onAllNodesWithText("A")
                .filter(hasClickAction())
                .fetchSemanticsNodes()
                .isEmpty(),
        )
        assertTrue(
            composeRule
                .onAllNodesWithText("B")
                .filter(hasClickAction())
                .fetchSemanticsNodes()
                .isEmpty(),
        )

        composeRule.mainClock.autoAdvance = false
        entry.performClick()
        composeRule.mainClock.advanceTimeBy(1_000)

        assertEquals(listOf("ar1", "ar2"), opened?.map { it.id })
    }

    @Test
    fun aSingleArtistOpensDirectly() {
        var opened: String? = null
        composeRule.setContent {
            SubtracksTheme {
                ItemContextMenu(
                    target = MenuTarget.Song(song.copy(artists = listOf(ArtistRef("ar1", "A")))),
                    actions = ItemActions(viewArtist = { opened = it }),
                    onDismiss = {},
                )
            }
        }

        composeRule.mainClock.autoAdvance = false
        composeRule.onAllNodesWithText("A").filterToOne(hasClickAction()).performClick()
        composeRule.mainClock.advanceTimeBy(1_000)

        assertEquals("ar1", opened)
    }

    @Test
    fun manyArtistsStillShareOneEntry() {
        var opened: List<ArtistRef>? = null
        composeRule.setContent {
            SubtracksTheme {
                ItemContextMenu(
                    target =
                        MenuTarget.Song(
                            song.copy(artists = (1..20).map { ArtistRef("ar$it", "Artist $it") }),
                        ),
                    actions = ItemActions(viewArtist = {}),
                    onDismiss = {},
                    onChooseArtists = { opened = it },
                )
            }
        }

        val entry =
            composeRule
                .onAllNodesWithText((1..20).joinToString(" · ") { "Artist $it" })
                .filterToOne(hasClickAction())
        entry.assertIsDisplayed()

        composeRule.mainClock.autoAdvance = false
        entry.performClick()
        composeRule.mainClock.advanceTimeBy(1_000)

        assertEquals(20, opened?.size)
    }
}
