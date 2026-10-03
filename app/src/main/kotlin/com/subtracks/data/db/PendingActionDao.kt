package com.subtracks.data.db

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.subtracks.data.model.PendingAction

@Dao
interface PendingActionDao {
    @Query("SELECT * FROM pending_actions WHERE sourceId = :sourceId ORDER BY id")
    suspend fun pending(sourceId: Long): List<PendingAction>

    @Insert
    suspend fun insert(action: PendingAction): Long

    @Query("DELETE FROM pending_actions WHERE id = :id")
    suspend fun delete(id: Long)

    @Query(
        "DELETE FROM pending_actions WHERE sourceId = :sourceId " +
            "AND kind IN ('Star', 'Unstar') AND starType = :starType AND targetId = :targetId",
    )
    suspend fun clearStar(
        sourceId: Long,
        starType: String,
        targetId: String,
    )
}
