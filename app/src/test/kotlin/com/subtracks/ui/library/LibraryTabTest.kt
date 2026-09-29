package com.subtracks.ui.library

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryTabTest {
    @Test
    fun theStoredNameMapsToItsTabAndAnythingElseToAlbums() {
        assertEquals(LibraryTab.Playlists, libraryTabFor("Playlists"))
        assertEquals(LibraryTab.Albums, libraryTabFor(null))
        assertEquals(LibraryTab.Albums, libraryTabFor("Books"))
    }
}
