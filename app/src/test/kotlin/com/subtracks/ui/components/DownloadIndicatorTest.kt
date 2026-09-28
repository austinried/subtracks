package com.subtracks.ui.components

import com.subtracks.data.model.ListDownloadStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadIndicatorTest {
    @Test
    fun anUntouchedListShowsNothing() {
        assertEquals(DownloadIndicator.Hidden, ListDownloadStatus().indicator())
    }

    @Test
    fun aListWithNothingStartedButNoSongsShowsNothing() {
        assertEquals(DownloadIndicator.Hidden, ListDownloadStatus(total = 4).indicator())
    }

    @Test
    fun aQueuedButNotYetDownloadedListShowsProgress() {
        assertEquals(DownloadIndicator.InProgress, ListDownloadStatus(total = 4, downloading = 4).indicator())
    }

    @Test
    fun aPartlyDownloadedListShowsProgress() {
        assertEquals(DownloadIndicator.InProgress, ListDownloadStatus(total = 4, downloaded = 2).indicator())
    }

    @Test
    fun aFullyDownloadedListShowsDone() {
        assertEquals(DownloadIndicator.Complete, ListDownloadStatus(total = 4, downloaded = 4).indicator())
    }
}
