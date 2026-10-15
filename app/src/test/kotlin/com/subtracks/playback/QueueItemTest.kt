package com.subtracks.playback

import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class QueueItemTest {
    @Test
    fun carriesTheSongsReplayGain() {
        val song =
            Song(
                sourceId = 1,
                id = "s1",
                albumId = "al1",
                artistId = "ar1",
                title = "Song",
                album = "Album",
                artist = "Artist",
                duration = 100,
                track = 1,
                disc = 1,
                starred = null,
                trackGain = -3.2f,
                albumGain = -2.1f,
                trackPeak = 0.98f,
                albumPeak = 0.99f,
            )

        val item = AlbumSongItem(song, coverArt = "al1").toQueueItem()

        assertEquals(
            ReplayGain(trackGain = -3.2f, albumGain = -2.1f, trackPeak = 0.98f, albumPeak = 0.99f),
            item.replayGain,
        )
    }

    @Test
    fun aSongWithoutGainCarriesNoGain() {
        val item =
            AlbumSongItem(
                Song(
                    sourceId = 1,
                    id = "s1",
                    albumId = "al1",
                    artistId = "ar1",
                    title = "Song",
                    album = "Album",
                    artist = "Artist",
                    duration = 100,
                    track = 1,
                    disc = 1,
                    starred = null,
                ),
                coverArt = null,
            ).toQueueItem()

        assertEquals(ReplayGain(), item.replayGain)
    }
}
