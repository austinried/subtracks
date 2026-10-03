package com.subtracks.data.db

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.subtracks.data.model.Album
import com.subtracks.data.model.AlbumSongItem
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

internal const val DOWNLOADED_SONG =
    "EXISTS (SELECT 1 FROM song_downloads sd WHERE sd.sourceId = playlist_songs.sourceId " +
        "AND sd.songId = playlist_songs.songId AND sd.status = 'Completed')"

internal const val ALBUMS_FILTER =
    "FROM albums WHERE sourceId = :sourceId " +
        "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
        "AND (:downloadedFilter = 0 OR EXISTS (SELECT 1 FROM songs " +
        "JOIN song_downloads sd ON sd.sourceId = songs.sourceId AND sd.songId = songs.id " +
        "WHERE songs.sourceId = albums.sourceId AND songs.albumId = albums.id AND sd.status = 'Completed')) " +
        "AND (:search = '' " +
        "OR (length(:search) >= 3 AND rowid IN (SELECT rowid FROM album_search " +
        "WHERE album_search MATCH '\"' || replace(:search, '\"', '\"\"') || '\"')) " +
        "OR (length(:search) < 3 AND (instr(lower(name), lower(:search)) > 0 " +
        "OR instr(lower(albumArtist), lower(:search)) > 0))) "

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

internal const val ALBUM_ORDER_BY_FREQUENT = "playCount DESC, name COLLATE NOCASE, id"
internal const val ALBUM_ORDER_BY_FREQUENT_REVERSED = "playCount ASC, name COLLATE NOCASE DESC, id DESC"
internal const val ALBUM_ORDER_BY_RECENT = "played DESC NULLS LAST, name COLLATE NOCASE, id"
internal const val ALBUM_ORDER_BY_RECENT_REVERSED = "played ASC NULLS LAST, name COLLATE NOCASE DESC, id DESC"

internal const val ARTISTS_FILTER =
    "FROM artists WHERE sourceId = :sourceId " +
        "AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL)) " +
        "AND (:downloadedFilter = 0 OR EXISTS (SELECT 1 FROM songs " +
        "JOIN albums dl ON dl.sourceId = songs.sourceId AND dl.id = songs.albumId " +
        "JOIN song_downloads sd ON sd.sourceId = songs.sourceId AND sd.songId = songs.id " +
        "WHERE songs.sourceId = artists.sourceId AND dl.artistId = artists.id AND sd.status = 'Completed')) " +
        "AND (:search = '' " +
        "OR (length(:search) >= 3 AND rowid IN (SELECT rowid FROM artist_search " +
        "WHERE artist_search MATCH '\"' || replace(:search, '\"', '\"\"') || '\"')) " +
        "OR (length(:search) < 3 AND instr(lower(name), lower(:search)) > 0)) "

// With the Downloaded filter on, count the albums that actually have a download so the number under
// an artist matches the albums its detail screen lists.
internal const val ARTISTS_SELECT =
    "SELECT artists.sourceId, artists.id, artists.name, " +
        "CASE WHEN :downloadedFilter = 1 THEN (" +
        "SELECT COUNT(DISTINCT dl.id) FROM songs " +
        "JOIN albums dl ON dl.sourceId = songs.sourceId AND dl.id = songs.albumId " +
        "JOIN song_downloads sd ON sd.sourceId = songs.sourceId AND sd.songId = songs.id " +
        "WHERE songs.sourceId = artists.sourceId AND dl.artistId = artists.id AND sd.status = 'Completed' " +
        ") ELSE artists.albumCount END AS albumCount, " +
        "artists.starred, artists.coverArt, artists.playCount, artists.played "

internal const val ARTIST_ORDER_BY_NAME = "name COLLATE NOCASE, id"
internal const val ARTIST_ORDER_BY_NAME_REVERSED = "name COLLATE NOCASE DESC, id DESC"
internal const val ARTIST_ORDER_BY_ALBUM_COUNT = "artists.albumCount DESC, name COLLATE NOCASE, id"
internal const val ARTIST_ORDER_BY_ALBUM_COUNT_REVERSED = "artists.albumCount ASC, name COLLATE NOCASE DESC, id DESC"
internal const val ARTIST_ORDER_BY_STARRED = "starred DESC NULLS LAST, name COLLATE NOCASE, id"
internal const val ARTIST_ORDER_BY_STARRED_REVERSED = "starred ASC NULLS LAST, name COLLATE NOCASE DESC, id DESC"
internal const val ARTIST_ORDER_BY_FREQUENT = "artists.playCount DESC, name COLLATE NOCASE, id"
internal const val ARTIST_ORDER_BY_FREQUENT_REVERSED = "artists.playCount ASC, name COLLATE NOCASE DESC, id DESC"
internal const val ARTIST_ORDER_BY_RECENT = "artists.played DESC NULLS LAST, name COLLATE NOCASE, id"
internal const val ARTIST_ORDER_BY_RECENT_REVERSED = "artists.played ASC NULLS LAST, name COLLATE NOCASE DESC, id DESC"

internal const val PLAYLISTS_FILTER =
    "FROM playlists WHERE sourceId = :sourceId " +
        "AND (:downloadedFilter = 0 OR EXISTS (SELECT 1 FROM playlist_songs ps " +
        "JOIN song_downloads sd ON sd.sourceId = ps.sourceId AND sd.songId = ps.songId " +
        "WHERE ps.sourceId = playlists.sourceId AND ps.playlistId = playlists.id AND sd.status = 'Completed')) " +
        "AND (:search = '' " +
        "OR (length(:search) >= 3 AND rowid IN (SELECT rowid FROM playlist_search " +
        "WHERE playlist_search MATCH '\"' || replace(:search, '\"', '\"\"') || '\"')) " +
        "OR (length(:search) < 3 AND instr(lower(name), lower(:search)) > 0)) "

// With the Downloaded filter on, count the songs that actually have a download so the number under
// a playlist matches the songs its detail screen lists.
internal const val PLAYLISTS_SELECT =
    "SELECT playlists.sourceId, playlists.id, playlists.name, playlists.comment, playlists.coverArt, " +
        "CASE WHEN :downloadedFilter = 1 THEN (" +
        "SELECT COUNT(DISTINCT ps.songId) FROM playlist_songs ps " +
        "JOIN song_downloads sd ON sd.sourceId = ps.sourceId AND sd.songId = ps.songId AND sd.status = 'Completed' " +
        "WHERE ps.sourceId = playlists.sourceId AND ps.playlistId = playlists.id " +
        ") ELSE playlists.songCount END AS songCount, " +
        "playlists.created, playlists.changed, playlists.duration "

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

    @Query(
        "UPDATE albums SET " +
            "playCount = COALESCE((SELECT SUM(s.playCount) FROM songs s " +
            "WHERE s.sourceId = albums.sourceId AND s.albumId = albums.id), 0), " +
            "played = (SELECT MAX(s.played) FROM songs s " +
            "WHERE s.sourceId = albums.sourceId AND s.albumId = albums.id) " +
            "WHERE sourceId = :sourceId AND (" +
            "playCount IS NOT COALESCE((SELECT SUM(s.playCount) FROM songs s " +
            "WHERE s.sourceId = albums.sourceId AND s.albumId = albums.id), 0) " +
            "OR played IS NOT (SELECT MAX(s.played) FROM songs s " +
            "WHERE s.sourceId = albums.sourceId AND s.albumId = albums.id))",
    )
    suspend fun recomputeAlbumPlayData(sourceId: Long)

    // INDEXED BY is required: the planner otherwise answers MAX(played) from index_albums_recent
    // (sourceId, played), which cannot apply the artistId equality.
    @Query(
        "UPDATE artists SET " +
            "playCount = COALESCE((SELECT SUM(a.playCount) FROM albums a INDEXED BY index_albums_sourceId_artistId " +
            "WHERE a.sourceId = artists.sourceId AND a.artistId = artists.id), 0), " +
            "played = (SELECT MAX(a.played) FROM albums a INDEXED BY index_albums_sourceId_artistId " +
            "WHERE a.sourceId = artists.sourceId AND a.artistId = artists.id) " +
            "WHERE sourceId = :sourceId AND (" +
            "playCount IS NOT COALESCE((SELECT SUM(a.playCount) FROM albums a INDEXED BY index_albums_sourceId_artistId " +
            "WHERE a.sourceId = artists.sourceId AND a.artistId = artists.id), 0) " +
            "OR played IS NOT (SELECT MAX(a.played) FROM albums a INDEXED BY index_albums_sourceId_artistId " +
            "WHERE a.sourceId = artists.sourceId AND a.artistId = artists.id))",
    )
    suspend fun recomputeArtistPlayData(sourceId: Long)

    @Transaction
    suspend fun recomputePlayData(sourceId: Long) {
        recomputeAlbumPlayData(sourceId)
        recomputeArtistPlayData(sourceId)
    }

    @Query(
        "UPDATE songs SET playCount = playCount + 1, played = MAX(COALESCE(played, :at), :at) " +
            "WHERE sourceId = :sourceId AND id = :songId",
    )
    suspend fun bumpSongPlay(
        sourceId: Long,
        songId: String,
        at: Long,
    )

    @Query(
        "UPDATE albums SET playCount = playCount + 1, played = MAX(COALESCE(played, :at), :at) " +
            "WHERE sourceId = :sourceId AND id = :albumId",
    )
    suspend fun bumpAlbumPlay(
        sourceId: Long,
        albumId: String,
        at: Long,
    )

    @Query(
        "UPDATE artists SET playCount = playCount + 1, played = MAX(COALESCE(played, :at), :at) " +
            "WHERE sourceId = :sourceId AND id = (SELECT artistId FROM albums WHERE sourceId = :sourceId AND id = :albumId)",
    )
    suspend fun bumpArtistPlay(
        sourceId: Long,
        albumId: String,
        at: Long,
    )

    @Query("SELECT albumId FROM songs WHERE sourceId = :sourceId AND id = :songId")
    suspend fun albumIdForSong(
        sourceId: Long,
        songId: String,
    ): String?

    // Counted locally when a scrobble is submitted, so the play sorts move before the next sync; the
    // sync then overwrites with the server's own number.
    @Transaction
    suspend fun recordPlay(
        sourceId: Long,
        songId: String,
        at: Long,
    ) {
        bumpSongPlay(sourceId, songId, at)
        val albumId = albumIdForSong(sourceId, songId) ?: return
        bumpAlbumPlay(sourceId, albumId, at)
        bumpArtistPlay(sourceId, albumId, at)
    }

    // Albums, not songs: these flows re-run on every write to their table, and songs are written
    // one batch at a time during a sync.
    @Query("SELECT EXISTS(SELECT 1 FROM albums WHERE sourceId = :sourceId AND playCount > 0)")
    fun hasAlbumPlayCount(sourceId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM albums WHERE sourceId = :sourceId AND played IS NOT NULL)")
    fun hasAlbumPlayed(sourceId: Long): Flow<Boolean>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId AND played IS NOT NULL " +
            "ORDER BY played DESC, name COLLATE NOCASE, id LIMIT :limit",
    )
    fun recentlyPlayedAlbums(
        sourceId: Long,
        limit: Int,
    ): Flow<List<Album>>

    @Query(
        "SELECT * FROM artists WHERE sourceId = :sourceId AND played IS NOT NULL " +
            "ORDER BY played DESC, name COLLATE NOCASE, id LIMIT :limit",
    )
    fun recentlyPlayedArtists(
        sourceId: Long,
        limit: Int,
    ): Flow<List<Artist>>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId AND playCount > 0 " +
            "ORDER BY playCount DESC, name COLLATE NOCASE, id LIMIT :limit",
    )
    fun mostPlayedAlbums(
        sourceId: Long,
        limit: Int,
    ): Flow<List<Album>>

    @Query(
        "SELECT * FROM artists WHERE sourceId = :sourceId AND playCount > 0 " +
            "ORDER BY playCount DESC, name COLLATE NOCASE, id LIMIT :limit",
    )
    fun mostPlayedArtists(
        sourceId: Long,
        limit: Int,
    ): Flow<List<Artist>>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId " +
            "ORDER BY created DESC, name COLLATE NOCASE, id LIMIT :limit",
    )
    fun recentlyAddedAlbums(
        sourceId: Long,
        limit: Int,
    ): Flow<List<Album>>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId AND played IS NOT NULL AND played < :cutoff " +
            "ORDER BY played ASC, name COLLATE NOCASE, id LIMIT :limit",
    )
    fun rediscoverAlbums(
        sourceId: Long,
        cutoff: Long,
        limit: Int,
    ): Flow<List<Album>>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId AND songs.starred IS NOT NULL " +
            "ORDER BY songs.starred DESC, songs.title COLLATE NOCASE, songs.id LIMIT :limit",
    )
    fun recentlyStarredSongs(
        sourceId: Long,
        limit: Int,
    ): Flow<List<AlbumSongItem>>

    @Query(
        "SELECT song_genres.genre FROM song_genres " +
            "JOIN songs ON songs.sourceId = song_genres.sourceId AND songs.id = song_genres.songId " +
            "WHERE song_genres.sourceId = :sourceId " +
            "GROUP BY song_genres.genre ORDER BY SUM(songs.playCount) DESC, song_genres.genre COLLATE NOCASE",
    )
    fun genresByMostPlayed(sourceId: Long): Flow<List<String>>

    @Query(
        "SELECT DISTINCT (year / 10) * 10 AS decade FROM albums " +
            "WHERE sourceId = :sourceId AND year IS NOT NULL AND year > 0 ORDER BY decade ASC",
    )
    fun decades(sourceId: Long): Flow<List<Long>>

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
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_NAME_REVERSED")
    fun albumsByNameReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_ARTIST")
    fun albumsByArtist(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_ARTIST_REVERSED")
    fun albumsByArtistReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_YEAR")
    fun albumsByYear(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_YEAR_REVERSED")
    fun albumsByYearReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_ADDED")
    fun albumsByRecentlyAdded(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_ADDED_REVERSED")
    fun albumsByRecentlyAddedReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_STARRED")
    fun albumsByStarred(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_STARRED_REVERSED")
    fun albumsByStarredReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_FREQUENT")
    fun albumsByFrequent(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_FREQUENT_REVERSED")
    fun albumsByFrequentReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_RECENT")
    fun albumsByRecent(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * $ALBUMS_FILTER ORDER BY $ALBUM_ORDER_BY_RECENT_REVERSED")
    fun albumsByRecentReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("$ARTISTS_SELECT $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_NAME")
    fun artistsByName(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Artist>

    @Query("$ARTISTS_SELECT $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_NAME_REVERSED")
    fun artistsByNameReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Artist>

    @Query("$ARTISTS_SELECT $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_ALBUM_COUNT")
    fun artistsByAlbumCount(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Artist>

    @Query("$ARTISTS_SELECT $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_ALBUM_COUNT_REVERSED")
    fun artistsByAlbumCountReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Artist>

    @Query("$ARTISTS_SELECT $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_STARRED")
    fun artistsByStarred(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Artist>

    @Query("$ARTISTS_SELECT $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_STARRED_REVERSED")
    fun artistsByStarredReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Artist>

    @Query("$ARTISTS_SELECT $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_FREQUENT")
    fun artistsByFrequent(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Artist>

    @Query("$ARTISTS_SELECT $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_FREQUENT_REVERSED")
    fun artistsByFrequentReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Artist>

    @Query("$ARTISTS_SELECT $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_RECENT")
    fun artistsByRecent(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Artist>

    @Query("$ARTISTS_SELECT $ARTISTS_FILTER ORDER BY $ARTIST_ORDER_BY_RECENT_REVERSED")
    fun artistsByRecentReversed(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Artist>

    @Query("$PLAYLISTS_SELECT $PLAYLISTS_FILTER ORDER BY $PLAYLIST_ORDER_BY_NAME")
    fun playlistsByName(
        sourceId: Long,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Playlist>

    @Query("$PLAYLISTS_SELECT $PLAYLISTS_FILTER ORDER BY $PLAYLIST_ORDER_BY_NAME_REVERSED")
    fun playlistsByNameReversed(
        sourceId: Long,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Playlist>

    @Query("$PLAYLISTS_SELECT $PLAYLISTS_FILTER ORDER BY $PLAYLIST_ORDER_BY_ADDED")
    fun playlistsByAdded(
        sourceId: Long,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Playlist>

    @Query("$PLAYLISTS_SELECT $PLAYLISTS_FILTER ORDER BY $PLAYLIST_ORDER_BY_ADDED_REVERSED")
    fun playlistsByAddedReversed(
        sourceId: Long,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Playlist>

    @Query("$PLAYLISTS_SELECT $PLAYLISTS_FILTER ORDER BY $PLAYLIST_ORDER_BY_UPDATED")
    fun playlistsByUpdated(
        sourceId: Long,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Playlist>

    @Query("$PLAYLISTS_SELECT $PLAYLISTS_FILTER ORDER BY $PLAYLIST_ORDER_BY_UPDATED_REVERSED")
    fun playlistsByUpdatedReversed(
        sourceId: Long,
        search: String,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Playlist>

    @Query("SELECT * $ALBUMS_FILTER AND played IS NOT NULL AND played < :cutoff ORDER BY played ASC, name COLLATE NOCASE, id")
    fun albumsByRediscover(
        sourceId: Long,
        starredFilter: Int,
        search: String,
        cutoff: Long,
        downloadedFilter: Int = 0,
    ): PagingSource<Int, Album>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId AND played IS NOT NULL ORDER BY played DESC, name COLLATE NOCASE, id")
    fun homeRecentlyPlayedAlbums(sourceId: Long): PagingSource<Int, Album>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId AND playCount > 0 ORDER BY playCount DESC, name COLLATE NOCASE, id")
    fun homeMostPlayedAlbums(sourceId: Long): PagingSource<Int, Album>

    @Query("SELECT * FROM albums WHERE sourceId = :sourceId ORDER BY created DESC, name COLLATE NOCASE, id")
    fun homeRecentlyAddedAlbums(sourceId: Long): PagingSource<Int, Album>

    @Query("SELECT * FROM artists WHERE sourceId = :sourceId AND played IS NOT NULL ORDER BY played DESC, name COLLATE NOCASE, id")
    fun homeRecentlyPlayedArtists(sourceId: Long): PagingSource<Int, Artist>

    @Query("SELECT * FROM artists WHERE sourceId = :sourceId AND playCount > 0 ORDER BY playCount DESC, name COLLATE NOCASE, id")
    fun homeMostPlayedArtists(sourceId: Long): PagingSource<Int, Artist>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId AND songs.starred IS NOT NULL " +
            "ORDER BY songs.starred DESC, songs.title COLLATE NOCASE, songs.id",
    )
    fun starredSongs(sourceId: Long): PagingSource<Int, AlbumSongItem>

    @Query(
        "SELECT songs.id FROM songs WHERE sourceId = :sourceId AND starred IS NOT NULL " +
            "ORDER BY starred DESC, title COLLATE NOCASE, id",
    )
    suspend fun starredSongIds(sourceId: Long): List<String>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "JOIN song_downloads d ON d.sourceId = songs.sourceId AND d.songId = songs.id " +
            "WHERE songs.sourceId = :sourceId AND d.status = 'Completed' " +
            "ORDER BY songs.title COLLATE NOCASE, songs.id",
    )
    fun downloadedSongs(sourceId: Long): Flow<List<AlbumSongItem>>

    @Query(
        "SELECT songs.id FROM songs " +
            "JOIN song_downloads d ON d.sourceId = songs.sourceId AND d.songId = songs.id " +
            "WHERE songs.sourceId = :sourceId AND d.status = 'Completed' " +
            "ORDER BY songs.title COLLATE NOCASE, songs.id",
    )
    suspend fun downloadedSongIds(sourceId: Long): List<String>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "JOIN song_downloads d ON d.sourceId = songs.sourceId AND d.songId = songs.id " +
            "WHERE songs.sourceId = :sourceId AND d.status = 'Completed' " +
            "ORDER BY songs.title COLLATE NOCASE, songs.id",
    )
    fun homeDownloadedSongs(sourceId: Long): PagingSource<Int, AlbumSongItem>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM song_genres " +
            "JOIN songs ON songs.sourceId = song_genres.sourceId AND songs.id = song_genres.songId " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE song_genres.sourceId = :sourceId AND song_genres.genre = :genre " +
            "ORDER BY songs.title COLLATE NOCASE, songs.id",
    )
    fun songsByGenre(
        sourceId: Long,
        genre: String,
    ): PagingSource<Int, AlbumSongItem>

    @Query(
        "SELECT songs.id FROM song_genres " +
            "JOIN songs ON songs.sourceId = song_genres.sourceId AND songs.id = song_genres.songId " +
            "WHERE song_genres.sourceId = :sourceId AND song_genres.genre = :genre " +
            "ORDER BY songs.title COLLATE NOCASE, songs.id",
    )
    suspend fun songsByGenreIds(
        sourceId: Long,
        genre: String,
    ): List<String>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId AND year >= :start AND year < :end " +
            "ORDER BY year DESC, name COLLATE NOCASE, id",
    )
    fun albumsByDecade(
        sourceId: Long,
        start: Long,
        end: Long,
    ): PagingSource<Int, Album>

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

    @Query("SELECT genre FROM song_genres WHERE sourceId = :sourceId AND songId = :songId ORDER BY position")
    fun songGenres(
        sourceId: Long,
        songId: String,
    ): Flow<List<String>>

    @Query(
        "SELECT DISTINCT song_genres.genre FROM song_genres " +
            "JOIN songs ON songs.sourceId = song_genres.sourceId AND songs.id = song_genres.songId " +
            "WHERE songs.sourceId = :sourceId AND songs.albumId = :albumId " +
            "ORDER BY song_genres.genre COLLATE NOCASE",
    )
    fun albumGenres(
        sourceId: Long,
        albumId: String,
    ): Flow<List<String>>

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
        "SELECT songs.* FROM songs " +
            "JOIN song_downloads sd ON sd.sourceId = songs.sourceId AND sd.songId = songs.id AND sd.status = 'Completed' " +
            "WHERE songs.sourceId = :sourceId AND songs.albumId = :albumId " +
            "ORDER BY songs.disc, songs.track, songs.id",
    )
    fun songsByAlbumDownloaded(
        sourceId: Long,
        albumId: String,
    ): Flow<List<Song>>

    @Query("$PLAYLIST_SONGS_SELECT AND $DOWNLOADED_SONG$PLAYLIST_SONGS_ORDER")
    fun playlistSongsDownloaded(
        sourceId: Long,
        playlistId: String,
    ): PagingSource<Int, PlaylistSongItem>

    @Query(
        "SELECT * FROM albums WHERE sourceId = :sourceId AND artistId = :artistId " +
            "AND EXISTS (SELECT 1 FROM songs JOIN song_downloads sd ON sd.sourceId = songs.sourceId AND sd.songId = songs.id " +
            "WHERE songs.sourceId = albums.sourceId AND songs.albumId = albums.id AND sd.status = 'Completed') " +
            "ORDER BY year DESC, name COLLATE NOCASE, id",
    )
    fun albumsForArtistDownloaded(
        sourceId: Long,
        artistId: String,
    ): Flow<List<Album>>
}
