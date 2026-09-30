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
class SyncServiceIntegrationTest {
    @Test
    fun syncMirrorsTheLibraryIntoRoomForEveryServer() =
        runBlocking {
            servers.forEach { server ->
                val context = ApplicationProvider.getApplicationContext<Context>()
                val db =
                    Room
                        .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                        .setDriver(BundledSQLiteDriver())
                        .build()
                try {
                    db.sourcesDao().upsertSource(
                        Source(id = 1, name = server.name, address = server.baseUrl, isActive = true, createdAt = 0),
                    )
                    SyncService(db, TestServers.source(server)).sync()

                    val library = db.libraryDao()
                    assertEquals(server.name, 2, library.artistIds(1).size)
                    assertEquals(server.name, 3, library.albumIds(1).size)
                    assertEquals(server.name, 20, library.songIdsAfter(1, "", 1000).size)
                    assertEquals(server.name, 1, library.playlistIds(1).size)
                } finally {
                    db.close()
                }
            }
        }

    private companion object {
        val servers = TestServers.all
    }
}
