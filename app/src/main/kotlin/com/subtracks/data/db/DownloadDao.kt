package com.subtracks.data.db

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.subtracks.data.model.DownloadArtwork
import com.subtracks.data.model.DownloadedSong
import com.subtracks.data.model.EntityDownloadStatus
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.SourceCoverArt
import kotlinx.coroutines.flow.Flow

// A song is attributed to its album artist and to its credited track artist, so a guest artist's
// downloads are visible even though they lead no albums of their own.
// Starts from songs so status totals include every song the artist is credited on.
internal const val ARTIST_DOWNLOAD_ROWS =
    "SELECT credits.artistId AS id, credits.songId AS songId, song_downloads.status AS status " +
        "FROM (" + CREDITED_SONG_ROWS + ") credits " +
        "LEFT JOIN song_downloads ON song_downloads.sourceId = :sourceId " +
        "AND song_downloads.songId = credits.songId"

// Starts from downloads so lists and filters over partially downloaded libraries do not scan songs.
// Kept hand-written for that plan.
internal const val DOWNLOADED_ARTIST_ROWS =
    "SELECT albums.artistId AS id, song_downloads.songId AS songId, " +
        "song_downloads.downloadedAt AS downloadedAt, song_downloads.status AS status " +
        "FROM song_downloads " +
        "JOIN songs ON songs.sourceId = song_downloads.sourceId AND songs.id = song_downloads.songId " +
        "JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "WHERE song_downloads.sourceId = :sourceId AND albums.artistId IS NOT NULL " +
        "UNION ALL " +
        "SELECT album_artists.artistId AS id, song_downloads.songId AS songId, " +
        "song_downloads.downloadedAt AS downloadedAt, song_downloads.status AS status " +
        "FROM song_downloads " +
        "JOIN songs ON songs.sourceId = song_downloads.sourceId AND songs.id = song_downloads.songId " +
        "JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
        "JOIN album_artists ON album_artists.sourceId = albums.sourceId AND album_artists.albumId = albums.id " +
        "WHERE song_downloads.sourceId = :sourceId " +
        "UNION ALL " +
        "SELECT songs.artistId AS id, song_downloads.songId AS songId, " +
        "song_downloads.downloadedAt AS downloadedAt, song_downloads.status AS status " +
        "FROM song_downloads " +
        "JOIN songs ON songs.sourceId = song_downloads.sourceId AND songs.id = song_downloads.songId " +
        "WHERE song_downloads.sourceId = :sourceId AND songs.artistId IS NOT NULL " +
        "UNION ALL " +
        "SELECT song_artists.artistId AS id, song_downloads.songId AS songId, " +
        "song_downloads.downloadedAt AS downloadedAt, song_downloads.status AS status " +
        "FROM song_downloads " +
        "JOIN songs ON songs.sourceId = song_downloads.sourceId AND songs.id = song_downloads.songId " +
        "JOIN song_artists ON song_artists.sourceId = songs.sourceId AND song_artists.songId = songs.id " +
        "WHERE song_downloads.sourceId = :sourceId"

@Dao
interface DownloadDao {
    @Query("SELECT * FROM song_downloads WHERE sourceId = :sourceId")
    fun downloads(sourceId: Long): Flow<List<SongDownload>>

    @Query("SELECT * FROM song_downloads WHERE status IN ('Queued', 'Running') AND (engineId IS NOT NULL OR sourceId = :sourceId)")
    fun activeDownloads(sourceId: Long): Flow<List<SongDownload>>

    @Query("SELECT * FROM song_downloads ORDER BY rowid")
    suspend fun all(): List<SongDownload>

    @Query(
        "SELECT COALESCE((SELECT songCount FROM artists " +
            "WHERE sourceId = :sourceId AND id = :artistId), 0) AS total, " +
            "COALESCE(statuses.downloaded, 0) AS downloaded, " +
            "COALESCE(statuses.downloading, 0) AS downloading " +
            "FROM (SELECT " +
            "COUNT(DISTINCT CASE WHEN status = 'Completed' THEN songId END) AS downloaded, " +
            "COUNT(DISTINCT CASE WHEN status IN ('Queued', 'Running') THEN songId END) AS downloading " +
            "FROM (" + DOWNLOADED_ARTIST_ROWS + ") " +
            "WHERE id = :artistId AND status IN ('Completed', 'Queued', 'Running')) statuses",
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
        "SELECT songs.albumId AS id, " +
            "COUNT(songs.id) AS total, " +
            "COALESCE(SUM(CASE WHEN song_downloads.status = 'Completed' THEN 1 ELSE 0 END), 0) AS downloaded, " +
            "COALESCE(SUM(CASE WHEN song_downloads.status IN ('Queued', 'Running') THEN 1 ELSE 0 END), 0) AS downloading " +
            "FROM songs " +
            "LEFT JOIN song_downloads ON song_downloads.sourceId = songs.sourceId AND song_downloads.songId = songs.id " +
            "WHERE songs.sourceId = :sourceId AND songs.albumId IS NOT NULL " +
            "GROUP BY songs.albumId " +
            "HAVING downloaded > 0 OR downloading > 0",
    )
    fun albumStatuses(sourceId: Long): Flow<List<EntityDownloadStatus>>

    @Query(
        "SELECT statuses.id, artists.songCount AS total, statuses.downloaded, statuses.downloading " +
            "FROM (SELECT id, " +
            "COUNT(DISTINCT CASE WHEN status = 'Completed' THEN songId END) AS downloaded, " +
            "COUNT(DISTINCT CASE WHEN status IN ('Queued', 'Running') THEN songId END) AS downloading " +
            "FROM (" + DOWNLOADED_ARTIST_ROWS + ") " +
            "WHERE status IN ('Completed', 'Queued', 'Running') " +
            "GROUP BY id) statuses " +
            "JOIN artists ON artists.sourceId = :sourceId AND artists.id = statuses.id " +
            "WHERE statuses.downloaded > 0 OR statuses.downloading > 0",
    )
    fun artistStatuses(sourceId: Long): Flow<List<EntityDownloadStatus>>

    @Query(
        "SELECT playlist_songs.playlistId AS id, " +
            "COUNT(DISTINCT songs.id) AS total, " +
            "COUNT(DISTINCT CASE WHEN song_downloads.status = 'Completed' THEN songs.id END) AS downloaded, " +
            "COUNT(DISTINCT CASE WHEN song_downloads.status IN ('Queued', 'Running') THEN songs.id END) AS downloading " +
            "FROM playlist_songs " +
            "JOIN songs ON songs.sourceId = playlist_songs.sourceId AND songs.id = playlist_songs.songId " +
            "LEFT JOIN song_downloads ON song_downloads.sourceId = songs.sourceId AND song_downloads.songId = songs.id " +
            "WHERE playlist_songs.sourceId = :sourceId " +
            "GROUP BY playlist_songs.playlistId " +
            "HAVING downloaded > 0 OR downloading > 0",
    )
    fun playlistStatuses(sourceId: Long): Flow<List<EntityDownloadStatus>>

    @Query(
        "SELECT d.sourceId AS sourceId, d.songId AS songId, s.title AS title, s.albumId AS albumId, al.name AS albumName, " +
            "al.artistId AS artistId, COALESCE(ar.name, al.albumArtist) AS artistName, d.status AS status, " +
            "d.bytes AS bytes, d.total AS total, " +
            "COALESCE(NULLIF(ar.sortName, ''), NULLIF(al.albumArtistSort, ''), ar.name, al.albumArtist, '') AS artistSort, " +
            "COALESCE(NULLIF(al.sortName, ''), al.name, '') AS albumSort, " +
            "COALESCE(NULLIF(s.sortName, ''), s.title, '') AS songSort " +
            "FROM song_downloads d " +
            "JOIN songs s ON s.sourceId = d.sourceId AND s.id = d.songId " +
            "LEFT JOIN albums al ON al.sourceId = s.sourceId AND al.id = s.albumId " +
            "LEFT JOIN artists ar ON ar.sourceId = al.sourceId AND ar.id = al.artistId " +
            "WHERE d.sourceId = :sourceId ORDER BY s.disc, s.track, s.id",
    )
    fun downloadedSongs(sourceId: Long): Flow<List<DownloadedSong>>

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

    @Query(
        "SELECT DISTINCT p.coverArt AS coverArt FROM playlist_songs ps " +
            "JOIN playlists p ON p.sourceId = ps.sourceId AND p.id = ps.playlistId " +
            "WHERE ps.sourceId = :sourceId AND ps.songId = :songId AND p.coverArt IS NOT NULL",
    )
    suspend fun playlistCoversForSong(
        sourceId: Long,
        songId: String,
    ): List<String>

    @Query(
        "SELECT DISTINCT ps.sourceId AS sourceId, p.coverArt AS coverArt " +
            "FROM playlist_songs ps " +
            "JOIN playlists p ON p.sourceId = ps.sourceId AND p.id = ps.playlistId " +
            "JOIN song_downloads d ON d.sourceId = ps.sourceId AND d.songId = ps.songId " +
            "WHERE p.coverArt IS NOT NULL",
    )
    suspend fun downloadedPlaylistCovers(): List<SourceCoverArt>

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

    @Query("DELETE FROM song_downloads WHERE sourceId = :sourceId AND songId IN (:songIds)")
    suspend fun deleteSongs(
        sourceId: Long,
        songIds: Collection<String>,
    )

    @Query("DELETE FROM song_downloads WHERE sourceId = :sourceId")
    suspend fun deleteSource(sourceId: Long)
}
