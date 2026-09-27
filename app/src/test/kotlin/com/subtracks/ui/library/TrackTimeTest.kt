package com.subtracks.ui.library

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackTimeTest {
    @Test
    fun minutesAreNotPaddedButSecondsAre() {
        assertEquals("4:11", formatTrackTime(251))
        assertEquals("12:05", formatTrackTime(725))
    }

    @Test
    fun underAMinuteKeepsASingleLeadingZero() {
        assertEquals("0:05", formatTrackTime(5))
    }

    @Test
    fun anHourOrLongerUsesTheHourWithoutPadding() {
        assertEquals("1:02:03", formatTrackTime(3_723))
    }

    @Test
    fun negativeDurationsClampToZero() {
        assertEquals("0:00", formatTrackTime(-5))
    }
}
