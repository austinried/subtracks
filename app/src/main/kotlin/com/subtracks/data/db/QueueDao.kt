package com.subtracks.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import com.subtracks.data.model.PlaybackCursor
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.ShuffleOrder
import com.subtracks.data.model.SongListItem

private const val ALBUM_SONGS_SQL =
    "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
        "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "WHERE songs.sourceId = :sourceId AND songs.albumId = :albumId " +
        "ORDER BY songs.disc, songs.track, songs.id"

private const val SONG_SQL =
    "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
        "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "WHERE songs.sourceId = :sourceId AND songs.id = :songId"

@Dao
interface QueueDao {
    @Query("DELETE FROM queue_entries")
    suspend fun clear()

    @Insert
    suspend fun insert(entries: List<QueueEntry>)

    @Query("SELECT * FROM queue_entries ORDER BY position, id")
    suspend fun entries(): List<QueueEntry>

    @Query("SELECT * FROM shuffle_order ORDER BY sequence")
    suspend fun shuffleOrder(): List<ShuffleOrder>

    @Query("DELETE FROM shuffle_order")
    suspend fun clearShuffleOrder()

    @Insert
    suspend fun insertShuffleOrder(entries: List<ShuffleOrder>)

    @Query("SELECT * FROM playback_cursor WHERE id = 1")
    suspend fun cursor(): PlaybackCursor?

    @Upsert
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

    @Query("SELECT COUNT(*) FROM songs WHERE sourceId = :sourceId")
    suspend fun songsLength(sourceId: Long): Long

    @Query("SELECT COUNT(*) FROM songs WHERE sourceId = :sourceId AND id = :songId")
    suspend fun songLength(
        sourceId: Long,
        songId: String,
    ): Long

    @Query("$PLAYLIST_SONGS_SQL LIMIT :limit OFFSET :offset")
    suspend fun playlistSongs(
        sourceId: Long,
        playlistId: String,
        offset: Long,
        limit: Int,
    ): List<SongListItem>

    @Query("$ALBUM_SONGS_SQL LIMIT :limit OFFSET :offset")
    suspend fun albumSongs(
        sourceId: Long,
        albumId: String,
        offset: Long,
        limit: Int,
    ): List<SongListItem>

    @Query("$SONGS_SQL LIMIT :limit OFFSET :offset")
    suspend fun songs(
        sourceId: Long,
        offset: Long,
        limit: Int,
    ): List<SongListItem>

    @Query("$SONG_SQL LIMIT :limit OFFSET :offset")
    suspend fun song(
        sourceId: Long,
        songId: String,
        offset: Long,
        limit: Int,
    ): List<SongListItem>
}
