package com.subtracks.data.db

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.subtracks.data.model.DownloadArtwork
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.model.SongDownload
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM song_downloads WHERE sourceId = :sourceId")
    fun downloads(sourceId: Long): Flow<List<SongDownload>>

    @Query("SELECT * FROM song_downloads ORDER BY rowid")
    suspend fun all(): List<SongDownload>

    @Query(
        "SELECT COUNT(songs.id) AS total, " +
            "COALESCE(SUM(CASE WHEN song_downloads.status = 'Completed' THEN 1 ELSE 0 END), 0) AS downloaded, " +
            "COALESCE(SUM(CASE WHEN song_downloads.status IN ('Queued', 'Running') THEN 1 ELSE 0 END), 0) AS downloading " +
            "FROM songs " +
            "LEFT JOIN song_downloads ON song_downloads.sourceId = songs.sourceId AND song_downloads.songId = songs.id " +
            "WHERE songs.sourceId = :sourceId " +
            "AND songs.albumId IN (SELECT id FROM albums WHERE sourceId = :sourceId AND artistId = :artistId)",
    )
    fun artistStatus(
        sourceId: Long,
        artistId: String,
    ): Flow<ListDownloadStatus>

    @Query(
        "SELECT COUNT(DISTINCT songs.id) AS total, " +
            "COUNT(DISTINCT CASE WHEN song_downloads.status = 'Completed' THEN songs.id END) AS downloaded, " +
            "COUNT(DISTINCT CASE WHEN song_downloads.status IN ('Queued', 'Running') THEN songs.id END) AS downloading " +
            "FROM playlist_songs " +
            "JOIN songs ON songs.sourceId = playlist_songs.sourceId AND songs.id = playlist_songs.songId " +
            "LEFT JOIN song_downloads ON song_downloads.sourceId = songs.sourceId AND song_downloads.songId = songs.id " +
            "WHERE playlist_songs.sourceId = :sourceId AND playlist_songs.playlistId = :playlistId",
    )
    fun playlistStatus(
        sourceId: Long,
        playlistId: String,
    ): Flow<ListDownloadStatus>

    @Query(
        "SELECT COUNT(songs.id) AS total, " +
            "COALESCE(SUM(CASE WHEN song_downloads.status = 'Completed' THEN 1 ELSE 0 END), 0) AS downloaded, " +
            "COALESCE(SUM(CASE WHEN song_downloads.status IN ('Queued', 'Running') THEN 1 ELSE 0 END), 0) AS downloading " +
            "FROM songs " +
            "LEFT JOIN song_downloads ON song_downloads.sourceId = songs.sourceId AND song_downloads.songId = songs.id " +
            "WHERE songs.sourceId = :sourceId AND songs.albumId = :albumId",
    )
    fun albumStatus(
        sourceId: Long,
        albumId: String,
    ): Flow<ListDownloadStatus>

    @Query(
        "SELECT d.sourceId AS sourceId, s.albumId AS albumId, s.artistId AS artistId, " +
            "al.id AS albumRow, al.coverArt AS albumCoverArt, " +
            "ar.id AS artistRow, ar.coverArt AS artistCoverArt " +
            "FROM song_downloads d " +
            "LEFT JOIN songs s ON s.sourceId = d.sourceId AND s.id = d.songId " +
            "LEFT JOIN albums al ON al.sourceId = s.sourceId AND al.id = s.albumId " +
            "LEFT JOIN artists ar ON ar.sourceId = s.sourceId AND ar.id = s.artistId",
    )
    suspend fun artwork(): List<DownloadArtwork>

    @Query("SELECT * FROM song_downloads WHERE sourceId = :sourceId AND songId = :songId")
    suspend fun find(
        sourceId: Long,
        songId: String,
    ): SongDownload?

    @Upsert
    suspend fun upsert(download: SongDownload)

    @Query("DELETE FROM song_downloads WHERE sourceId = :sourceId AND songId = :songId")
    suspend fun delete(
        sourceId: Long,
        songId: String,
    )

    @Query("DELETE FROM song_downloads WHERE sourceId = :sourceId")
    suspend fun deleteSource(sourceId: Long)
}
