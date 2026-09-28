package com.subtracks.data.source

/**
 * Writes the app pushes back to the server. Today they go straight over the
 * network, but funneling them through one seam leaves room to persist them and
 * replay later while the server is unreachable.
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
}
