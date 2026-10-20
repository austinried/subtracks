package com.subtracks.playback

import androidx.media3.common.C
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.ArtistRef
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaItemTest {
    private val song =
        QueueItem(
            id = "s1",
            title = "Song",
            artist = "Artist",
            album = "Album",
            coverArtId = "al1",
            durationMs = 100_000L,
        )

    @Test
    fun aDownloadedFileIsPreferredOverTheStream() {
        val item = queueMediaItem(song, local = "file:///downloads/1/s1", stream = "http://server/stream?s=s1", transcode = false)

        assertEquals("file:///downloads/1/s1", item.localConfiguration?.uri.toString())
    }

    @Test
    fun theStreamIsUsedWhenThereIsNoDownload() {
        val item = queueMediaItem(song, local = null, stream = "http://server/stream?s=s1", transcode = false)

        assertEquals("http://server/stream?s=s1", item.localConfiguration?.uri.toString())
    }

    @Test
    fun aDownloadedFileIsNotClippedEvenForATranscodingQuality() {
        val item = queueMediaItem(song, local = "file:///downloads/1/s1", stream = "http://server/stream?s=s1", transcode = true)

        assertEquals(C.TIME_END_OF_SOURCE, item.clippingConfiguration.endPositionMs)
    }

    @Test
    fun aTranscodedStreamIsClippedToItsDeclaredDuration() {
        val item = queueMediaItem(song, local = null, stream = "http://server/stream?s=s1", transcode = true)

        assertEquals(100_000L, item.clippingConfiguration.endPositionMs)
    }

    @Test
    fun theItemKeepsItsIdTitleAndCoverArt() {
        val item = queueMediaItem(song, local = null, stream = "http://server/stream?s=s1", transcode = false)

        assertEquals("s1", item.mediaId)
        assertEquals("Song", item.mediaMetadata.title.toString())
        assertEquals(CoverArtArtwork.uri("al1"), item.mediaMetadata.artworkUri)
    }

    @Test
    fun theItemCarriesTheReplayGain() {
        val gain = ReplayGain(trackGain = -3.2f, albumGain = -2.1f, trackPeak = 0.98f, albumPeak = 0.99f)

        val item =
            queueMediaItem(
                song.copy(replayGain = gain),
                local = null,
                stream = "http://server/stream?s=s1",
                transcode = false,
            )

        assertEquals(gain, item.replayGain())
    }

    @Test
    fun theItemCarriesEveryArtist() {
        val artists = listOf(ArtistRef("ar1", "A"), ArtistRef("ar2", "B"))

        val item =
            queueMediaItem(
                song.copy(artists = artists),
                local = null,
                stream = "http://server/stream?s=s1",
                transcode = false,
            )

        assertEquals(artists, item.artistRefs())
    }

    @Test
    fun theItemCarriesItsGainContext() {
        val item =
            queueMediaItem(
                song.copy(albumContext = true),
                local = null,
                stream = "http://server/stream?s=s1",
                transcode = false,
            )

        assertEquals(true, item.albumContext())
        assertEquals(false, queueMediaItem(song, local = null, stream = "http://server/stream?s=s1", transcode = false).albumContext())
    }

    @Test
    fun anItemWithoutGainReadsBackAsNoGain() {
        val item = queueMediaItem(song, local = null, stream = "http://server/stream?s=s1", transcode = false)

        assertEquals(ReplayGain(), item.replayGain())
    }

    @Test
    fun aZeroGainIsNotMistakenForNoGain() {
        val item =
            queueMediaItem(
                song.copy(replayGain = ReplayGain(trackGain = 0f, trackPeak = 0.5f)),
                local = null,
                stream = "http://server/stream?s=s1",
                transcode = false,
            )

        assertEquals(ReplayGain(trackGain = 0f, trackPeak = 0.5f), item.replayGain())
    }
}
