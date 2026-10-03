package com.subtracks.ui.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeRevealTest {
    @Test
    fun noPreviousFrontDoesNotReveal() {
        assertFalse(shouldRevealNewFront(previousKey = null, firstKey = "a", firstVisibleItemIndex = 0))
    }

    @Test
    fun noFrontItemDoesNotReveal() {
        assertFalse(shouldRevealNewFront(previousKey = "a", firstKey = null, firstVisibleItemIndex = 0))
    }

    @Test
    fun anUnchangedFrontDoesNotReveal() {
        assertFalse(shouldRevealNewFront(previousKey = "a", firstKey = "a", firstVisibleItemIndex = 0))
    }

    @Test
    fun aChangedFrontRevealsAtTheStart() {
        assertTrue(shouldRevealNewFront(previousKey = "a", firstKey = "b", firstVisibleItemIndex = 0))
    }

    @Test
    fun aChangedFrontRevealsOneRowIn() {
        assertTrue(shouldRevealNewFront(previousKey = "a", firstKey = "b", firstVisibleItemIndex = 1))
    }

    @Test
    fun aChangedFrontDoesNotRevealWhenScrolledAway() {
        assertFalse(shouldRevealNewFront(previousKey = "a", firstKey = "b", firstVisibleItemIndex = 2))
    }
}
