package com.subtracks.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.ArtistRef
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ArtistChooserSheetTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun listsEveryArtistAndOpensTheChosenOne() {
        var opened: ArtistRef? = null
        composeRule.setContent {
            SubtracksTheme {
                ArtistChooserSheet(
                    artists = listOf(ArtistRef("ar1", "A"), ArtistRef("ar2", "B")),
                    onArtistClick = { opened = it },
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("Artists").assertIsDisplayed()
        composeRule.onNodeWithText("A").assertIsDisplayed()
        composeRule.onNodeWithText("B").assertIsDisplayed()

        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithText("B").performClick()
        composeRule.mainClock.advanceTimeBy(1_000)

        assertEquals(ArtistRef("ar2", "B"), opened)
    }
}
