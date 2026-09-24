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
        val dao = db.artworkSeedDao()
        val now = System.currentTimeMillis()
        dao.upsert(ArtworkSeed(cacheKey, seeds.first, seeds.second, now))
        dao.pruneExpired(now - MAX_AGE_MS)
    }

    private companion object {
        const val MAX_AGE_MS = 30L * 24 * 60 * 60 * 1000
    }
}
