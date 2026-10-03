package com.subtracks.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import com.subtracks.data.model.AlbumSongItem
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

private const val ARTIST_SONGS_SELECT =
    "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
        "JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "WHERE songs.sourceId = :sourceId AND albums.artistId = :artistId"

private const val ARTIST_SONGS_ORDER =
    " ORDER BY albums.year DESC, albums.name COLLATE NOCASE, albums.id, songs.disc, songs.track, songs.id"

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

    @Query("DELETE FROM up_next_entries")
    suspend fun clearUpNext()

    @Insert
    suspend fun insertUpNext(entries: List<UpNextEntry>)

    @Query("SELECT * FROM up_next_entries ORDER BY position, id")
    suspend fun upNextEntries(): List<UpNextEntry>

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

    @Query("SELECT COUNT(*) FROM songs WHERE sourceId = :sourceId AND id = :songId")
    suspend fun songLength(
        sourceId: Long,
        songId: String,
    ): Long

    @Query(
        "SELECT COUNT(*) FROM songs " +
            "JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId AND albums.artistId = :artistId",
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
            "JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId AND albums.artistId = :artistId" +
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
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId AND songs.id IN (:ids)",
    )
    suspend fun songsByIds(
        sourceId: Long,
        ids: List<String>,
    ): List<AlbumSongItem>

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
            "ORDER BY songs.title COLLATE NOCASE, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun genreSongs(
        sourceId: Long,
        genre: String,
        offset: Long,
        limit: Int,
    ): List<AlbumSongItem>

    @Query(
        "SELECT songs.id FROM song_genres " +
            "JOIN songs ON songs.sourceId = song_genres.sourceId AND songs.id = song_genres.songId " +
            "WHERE song_genres.sourceId = :sourceId AND song_genres.genre = :genre " +
            "ORDER BY songs.title COLLATE NOCASE, songs.id",
    )
    suspend fun genreSongIds(
        sourceId: Long,
        genre: String,
    ): List<String>

    @Query("SELECT COUNT(*) FROM song_downloads WHERE sourceId = :sourceId AND status = 'Completed'")
    suspend fun downloadedLength(sourceId: Long): Long

    @Query("SELECT COUNT(*) FROM songs WHERE sourceId = :sourceId AND starred IS NOT NULL")
    suspend fun starredLength(sourceId: Long): Long

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM songs " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE songs.sourceId = :sourceId AND songs.starred IS NOT NULL " +
            "ORDER BY songs.starred DESC, songs.title COLLATE NOCASE, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun starredSongs(
        sourceId: Long,
        offset: Long,
        limit: Int,
    ): List<AlbumSongItem>

    @Query(
        "SELECT songs.id FROM songs WHERE sourceId = :sourceId AND starred IS NOT NULL " +
            "ORDER BY starred DESC, title COLLATE NOCASE, id",
    )
    suspend fun starredSongIds(sourceId: Long): List<String>

    @Query(
        "SELECT songs.*, albums.coverArt AS coverArt FROM song_downloads d " +
            "JOIN songs ON songs.sourceId = d.sourceId AND songs.id = d.songId " +
            "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
            "WHERE d.sourceId = :sourceId AND d.status = 'Completed' " +
            "ORDER BY d.downloadedAt DESC, songs.title COLLATE NOCASE, songs.id LIMIT :limit OFFSET :offset",
    )
    suspend fun downloadedSongs(
        sourceId: Long,
        offset: Long,
        limit: Int,
    ): List<AlbumSongItem>

    @Query(
        "SELECT songs.id FROM song_downloads d " +
            "JOIN songs ON songs.sourceId = d.sourceId AND songs.id = d.songId " +
            "WHERE d.sourceId = :sourceId AND d.status = 'Completed' " +
            "ORDER BY d.downloadedAt DESC, songs.title COLLATE NOCASE, songs.id",
    )
    suspend fun downloadedSongIds(sourceId: Long): List<String>
}
