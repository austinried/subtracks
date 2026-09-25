package com.subtracks.data.repo

import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.ArtworkSeed

private const val MAX_STORED_SEEDS = 1024

interface ArtworkSeedStore {
    suspend fun seed(cacheKey: String): ArtworkSeed?

    suspend fun save(seed: ArtworkSeed)
}

class ArtworkSeedRepository(
    private val db: SubtracksDatabase,
    private val maxStoredSeeds: Int = MAX_STORED_SEEDS,
) : ArtworkSeedStore {
    override suspend fun seed(cacheKey: String): ArtworkSeed? {
        val dao = db.artworkSeedDao()
        val row = dao.seed(cacheKey) ?: return null
        dao.touch(cacheKey, System.currentTimeMillis())
        return row
    }

    override suspend fun save(seed: ArtworkSeed) {
        val sourceId = seed.cacheKey.substringBefore(':').toLongOrNull() ?: return
        val dao = db.artworkSeedDao()
        dao.upsert(seed.copy(sourceId = sourceId, lastUsed = System.currentTimeMillis()))
        dao.prune(maxStoredSeeds)
    }
}
