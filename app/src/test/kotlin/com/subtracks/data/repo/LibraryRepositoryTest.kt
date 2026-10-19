package com.subtracks.data.repo

import android.content.Context
import android.content.res.Resources
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.model.Album
import com.subtracks.data.model.ArtistBio
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.Source
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.StarredFilter
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.source.StarType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

@RunWith(AndroidJUnit4::class)
class LibraryRepositoryTest {
    private lateinit var db: SubtracksDatabase
    private lateinit var sourceRepository: SourceRepository
    private lateinit var repository: LibraryRepository
    private lateinit var scope: CoroutineScope
    private val messages = ArrayList<String>()
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        resources = context.resources
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
                showMessage = { messages += it.resolve(resources) },
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

    @Test
    fun theAlbumGridPresentsTheWholeLibraryAndSurvivesAReload() =
        runBlocking {
            val sourceId = sourceRepository.addSource("nav", "http://localhost/", "u", "p", false)
            db.libraryDao().upsertAlbums((0 until 250).map { seedAlbum(sourceId, it) })

            val presenterDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            val presenter = RecordingPresenter(presenterDispatcher)
            val job =
                launch {
                    repository
                        .albums(sourceId, AlbumSort.Name, false, StarredFilter.Any, "", false)
                        .collect { presenter.collectFrom(it) }
                }
            try {
                awaitRefreshes(presenter, 1)
                assertEquals(250, presented(presenter, presenterDispatcher))

                db.libraryDao().upsertAlbums(listOf(seedAlbum(sourceId, 0).copy(playCount = 1)))
                awaitRefreshes(presenter, 2)

                assertEquals("a reload must not collapse the grid to one page", 250, presented(presenter, presenterDispatcher))
            } finally {
                job.cancelAndJoin()
                presenterDispatcher.close()
            }
        }

    @Test
    fun artistAlbumsSeparatesAlbumCreditsFromTrackAppearances() =
        runBlocking {
            val sourceId = 1L
            db.sourcesDao().upsertSource(
                Source(id = sourceId, name = "test", address = "http://localhost", isActive = true, createdAt = 0),
            )
            db.libraryDao().upsertAlbums(
                listOf(
                    seedAlbum(sourceId, 1),
                    seedAlbum(sourceId, 2).copy(artistId = "ar2"),
                ),
            )
            db.libraryDao().upsertSongs(
                listOf(
                    seedSong(sourceId).copy(albumId = "al-0001"),
                    seedSong(sourceId).copy(id = "s2", albumId = "al-0002"),
                ),
            )

            val albums = repository.artistAlbums(sourceId, "ar1").first()

            assertEquals(listOf("al-0001"), albums.albumCredits.map { it.id })
            assertEquals(listOf("al-0002"), albums.appearances.map { it.id })
            assertEquals(2, albums.count)
        }

    @Test
    fun downloadedArtistAlbumsKeepPerCreditVisibility() =
        runBlocking {
            val sourceId = 1L
            db.sourcesDao().upsertSource(
                Source(id = sourceId, name = "test", address = "http://localhost", isActive = true, createdAt = 0),
            )
            db.libraryDao().upsertAlbums(
                listOf(
                    seedAlbum(sourceId, 1),
                    seedAlbum(sourceId, 2).copy(artistId = "ar2"),
                ),
            )
            db.libraryDao().upsertSongs(
                listOf(
                    seedSong(sourceId).copy(albumId = "al-0001"),
                    seedSong(sourceId).copy(id = "s2", albumId = "al-0002"),
                    seedSong(sourceId).copy(id = "s3", albumId = "al-0002", artistId = "ar2"),
                    seedSong(sourceId).copy(id = "s4", albumId = "al-0001", artistId = "ar2"),
                ),
            )

            db.downloadDao().upsert(SongDownload(sourceId, "s4", DownloadStatus.Completed))
            val otherOnAlbumCredit = repository.artistAlbums(sourceId, "ar1", downloaded = true).first()
            assertEquals(listOf("al-0001"), otherOnAlbumCredit.albumCredits.map { it.id })
            assertEquals(emptyList<String>(), otherOnAlbumCredit.appearances.map { it.id })

            db.downloadDao().upsert(SongDownload(sourceId, "s3", DownloadStatus.Completed))
            val otherOnTrackCredit = repository.artistAlbums(sourceId, "ar1", downloaded = true).first()
            assertEquals(listOf("al-0001"), otherOnTrackCredit.albumCredits.map { it.id })
            assertEquals(emptyList<String>(), otherOnTrackCredit.appearances.map { it.id })

            db.downloadDao().upsert(SongDownload(sourceId, "s2", DownloadStatus.Completed))
            val creditedTrack = repository.artistAlbums(sourceId, "ar1", downloaded = true).first()
            assertEquals(listOf("al-0001"), creditedTrack.albumCredits.map { it.id })
            assertEquals(listOf("al-0002"), creditedTrack.appearances.map { it.id })
        }

    @Test
    fun refreshingAnArtistBioStoresItAndUpdatesItOnTheNextOpen() =
        runBlocking {
            withSource { server ->
                val sourceId = sourceRepository.activeSourceIdOnce()!!
                server.enqueue(artistInfo("First bio"))
                assertTrue(repository.refreshArtistBio(sourceId, "ar1").isSuccess)
                assertEquals("First bio", repository.artistBio(sourceId, "ar1").first()?.biography)

                server.enqueue(artistInfo("Second bio"))
                assertTrue(repository.refreshArtistBio(sourceId, "ar1").isSuccess)

                assertEquals("Second bio", repository.artistBio(sourceId, "ar1").first()?.biography)
                val first = server.takeRequest()
                assertEquals("/rest/getArtistInfo2.view", first.requestUrl!!.encodedPath)
                assertEquals("ar1", first.requestUrl!!.queryParameter("id"))
                assertEquals("/rest/getArtistInfo2.view", server.takeRequest().requestUrl!!.encodedPath)
            }
        }

    @Test
    fun anArtistWithoutABioStillRecordsTheFetchedState() =
        runBlocking {
            withSource { server ->
                val sourceId = sourceRepository.activeSourceIdOnce()!!
                server.enqueue(
                    MockResponse().setBody(
                        "<subsonic-response status=\"ok\"><artistInfo2/></subsonic-response>",
                    ),
                )

                assertTrue(repository.refreshArtistBio(sourceId, "ar1").isSuccess)

                val bio = repository.artistBio(sourceId, "ar1").first()
                assertNotNull(bio)
                assertNull(bio?.biography)
            }
        }

    @Test
    fun anEmptyRefreshKeepsTheStoredBio() =
        runBlocking {
            withSource { server ->
                val sourceId = sourceRepository.activeSourceIdOnce()!!
                db.libraryDao().upsertArtistBio(ArtistBio(sourceId, "ar1", "Kept"))
                server.enqueue(
                    MockResponse().setBody(
                        "<subsonic-response status=\"ok\"><artistInfo2/></subsonic-response>",
                    ),
                )

                assertTrue(repository.refreshArtistBio(sourceId, "ar1").isSuccess)

                assertEquals("Kept", repository.artistBio(sourceId, "ar1").first()?.biography)
            }
        }

    @Test
    fun biographiesKeepLineAndParagraphBreaks() {
        assertEquals("Line one\nLine two", normalizeBiography("Line one\r\nLine two"))
        assertEquals("Line one\nLine two", normalizeBiography("Line one\nLine two"))
        assertEquals("One\n\nTwo", normalizeBiography("One   Two"))
        assertEquals("One\n\nTwo", normalizeBiography("One\n\nTwo"))
        assertNull(normalizeBiography("   "))
    }

    @Test
    fun aFailedRefreshKeepsTheStoredBio() =
        runBlocking {
            withSource { server ->
                val sourceId = sourceRepository.activeSourceIdOnce()!!
                db.libraryDao().upsertArtistBio(ArtistBio(sourceId, "ar1", "Kept"))
                server.enqueue(MockResponse().setResponseCode(500))

                assertTrue(repository.refreshArtistBio(sourceId, "ar1").isFailure)

                assertEquals("Kept", repository.artistBio(sourceId, "ar1").first()?.biography)
            }
        }

    private suspend fun awaitRefreshes(
        presenter: RecordingPresenter,
        target: Int,
    ) {
        withTimeout(30_000) {
            while (presenter.refreshes.get() < target) delay(10)
        }
    }

    private suspend fun presented(
        presenter: RecordingPresenter,
        dispatcher: CoroutineDispatcher,
    ): Int = withContext(dispatcher) { presenter.size }

    private fun seedAlbum(
        sourceId: Long,
        index: Int,
    ) = Album(
        sourceId = sourceId,
        id = "al-" + index.toString().padStart(4, '0'),
        artistId = "ar1",
        name = "Album " + index.toString().padStart(4, '0'),
        albumArtist = "Artist",
        created = 0,
        coverArt = null,
        genre = null,
        year = null,
        starred = null,
        songCount = 1,
    )

    private class RecordingPresenter(
        context: CoroutineContext,
    ) : PagingDataPresenter<Album>(context) {
        val refreshes = AtomicInteger()

        override suspend fun presentPagingDataEvent(event: PagingDataEvent<Album>) {
            if (event is PagingDataEvent.Refresh) refreshes.incrementAndGet()
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

    private fun artistInfo(biography: String) =
        MockResponse().setBody(
            "<subsonic-response status=\"ok\"><artistInfo2>" +
                "<biography>$biography</biography>" +
                "</artistInfo2></subsonic-response>",
        )

    private fun ok() = MockResponse().setBody("<subsonic-response status=\"ok\" version=\"1.16.1\"/>")

    private fun failed(code: Int) =
        MockResponse().setBody("<subsonic-response status=\"failed\"><error code=\"$code\" message=\"nope\"/></subsonic-response>")
}
