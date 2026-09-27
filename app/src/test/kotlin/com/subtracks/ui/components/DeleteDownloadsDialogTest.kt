package com.subtracks.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DeleteDownloadsDialogTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun theDialogNamesTheThingAndTheSpaceItFrees() {
        render(bytes = 1_500_000)

        composeRule.onNodeWithText("Kid A", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("1.5 MB", substring = true).assertIsDisplayed()
    }

    @Test
    fun deleteConfirms() {
        var confirms = 0
        render(onConfirm = { confirms++ })

        composeRule.onNodeWithText("Delete").performClick()

        assertEquals(1, confirms)
    }

    @Test
    fun cancelDismisses() {
        var dismisses = 0
        render(onDismiss = { dismisses++ })

        composeRule.onNodeWithText("Cancel").performClick()

        assertEquals(1, dismisses)
    }

    @Test
    fun bytesReadAsUnits() {
        assertEquals("512 B", formatBytes(512))
        assertEquals("1.0 kB", formatBytes(1_024))
        assertEquals("1.5 MB", formatBytes(1_500_000))
        assertEquals("2.0 GB", formatBytes(2_000_000_000))
    }

    private fun render(
        bytes: Long = 0,
        onConfirm: () -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        composeRule.setContent {
            SubtracksTheme {
                DeleteDownloadsDialog(name = "Kid A", bytes = bytes, onConfirm = onConfirm, onDismiss = onDismiss)
            }
        }
    }
}
