package com.subtracks.ui.library

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.prefs.ListQuery
import com.subtracks.data.prefs.PlaylistSort
import org.junit.Assert.assertEquals
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
}
