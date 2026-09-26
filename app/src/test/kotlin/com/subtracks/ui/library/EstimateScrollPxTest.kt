package com.subtracks.ui.library

import org.junit.Assert.assertEquals
import org.junit.Test

class EstimateScrollPxTest {
    private val image = 432f
    private val album = 200f
    private val gap = 16f

    @Test
    fun theHeroUsesTheRawOffset() {
        assertEquals(50f, estimateScrollPx(0, 50f, image, album, gap), 0.01f)
    }

    @Test
    fun theFirstAlbumRowAddsTheHeroAndOneGap() {
        assertEquals(image + gap + 10f, estimateScrollPx(1, 10f, image, album, gap), 0.01f)
    }

    @Test
    fun theSecondCellOfARowDoesNotAdvance() {
        assertEquals(estimateScrollPx(1, 10f, image, album, gap), estimateScrollPx(2, 10f, image, album, gap), 0.01f)
    }

    @Test
    fun theNextRowAddsACellAndAGap() {
        assertEquals(image + gap + (album + gap) + 10f, estimateScrollPx(3, 10f, image, album, gap), 0.01f)
    }
}
