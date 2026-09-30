package com.subtracks.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoverArtInitialTest {
    @Test
    fun ideographicInitialsAreDetected() {
        assertTrue("東京".isIdeographicInitial())
        assertTrue("あ".isIdeographicInitial())
        assertTrue("カ".isIdeographicInitial())
        assertTrue("한".isIdeographicInitial())
        assertTrue("𠮟".isIdeographicInitial())
    }

    @Test
    fun latinInitialsAndEmptyNamesAreNotIdeographic() {
        assertFalse("Abbey Road".isIdeographicInitial())
        assertFalse("".isIdeographicInitial())
    }
}
