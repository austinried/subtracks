package com.subtracks.data.download

import android.app.DownloadManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class SystemDownloadEngineTest {
    private lateinit var engine: SystemDownloadEngine
    private lateinit var downloadManager: DownloadManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        engine = SystemDownloadEngine(context)
        downloadManager = context.getSystemService(DownloadManager::class.java)
    }

    @Test
    fun successfulRunningAndFailedStatusesAreMapped() {
        assertEquals(EngineStatus.Completed, engineStatus(DownloadManager.STATUS_SUCCESSFUL))
        assertEquals(EngineStatus.Running, engineStatus(DownloadManager.STATUS_RUNNING))
        assertEquals(EngineStatus.Failed, engineStatus(DownloadManager.STATUS_FAILED))
    }

    @Test
    fun aWaitingDownloadIsPending() {
        assertEquals(EngineStatus.Pending, engineStatus(DownloadManager.STATUS_PENDING))
        assertEquals(EngineStatus.Pending, engineStatus(DownloadManager.STATUS_PAUSED))
        assertEquals(EngineStatus.Pending, engineStatus(Int.MIN_VALUE))
    }

    @Test
    fun failureReasonsReadAsSentences() {
        assertEquals("Not enough space to download", failureMessage(DownloadManager.ERROR_INSUFFICIENT_SPACE))
        assertEquals("Storage is unavailable", failureMessage(DownloadManager.ERROR_DEVICE_NOT_FOUND))
        assertEquals("Download failed", failureMessage(DownloadManager.ERROR_UNKNOWN))
    }

    @Test
    fun enqueuedRequestIsStoredAndTargetsTheSourceAndSongDirectory() {
        val id =
            engine.enqueue(
                EngineRequest(
                    uri = "https://server.example/rest/stream?id=song-9",
                    path = "downloads/7/song-9",
                    title = "A Song",
                ),
            )

        val downloads = shadowOf(downloadManager)
        assertEquals(1, downloads.requestCount)

        val request = shadowOf(downloads.getRequest(id))
        assertEquals(id, request.id)
        assertEquals("https://server.example/rest/stream?id=song-9", request.uri.toString())
        assertEquals("A Song", request.title.toString())

        val destination = request.destination
        assertNotNull(destination)
        val path = destination!!.path!!
        assertTrue("destination $path is not under external files", path.contains("/external-files/"))
        assertTrue(
            "destination $path is not under the source/song downloads directory",
            path.endsWith("/Music/downloads/7/song-9"),
        )
    }

    @Test
    fun meteredPreferenceIsAppliedForBothValues() {
        val allowed =
            engine.enqueue(
                EngineRequest(
                    uri = "https://server.example/a",
                    path = "downloads/1/a",
                    title = "a",
                    allowMetered = true,
                ),
            )
        val blocked =
            engine.enqueue(
                EngineRequest(
                    uri = "https://server.example/b",
                    path = "downloads/1/b",
                    title = "b",
                    allowMetered = false,
                ),
            )

        val downloads = shadowOf(downloadManager)
        assertEquals(2, downloads.requestCount)

        val allowedRequest = shadowOf(downloads.getRequest(allowed))
        assertTrue(allowedRequest.allowedOverMetered)
        assertTrue(allowedRequest.allowedOverRoaming)

        val blockedRequest = shadowOf(downloads.getRequest(blocked))
        assertFalse(blockedRequest.allowedOverMetered)
        assertFalse(blockedRequest.allowedOverRoaming)
    }
}
