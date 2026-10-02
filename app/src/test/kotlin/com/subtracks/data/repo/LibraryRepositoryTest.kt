package com.subtracks.data.repo

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.model.Song
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.source.StarType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class LibraryRepositoryTest {
    private lateinit var db: SubtracksDatabase
    private lateinit var sourceRepository: SourceRepository
    private lateinit var repository: LibraryRepository
    private lateinit var scope: CoroutineScope
    private val messages = ArrayList<String>()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        sourceRepository =
            SourceRepository(db, OkHttpClient(), fakeUserPreferences(), ArtworkStore(File(context.cacheDir, "art")), scope = scope)
        repository =
            LibraryRepository(
                db,
                sourceRepository,
                NetworkServerActionSink(sourceRepository),
                showMessage = { messages += it },
                scope = scope,
            )
    }

    @After
    fun tearDown() {
        cancelAndJoinBlocking(scope)
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
    fun starringUpdatesTheRowBeforeTheServerResponds() =
        runBlocking {
            withSource { server ->
                server.enqueue(ok().setBodyDelay(250, TimeUnit.MILLISECONDS))

                val call = launch { repository.setStar(StarType.Song, "s1", true) }
                withTimeout(3_000) { while (starred() == null) delay(10) }
                call.cancel()
                call.join()

                assertNotNull(starred())
                assertEquals(emptyList<String>(), messages)
            }
        }

    @Test
    fun aRejectedStarRollsBackAndReports() =
        runBlocking {
            withSource { server ->
                server.enqueue(failed(40))

                assertTrue(repository.setStar(StarType.Song, "s1", true).isFailure)

                assertNull(starred())
                assertEquals(listOf("Could not update star"), messages)
            }
        }

    @Test
    fun aRejectedUnstarRestoresTheStarredRow() =
        runBlocking {
            withSource { server ->
                server.enqueue(ok())
                repository.setStar(StarType.Song, "s1", true)
                val starredAt = starred()
                server.enqueue(failed(40))

                assertTrue(repository.setStar(StarType.Song, "s1", false).isFailure)

                assertEquals(starredAt, starred())
                assertEquals(listOf("Could not update star"), messages)
            }
        }

    @Test
    fun albumSongOrdinalFollowsTheAlbumOrder() =
        runBlocking {
            val sourceId = sourceRepository.addSource("nav", "http://localhost/", "u", "p", false)
            db.libraryDao().upsertSongs(
                listOf(
                    seedSong(sourceId).copy(id = "s3", track = 3),
                    seedSong(sourceId).copy(id = "s1", track = 1),
                    seedSong(sourceId).copy(id = "s2", track = 2),
                ),
            )

            assertEquals(0L, repository.albumSongOrdinal(sourceId, "al1", "s1"))
            assertEquals(2L, repository.albumSongOrdinal(sourceId, "al1", "s3"))
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
