package com.subtracks.ui.playback

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.R
import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.Song
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class QueueScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun shuffledContextRowsDoNotExposeRemoval() {
        render(visible = listOf(row(upNext = false)), shuffle = true)

        composeRule.onNodeWithContentDescription("Remove from queue").assertDoesNotExist()
        composeRule.onNodeWithText("Removed from queue").assertDoesNotExist()
    }

    @Test
    fun shuffledUpNextRowsStillRemoveAndShowUndo() {
        var removed: Long? = null
        render(
            visible = listOf(row(upNext = true)),
            shuffle = true,
            onRemove = { position, onComplete ->
                removed = position
                onComplete(true)
            },
        )

        composeRule.onNodeWithContentDescription("Remove from queue").performClick()

        assertEquals(0L, removed)
        composeRule.onNodeWithText("Removed from queue").assertIsDisplayed()
        composeRule.onNodeWithText("Undo").assertIsDisplayed()
    }

    @Test
    fun rejectedRemovalDoesNotShowSuccessOrUndo() {
        render(onRemove = { _, onComplete -> onComplete(false) })

        removeFirstRow()
        composeRule.waitForIdle()

        assertNoSnackbar(R.string.queue_removed)
    }

    @Test
    fun appliedRemovalShowsUndoAndRunsItOnlyFromTheAction() {
        var undos = 0
        render(onRemove = { _, onComplete -> onComplete(true) }, onUndo = { undos++ })

        removeFirstRow()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.queue_removed)).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.undo)).performClick()
        composeRule.runOnIdle { assertEquals(1, undos) }
    }

    private fun render(
        visible: List<QueueRow> = rows,
        shuffle: Boolean = false,
        onRemove: (Long, (Boolean) -> Unit) -> Unit = { _, onComplete -> onComplete(false) },
        onMove: (QueueMove, (Boolean) -> Unit) -> Unit = { _, onComplete -> onComplete(false) },
        onUndo: () -> Unit = {},
    ) {
        val visibleRows = mutableStateListOf<QueueRow>().also { it.addAll(visible) }
        composeRule.setContent {
            SubtracksTheme {
                QueueScreen(
                    rows = visibleRows,
                    ready = true,
                    currentPosition = null,
                    currentSongId = null,
                    shuffle = shuffle,
                    coverArt = { _, _ -> null },
                    onBack = {},
                    onPlay = {},
                    onRemove = onRemove,
                    onReorder = { from, to -> visibleRows.add(to, visibleRows.removeAt(from)) },
                    onMove = onMove,
                    onCancelMove = {},
                    onLoadOlder = {},
                    onLoadNewer = {},
                    onUndo = onUndo,
                )
            }
        }
    }

    private fun removeFirstRow() {
        composeRule.onAllNodesWithContentDescription(composeRule.activity.getString(R.string.queue_remove))[0].performClick()
    }

    private fun assertNoSnackbar(message: Int) {
        composeRule.onNodeWithText(composeRule.activity.getString(message)).assertDoesNotExist()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.undo)).assertDoesNotExist()
    }

    private fun row(upNext: Boolean) =
        QueueRow(
            id = 0,
            position = 0,
            song = song("s1", 1),
            upNext = upNext,
        )

    private val rows =
        (0..2).map { index ->
            QueueRow(
                id = index.toLong(),
                position = index.toLong(),
                song = song("s$index", index.toLong()),
            )
        }

    private fun song(
        id: String,
        track: Long,
    ) = AlbumSongItem(
        Song(
            sourceId = 1,
            id = id,
            albumId = "al1",
            artistId = "ar1",
            title = "Song $id",
            album = "Album",
            artist = "Artist",
            duration = 100,
            track = track,
            disc = 1,
            starred = null,
            genre = null,
        ),
        coverArt = null,
    )
}
