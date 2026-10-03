package com.subtracks.data.source

/**
 * Writes the app pushes back to the server. Actions that could not be delivered
 * while the server was unreachable are persisted and replayed by [flush].
 */
interface ServerActionSink {
    suspend fun nowPlaying(songId: String)

    suspend fun scrobble(
        songId: String,
        time: Long,
    )

    suspend fun setStar(
        type: StarType,
        id: String,
        starred: Boolean,
    )

    suspend fun flush() = Unit
}
