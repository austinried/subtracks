package com.subtracks.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NowPlayingRequestTest {
    @Test
    fun aNotificationWithNothingPlayingDoesNotOpen() {
        assertFalse(shouldOpenNowPlaying(request = 1, handled = 0, playerVisible = false))
    }

    @Test
    fun aNotificationWithSomethingPlayingOpens() {
        assertTrue(shouldOpenNowPlaying(request = 1, handled = 0, playerVisible = true))
    }

    @Test
    fun anAlreadyHandledRequestDoesNotReopen() {
        assertFalse(shouldOpenNowPlaying(request = 1, handled = 1, playerVisible = true))
    }

    @Test
    fun aNewerRequestOpensAgain() {
        assertTrue(shouldOpenNowPlaying(request = 2, handled = 1, playerVisible = true))
    }

    @Test
    fun anOldRequestWaitsUntilSomethingIsPlaying() {
        assertFalse(shouldOpenNowPlaying(request = 1, handled = 0, playerVisible = false))
        assertTrue(shouldOpenNowPlaying(request = 1, handled = 0, playerVisible = true))
    }
}
