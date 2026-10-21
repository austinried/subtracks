package com.subtracks.ui.components

import com.subtracks.data.model.ListDownloadStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadIndicatorTest {
    @Test
    fun anUntouchedListIsNotDownloading() {
        assertFalse(ListDownloadStatus().isDownloading())
    }

    @Test
    fun aPopulatedButUnstartedListIsNotDownloading() {
        assertFalse(ListDownloadStatus(total = 4).isDownloading())
    }

    @Test
    fun aQueuedButNotYetDownloadedListIsDownloading() {
        assertTrue(ListDownloadStatus(total = 4, downloading = 4).isDownloading())
    }

    @Test
    fun aPartlyDownloadedIdleListIsNotDownloading() {
        assertFalse(ListDownloadStatus(total = 4, downloaded = 2).isDownloading())
    }

    @Test
    fun aPartlyDownloadedListWithSomethingInFlightIsDownloading() {
        assertTrue(ListDownloadStatus(total = 4, downloaded = 2, downloading = 1).isDownloading())
    }

    @Test
    fun aFullyDownloadedListIsNotDownloading() {
        assertFalse(ListDownloadStatus(total = 4, downloaded = 4).isDownloading())
    }
}
