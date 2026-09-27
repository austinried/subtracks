package com.subtracks.ui.components

import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.ListDownloadStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class BulkDownloadActionTest {
    @Test
    fun anUndownloadedListOffersDownload() {
        assertEquals(BulkDownloadAction.Download, ListDownloadStatus(total = 3).action())
        assertEquals(BulkDownloadAction.Download, ListDownloadStatus().action())
    }

    @Test
    fun aPartiallyDownloadedListStillOffersDownload() {
        assertEquals(BulkDownloadAction.Download, ListDownloadStatus(total = 3, downloaded = 1).action())
    }

    @Test
    fun aListWithDownloadsInFlightOffersCancel() {
        assertEquals(BulkDownloadAction.Cancel, ListDownloadStatus(total = 3, downloaded = 1, downloading = 1).action())
    }

    @Test
    fun aFullyDownloadedListOffersDelete() {
        assertEquals(BulkDownloadAction.Delete, ListDownloadStatus(total = 3, downloaded = 3).action())
    }
}
