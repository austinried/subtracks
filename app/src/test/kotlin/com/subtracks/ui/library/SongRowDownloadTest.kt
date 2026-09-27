package com.subtracks.ui.library

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.ui.theme.SubtracksTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SongRowDownloadTest {
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
    fun aCompletedDownloadShowsTheDownloadedIndicator() {
        render(download(DownloadStatus.Completed))

        composeRule.onNodeWithContentDescription("Downloaded").assertIsDisplayed()
    }

    @Test
    fun aRunningDownloadShowsProgressRatherThanTheDownloadedIndicator() {
        render(download(DownloadStatus.Running))

        composeRule.onNodeWithContentDescription("Downloaded").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Downloading").assertIsDisplayed()
    }

    @Test
    fun aSongWithoutADownloadShowsNoIndicator() {
        render(null)

        composeRule.onNodeWithText("Song").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Downloaded").assertDoesNotExist()
    }

    private fun download(status: DownloadStatus) =
        SongDownload(sourceId = 1, songId = "s1", status = status, engineId = 7, bytes = 40, total = 100)

    private fun render(download: SongDownload?) {
        composeRule.setContent {
            SubtracksTheme {
                SongRow(song = song, download = download)
            }
        }
    }
}
