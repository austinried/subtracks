package com.subtracks.ui.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeRevealTest {
    @Test
    fun noPreviousFrontDoesNotReveal() {
        assertFalse(shouldRevealNewFront(previousKey = null, firstKey = "a", insertedCount = 1, firstVisibleItemIndex = 1))
    }

    @Test
    fun noFrontItemDoesNotReveal() {
        assertFalse(shouldRevealNewFront(previousKey = "a", firstKey = null, insertedCount = 1, firstVisibleItemIndex = 1))
    }

    @Test
    fun anUnchangedFrontDoesNotReveal() {
        assertFalse(shouldRevealNewFront(previousKey = "a", firstKey = "a", insertedCount = 0, firstVisibleItemIndex = 0))
    }

    @Test
    fun aMissingPreviousFrontDoesNotReveal() {
        assertFalse(shouldRevealNewFront(previousKey = "a", firstKey = "b", insertedCount = -1, firstVisibleItemIndex = 0))
    }

    @Test
    fun aChangedFrontRevealsWhenTheRowWasAtTheStart() {
        assertTrue(shouldRevealNewFront(previousKey = "a", firstKey = "b", insertedCount = 1, firstVisibleItemIndex = 1))
    }

    @Test
    fun aChangedFrontRevealsWhenTheRowWasOneIn() {
        assertTrue(shouldRevealNewFront(previousKey = "a", firstKey = "b", insertedCount = 1, firstVisibleItemIndex = 2))
    }

    @Test
    fun aChangedFrontRevealsWhenSeveralItemsArePrepended() {
        assertTrue(shouldRevealNewFront(previousKey = "a", firstKey = "c", insertedCount = 2, firstVisibleItemIndex = 2))
    }

    @Test
    fun aChangedFrontDoesNotRevealWhenScrolledAway() {
        assertFalse(shouldRevealNewFront(previousKey = "a", firstKey = "b", insertedCount = 1, firstVisibleItemIndex = 3))
    }
}
