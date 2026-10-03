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
    fun anInProgressNodeShowsCancelAndACompletedNodeShowsDelete() {
        var cancelled: List<String>? = null
        render(
            tree =
                DownloadTree(
                    artists =
                        listOf(
                            artist("ar1", "Active", listOf(active)),
                            artist("ar2", "Done", listOf(done)),
                        ),
                ),
            onCancel = { cancelled = it },
        )

        composeRule.onAllNodesWithContentDescription("Cancel download", substring = true).assertCountEquals(1)
        composeRule.onAllNodesWithContentDescription("Delete downloads", substring = true).assertCountEquals(1)

        composeRule.onNodeWithContentDescription("Cancel download of Active").performClick()
        assertEquals(listOf("s1"), cancelled)
    }

    @Test
    fun songsUnderAnInProgressNodeAlsoShowCancel() {
        render(tree = DownloadTree(artists = listOf(artist("ar1", "Artist", listOf(active, done)))), onCancel = {})

        expandArtistAndAlbum()

        composeRule.onAllNodesWithContentDescription("Cancel download", substring = true).assertCountEquals(3)
        composeRule.onAllNodesWithContentDescription("Delete download of Done").assertCountEquals(1)
    }

    private fun artist(
        id: String,
        name: String,
        songs: List<DownloadedSong>,
    ) = DownloadArtistNode(
        id = id,
        name = name,
        bytes = 0,
        albums = listOf(DownloadAlbumNode(id = "al-$id", name = "Album", bytes = 0, songs = songs)),
    )

    private fun render(
        tree: DownloadTree,
        onCancel: (List<String>) -> Unit,
    ) {
        composeRule.setContent {
            SubtracksTheme {
                DownloadsScreen(
                    tree = tree,
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
