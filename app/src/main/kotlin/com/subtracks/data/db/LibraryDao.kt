package com.subtracks.data.db

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Upsert
    suspend fun upsertArtists(items: List<Artist>)

    @Upsert
    suspend fun upsertAlbums(items: List<Album>)

    @Upsert
    suspend fun upsertPlaylists(items: List<Playlist>)

    @Upsert
    suspend fun upsertSongs(items: List<Song>)

    @Upsert
    suspend fun upsertPlaylistSongs(items: List<PlaylistSong>)

    @Query("SELECT id FROM artists WHERE sourceId = :sourceId")
    suspend fun artistIds(sourceId: Long): List<String>

    @Query("SELECT id FROM albums WHERE sourceId = :sourceId")
    suspend fun albumIds(sourceId: Long): List<String>

    @Query("SELECT id FROM playlists WHERE sourceId = :sourceId")
    suspend fun playlistIds(sourceId: Long): List<String>

    @Query("SELECT id FROM songs WHERE sourceId = :sourceId")
    suspend fun songIds(sourceId: Long): List<String>

    @Query("DELETE FROM artists WHERE sourceId = :sourceId AND id IN (:ids)")
    suspend fun deleteArtists(sourceId: Long, ids: Collection<String>)

    @Query("DELETE FROM albums WHERE sourceId = :sourceId AND id IN (:ids)")
    suspend fun deleteAlbums(sourceId: Long, ids: Collection<String>)

    @Query("DELETE FROM playlists WHERE sourceId = :sourceId AND id IN (:ids)")
    suspend fun deletePlaylists(sourceId: Long, ids: Collection<String>)

    @Query("DELETE FROM songs WHERE sourceId = :sourceId AND id IN (:ids)")
    suspend fun deleteSongs(sourceId: Long, ids: Collection<String>)

    @Query("DELETE FROM playlist_songs WHERE sourceId = :sourceId")
    suspend fun deletePlaylistSongs(sourceId: Long)

    @Query("SELECT * FROM artists WHERE sourceId = :sourceId ORDER BY name")
    fun artists(sourceId: Long): Flow<List<Artist>>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId ORDER BY name")
    fun albums(sourceId: Long): Flow<List<Album>>

    @Query("SELECT * FROM playlists WHERE sourceId = :sourceId ORDER BY name")
    fun playlists(sourceId: Long): Flow<List<Playlist>>

    @Query("SELECT * FROM songs WHERE sourceId = :sourceId ORDER BY artist, album, disc, track")
    fun songs(sourceId: Long): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE sourceId = :sourceId AND albumId = :albumId ORDER BY disc, track")
    fun songsByAlbum(sourceId: Long, albumId: String): Flow<List<Song>>

    @Query(
        "SELECT songs.* FROM playlist_songs " +
            "JOIN songs ON songs.sourceId = playlist_songs.sourceId AND songs.id = playlist_songs.songId " +
            "WHERE playlist_songs.sourceId = :sourceId AND playlist_songs.playlistId = :playlistId " +
            "ORDER BY playlist_songs.position",
    )
    fun songsByPlaylist(sourceId: Long, playlistId: String): Flow<List<Song>>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId AND artistId = :artistId ORDER BY year DESC, name")
    fun albumsByArtist(sourceId: Long, artistId: String): Flow<List<Album>>
}
