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
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PruneSyncIntegrationTest {
    @Test
    fun resyncPrunesAlbumsAndSongsRemovedFromTheServer() =
        runBlocking {
            val baseUrl = requireProperty("prune.baseUrl")
            val musicDir = requireProperty("prune.musicDir")
            val removed = File(musicDir, REMOVED_ALBUM_DIR)
            check(removed.isDirectory) { "$removed is missing; the harness must prepare the prune library" }

            val context = ApplicationProvider.getApplicationContext<Context>()
            val db =
                Room
                    .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                    .setDriver(BundledSQLiteDriver())
                    .build()
            try {
                db.sourcesDao().upsertSource(
                    Source(id = 1, name = "prune", address = baseUrl, isActive = true, createdAt = 0),
                )
                val source =
                    SubsonicSource(
                        1,
                        SubsonicClient(
                            baseUrl = baseUrl.toHttpUrl(),
                            username = "admin",
                            password = "password",
                            useTokenAuth = false,
                            http = OkHttpClient(),
                        ),
                    )

                SyncService(db, source).sync()
                val library = db.libraryDao()
                assertEquals(1, library.artistIds(1).size)
                assertEquals(2, library.albumIds(1).size)
                assertEquals(8, library.songIdsAfter(1, "", 1000).size)

                removed.deleteRecursively()
                rescan(baseUrl)
                SyncService(db, source).sync()

                assertEquals(1, library.artistIds(1).size)
                assertEquals(1, library.albumIds(1).size)
                assertEquals(4, library.songIdsAfter(1, "", 1000).size)
            } finally {
                db.close()
            }
        }

    private fun rescan(baseUrl: String) {
        val http = OkHttpClient()
        val root = baseUrl.trimEnd('/')

        fun call(
            method: String,
            extra: String = "",
        ): String {
            val url = "$root/rest/$method.view?u=admin&p=password&v=1.16.1&c=subtracks-test&f=json$extra"
            return http.newCall(Request.Builder().url(url).build()).execute().use { it.body.string() }
        }

        call("startScan")
        repeat(120) {
            if (!call("getScanStatus").contains("\"scanning\":true")) return
            Thread.sleep(500)
        }
        error("the prune server did not finish rescanning")
    }

    private fun requireProperty(name: String): String =
        System.getProperty(name)?.takeIf { it.isNotBlank() }
            ?: error("$name is not set; run the integration harness (tools/integration-test.nu)")

    private companion object {
        const val REMOVED_ALBUM_DIR = "197"
    }
}
