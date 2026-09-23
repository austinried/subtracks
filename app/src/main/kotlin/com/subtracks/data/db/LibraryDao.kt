package com.subtracks.data.db

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongListItem
import kotlinx.coroutines.flow.Flow

internal const val PLAYLIST_SONGS_SQL =
    "SELECT songs.*, albums.coverArt AS coverArt FROM playlist_songs " +
        "JOIN songs ON songs.sourceId = playlist_songs.sourceId AND songs.id = playlist_songs.songId " +
        "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "WHERE playlist_songs.sourceId = :sourceId AND playlist_songs.playlistId = :playlistId " +
        "ORDER BY playlist_songs.position"

internal const val SONGS_SQL =
    "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
        "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "WHERE songs.sourceId = :sourceId " +
        "ORDER BY albums.albumArtist COLLATE NOCASE, songs.album COLLATE NOCASE, songs.disc, songs.track, " +
        "songs.title COLLATE NOCASE, songs.id"

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
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
    suspend fun deleteArtists(
        sourceId: Long,
        ids: Collection<String>,
    )

    @Query("DELETE FROM albums WHERE sourceId = :sourceId AND id IN (:ids)")
    suspend fun deleteAlbums(
        sourceId: Long,
        ids: Collection<String>,
    )

    @Query("DELETE FROM playlists WHERE sourceId = :sourceId AND id IN (:ids)")
    suspend fun deletePlaylists(
        sourceId: Long,
        ids: Collection<String>,
    )

    @Query("DELETE FROM songs WHERE sourceId = :sourceId AND id IN (:ids)")
    suspend fun deleteSongs(
        sourceId: Long,
        ids: Collection<String>,
    )

    @Query("DELETE FROM playlist_songs WHERE sourceId = :sourceId AND playlistId = :playlistId AND position >= :position")
    suspend fun deletePlaylistSongsFrom(
        sourceId: Long,
        playlistId: String,
        position: Long,
    )

    @Query("SELECT * FROM artists WHERE sourceId = :sourceId ORDER BY name COLLATE NOCASE")
    fun artists(sourceId: Long): PagingSource<Int, Artist>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId ORDER BY name COLLATE NOCASE")
    fun albumsByName(sourceId: Long): PagingSource<Int, Album>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId ORDER BY albumArtist COLLATE NOCASE, year, name COLLATE NOCASE")
    fun albumsByArtist(sourceId: Long): PagingSource<Int, Album>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId ORDER BY year DESC, name COLLATE NOCASE")
    fun albumsByYear(sourceId: Long): PagingSource<Int, Album>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId ORDER BY created DESC, name COLLATE NOCASE")
    fun albumsByRecentlyAdded(sourceId: Long): PagingSource<Int, Album>

    @Query(SONGS_SQL)
    fun songs(sourceId: Long): PagingSource<Int, SongListItem>

    @Query("SELECT * FROM playlists WHERE sourceId = :sourceId ORDER BY name COLLATE NOCASE")
    fun playlists(sourceId: Long): PagingSource<Int, Playlist>

    @Query(PLAYLIST_SONGS_SQL)
    fun playlistSongs(
        sourceId: Long,
        playlistId: String,
    ): PagingSource<Int, SongListItem>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId AND id = :albumId")
    fun album(
        sourceId: Long,
        albumId: String,
    ): Flow<Album?>

    @Query("SELECT * FROM artists WHERE sourceId = :sourceId AND id = :artistId")
    fun artist(
        sourceId: Long,
        artistId: String,
    ): Flow<Artist?>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId AND artistId = :artistId ORDER BY year DESC, name COLLATE NOCASE")
    fun albumsForArtist(
        sourceId: Long,
        artistId: String,
    ): Flow<List<Album>>

    @Query("SELECT * FROM playlists WHERE sourceId = :sourceId AND id = :playlistId")
    fun playlist(
        sourceId: Long,
        playlistId: String,
    ): Flow<Playlist?>

    @Query("SELECT * FROM songs WHERE sourceId = :sourceId AND albumId = :albumId ORDER BY disc, track")
    fun songsByAlbum(
        sourceId: Long,
        albumId: String,
    ): Flow<List<Song>>
}
