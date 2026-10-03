package com.subtracks.ui.downloads

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.DownloadedSong
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DownloadsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val active =
        DownloadedSong(
            songId = "s1",
            sourceId = 1,
            title = "Active",
            albumId = "al1",
            albumName = "Album",
            artistId = "ar1",
            artistName = "Artist",
            status = DownloadStatus.Running,
            size = 40,
            bytes = 40,
            total = 100,
        )
    private val done = active.copy(songId = "s2", title = "Done", status = DownloadStatus.Completed)

    @Test
    fun anInProgressDownloadShowsCancelAndACompletedOneShowsDelete() {
        var cancelled: List<String>? = null
        render(onCancel = { cancelled = it })

        expandArtistAndAlbum()

        composeRule.onAllNodesWithContentDescription("Cancel download").assertCountEquals(1)
        composeRule.onAllNodesWithContentDescription("Delete download").assertCountEquals(1)

        composeRule.onNodeWithContentDescription("Cancel download").performClick()
        assertEquals(listOf("s1"), cancelled)
    }

    private fun render(onCancel: (List<String>) -> Unit) {
        composeRule.setContent {
            SubtracksTheme {
                DownloadsScreen(
                    tree =
                        DownloadTree(
                            artists =
                                listOf(
                                    DownloadArtistNode(
                                        id = "ar1",
                                        name = "Artist",
                                        bytes = 0,
                                        albums =
                                            listOf(
                                                DownloadAlbumNode(
                                                    id = "al1",
                                                    name = "Album",
                                                    bytes = 0,
                                                    songs = listOf(active, done),
                                                ),
                                            ),
                                    ),
                                ),
                        ),
                    loadEncoding = { null },
                    onBack = {},
                    onDelete = {},
                    onCancel = onCancel,
                )
            }
        }
    }

    private fun expandArtistAndAlbum() {
        composeRule.onAllNodesWithContentDescription("Expand")[0].performClick()
        composeRule.onAllNodesWithContentDescription("Expand")[0].performClick()
    }
}
