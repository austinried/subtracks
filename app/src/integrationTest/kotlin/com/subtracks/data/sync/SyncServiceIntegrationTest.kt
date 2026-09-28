package com.subtracks.data.sync

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Source
import com.subtracks.data.source.subsonic.SubsonicClient
import com.subtracks.data.source.subsonic.SubsonicSource
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
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
                    val source =
                        SubsonicSource(
                            id = 1,
                            client =
                                SubsonicClient(
                                    baseUrl = server.baseUrl.toHttpUrl(),
                                    username = server.username,
                                    password = server.password,
                                    useTokenAuth = false,
                                    http = OkHttpClient(),
                                ),
                        )

                    SyncService(db, source).sync()

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

    private data class Server(
        val name: String,
        val baseUrl: String,
        val username: String,
        val password: String,
    )

    private companion object {
        val servers =
            listOf(
                Server("navidrome", "http://localhost:4533/", "admin", "password"),
                Server("gonic", "http://localhost:4747/", "admin", "admin"),
            )
    }
}
