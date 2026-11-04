package com.subtracks.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.DownloadList
import com.subtracks.ui.theme.SubtracksTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        composeRule.onNodeWithText("Deleting will free", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("MB", substring = true).assertIsDisplayed()
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
        render(bytes = 512)

        composeRule.onNodeWithText("512 B", substring = true).assertIsDisplayed()
    }

    @Test
    fun sharedWorkflowLooksUpBytesAndConfirmsTheRequestedTargetOnce() {
        val actions = mutableListOf<Triple<DownloadList, String, BulkDownloadAction>>()
        lateinit var controller: PendingDownloadDeleteController
        renderWorkflow(
            downloadedBytes = { list, refId ->
                assertEquals(DownloadList.Album, list)
                assertEquals("al1", refId)
                1_500_000
            },
            onAction = { list, refId, action -> actions += Triple(list, refId, action) },
            onController = { controller = it },
        )

        composeRule.runOnIdle {
            controller.onAction("Kid A", DownloadList.Album, "al1", BulkDownloadAction.Delete) { _, _, _ -> }
        }
        composeRule.waitUntil { controller.pending != null }
        composeRule.onNodeWithText("Kid A", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("MB", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Delete").performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(Triple(DownloadList.Album, "al1", BulkDownloadAction.Delete)), actions)
            assertNull(controller.pending)
        }
    }

    @Test
    fun sharedWorkflowCancelDoesNotConfirm() {
        val actions = mutableListOf<BulkDownloadAction>()
        lateinit var controller: PendingDownloadDeleteController
        renderWorkflow(
            onAction = { _, _, action -> actions += action },
            onController = { controller = it },
        )

        composeRule.runOnIdle {
            controller.onAction("Kid A", DownloadList.Album, "al1", BulkDownloadAction.Delete) { _, _, _ -> }
        }
        composeRule.waitUntil { controller.pending != null }
        composeRule.onNodeWithText("Cancel").performClick()

        composeRule.runOnIdle {
            assertEquals(emptyList<BulkDownloadAction>(), actions)
            assertNull(controller.pending)
        }
    }

    @Test
    fun sharedWorkflowDismissesWithTheContextMenu() {
        val dismissals = MutableSharedFlow<Unit>(replay = 1)
        lateinit var controller: PendingDownloadDeleteController
        renderWorkflow(dismissals = dismissals, onController = { controller = it })

        composeRule.runOnIdle {
            controller.onAction("Kid A", DownloadList.Album, "al1", BulkDownloadAction.Delete) { _, _, _ -> }
        }
        composeRule.waitUntil { controller.pending != null }
        composeRule.runOnIdle { dismissals.tryEmit(Unit) }

        composeRule.waitUntil { controller.pending == null }
        composeRule.onNodeWithText("Delete").assertDoesNotExist()
    }

    @Test
    fun sharedWorkflowDispatchesNonDeleteActionsImmediately() {
        val actions = mutableListOf<Triple<DownloadList, String, BulkDownloadAction>>()
        var lookups = 0
        lateinit var controller: PendingDownloadDeleteController
        renderWorkflow(
            downloadedBytes = { _, _ ->
                lookups++
                0
            },
            onAction = { list, refId, action -> actions += Triple(list, refId, action) },
            onController = { controller = it },
        )

        composeRule.runOnIdle {
            controller.onAction("Kid A", DownloadList.Album, "al1", BulkDownloadAction.Download) { list, refId, action ->
                actions += Triple(list, refId, action)
            }
        }

        composeRule.runOnIdle {
            assertEquals(listOf(Triple(DownloadList.Album, "al1", BulkDownloadAction.Download)), actions)
            assertEquals(0, lookups)
            assertNull(controller.pending)
        }
    }

    private fun renderWorkflow(
        dismissals: kotlinx.coroutines.flow.Flow<Unit> = emptyFlow(),
        downloadedBytes: suspend (DownloadList, String) -> Long = { _, _ -> 0 },
        onAction: (DownloadList, String, BulkDownloadAction) -> Unit = { _, _, _ -> },
        onController: (PendingDownloadDeleteController) -> Unit,
    ) {
        composeRule.setContent {
            SubtracksTheme {
                val controller = rememberPendingDownloadDelete(dismissals, downloadedBytes)
                onController(controller)
                PendingDownloadDeleteConfirmation(controller) { pending ->
                    onAction(pending.list, pending.refId, BulkDownloadAction.Delete)
                }
            }
        }
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
