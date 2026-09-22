package com.subtracks.data.repo

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SourceRepositoryTest {
    private lateinit var db: SubtracksDatabase
    private lateinit var repository: SourceRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        repository = SourceRepository(db, OkHttpClient())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addingASourceMakesItTheSingleActiveOne() =
        runTest {
            repository.addSource("first", "http://a.example", "u", "p", true)
            val firstId = repository.activeSourceId().first()

            repository.addSource("second", "http://b.example", "u", "p", true)

            assertEquals(1, repository.sources().first().count { it.isActive })
            assertEquals("second", repository.activeConfig().first()?.name)
            assertEquals(
                "http://a.example/",
                repository
                    .sources()
                    .first()
                    .first { it.id == firstId }
                    .address,
            )
        }
}
