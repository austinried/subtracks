package com.subtracks.data.sync

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Source
import com.subtracks.data.source.subsonic.TestServers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MultiSourceSyncIntegrationTest {
    @Test
    fun syncsEveryServerIntoOneLibraryWithoutInterference() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val db =
                Room
                    .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                    .setDriver(BundledSQLiteDriver())
                    .build()
            try {
                TestServers.all.forEachIndexed { index, server ->
                    val id = index + 1L
                    db.sourcesDao().upsertSource(
                        Source(id = id, name = server.name, address = server.baseUrl, isActive = id == 1L, createdAt = 0),
                    )
                    SyncService(db, TestServers.source(server, id = id)).sync()
                }

                val library = db.libraryDao()
                TestServers.all.forEachIndexed { index, server ->
                    val id = index + 1L
                    assertEquals(server.name, 2, library.artistIds(id).size)
                    assertEquals(server.name, 3, library.albumIds(id).size)
                    assertEquals(server.name, 20, library.songIdsAfter(id, "", 1000).size)
                    assertEquals(server.name, 1, library.playlistIds(id).size)
                }

                SyncService(db, TestServers.source(TestServers.all.first(), id = 1)).sync()

                TestServers.all.drop(1).forEachIndexed { index, server ->
                    assertEquals("$server survived a resync of another source", 3, library.albumIds(index + 2L).size)
                }
            } finally {
                db.close()
            }
        }
}
