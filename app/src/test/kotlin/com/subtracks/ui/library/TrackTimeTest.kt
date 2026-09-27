package com.subtracks.ui.library

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackTimeTest {
    @Test
    fun minutesAndSecondsAreZeroPadded() {
        assertEquals("04:11", formatTrackTime(251))
    }

    @Test
    fun anHourOrLongerUsesTheHourWithoutPadding() {
        assertEquals("1:02:03", formatTrackTime(3_723))
    }

    @Test
    fun negativeDurationsClampToZero() {
        assertEquals("00:00", formatTrackTime(-5))
    }
}
