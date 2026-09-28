package com.subtracks.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HeroDownloadTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tappingTheButtonReportsTheActionItsStateOffers() {
        val actions = mutableListOf<BulkDownloadAction>()
        render(ListDownloadStatus(total = 3, downloaded = 1, downloading = 1)) { actions += it }

        composeRule.onNodeWithContentDescription("Cancel download").performClick()

        assertEquals(listOf(BulkDownloadAction.Cancel), actions)
    }

    @Test
    fun aFullyDownloadedListTapsThroughToDelete() {
        val actions = mutableListOf<BulkDownloadAction>()
        render(ListDownloadStatus(total = 3, downloaded = 3)) { actions += it }

        composeRule.onNodeWithContentDescription("Delete download").performClick()

        assertEquals(listOf(BulkDownloadAction.Delete), actions)
    }

    @Test
    fun longPressingAPartiallyDownloadedListAsksToDelete() {
        val actions = mutableListOf<BulkDownloadAction>()
        render(ListDownloadStatus(total = 3, downloaded = 1)) { actions += it }

        composeRule.onNodeWithContentDescription("Download").performTouchInput { longClick() }

        assertEquals(listOf(BulkDownloadAction.Delete), actions)
    }

    @Test
    fun longPressingAnUndownloadedListNeverDeletes() {
        val actions = mutableListOf<BulkDownloadAction>()
        render(ListDownloadStatus(total = 3)) { actions += it }

        composeRule.onNodeWithContentDescription("Download").performTouchInput { longClick() }

        assertTrue("reported $actions", BulkDownloadAction.Delete !in actions)
    }

    @Test
    fun theButtonIsDisabledWithoutSongs() {
        render(ListDownloadStatus(), hasSongs = false) {}

        composeRule.onNodeWithContentDescription("Download").assertIsNotEnabled()
    }

    private fun render(
        status: ListDownloadStatus,
        hasSongs: Boolean = true,
        onAction: (BulkDownloadAction) -> Unit,
    ) {
        composeRule.setContent {
            SubtracksTheme {
                HeroHeader(
                    art = null,
                    name = "Album",
                    subtitle = "Artist",
                    hasSongs = hasSongs,
                    onPlay = {},
                    onShuffle = {},
                    downloadStatus = status,
                    onDownloadAction = onAction,
                    onMore = {},
                    topInset = 0.dp,
                )
            }
        }
    }
}
