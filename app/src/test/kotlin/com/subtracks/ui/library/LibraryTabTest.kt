package com.subtracks.ui.library

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryTabTest {
    @Test
    fun theStoredNameMapsToItsTabAndAnythingElseToHome() {
        assertEquals(LibraryTab.Playlists, libraryTabFor("Playlists"))
        assertEquals(LibraryTab.Home, libraryTabFor(null))
        assertEquals(LibraryTab.Home, libraryTabFor("Books"))
    }
}
