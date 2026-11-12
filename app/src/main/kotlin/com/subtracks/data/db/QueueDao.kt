package com.subtracks.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.PendingSyncOrigin
import com.subtracks.data.model.PlaybackCursor
import com.subtracks.data.model.PlaylistSongItem
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.UpNextEntry

private const val ALBUM_SONGS_SELECT =
    "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
        "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "WHERE songs.sourceId = :sourceId AND songs.albumId = :albumId"

private const val ALBUM_SONGS_ORDER = " ORDER BY songs.disc, songs.track, songs.id"

private const val ALBUM_SONGS_SQL = ALBUM_SONGS_SELECT + ALBUM_SONGS_ORDER

private const val ARTIST_SONG_IDS =
    "(SELECT songId FROM ($CREDITED_SONG_ROWS) WHERE artistId = :artistId)"

private const val ARTIST_SONGS_SELECT =
    "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
        "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "WHERE songs.sourceId = :sourceId AND songs.id IN $ARTIST_SONG_IDS"

private const val ARTIST_SONGS_ORDER =
    " ORDER BY albums.id IS NULL, albums.year DESC, albums.sortName COLLATE NOCASE, albums.id, songs.disc, songs.track, songs.id"

private const val SONG_SQL =
    "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
        "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "WHERE songs.sourceId = :sourceId AND songs.id = :songId"

@Dao
interface QueueDao {
    @Query("SELECT * FROM pending_sync_origins WHERE sourceId = :sourceId")
    suspend fun pendingSyncOrigins(sourceId: Long): List<PendingSyncOrigin>

    @Query("DELETE FROM pending_sync_origins WHERE sourceId = :sourceId")
    suspend fun clearPendingSyncOrigins(sourceId: Long)

    @Query("DELETE FROM queue_entries")
    suspend fun clear()

    @Insert
    suspend fun insert(entries: List<QueueEntry>)

    @Query("SELECT * FROM queue_entries ORDER BY position, id")
    suspend fun entries(): List<QueueEntry>

    @Query("DELETE FROM up_next_entries")
    suspend fun clearUpNext()

    @Insert
    suspend fun insertUpNext(entries: List<UpNextEntry>)

    @Query("SELECT * FROM up_next_entries ORDER BY position, id")
    suspend fun upNextEntries(): List<UpNextEntry>

    @Query("SELECT * FROM playback_cursor WHERE id = 1")
    suspend fun cursor(): PlaybackCursor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setCursor(cursor: PlaybackCursor)

    @Query(
        "SELECT COUNT(*) FROM playlist_songs " +
            "JOIN songs ON songs.sourceId = playlist_songs.sourceId AND songs.id = playlist_songs.songId " +
            "WHERE playlist_songs.sourceId = :sourceId AND playlist_songs.playlistId = :playlistId",
    )
    suspend fun playlistLength(
        sourceId: Long,
        playlistId: String,
    ): Long

    @Query("SELECT COUNT(*) FROM songs WHERE sourceId = :sourceId AND albumId = :albumId")
    suspend fun albumLength(
        sourceId: Long,
        albumId: String,
    ): Long

    @Query("SELECT COUNT(*) FROM songs WHERE sourceId = :sourceId AND id = :songId")
    suspend fun songLength(
        sourceId: Long,
        songId: String,
    ): Long

    @Query(
        "SELECT COUNT(*) FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId AND songs.id IN $ARTIST_SONG_IDS",
    )
    suspend fun artistLength(
        sourceId: Long,
        artistId: String,
    ): Long

    @Query("$PLAYLIST_SONGS_SQL LIMIT :limit OFFSET :offset")
    suspend fun playlistSongs(
        sourceId: Long,
        playlistId: String,
        offset: Long,
        limit: Int,
    ): List<PlaylistSongItem>

    @Query(
        "$PLAYLIST_SONGS_SELECT AND playlist_songs.position >= :position$PLAYLIST_SONGS_ORDER " +
            "LIMIT :limit OFFSET :skip",
    )
    suspend fun playlistSongsFrom(
        sourceId: Long,
        playlistId: String,
        position: Long,
        skip: Long,
        limit: Int,
    ): List<PlaylistSongItem>

    @Query(
        "$PLAYLIST_SONGS_SELECT AND playlist_songs.position < :position " +
            "ORDER BY playlist_songs.position DESC LIMIT :limit OFFSET :skip",
    )
    suspend fun playlistSongsBefore(
        sourceId: Long,
        playlistId: String,
        position: Long,
        skip: Long,
        limit: Int,
    ): List<PlaylistSongItem>

    @Query("$ALBUM_SONGS_SQL LIMIT :limit OFFSET :offset")
    suspend fun albumSongs(
        sourceId: Long,
        albumId: String,
        offset: Long,
        limit: Int,
    ): List<AlbumSongItem>

    @Query(
        "$ALBUM_SONGS_SELECT AND (songs.disc, songs.track, songs.id) >= (:disc, :track, :id)" +
            "$ALBUM_SONGS_ORDER LIMIT :limit OFFSET :skip",
    )
    suspend fun albumSongsFrom(
        sourceId: Long,
        albumId: String,
        disc: Long,
        track: Long,
        id: String,
        skip: Long,
        limit: Int,
    ): List<AlbumSongItem>

    // Row-value `<` is null-intolerant, and NULL disc/track sort first, so a plain
    // `(disc, track, id) < (...)` would drop exactly the untagged tracks a backward window needs.
    @Query(
        "$ALBUM_SONGS_SELECT AND (" +
            "songs.disc IS NULL OR songs.disc < :disc OR " +
            "(songs.disc = :disc AND (songs.track IS NULL OR songs.track < :track OR " +
            "(songs.track = :track AND songs.id < :id)))) " +
            "ORDER BY songs.disc DESC, songs.track DESC, songs.id DESC LIMIT :limit OFFSET :skip",
    )
    suspend fun albumSongsBefore(
        sourceId: Long,
        albumId: String,
        disc: Long,
        track: Long,
        id: String,
        skip: Long,
        limit: Int,
    ): List<AlbumSongItem>

    @Query("$ARTIST_SONGS_SELECT$ARTIST_SONGS_ORDER LIMIT :limit OFFSET :offset")
    suspend fun artistSongs(
        sourceId: Long,
        artistId: String,
        offset: Long,
        limit: Int,
    ): List<AlbumSongItem>

    @Query("$SONG_SQL LIMIT :limit OFFSET :offset")
    suspend fun song(
        sourceId: Long,
        songId: String,
        offset: Long,
        limit: Int,
    ): List<AlbumSongItem>

    @Query(
        "SELECT songs.id FROM songs WHERE songs.sourceId = :sourceId AND songs.albumId = :albumId " +
            "ORDER BY songs.disc, songs.track, songs.id",
    )
    suspend fun albumSongIds(
        sourceId: Long,
        albumId: String,
    ): List<String>

    @Query(
        "SELECT songs.id FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId AND songs.id IN $ARTIST_SONG_IDS" +
            ARTIST_SONGS_ORDER,
    )
    suspend fun artistSongIds(
        sourceId: Long,
        artistId: String,
    ): List<String>

    @Query(
        "SELECT songs.id FROM playlist_songs " +
            "JOIN songs ON songs.sourceId = playlist_songs.sourceId AND songs.id = playlist_songs.songId " +
            "WHERE playlist_songs.sourceId = :sourceId AND playlist_songs.playlistId = :playlistId " +
            "ORDER BY playlist_songs.position",
    )
    suspend fun playlistSongIds(
        sourceId: Long,
        playlistId: String,
    ): List<String>

    @Query(
        "SELECT CASE WHEN EXISTS (SELECT 1 FROM songs WHERE sourceId = :sourceId AND albumId = :albumId AND id = :songId) " +
            "THEN (SELECT COUNT(*) FROM songs ranked CROSS JOIN songs target " +
            "WHERE ranked.sourceId = :sourceId AND ranked.albumId = :albumId " +
            "AND target.sourceId = :sourceId AND target.albumId = :albumId AND target.id = :songId " +
            "AND ((ranked.disc IS NULL AND target.disc IS NOT NULL) OR ranked.disc < target.disc OR " +
            "(ranked.disc IS target.disc AND ((ranked.track IS NULL AND target.track IS NOT NULL) OR ranked.track < target.track OR " +
            "(ranked.track IS target.track AND ranked.id < target.id))))) END",
    )
    suspend fun albumRank(
        sourceId: Long,
        albumId: String,
        songId: String,
    ): Long?

    @Query(
        "SELECT CASE WHEN firstPosition IS NULL THEN NULL ELSE " +
            "(SELECT COUNT(*) - 1 FROM playlist_songs " +
            "JOIN songs ON songs.sourceId = playlist_songs.sourceId AND songs.id = playlist_songs.songId " +
            "WHERE playlist_songs.sourceId = :sourceId AND playlist_songs.playlistId = :playlistId AND position <= firstPosition) END " +
            "FROM (SELECT MIN(position) AS firstPosition FROM playlist_songs " +
            "JOIN songs ON songs.sourceId = playlist_songs.sourceId AND songs.id = playlist_songs.songId " +
            "WHERE playlist_songs.sourceId = :sourceId AND playlist_songs.playlistId = :playlistId AND songId = :songId)",
    )
    suspend fun playlistRank(
        sourceId: Long,
        playlistId: String,
        songId: String,
    ): Long?

    @Query(
        "SELECT COUNT(*) FROM playlist_songs " +
            "JOIN songs ON songs.sourceId = playlist_songs.sourceId AND songs.id = playlist_songs.songId " +
            "WHERE playlist_songs.sourceId = :sourceId AND playlist_songs.playlistId = :playlistId " +
            "AND playlist_songs.songId = :songId AND playlist_songs.position < :position",
    )
    suspend fun playlistOccurrenceRank(
        sourceId: Long,
        playlistId: String,
        songId: String,
        position: Long,
    ): Long

    @Query(
        "SELECT playlist_songs.position FROM playlist_songs " +
            "JOIN songs ON songs.sourceId = playlist_songs.sourceId AND songs.id = playlist_songs.songId " +
            "WHERE playlist_songs.sourceId = :sourceId AND playlist_songs.playlistId = :playlistId " +
            "AND playlist_songs.songId = :songId ORDER BY playlist_songs.position LIMIT 1 OFFSET :occurrence",
    )
    suspend fun playlistPositionForOccurrence(
        sourceId: Long,
        playlistId: String,
        songId: String,
        occurrence: Long,
    ): Long?

    @Query(
        "SELECT COUNT(*) - 1 FROM playlist_songs " +
            "JOIN songs ON songs.sourceId = playlist_songs.sourceId AND songs.id = playlist_songs.songId " +
            "WHERE playlist_songs.sourceId = :sourceId AND playlist_songs.playlistId = :playlistId " +
            "AND playlist_songs.position <= :position",
    )
    suspend fun playlistRankAtPosition(
        sourceId: Long,
        playlistId: String,
        position: Long,
    ): Long

    @Query("SELECT COUNT(*) FROM song_genres WHERE sourceId = :sourceId AND genre = :genre")
    suspend fun genreLength(
        sourceId: Long,
        genre: String,
    ): Long

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM song_genres " +
            "JOIN songs ON songs.sourceId = song_genres.sourceId AND songs.id = song_genres.songId " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE song_genres.sourceId = :sourceId AND song_genres.genre = :genre " +
            "ORDER BY songs.sortName COLLATE NOCASE, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun genreSongs(
        sourceId: Long,
        genre: String,
        offset: Long,
        limit: Int,
    ): List<AlbumSongItem>

    @Query(
        "SELECT COUNT(*) FROM song_downloads d " +
            "JOIN songs ON songs.sourceId = d.sourceId AND songs.id = d.songId " +
            "WHERE d.sourceId = :sourceId AND d.status = 'Completed'",
    )
    suspend fun downloadedLength(sourceId: Long): Long

    @Query("SELECT COUNT(*) FROM songs WHERE sourceId = :sourceId AND starred IS NOT NULL")
    suspend fun starredLength(sourceId: Long): Long

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId AND songs.starred IS NOT NULL " +
            "ORDER BY songs.starred DESC, songs.sortName COLLATE NOCASE, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun starredSongs(
        sourceId: Long,
        offset: Long,
        limit: Int,
    ): List<AlbumSongItem>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM song_downloads d " +
            "JOIN songs ON songs.sourceId = d.sourceId AND songs.id = d.songId " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE d.sourceId = :sourceId AND d.status = 'Completed' " +
            "ORDER BY d.downloadedAt DESC, songs.sortName COLLATE NOCASE, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun downloadedSongs(
        sourceId: Long,
        offset: Long,
        limit: Int,
    ): List<AlbumSongItem>
}
