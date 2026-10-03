package com.subtracks.data.download

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.SongDownload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DownloadNotificationTest {
    private val resources: Resources = ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun nothingInFlightMeansNoNotification() {
        assertNull(downloadNotification(resources, emptyList()))
        assertNull(downloadNotification(resources, listOf(row("s1", DownloadStatus.Completed), row("s2", DownloadStatus.Failed))))
    }

    @Test
    fun oneRunningDownloadReportsItsShare() {
        val notification = downloadNotification(resources, listOf(row("s1", DownloadStatus.Running, bytes = 50, total = 100)))

        assertEquals("Downloading 1 song", notification?.title)
        assertEquals("50%", notification?.text)
        assertEquals(0.5f, notification?.progress)
    }

    @Test
    fun theProgressIsTheShareOfEveryActiveDownload() {
        val notification =
            downloadNotification(
                resources,
                listOf(
                    row("s1", DownloadStatus.Running, bytes = 50, total = 100),
                    row("s2", DownloadStatus.Queued, bytes = 150, total = 300),
                    row("s3", DownloadStatus.Completed, bytes = 10, total = 10),
                ),
            )

        assertEquals("Downloading 2 songs", notification?.title)
        assertEquals(0.5f, notification?.progress)
    }

    @Test
    fun anUnknownTotalLeavesTheProgressIndeterminate() {
        val notification = downloadNotification(resources, listOf(row("s1", DownloadStatus.Running)))

        assertEquals("In progress", notification?.text)
        assertNull(notification?.progress)
    }

    private fun row(
        songId: String,
        status: DownloadStatus,
        bytes: Long = 0,
        total: Long = 0,
    ) = SongDownload(sourceId = 1, songId = songId, status = status, bytes = bytes, total = total)
}
