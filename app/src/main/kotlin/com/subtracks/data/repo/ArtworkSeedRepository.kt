package com.subtracks.data.repo

import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.ArtworkSeed
import java.util.concurrent.atomic.AtomicInteger

private const val MAX_STORED_SEEDS = 10_240
private const val PRUNE_EVERY_SAVES = 128

interface ArtworkSeedStore {
    suspend fun seed(cacheKey: String): ArtworkSeed?

    suspend fun save(seed: ArtworkSeed)
}

class ArtworkSeedRepository(
    private val db: SubtracksDatabase,
    private val maxStoredSeeds: Int = MAX_STORED_SEEDS,
    private val pruneEverySaves: Int = PRUNE_EVERY_SAVES,
) : ArtworkSeedStore {
    private val savesSincePrune = AtomicInteger(0)

    override suspend fun seed(cacheKey: String): ArtworkSeed? = db.artworkSeedDao().seed(cacheKey)

    override suspend fun save(seed: ArtworkSeed) {
        val sourceId = seed.cacheKey.substringBefore(':').toLongOrNull() ?: return
        val dao = db.artworkSeedDao()
        dao.upsert(seed.copy(sourceId = sourceId, storedAt = System.currentTimeMillis()))
        if (savesSincePrune.incrementAndGet() >= pruneEverySaves) {
            savesSincePrune.set(0)
            dao.prune(maxStoredSeeds)
        }
    }
}
