package com.subtracks.data.source

import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song

interface MusicSource {
    val id: Long

    suspend fun ping()

    suspend fun getArtists(): List<Artist>

    suspend fun getAlbums(): List<Album>

    suspend fun getSongs(): List<Song>

    suspend fun getPlaylists(): List<Playlist>

    suspend fun getPlaylistSongs(playlists: List<Playlist>): List<PlaylistSong>
}
