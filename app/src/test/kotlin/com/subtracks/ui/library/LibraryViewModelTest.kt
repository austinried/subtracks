package com.subtracks.ui.library

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.DownloadProgress
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.SongDownload
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.prefs.ListQuery
import com.subtracks.data.prefs.PlaylistSort
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryViewModelTest {
    @Test
    fun aKnownSortStringDecodesToItsEnum() {
        assertEquals(AlbumSort.Year, ListQuery("Year").albumSort())
        assertEquals(ArtistSort.AlbumCount, ListQuery("AlbumCount").artistSort())
        assertEquals(PlaylistSort.Updated, ListQuery("Updated").playlistSort())
    }

    @Test
    fun anUnknownSortStringFallsBackToName() {
        assertEquals(AlbumSort.Name, ListQuery("LegacyAlbumOrder").albumSort())
        assertEquals(ArtistSort.Name, ListQuery("LegacyArtistOrder").artistSort())
        assertEquals(PlaylistSort.Name, ListQuery("LegacyPlaylistOrder").playlistSort())
    }

    @Test
    fun downloadProgressOnlyCountsTheActiveSource() {
        val rows = listOf(download(1, bytes = 40, total = 100), download(2, bytes = 10, total = 20))

        assertEquals(DownloadProgress(bytes = 40, total = 100), downloadProgress(rows, sourceId = 1))
    }

    @Test
    fun anotherSourcesDownloadsDoNotShowAProgressRing() {
        assertNull(downloadProgress(listOf(download(2, bytes = 10, total = 20)), sourceId = 1))
    }

    @Test
    fun aTransferWithoutAKnownSizeIsIndeterminate() {
        val rows =
            listOf(
                download(1, bytes = 100, total = 100),
                download(1, bytes = 20, total = 0).copy(songId = "s2"),
            )

        assertEquals(DownloadProgress(bytes = 0, total = 0), downloadProgress(rows, sourceId = 1))
    }

    private fun download(
        sourceId: Long,
        bytes: Long,
        total: Long,
    ) = SongDownload(
        sourceId = sourceId,
        songId = "s1",
        status = DownloadStatus.Running,
        bytes = bytes,
        total = total,
    )
}
