package com.subtracks.data.repo

import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.ArtworkSeed

interface ArtworkSeedStore {
    suspend fun seed(cacheKey: String): ArtworkSeed?

    suspend fun save(seed: ArtworkSeed)
}

class ArtworkSeedRepository(
    private val db: SubtracksDatabase,
) : ArtworkSeedStore {
    override suspend fun seed(cacheKey: String): ArtworkSeed? = db.artworkSeedDao().seed(cacheKey)

    override suspend fun save(seed: ArtworkSeed) {
        db.artworkSeedDao().upsert(seed)
    }
}
