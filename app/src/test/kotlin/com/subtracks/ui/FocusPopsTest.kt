package com.subtracks.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FocusPopsTest {
    private val library = BackStackKey("library", null)
    private val artistA = BackStackKey("artist/{artistId}?coverArt={coverArt}", "ar1")
    private val albumA = BackStackKey("album/{albumId}?coverArt={coverArt}", "al1")
    private val albumB = BackStackKey("album/{albumId}?coverArt={coverArt}", "al2")

    @Test
    fun anAbsentTargetNavigates() {
        assertNull(focusPops(listOf(library, albumA), "artist/{artistId}?coverArt={coverArt}", "ar1"))
    }

    @Test
    fun anEmptyStackNavigates() {
        assertNull(focusPops(emptyList(), "album/{albumId}?coverArt={coverArt}", "al1"))
    }

    @Test
    fun aTargetAlreadyOnTopNeedsNoPops() {
        assertEquals(0, focusPops(listOf(library, artistA), "artist/{artistId}?coverArt={coverArt}", "ar1"))
    }

    @Test
    fun aTargetUnderTheTopPopsDownToIt() {
        assertEquals(1, focusPops(listOf(library, artistA, albumA), "artist/{artistId}?coverArt={coverArt}", "ar1"))
    }

    @Test
    fun theMatchingEntryIsChosenNotJustTheDestination() {
        val stack = listOf(library, albumA, artistA, albumB)
        assertEquals(2, focusPops(stack, "album/{albumId}?coverArt={coverArt}", "al1"))
        assertEquals(0, focusPops(stack, "album/{albumId}?coverArt={coverArt}", "al2"))
    }
}
