package com.subtracks.data.db

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.subtracks.data.model.ArtworkSeed

@Dao
interface ArtworkSeedDao {
    @Query("SELECT * FROM artwork_seeds WHERE cacheKey = :cacheKey")
    suspend fun seed(cacheKey: String): ArtworkSeed?

    @Upsert
    suspend fun upsert(seed: ArtworkSeed)

    @Query(
        "DELETE FROM artwork_seeds WHERE cacheKey NOT IN " +
            "(SELECT cacheKey FROM artwork_seeds ORDER BY storedAt DESC LIMIT :keep)",
    )
    suspend fun prune(keep: Int)
}
