package com.subtracks.data.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Id3v1GenresTest {
    @Test
    fun knownIndicesMapToTheirNames() {
        assertEquals("Blues", id3v1Genre("0"))
        assertEquals("Rock", id3v1Genre("17"))
        assertEquals("Electronic", id3v1Genre("52"))
        assertEquals("Worldbeat", id3v1Genre("133"))
        assertEquals("Abstract", id3v1Genre("148"))
        assertEquals("Psybient", id3v1Genre("191"))
    }

    @Test
    fun parenthesisedAndPaddedIndicesMap() {
        assertEquals("Rock", id3v1Genre("(17)"))
        assertEquals("Rock", id3v1Genre(" 17 "))
    }

    @Test
    fun unknownValuesAreLeftUnmapped() {
        assertNull(id3v1Genre("192"))
        assertNull(id3v1Genre("-1"))
        assertNull(id3v1Genre("Rock"))
        assertNull(id3v1Genre("17.0"))
    }
}
