package com.subtracks.data.db

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.subtracks.data.model.SongDownload
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM song_downloads WHERE sourceId = :sourceId")
    fun downloads(sourceId: Long): Flow<List<SongDownload>>

    @Query("SELECT * FROM song_downloads")
    suspend fun all(): List<SongDownload>

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
