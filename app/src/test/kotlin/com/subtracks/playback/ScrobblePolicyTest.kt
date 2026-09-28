package com.subtracks.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScrobblePolicyTest {
    private var clock = 1_000L
    private val policy = ScrobblePolicy(now = { clock })

    private fun track(
        id: String = "s1",
        durationMs: Long = 200_000,
    ) = QueueItem(id = id, title = "Title", artist = "Artist", album = "Album", coverArtId = null, durationMs = durationMs)

    private fun play(
        item: QueueItem?,
        toPositionMs: Long,
        stepMs: Long = 500,
        playing: Boolean = true,
    ): List<Scrobble> {
        val events = mutableListOf<Scrobble>()
        var position = 0L
        while (position <= toPositionMs) {
            policy.advance(item, playing, position)?.let(events::add)
            position += stepMs
        }
        return events
    }

    @Test
    fun sendsNowPlayingWhenPlaybackStarts() {
        val events = play(track(), toPositionMs = 0)
        assertEquals(listOf<Scrobble>(Scrobble.NowPlaying("s1")), events)
    }

    @Test
    fun waitsForPlaybackBeforeNowPlaying() {
        val item = track()
        assertNull(policy.advance(item, isPlaying = false, positionMs = 0))
        clock = 5_000L
        assertEquals(Scrobble.NowPlaying("s1"), policy.advance(item, isPlaying = true, positionMs = 0))
    }

    @Test
    fun submitsAfterHalfTheDuration() {
        val events = play(track(durationMs = 200_000), toPositionMs = 100_000)
        assertEquals(Scrobble.NowPlaying("s1"), events.first())
        assertEquals(1, events.filterIsInstance<Scrobble.Submission>().size)
        assertEquals(Scrobble.Submission("s1", time = 1_000L), events.last())
    }

    @Test
    fun doesNotSubmitBeforeHalfTheDuration() {
        val events = play(track(durationMs = 200_000), toPositionMs = 99_500)
        assertEquals(listOf<Scrobble>(Scrobble.NowPlaying("s1")), events)
    }

    @Test
    fun submitsAfterFourMinutesForLongTracks() {
        val events = play(track(durationMs = 10 * 60_000), toPositionMs = 4 * 60_000)
        assertEquals(Scrobble.Submission("s1", time = 1_000L), events.last())
    }

    @Test
    fun doesNotSubmitShortTracks() {
        val events = play(track(durationMs = 20_000), toPositionMs = 20_000)
        assertEquals(listOf<Scrobble>(Scrobble.NowPlaying("s1")), events)
    }

    @Test
    fun submitsOnlyOncePerTrack() {
        val events = play(track(durationMs = 200_000), toPositionMs = 200_000)
        assertEquals(1, events.filterIsInstance<Scrobble.Submission>().size)
    }

    @Test
    fun seekingAheadDoesNotCountAsPlayback() {
        val item = track(durationMs = 200_000)
        assertEquals(Scrobble.NowPlaying("s1"), policy.advance(item, isPlaying = true, positionMs = 0))
        assertNull(policy.advance(item, isPlaying = true, positionMs = 180_000))
        assertNull(policy.advance(item, isPlaying = true, positionMs = 180_500))
    }

    @Test
    fun pausedTimeIsNotCounted() {
        val item = track(durationMs = 200_000)
        assertEquals(Scrobble.NowPlaying("s1"), policy.advance(item, isPlaying = true, positionMs = 0))
        assertNull(policy.advance(item, isPlaying = false, positionMs = 120_000))
        assertNull(policy.advance(item, isPlaying = true, positionMs = 120_500))
    }

    @Test
    fun newTrackResetsAndScrobblesAgain() {
        val first = play(track(id = "s1"), toPositionMs = 100_000)
        assertEquals(Scrobble.Submission("s1", 1_000L), first.last())
        clock = 60_000L
        val second = play(track(id = "s2"), toPositionMs = 100_000)
        assertEquals(Scrobble.NowPlaying("s2"), second.first())
        assertEquals(Scrobble.Submission("s2", 60_000L), second.last())
    }

    @Test
    fun restartingTheSameTrackScrobblesAgain() {
        val item = track(durationMs = 200_000)
        play(item, toPositionMs = 100_000)
        clock = 120_000L
        val repeat = play(item, toPositionMs = 100_000)
        assertEquals(Scrobble.NowPlaying("s1"), repeat.first())
        assertEquals(Scrobble.Submission("s1", 120_000L), repeat.last())
    }
}
