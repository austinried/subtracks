package com.subtracks.playback

import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.SongItem

fun SongItem.toQueueItem(albumContext: Boolean = false) =
    QueueItem(
        id = song.id,
        title = song.title,
        artist = song.artist,
        artists = song.artistRefs,
        album = song.album,
        coverArtId = coverArt,
        durationMs = song.duration?.let { it * 1000 },
        replayGain = song.replayGain(),
        albumContext = albumContext,
    )
