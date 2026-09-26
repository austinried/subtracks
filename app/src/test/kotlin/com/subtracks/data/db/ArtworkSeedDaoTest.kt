package com.subtracks.data.db

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.ArtworkSeed
import com.subtracks.data.model.Source
import com.subtracks.data.repo.ArtworkSeedRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArtworkSeedDaoTest {
    private lateinit var db: SubtracksDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun deletingASourceCascadesToItsSeeds() =
        runTest {
            val sourceId = source()
            val dao = db.artworkSeedDao()
            dao.upsert(ArtworkSeed(cacheKey = "$sourceId:cover:false", sourceId = sourceId, primary = 1, secondary = null))

            db.sourcesDao().deleteSource(sourceId)

            assertNull(dao.seed("$sourceId:cover:false"))
        }

    @Test
    fun pruningKeepsTheMostRecentlyStoredSeeds() =
        runTest {
            val sourceId = source()
            val dao = db.artworkSeedDao()
            (1..5).forEach { index ->
                dao.upsert(
                    ArtworkSeed(
                        cacheKey = "$sourceId:cover$index:false",
                        sourceId = sourceId,
                        primary = index,
                        secondary = null,
                        storedAt = index.toLong(),
                    ),
                )
            }

            dao.prune(keep = 2)

            val remaining = (1..5).mapNotNull { dao.seed("$sourceId:cover$it:false")?.cacheKey }.toSet()
            assertEquals(setOf("$sourceId:cover5:false", "$sourceId:cover4:false"), remaining)
        }

    @Test
    fun savingDerivesTheSourceFromTheCacheKey() =
        runTest {
            val sourceId = source()
            val repository = ArtworkSeedRepository(db)

            repository.save(ArtworkSeed(cacheKey = "$sourceId:cover:true", primary = 10, secondary = 20))

            val row = db.artworkSeedDao().seed("$sourceId:cover:true")
            assertEquals(sourceId, row?.sourceId)
            assertEquals(10, row?.primary)
            assertEquals(20, row?.secondary)
        }

    @Test
    fun savingBeyondTheCapPrunesTheOldestSeeds() =
        runTest {
            val sourceId = source()
            val repository = ArtworkSeedRepository(db, maxStoredSeeds = 2, pruneEverySaves = 1)

            repository.save(ArtworkSeed(cacheKey = "$sourceId:cover1:false", primary = 1, secondary = null))
            repository.save(ArtworkSeed(cacheKey = "$sourceId:cover2:false", primary = 2, secondary = null))
            repository.save(ArtworkSeed(cacheKey = "$sourceId:cover3:false", primary = 3, secondary = null))

            val stored = (1..3).count { db.artworkSeedDao().seed("$sourceId:cover$it:false") != null }
            assertEquals(2, stored)
        }

    private suspend fun source(): Long =
        db.sourcesDao().insertSource(Source(name = "Navidrome", address = "http://localhost", isActive = true, createdAt = 0))
}
