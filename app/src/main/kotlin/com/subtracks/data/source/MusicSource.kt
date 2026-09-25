package com.subtracks.data.source

import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicSource {
    val id: Long

    suspend fun ping()

    fun artists(): Flow<List<Artist>>

    fun albums(): Flow<List<Album>>

    fun songs(): Flow<List<Song>>

    fun playlists(): Flow<List<Playlist>>

    fun playlistSongs(playlistIds: List<String>): Flow<List<PlaylistSong>>
}
