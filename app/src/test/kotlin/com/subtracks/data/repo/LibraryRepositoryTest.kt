package com.subtracks.data.repo

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Song
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.source.StarType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryRepositoryTest {
    private lateinit var db: SubtracksDatabase
    private lateinit var sourceRepository: SourceRepository
    private lateinit var repository: LibraryRepository
    private val messages = ArrayList<String>()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        sourceRepository = SourceRepository(db, OkHttpClient(), fakeUserPreferences())
        repository = LibraryRepository(db, sourceRepository, showMessage = { messages += it })
    }

    @After
    fun tearDown() {
        sourceRepository.close()
        db.close()
    }

    @Test
    fun starringASongPostsToTheServerAndUpdatesTheRow() =
        runBlocking {
            withSource { server ->
                server.enqueue(ok())

                assertTrue(repository.setStar(StarType.Song, "s1", true).isSuccess)

                val request = server.takeRequest()
                assertEquals("/rest/star.view", request.requestUrl!!.encodedPath)
                assertEquals("s1", request.requestUrl!!.queryParameter("id"))
                assertNotNull(starred())
            }
        }

    @Test
    fun unstarringPostsToTheServerAndClearsTheRow() =
        runBlocking {
            withSource { server ->
                server.enqueue(ok())
                repository.setStar(StarType.Song, "s1", true)
                server.enqueue(ok())

                repository.setStar(StarType.Song, "s1", false)

                server.takeRequest()
                assertEquals("/rest/unstar.view", server.takeRequest().requestUrl!!.encodedPath)
                assertNull(starred())
            }
        }

    @Test
    fun aRejectedStarLeavesTheRowUntouchedAndReports() =
        runBlocking {
            withSource { server ->
                server.enqueue(failed(40))

                assertTrue(repository.setStar(StarType.Song, "s1", true).isFailure)

                assertNull(starred())
                assertEquals(listOf("Could not update star"), messages)
            }
        }

    private suspend fun withSource(block: suspend (MockWebServer) -> Unit) {
        val server = MockWebServer()
        server.start()
        try {
            val sourceId = sourceRepository.addSource("nav", server.url("/").toString(), "u", "p", false)
            db.libraryDao().upsertSongs(listOf(seedSong(sourceId)))
            block(server)
        } finally {
            server.shutdown()
        }
    }

    private suspend fun starred(): Long? =
        repository
            .albumSongs(sourceRepository.activeSourceIdOnce()!!, "al1")
            .first()
            .first { it.id == "s1" }
            .starred

    private fun seedSong(sourceId: Long) =
        Song(
            sourceId = sourceId,
            id = "s1",
            albumId = "al1",
            artistId = "ar1",
            title = "Track",
            album = "Album",
            artist = "Artist",
            duration = 1_000,
            track = 1,
            disc = 1,
            starred = null,
            genre = null,
        )

    private fun ok() = MockResponse().setBody("<subsonic-response status=\"ok\" version=\"1.16.1\"/>")

    private fun failed(code: Int) =
        MockResponse().setBody("<subsonic-response status=\"failed\"><error code=\"$code\" message=\"nope\"/></subsonic-response>")
}
