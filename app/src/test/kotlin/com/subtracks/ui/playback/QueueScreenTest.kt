package com.subtracks.ui.playback

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        render(row(upNext = false), shuffle = true)

        composeRule.onNodeWithContentDescription("Remove from queue").assertDoesNotExist()
        composeRule.onNodeWithText("Removed from queue").assertDoesNotExist()
    }

    @Test
    fun shuffledUpNextRowsStillRemoveAndShowUndo() {
        var removed: Long? = null
        render(row(upNext = true), shuffle = true, onRemove = { removed = it })

        composeRule.onNodeWithContentDescription("Remove from queue").performClick()

        assertEquals(0L, removed)
        composeRule.onNodeWithText("Removed from queue").assertIsDisplayed()
        composeRule.onNodeWithText("Undo").assertIsDisplayed()
    }

    private fun render(
        row: QueueRow,
        shuffle: Boolean,
        onRemove: (Long) -> Unit = {},
    ) {
        composeRule.setContent {
            SubtracksTheme {
                QueueScreen(
                    rows = listOf(row),
                    ready = true,
                    currentPosition = null,
                    currentSongId = null,
                    shuffle = shuffle,
                    coverArt = { _, _ -> null },
                    onBack = {},
                    onPlay = {},
                    onRemove = onRemove,
                    onReorder = { _, _ -> },
                    onMove = { _, _ -> false },
                    onLoadOlder = {},
                    onLoadNewer = {},
                    onUndo = {},
                )
            }
        }
    }

    private fun row(upNext: Boolean) =
        QueueRow(
            id = 0,
            position = 0,
            song =
                AlbumSongItem(
                    song =
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
                        ),
                    coverArt = null,
                ),
            upNext = upNext,
        )
}
