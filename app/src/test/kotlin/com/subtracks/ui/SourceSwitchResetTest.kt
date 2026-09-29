package com.subtracks.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceSwitchResetTest {
    private var nowPlayingClosed = false
    private var queueClosed = false
    private var detailPopped = false

    private fun reset(
        previous: Long?,
        current: Long?,
        route: String?,
    ) {
        nowPlayingClosed = false
        queueClosed = false
        detailPopped = false
        resetOnSourceSwitch(
            previousSourceId = previous,
            currentSourceId = current,
            route = route,
            closeNowPlaying = { nowPlayingClosed = true },
            closeQueue = { queueClosed = true },
            popDetail = { detailPopped = true },
        )
    }

    @Test
    fun theFirstSourceResetsNothing() {
        reset(previous = null, current = 1L, route = "album/{albumId}?coverArt={coverArt}")

        assertFalse(nowPlayingClosed)
        assertFalse(queueClosed)
        assertFalse(detailPopped)
    }

    @Test
    fun reselectingTheSameSourceResetsNothing() {
        reset(previous = 1L, current = 1L, route = "settings")

        assertFalse(nowPlayingClosed)
        assertFalse(queueClosed)
        assertFalse(detailPopped)
    }

    @Test
    fun losingTheSourceResetsNothing() {
        reset(previous = 1L, current = null, route = "settings")

        assertFalse(nowPlayingClosed)
        assertFalse(queueClosed)
        assertFalse(detailPopped)
    }

    @Test
    fun aSwitchClosesTheOverlays() {
        reset(previous = 1L, current = 2L, route = "settings")

        assertTrue(nowPlayingClosed)
        assertTrue(queueClosed)
        assertFalse(detailPopped)
    }

    @Test
    fun aSwitchOnADetailPopsIt() {
        reset(previous = 1L, current = 2L, route = "album/{albumId}?coverArt={coverArt}")

        assertTrue(nowPlayingClosed)
        assertTrue(queueClosed)
        assertTrue(detailPopped)
    }

    @Test
    fun everyDetailRoutePopsOnASwitch() {
        listOf(
            "album/{albumId}?coverArt={coverArt}",
            "artist/{artistId}?coverArt={coverArt}",
            "playlist/{playlistId}",
        ).forEach { route ->
            reset(previous = 1L, current = 2L, route = route)
            assertTrue("$route should be dismissed", detailPopped)
        }
    }
}
