package com.subtracks.data.repo

import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.ArtworkSeed

interface ArtworkSeedStore {
    suspend fun seed(cacheKey: String): Pair<Int, Int?>?

    suspend fun save(
        cacheKey: String,
        seeds: Pair<Int, Int?>,
    )
}

class ArtworkSeedRepository(
    private val db: SubtracksDatabase,
) : ArtworkSeedStore {
    override suspend fun seed(cacheKey: String): Pair<Int, Int?>? {
        val row = db.artworkSeedDao().seed(cacheKey) ?: return null
        return row.primary to row.secondary
    }

    override suspend fun save(
        cacheKey: String,
        seeds: Pair<Int, Int?>,
    ) {
        db.artworkSeedDao().upsert(ArtworkSeed(cacheKey, seeds.first, seeds.second))
    }
}
