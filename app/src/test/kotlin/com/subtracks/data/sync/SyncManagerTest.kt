package com.subtracks.data.sync

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SyncManagerTest {
    private lateinit var db: SubtracksDatabase
    private lateinit var sourceRepository: SourceRepository
    private lateinit var queueRepository: QueueRepository
    private lateinit var manager: SyncManager

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        sourceRepository = SourceRepository(db, OkHttpClient(), fakeUserPreferences(), ArtworkStore(File(context.cacheDir, "art")))
        queueRepository = QueueRepository(db)
        manager = SyncManager(db, sourceRepository, queueRepository)
    }

    @After
    fun tearDown() {
        sourceRepository.close()
        db.close()
    }

    @Test
    fun aFailedSyncStillInvalidatesTheLibraryCache() =
        runBlocking {
            val before = queueRepository.snapshot().version

            manager.requestSync()
            val status = withTimeout(10_000) { manager.status.first { it is SyncStatus.Failed } }

            assertTrue(status is SyncStatus.Failed)
            assertEquals(before + 1, queueRepository.snapshot().version)
        }
}
