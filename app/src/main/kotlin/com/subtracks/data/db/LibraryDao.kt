package com.subtracks.data.db

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Disc
import com.subtracks.data.model.DiscKey
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.PlaylistSongItem
import com.subtracks.data.model.Song
import kotlinx.coroutines.flow.Flow

internal const val PLAYLIST_SONGS_SELECT =
    "SELECT songs.*, albums.coverArt AS coverArt, playlist_songs.position AS position FROM playlist_songs " +
        "JOIN songs ON songs.sourceId = playlist_songs.sourceId AND songs.id = playlist_songs.songId " +
        "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "WHERE playlist_songs.sourceId = :sourceId AND playlist_songs.playlistId = :playlistId"

internal const val PLAYLIST_SONGS_ORDER = " ORDER BY playlist_songs.position"

internal const val PLAYLIST_SONGS_SQL = PLAYLIST_SONGS_SELECT + PLAYLIST_SONGS_ORDER

internal const val ALBUMS_FILTER =
    "FROM albums WHERE sourceId = :sourceId " +
        "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
        "AND (:search = '' OR instr(lower(name), lower(:search)) > 0 OR instr(lower(albumArtist), lower(:search)) > 0) "

internal const val ALBUM_ORDER_BY_NAME = "name COLLATE NOCASE, id"
internal const val ALBUM_ORDER_BY_NAME_REVERSED = "name COLLATE NOCASE DESC, id DESC"
internal const val ALBUM_ORDER_BY_ARTIST = "albumArtist COLLATE NOCASE, year, name COLLATE NOCASE, id"
internal const val ALBUM_ORDER_BY_ARTIST_REVERSED = "albumArtist COLLATE NOCASE DESC, year DESC, name COLLATE NOCASE DESC, id DESC"
internal const val ALBUM_ORDER_BY_YEAR = "year DESC, name COLLATE NOCASE, id"
internal const val ALBUM_ORDER_BY_YEAR_REVERSED = "year ASC, name COLLATE NOCASE DESC, id DESC"
internal const val ALBUM_ORDER_BY_ADDED = "created DESC, name COLLATE NOCASE, id"
internal const val ALBUM_ORDER_BY_ADDED_REVERSED = "created ASC, name COLLATE NOCASE DESC, id DESC"

// Starred orders keep unstarred rows last in both directions, matching the list grouping, so the
// reversed order is not a literal mirror of the base order.
internal const val ALBUM_ORDER_BY_STARRED = "starred DESC NULLS LAST, name COLLATE NOCASE, id"
internal const val ALBUM_ORDER_BY_STARRED_REVERSED = "starred ASC NULLS LAST, name COLLATE NOCASE DESC, id DESC"

internal const val ARTISTS_FILTER =
    "FROM artists WHERE sourceId = :sourceId " +
        "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
        "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) "

internal const val ARTIST_ORDER_BY_NAME = "name COLLATE NOCASE, id"
internal const val ARTIST_ORDER_BY_NAME_REVERSED = "name COLLATE NOCASE DESC, id DESC"
internal const val ARTIST_ORDER_BY_ALBUM_COUNT = "albumCount DESC, name COLLATE NOCASE, id"
internal const val ARTIST_ORDER_BY_ALBUM_COUNT_REVERSED = "albumCount ASC, name COLLATE NOCASE DESC, id DESC"
internal const val ARTIST_ORDER_BY_STARRED = "starred DESC NULLS LAST, name COLLATE NOCASE, id"
internal const val ARTIST_ORDER_BY_STARRED_REVERSED = "starred ASC NULLS LAST, name COLLATE NOCASE DESC, id DESC"

internal const val PLAYLISTS_FILTER =
    "FROM playlists WHERE sourceId = :sourceId " +
        "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) "

internal const val PLAYLIST_ORDER_BY_NAME = "name COLLATE NOCASE, id"
internal const val PLAYLIST_ORDER_BY_NAME_REVERSED = "name COLLATE NOCASE DESC, id DESC"
internal const val PLAYLIST_ORDER_BY_ADDED = "created DESC, name COLLATE NOCASE, id"
internal const val PLAYLIST_ORDER_BY_ADDED_REVERSED = "created ASC, name COLLATE NOCASE DESC, id DESC"
internal const val PLAYLIST_ORDER_BY_UPDATED = "changed DESC, name COLLATE NOCASE, id"
internal const val PLAYLIST_ORDER_BY_UPDATED_REVERSED = "changed ASC, name COLLATE NOCASE DESC, id DESC"

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

    @Query("UPDATE artists SET starred = :starred WHERE sourceId = :sourceId AND id = :id")
    suspend fun setArtistStar(
        sourceId: Long,
        id: String,
        starred: Long?,
    )

    @Query("UPDATE albums SET starred = :starred WHERE sourceId = :sourceId AND id = :id")
    suspend fun setAlbumStar(
        sourceId: Long,
        id: String,
        starred: Long?,
    )

    @Query("UPDATE songs SET starred = :starred WHERE sourceId = :sourceId AND id = :id")
    suspend fun setSongStar(
        sourceId: Long,
        id: String,
        starred: Long?,
    )

    @Query("SELECT id FROM artists WHERE sourceId = :sourceId")
    suspend fun artistIds(sourceId: Long): List<String>

    @Query("SELECT id FROM artists WHERE sourceId = :sourceId AND id > :afterId ORDER BY id LIMIT :limit")
    suspend fun artistIdsAfter(
        sourceId: Long,
        afterId: String,
        limit: Int,
    ): List<String>

    @Query("SELECT id FROM albums WHERE sourceId = :sourceId")
    suspend fun albumIds(sourceId: Long): List<String>

    @Query("SELECT id FROM albums WHERE sourceId = :sourceId AND id > :afterId ORDER BY id LIMIT :limit")
    suspend fun albumIdsAfter(
        sourceId: Long,
        afterId: String,
        limit: Int,
    ): List<String>

    @Query(
        "SELECT albumId, disc FROM discs WHERE sourceId = :sourceId " +
            "AND (albumId > :afterAlbumId OR (albumId = :afterAlbumId AND disc > :afterDisc)) " +
            "ORDER BY albumId, disc LIMIT :limit",
    )
    suspend fun discKeysAfter(
        sourceId: Long,
        afterAlbumId: String,
        afterDisc: Long,
        limit: Int,
    ): List<DiscKey>

    @Query("SELECT id FROM playlists WHERE sourceId = :sourceId")
    suspend fun playlistIds(sourceId: Long): List<String>

    @Query("SELECT id FROM playlists WHERE sourceId = :sourceId AND id > :afterId ORDER BY id LIMIT :limit")
    suspend fun playlistIdsAfter(
        sourceId: Long,
        afterId: String,
        limit: Int,
    ): List<String>

    @Query("SELECT id FROM songs WHERE sourceId = :sourceId AND id > :afterId ORDER BY id LIMIT :limit")
    suspend fun songIdsAfter(
        sourceId: Long,
        afterId: String,
        limit: Int,
    ): List<String>

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

    @Query("DELETE FROM discs WHERE sourceId = :sourceId AND albumId = :albumId AND disc = :disc")
    suspend fun deleteDisc(
        sourceId: Long,
        albumId: String,
        disc: Long,
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

    @Query(
        "DELETE FROM playlist_songs WHERE sourceId = :sourceId " +
            "AND playlistId NOT IN (SELECT id FROM playlists WHERE sourceId = :sourceId)",
    )
    suspend fun deleteOrphanPlaylistSongs(sourceId: Long)

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_NAME")
    fun albumsByName(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_NAME_REVERSED")
    fun albumsByNameReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_ARTIST")
    fun albumsByArtist(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_ARTIST_REVERSED")
    fun albumsByArtistReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_YEAR")
    fun albumsByYear(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_YEAR_REVERSED")
    fun albumsByYearReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_ADDED")
    fun albumsByRecentlyAdded(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_ADDED_REVERSED")
    fun albumsByRecentlyAddedReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_STARRED")
    fun albumsByStarred(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_STARRED_REVERSED")
    fun albumsByStarredReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_NAME")
    fun artistsByName(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Artist>

    @Query("SELECT * $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_NAME_REVERSED")
    fun artistsByNameReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Artist>

    @Query("SELECT * $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_ALBUM_COUNT")
    fun artistsByAlbumCount(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Artist>

    @Query("SELECT * $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_ALBUM_COUNT_REVERSED")
    fun artistsByAlbumCountReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Artist>

    @Query("SELECT * $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_STARRED")
    fun artistsByStarred(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Artist>

    @Query("SELECT * $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_STARRED_REVERSED")
    fun artistsByStarredReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Artist>

    @Query("SELECT * $PLAYLISTS_FILTER ORDER BY $PLAYLIST_ORDER_BY_NAME")
    fun playlistsByName(
        sourceId: Long,
        search: String,
    ): PagingSource<Int, Playlist>

    @Query("SELECT * $PLAYLISTS_FILTER ORDER BY $PLAYLIST_ORDER_BY_NAME_REVERSED")
    fun playlistsByNameReversed(
        sourceId: Long,
        search: String,
    ): PagingSource<Int, Playlist>

    @Query("SELECT * $PLAYLISTS_FILTER ORDER BY $PLAYLIST_ORDER_BY_ADDED")
    fun playlistsByAdded(
        sourceId: Long,
        search: String,
    ): PagingSource<Int, Playlist>

    @Query("SELECT * $PLAYLISTS_FILTER ORDER BY $PLAYLIST_ORDER_BY_ADDED_REVERSED")
    fun playlistsByAddedReversed(
        sourceId: Long,
        search: String,
    ): PagingSource<Int, Playlist>

    @Query("SELECT * $PLAYLISTS_FILTER ORDER BY $PLAYLIST_ORDER_BY_UPDATED")
    fun playlistsByUpdated(
        sourceId: Long,
        search: String,
    ): PagingSource<Int, Playlist>

    @Query("SELECT * $PLAYLISTS_FILTER ORDER BY $PLAYLIST_ORDER_BY_UPDATED_REVERSED")
    fun playlistsByUpdatedReversed(
        sourceId: Long,
        search: String,
    ): PagingSource<Int, Playlist>

    @Query(PLAYLIST_SONGS_SQL)
    fun playlistSongs(
        sourceId: Long,
        playlistId: String,
    ): PagingSource<Int, PlaylistSongItem>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId AND id = :albumId")
    fun album(
        sourceId: Long,
        albumId: String,
    ): Flow<Album?>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId AND id = :albumId")
    suspend fun albumOnce(
        sourceId: Long,
        albumId: String,
    ): Album?

    @Query("SELECT * FROM artists WHERE sourceId = :sourceId AND id = :artistId")
    suspend fun artistOnce(
        sourceId: Long,
        artistId: String,
    ): Artist?

    @Query("SELECT * FROM artists WHERE sourceId = :sourceId AND id = :artistId")
    fun artist(
        sourceId: Long,
        artistId: String,
    ): Flow<Artist?>

    @Query("SELECT * FROM songs WHERE sourceId = :sourceId AND id = :songId")
    fun song(
        sourceId: Long,
        songId: String,
    ): Flow<Song?>

    @Query("SELECT * FROM songs WHERE sourceId = :sourceId AND id = :songId")
    suspend fun songOnce(
        sourceId: Long,
        songId: String,
    ): Song?

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId AND artistId = :artistId ORDER BY year DESC, name COLLATE NOCASE, id")
    fun albumsForArtist(
        sourceId: Long,
        artistId: String,
    ): Flow<List<Album>>

    @Query("SELECT * FROM discs WHERE sourceId = :sourceId AND albumId = :albumId ORDER BY disc")
    fun discs(
        sourceId: Long,
        albumId: String,
    ): Flow<List<Disc>>

    @Query("SELECT * FROM playlists WHERE sourceId = :sourceId AND id = :playlistId")
    fun playlist(
        sourceId: Long,
        playlistId: String,
    ): Flow<Playlist?>

    @Query("SELECT * FROM songs WHERE sourceId = :sourceId AND albumId = :albumId ORDER BY disc, track, id")
    fun songsByAlbum(
        sourceId: Long,
        albumId: String,
    ): Flow<List<Song>>
}
