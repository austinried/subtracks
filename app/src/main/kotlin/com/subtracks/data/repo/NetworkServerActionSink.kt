package com.subtracks.data.repo

import com.subtracks.data.source.ServerActionSink
import com.subtracks.data.source.StarType

class NetworkServerActionSink(
    private val sources: SourceRepository,
) : ServerActionSink {
    override suspend fun nowPlaying(songId: String) {
        sources.activeMusicSource()?.scrobble(songId, submission = false)
    }

    override suspend fun scrobble(
        songId: String,
        time: Long,
    ) {
        sources.activeMusicSource()?.scrobble(songId, submission = true, time = time)
    }

    override suspend fun setStar(
        type: StarType,
        id: String,
        starred: Boolean,
    ) {
        val source = sources.activeMusicSource() ?: throw IllegalStateException("No active server")
        source.setStar(type, id, starred)
    }

    override suspend fun flush() = Unit
}
