package com.subtracks.data.db

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.subtracks.data.model.AppSettings
import com.subtracks.data.model.Source
import com.subtracks.data.model.SubsonicSource
import kotlinx.coroutines.flow.Flow

@Dao
interface SourcesDao {
    @Query("SELECT * FROM sources ORDER BY createdAt")
    fun sources(): Flow<List<Source>>

    @Query("SELECT * FROM sources WHERE id = :id")
    fun source(id: Long): Flow<Source?>

    @Query("SELECT id FROM sources WHERE isActive = 1 LIMIT 1")
    fun activeSourceId(): Flow<Long?>

    @Query("SELECT * FROM subsonic_sources WHERE sourceId = :sourceId")
    fun subsonicSource(sourceId: Long): Flow<SubsonicSource?>

    @Upsert
    suspend fun upsertSource(source: Source)

    @Upsert
    suspend fun upsertSubsonicSource(subsonic: SubsonicSource)

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun deleteSourceRow(id: Long)

    @Query("DELETE FROM search_index WHERE sourceId = :sourceId")
    suspend fun clearSearchIndex(sourceId: String)

    @Transaction
    suspend fun deleteSource(id: Long) {
        deleteSourceRow(id)
        clearSearchIndex(id.toString())
    }

    @Query("UPDATE sources SET isActive = (id = :sourceId)")
    suspend fun setActiveSource(sourceId: Long)

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun appSettings(): Flow<AppSettings?>

    @Upsert
    suspend fun upsertAppSettings(settings: AppSettings)
}
