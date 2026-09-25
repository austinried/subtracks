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

internal const val SONGS_IDS_SELECT =
    "SELECT songs.id FROM songs " +
        "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "WHERE songs.sourceId = :sourceId " +
        "AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR " +
        "(:starredFilter = 2 AND songs.starred IS NULL)) "

internal const val SONGS_LIST_SELECT =
    "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
        "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "WHERE songs.sourceId = :sourceId " +
        "AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR " +
        "(:starredFilter = 2 AND songs.starred IS NULL)) "

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

    @Query("SELECT albumId, disc FROM discs WHERE sourceId = :sourceId")
    suspend fun discKeys(sourceId: Long): List<DiscKey>

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
        "SELECT * FROM albums WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0 OR instr(lower(albumArtist), lower(:search)) > 0) " +
            "ORDER BY name COLLATE NOCASE, id",
    )
    fun albumsByName(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0 OR instr(lower(albumArtist), lower(:search)) > 0) " +
            "ORDER BY name COLLATE NOCASE DESC, id",
    )
    fun albumsByNameReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0 OR instr(lower(albumArtist), lower(:search)) > 0) " +
            "ORDER BY albumArtist COLLATE NOCASE, year, name COLLATE NOCASE, id",
    )
    fun albumsByArtist(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0 OR instr(lower(albumArtist), lower(:search)) > 0) " +
            "ORDER BY albumArtist COLLATE NOCASE DESC, year DESC, name COLLATE NOCASE DESC, id",
    )
    fun albumsByArtistReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0 OR instr(lower(albumArtist), lower(:search)) > 0) " +
            "ORDER BY year DESC, name COLLATE NOCASE, id",
    )
    fun albumsByYear(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0 OR instr(lower(albumArtist), lower(:search)) > 0) " +
            "ORDER BY year ASC, name COLLATE NOCASE, id",
    )
    fun albumsByYearReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0 OR instr(lower(albumArtist), lower(:search)) > 0) " +
            "ORDER BY created DESC, name COLLATE NOCASE, id",
    )
    fun albumsByRecentlyAdded(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0 OR instr(lower(albumArtist), lower(:search)) > 0) " +
            "ORDER BY created ASC, name COLLATE NOCASE, id",
    )
    fun albumsByRecentlyAddedReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0 OR instr(lower(albumArtist), lower(:search)) > 0) " +
            "ORDER BY starred IS NULL, starred DESC, name COLLATE NOCASE, id",
    )
    fun albumsByStarred(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0 OR instr(lower(albumArtist), lower(:search)) > 0) " +
            "ORDER BY starred IS NULL, starred ASC, name COLLATE NOCASE, id",
    )
    fun albumsByStarredReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Album>

    @Query(
        "SELECT * FROM artists WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) " +
            "ORDER BY name COLLATE NOCASE, id",
    )
    fun artistsByName(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Artist>

    @Query(
        "SELECT * FROM artists WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) " +
            "ORDER BY name COLLATE NOCASE DESC, id",
    )
    fun artistsByNameReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Artist>

    @Query(
        "SELECT * FROM artists WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) " +
            "ORDER BY albumCount DESC, name COLLATE NOCASE, id",
    )
    fun artistsByAlbumCount(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Artist>

    @Query(
        "SELECT * FROM artists WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) " +
            "ORDER BY albumCount ASC, name COLLATE NOCASE, id",
    )
    fun artistsByAlbumCountReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Artist>

    @Query(
        "SELECT * FROM artists WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) " +
            "ORDER BY starred IS NULL, starred DESC, name COLLATE NOCASE, id",
    )
    fun artistsByStarred(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Artist>

    @Query(
        "SELECT * FROM artists WHERE sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) " +
            "ORDER BY starred IS NULL, starred ASC, name COLLATE NOCASE, id",
    )
    fun artistsByStarredReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, Artist>

    @Query(
        "SELECT * FROM playlists WHERE sourceId = :sourceId " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) " +
            "ORDER BY name COLLATE NOCASE, id",
    )
    fun playlistsByName(
        sourceId: Long,
        search: String,
    ): PagingSource<Int, Playlist>

    @Query(
        "SELECT * FROM playlists WHERE sourceId = :sourceId " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) " +
            "ORDER BY name COLLATE NOCASE DESC, id",
    )
    fun playlistsByNameReversed(
        sourceId: Long,
        search: String,
    ): PagingSource<Int, Playlist>

    @Query(
        "SELECT * FROM playlists WHERE sourceId = :sourceId " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) " +
            "ORDER BY created DESC, name COLLATE NOCASE, id",
    )
    fun playlistsByAdded(
        sourceId: Long,
        search: String,
    ): PagingSource<Int, Playlist>

    @Query(
        "SELECT * FROM playlists WHERE sourceId = :sourceId " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) " +
            "ORDER BY created ASC, name COLLATE NOCASE, id",
    )
    fun playlistsByAddedReversed(
        sourceId: Long,
        search: String,
    ): PagingSource<Int, Playlist>

    @Query(
        "SELECT * FROM playlists WHERE sourceId = :sourceId " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) " +
            "ORDER BY changed DESC, name COLLATE NOCASE, id",
    )
    fun playlistsByUpdated(
        sourceId: Long,
        search: String,
    ): PagingSource<Int, Playlist>

    @Query(
        "SELECT * FROM playlists WHERE sourceId = :sourceId " +
            "AND (:search = '' OR instr(lower(name), lower(:search)) > 0) " +
            "ORDER BY changed ASC, name COLLATE NOCASE, id",
    )
    fun playlistsByUpdatedReversed(
        sourceId: Long,
        search: String,
    ): PagingSource<Int, Playlist>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR " +
            "(:starredFilter = 2 AND songs.starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(songs.title), lower(:search)) > 0 " +
            "OR instr(lower(songs.artist), lower(:search)) > 0 OR instr(lower(songs.album), lower(:search)) > 0) " +
            "ORDER BY albums.albumArtist COLLATE NOCASE, songs.album COLLATE NOCASE, songs.disc, songs.track, " +
            "songs.title COLLATE NOCASE, songs.id",
    )
    fun songs(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, SongListItem>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR " +
            "(:starredFilter = 2 AND songs.starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(songs.title), lower(:search)) > 0 " +
            "OR instr(lower(songs.artist), lower(:search)) > 0 OR instr(lower(songs.album), lower(:search)) > 0) " +
            "ORDER BY albums.albumArtist COLLATE NOCASE DESC, songs.album COLLATE NOCASE DESC, songs.disc DESC, " +
            "songs.track DESC, songs.title COLLATE NOCASE DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id DESC",
    )
    fun songsByAlbumReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, SongListItem>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR " +
            "(:starredFilter = 2 AND songs.starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(songs.title), lower(:search)) > 0 " +
            "OR instr(lower(songs.artist), lower(:search)) > 0 OR instr(lower(songs.album), lower(:search)) > 0) " +
            "ORDER BY songs.title COLLATE NOCASE, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id",
    )
    fun songsByTitle(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, SongListItem>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR " +
            "(:starredFilter = 2 AND songs.starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(songs.title), lower(:search)) > 0 " +
            "OR instr(lower(songs.artist), lower(:search)) > 0 OR instr(lower(songs.album), lower(:search)) > 0) " +
            "ORDER BY songs.title COLLATE NOCASE DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id",
    )
    fun songsByTitleReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, SongListItem>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR " +
            "(:starredFilter = 2 AND songs.starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(songs.title), lower(:search)) > 0 " +
            "OR instr(lower(songs.artist), lower(:search)) > 0 OR instr(lower(songs.album), lower(:search)) > 0) " +
            "ORDER BY songs.artist COLLATE NOCASE, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id",
    )
    fun songsByArtist(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, SongListItem>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR " +
            "(:starredFilter = 2 AND songs.starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(songs.title), lower(:search)) > 0 " +
            "OR instr(lower(songs.artist), lower(:search)) > 0 OR instr(lower(songs.album), lower(:search)) > 0) " +
            "ORDER BY songs.artist COLLATE NOCASE DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id",
    )
    fun songsByArtistReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, SongListItem>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR " +
            "(:starredFilter = 2 AND songs.starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(songs.title), lower(:search)) > 0 " +
            "OR instr(lower(songs.artist), lower(:search)) > 0 OR instr(lower(songs.album), lower(:search)) > 0) " +
            "ORDER BY songs.starred IS NULL, songs.starred DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id",
    )
    fun songsByStarred(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, SongListItem>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR " +
            "(:starredFilter = 2 AND songs.starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(songs.title), lower(:search)) > 0 " +
            "OR instr(lower(songs.artist), lower(:search)) > 0 OR instr(lower(songs.album), lower(:search)) > 0) " +
            "ORDER BY songs.starred IS NULL, songs.starred ASC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id",
    )
    fun songsByStarredReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, SongListItem>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR " +
            "(:starredFilter = 2 AND songs.starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(songs.title), lower(:search)) > 0 " +
            "OR instr(lower(songs.artist), lower(:search)) > 0 OR instr(lower(songs.album), lower(:search)) > 0) " +
            "ORDER BY songs.created DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id",
    )
    fun songsByAdded(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, SongListItem>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId " +
            "AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR " +
            "(:starredFilter = 2 AND songs.starred IS NULL)) " +
            "AND (:search = '' OR instr(lower(songs.title), lower(:search)) > 0 " +
            "OR instr(lower(songs.artist), lower(:search)) > 0 OR instr(lower(songs.album), lower(:search)) > 0) " +
            "ORDER BY songs.created ASC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id",
    )
    fun songsByAddedReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
    ): PagingSource<Int, SongListItem>

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

    @Query(
        "SELECT COUNT(*) FROM songs WHERE songs.sourceId = :sourceId AND (:starredFilter = 0 OR (:starredFilter = 1 AND songs.starred IS NOT NULL) OR (:starredFilter = 2 AND songs.starred IS NULL))",
    )
    suspend fun songCountForFilter(
        sourceId: Long,
        starredFilter: Int,
    ): Long

    @Query(
        "${SONGS_IDS_SELECT}ORDER BY albums.albumArtist COLLATE NOCASE, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.title COLLATE NOCASE, songs.id",
    )
    suspend fun songIdsByAlbum(
        sourceId: Long,
        starredFilter: Int,
    ): List<String>

    @Query(
        "${SONGS_IDS_SELECT}ORDER BY albums.albumArtist COLLATE NOCASE DESC, songs.album COLLATE NOCASE DESC, " +
            "songs.disc DESC, songs.track DESC, songs.title COLLATE NOCASE DESC, songs.id DESC",
    )
    suspend fun songIdsByAlbumReversed(
        sourceId: Long,
        starredFilter: Int,
    ): List<String>

    @Query("${SONGS_IDS_SELECT}ORDER BY songs.title COLLATE NOCASE, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id")
    suspend fun songIdsByTitle(
        sourceId: Long,
        starredFilter: Int,
    ): List<String>

    @Query("${SONGS_IDS_SELECT}ORDER BY songs.title COLLATE NOCASE DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id")
    suspend fun songIdsByTitleReversed(
        sourceId: Long,
        starredFilter: Int,
    ): List<String>

    @Query("${SONGS_IDS_SELECT}ORDER BY songs.artist COLLATE NOCASE, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id")
    suspend fun songIdsByArtist(
        sourceId: Long,
        starredFilter: Int,
    ): List<String>

    @Query("${SONGS_IDS_SELECT}ORDER BY songs.artist COLLATE NOCASE DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id")
    suspend fun songIdsByArtistReversed(
        sourceId: Long,
        starredFilter: Int,
    ): List<String>

    @Query(
        "${SONGS_IDS_SELECT}ORDER BY songs.starred IS NULL, songs.starred DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id",
    )
    suspend fun songIdsByStarred(
        sourceId: Long,
        starredFilter: Int,
    ): List<String>

    @Query(
        "${SONGS_IDS_SELECT}ORDER BY songs.starred IS NULL, songs.starred ASC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id",
    )
    suspend fun songIdsByStarredReversed(
        sourceId: Long,
        starredFilter: Int,
    ): List<String>

    @Query("${SONGS_IDS_SELECT}ORDER BY songs.created DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id")
    suspend fun songIdsByAdded(
        sourceId: Long,
        starredFilter: Int,
    ): List<String>

    @Query("${SONGS_IDS_SELECT}ORDER BY songs.created ASC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id")
    suspend fun songIdsByAddedReversed(
        sourceId: Long,
        starredFilter: Int,
    ): List<String>

    @Query(
        "${SONGS_LIST_SELECT}ORDER BY albums.albumArtist COLLATE NOCASE, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.title COLLATE NOCASE, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun songsPageByAlbum(
        sourceId: Long,
        starredFilter: Int,
        limit: Int,
        offset: Long,
    ): List<SongListItem>

    @Query(
        "${SONGS_LIST_SELECT}ORDER BY albums.albumArtist COLLATE NOCASE DESC, songs.album COLLATE NOCASE DESC, songs.disc DESC, songs.track DESC, songs.title COLLATE NOCASE DESC, songs.id DESC LIMIT :limit OFFSET :offset",
    )
    suspend fun songsPageByAlbumReversed(
        sourceId: Long,
        starredFilter: Int,
        limit: Int,
        offset: Long,
    ): List<SongListItem>

    @Query(
        "${SONGS_LIST_SELECT}ORDER BY songs.title COLLATE NOCASE, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun songsPageByTitle(
        sourceId: Long,
        starredFilter: Int,
        limit: Int,
        offset: Long,
    ): List<SongListItem>

    @Query(
        "${SONGS_LIST_SELECT}ORDER BY songs.title COLLATE NOCASE DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun songsPageByTitleReversed(
        sourceId: Long,
        starredFilter: Int,
        limit: Int,
        offset: Long,
    ): List<SongListItem>

    @Query(
        "${SONGS_LIST_SELECT}ORDER BY songs.artist COLLATE NOCASE, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun songsPageByArtist(
        sourceId: Long,
        starredFilter: Int,
        limit: Int,
        offset: Long,
    ): List<SongListItem>

    @Query(
        "${SONGS_LIST_SELECT}ORDER BY songs.artist COLLATE NOCASE DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun songsPageByArtistReversed(
        sourceId: Long,
        starredFilter: Int,
        limit: Int,
        offset: Long,
    ): List<SongListItem>

    @Query(
        "${SONGS_LIST_SELECT}ORDER BY songs.starred IS NULL, songs.starred DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun songsPageByStarred(
        sourceId: Long,
        starredFilter: Int,
        limit: Int,
        offset: Long,
    ): List<SongListItem>

    @Query(
        "${SONGS_LIST_SELECT}ORDER BY songs.starred IS NULL, songs.starred ASC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun songsPageByStarredReversed(
        sourceId: Long,
        starredFilter: Int,
        limit: Int,
        offset: Long,
    ): List<SongListItem>

    @Query(
        "${SONGS_LIST_SELECT}ORDER BY songs.created DESC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun songsPageByAdded(
        sourceId: Long,
        starredFilter: Int,
        limit: Int,
        offset: Long,
    ): List<SongListItem>

    @Query(
        "${SONGS_LIST_SELECT}ORDER BY songs.created ASC, songs.album COLLATE NOCASE, songs.disc, songs.track, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun songsPageByAddedReversed(
        sourceId: Long,
        starredFilter: Int,
        limit: Int,
        offset: Long,
    ): List<SongListItem>
}
