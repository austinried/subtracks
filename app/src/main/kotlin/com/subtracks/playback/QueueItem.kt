package com.subtracks.playback

import com.subtracks.data.model.SongItem

fun SongItem.toQueueItem() =
    QueueItem(
        id = song.id,
        title = song.title,
        artist = song.artist,
        album = song.album,
        coverArtId = coverArt,
        durationMs = song.duration?.let { it * 1000 },
    )
