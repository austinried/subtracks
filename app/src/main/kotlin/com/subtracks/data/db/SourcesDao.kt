package com.subtracks.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Upsert
import com.subtracks.data.model.Source
import com.subtracks.data.model.SubsonicConfig
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

    @Query("SELECT id FROM sources WHERE isActive = 1 LIMIT 1")
    suspend fun activeSourceIdOnce(): Long?

    @Query("SELECT id FROM sources ORDER BY createdAt LIMIT 1")
    suspend fun firstSourceId(): Long?

    @Insert
    suspend fun insertSource(source: Source): Long

    @Upsert
    suspend fun upsertSource(source: Source)

    @Upsert
    suspend fun upsertSubsonicSource(subsonic: SubsonicSource)

    @Query("UPDATE subsonic_sources SET useTokenAuth = 0 WHERE sourceId = :sourceId")
    suspend fun disableTokenAuth(sourceId: Long)

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun deleteSourceRow(id: Long)

    suspend fun deleteSource(id: Long) = deleteSourceRow(id)

    @Query("UPDATE sources SET isActive = (id = :sourceId)")
    suspend fun setActiveSource(sourceId: Long)

    @Query(
        "SELECT s.id AS id, s.name AS name, s.address AS address, c.username AS username, " +
            "c.password AS password, c.useTokenAuth AS useTokenAuth " +
            "FROM sources s JOIN subsonic_sources c ON c.sourceId = s.id " +
            "WHERE s.isActive = 1 LIMIT 1",
    )
    fun activeSubsonicConfig(): Flow<SubsonicConfig?>

    @Query(
        "SELECT s.id AS id, s.name AS name, s.address AS address, c.username AS username, " +
            "c.password AS password, c.useTokenAuth AS useTokenAuth " +
            "FROM sources s JOIN subsonic_sources c ON c.sourceId = s.id " +
            "WHERE s.isActive = 1 LIMIT 1",
    )
    suspend fun activeSubsonicConfigOnce(): SubsonicConfig?
}
