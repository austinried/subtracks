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
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Song
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ItemContextMenuStarTest {
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
            artist = "Artist",
            duration = 100,
            track = 1,
            disc = 1,
            starred = null,
            genre = null,
        )

    @Test
    fun starringFillsTheIconImmediatelyThenDismisses() {
        var dismissed = false
        var starred: Boolean? = null
        composeRule.setContent {
            SubtracksTheme {
                var show by remember { mutableStateOf(true) }
                if (show) {
                    ItemContextMenu(
                        target = MenuTarget.Song(song),
                        actions = ItemActions(setStar = { _, _, value -> starred = value }),
                        onDismiss = {
                            dismissed = true
                            show = false
                        },
                    )
                }
            }
        }

        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithText("Star").performClick()
        composeRule.mainClock.advanceTimeBy(100)

        composeRule.onNodeWithText("Unstar").assertIsDisplayed()
        assertEquals(true, starred)
        assertFalse("the menu should stay open so the new icon is visible", dismissed)

        composeRule.mainClock.advanceTimeBy(1_000)
        assertTrue("starring should dismiss the menu", dismissed)
    }
}
