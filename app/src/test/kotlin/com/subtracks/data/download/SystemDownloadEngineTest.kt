package com.subtracks.data.download

import android.app.DownloadManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SystemDownloadEngineTest {
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
}
