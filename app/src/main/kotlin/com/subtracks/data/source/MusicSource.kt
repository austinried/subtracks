package com.subtracks.data.source

import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import kotlinx.coroutines.flow.Flow

const val DEFAULT_FETCH_CONCURRENCY = 4

interface MusicSource {
    val id: Long

    suspend fun ping()

    suspend fun setStar(
        type: StarType,
        id: String,
        starred: Boolean,
    )

    fun artists(): Flow<List<Artist>>

    fun albums(): Flow<List<Album>>

    fun songs(): Flow<List<Song>>

    fun playlists(): Flow<List<Playlist>>

    fun playlistSongs(playlistIds: List<String>): Flow<List<PlaylistSong>>
}

enum class StarType {
    Song,
    Album,
    Artist,
}
