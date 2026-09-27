package com.subtracks.data.repo

import android.content.Context
import android.net.Uri
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkFetcher
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.download.FakeDownloadEngine
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.Source
import com.subtracks.data.model.SubsonicSource
import com.subtracks.data.model.coverArtKey
import com.subtracks.data.prefs.fakeUserPreferences
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(AndroidJUnit4::class)
class DownloadRepositoryTest {
    private lateinit var db: SubtracksDatabase
    private lateinit var sources: SourceRepository
    private lateinit var engine: FakeDownloadEngine
    private lateinit var repository: DownloadRepository
    private lateinit var artwork: ArtworkStore
    private lateinit var dir: File
    private val messages = CopyOnWriteArrayList<String>()
    private val requestedArt = CopyOnWriteArrayList<String>()
    private val artFetchedAfterTheEngineRequest = CopyOnWriteArrayList<Boolean>()
    private var failArtworkFetch = false
    private val fetcher =
        ArtworkFetcher { url ->
            requestedArt += url
            artFetchedAfterTheEngineRequest += engine.requests.isNotEmpty()
            if (failArtworkFetch) throw IOException("boom")
            byteArrayOf(1, 2, 3)
        }

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        artwork = ArtworkStore(File(context.cacheDir, "art-${System.nanoTime()}"))
        sources = SourceRepository(db, OkHttpClient(), fakeUserPreferences(), artwork)
        engine = FakeDownloadEngine()
        dir = File(context.cacheDir, "downloads-${System.nanoTime()}")
        repository =
            DownloadRepository(
                db,
                sources,
                engine,
                dir,
                artworkStore = artwork,
                artworkFetcher = fetcher,
                showMessage = { messages += it },
            )
    }

    @After
    fun tearDown() {
        repository.close()
        sources.close()
        db.close()
        dir.deleteRecursively()
    }

    @Test
    fun startingADownloadAsksTheEngineForTheOriginalAndRecordsIt() =
        runTest {
            seedLibrary()

            runBlocking { repository.download(1, "s1") }

            val request = engine.requests.single().second
            assertTrue("expected the download endpoint, was ${request.uri}", request.uri.contains("/rest/download"))
            assertEquals("downloads/1/s1", request.path)
            assertEquals("Song 1", request.title)
            assertEquals(DownloadStatus.Queued, row(1, "s1")?.status)
        }

    @Test
    fun progressAndCompletionAreMirroredAndTheFileBecomesPlayable() =
        runTest {
            seedLibrary()
            repository.start()
            runBlocking { repository.download(1, "s1") }
            val id = engine.requests.single().first

            engine.running(id, bytes = 40, total = 100)
            await { row(1, "s1")?.status == DownloadStatus.Running }
            assertEquals(40L, row(1, "s1")?.bytes)

            engine.complete(id, total = 100)
            writeFile(1, "s1", "audio")
            runBlocking { repository.reconcile() }

            await { repository.localUri("s1") != null }
            assertEquals(DownloadStatus.Completed, row(1, "s1")?.status)
            assertEquals(Uri.fromFile(file(1, "s1")).toString(), repository.localUri("s1"))
        }

    @Test
    fun aPartialFileIsNotTreatedAsPlayableWhileTheDownloadRuns() =
        runTest {
            seedLibrary()
            repository.start()
            runBlocking { repository.download(1, "s1") }
            val id = engine.requests.single().first
            engine.running(id, bytes = 40, total = 100)
            writeFile(1, "s1", "half")
            await { row(1, "s1")?.status == DownloadStatus.Running }

            assertNull(repository.localUri("s1"))
        }

    @Test
    fun aCompletedRowWhoseFileIsGoneIsFailedRatherThanLeftCompleted() =
        runTest {
            seedLibrary()
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed, engineId = null))

            runBlocking { repository.reconcile() }

            assertEquals(DownloadStatus.Failed, row(1, "s1")?.status)
        }

    @Test
    fun aCompletedEngineStateWithoutAFileIsFailed() =
        runTest {
            seedLibrary()
            runBlocking { repository.download(1, "s1") }
            val id = engine.requests.single().first
            engine.complete(id, total = 100)

            runBlocking { repository.reconcile() }

            assertEquals(DownloadStatus.Failed, row(1, "s1")?.status)
        }

    @Test
    fun aQueuedRowWhoseEngineEntryIsGoneIsStartedAgain() =
        runTest {
            seedLibrary()
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Queued, engineId = 7))

            runBlocking { repository.reconcile() }

            val request = engine.requests.single()
            assertNotEquals(7L, request.first)
            assertEquals("downloads/1/s1", request.second.path)
        }

    @Test
    fun removingADownloadCancelsItAndDeletesTheFile() =
        runTest {
            seedLibrary()
            writeFile(1, "s1", "audio")
            runBlocking { repository.download(1, "s1") }
            val id = engine.requests.single().first

            runBlocking { repository.remove(1, "s1") }

            assertTrue(engine.cancelled.contains(id))
            assertFalse(file(1, "s1").exists())
            assertNull(row(1, "s1"))
        }

    @Test
    fun aFailedDownloadCanBeStartedAgain() =
        runTest {
            seedLibrary()
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Failed, engineId = 3, error = "boom"))

            runBlocking { repository.download(1, "s1") }

            assertEquals(1, engine.requests.size)
            assertEquals(DownloadStatus.Queued, row(1, "s1")?.status)
        }

    @Test
    fun aFailedDownloadIsNotRestartedByReconcile() =
        runTest {
            seedLibrary()
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Failed, engineId = null, error = "boom"))

            runBlocking { repository.reconcile() }
            runBlocking { repository.reconcile() }

            assertTrue(engine.requests.isEmpty())
            assertEquals(DownloadStatus.Failed, row(1, "s1")?.status)
            assertEquals("boom", row(1, "s1")?.error)
        }

    @Test
    fun anEngineFailureIsMirroredWithItsMessageAndLeavesNoPartialFile() =
        runTest {
            seedLibrary()
            runBlocking { repository.download(1, "s1") }
            val id = engine.requests.single().first
            writeFile(1, "s1", "half")

            engine.fail(id)
            runBlocking { repository.reconcile() }

            assertEquals(DownloadStatus.Failed, row(1, "s1")?.status)
            assertEquals("Download failed", row(1, "s1")?.error)
            assertFalse(file(1, "s1").exists())
        }

    @Test
    fun aRunningDownloadWhoseEngineEntryVanishesIsFailedRatherThanRestarted() =
        runTest {
            seedLibrary()
            repository.start()
            runBlocking { repository.download(1, "s1") }
            val id = engine.requests.single().first
            engine.running(id, bytes = 40, total = 100)
            await { row(1, "s1")?.status == DownloadStatus.Running }

            engine.forget(id)
            runBlocking { repository.reconcile() }

            assertEquals(DownloadStatus.Failed, row(1, "s1")?.status)
            assertEquals(1, engine.requests.size)
        }

    @Test
    fun aSongRemovedFromTheServerTakesItsDownloadRowWithIt() =
        runTest {
            seedLibrary()
            runBlocking { repository.download(1, "s1") }

            runBlocking { db.libraryDao().deleteSongs(1, listOf("s1")) }

            assertNull(row(1, "s1"))
        }

    @Test
    fun downloadingFromASourceThatIsNotActiveIsRefused() =
        runTest {
            seedLibrary()
            db.sourcesDao().upsertSource(Source(2, "other", "http://other/", isActive = false, createdAt = 0))
            db.sourcesDao().upsertSubsonicSource(SubsonicSource(2, "user", "pass"))

            runBlocking { repository.download(2, "s1") }

            assertTrue(engine.requests.isEmpty())
            assertEquals(listOf("Can't download from a source that isn't active"), messages.toList())
        }

    @Test
    fun removingASourceCancelsItsDownloadsAndDeletesItsFiles() =
        runTest {
            seedLibrary()
            writeFile(1, "s1", "audio")
            runBlocking { repository.download(1, "s1") }
            val id = engine.requests.single().first

            runBlocking { repository.removeSource(1) }

            assertTrue(engine.cancelled.contains(id))
            assertFalse(file(1, "s1").exists())
            assertTrue(allRows().isEmpty())
        }

    @Test
    fun reconcileDeletesFilesThatBelongToNoDownload() =
        runTest {
            seedLibrary()
            writeFile(1, "orphan", "audio")

            runBlocking { repository.reconcile() }

            assertFalse(file(1, "orphan").exists())
        }

    @Test
    fun startingADownloadAlsoStoresTheAlbumAndArtistArtwork() =
        runTest {
            seedLibrary()

            runBlocking { repository.download(1, "s1") }

            assertEquals(4, requestedArt.size)
            listOf(ALBUM_ART, ARTIST_ART).forEach { cover ->
                listOf(false, true).forEach { thumbnail ->
                    val key = coverArtKey(1, cover, thumbnail)
                    assertNotNull("expected stored art for $key", artwork.uri(1, key))
                }
            }
        }

    @Test
    fun theDownloadStartsBeforeItsArtworkIsFetched() =
        runTest {
            seedLibrary()

            runBlocking { repository.download(1, "s1") }

            assertTrue(artFetchedAfterTheEngineRequest.isNotEmpty())
            assertTrue(artFetchedAfterTheEngineRequest.all { it })
        }

    @Test
    fun resumingAQueuedDownloadAlsoStoresItsArtwork() =
        runTest {
            seedLibrary()
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Queued, engineId = 7))

            runBlocking { repository.reconcile() }

            assertEquals(1, engine.requests.size)
            assertNotNull(artwork.uri(1, coverArtKey(1, ALBUM_ART, false)))
        }

    @Test
    fun aSongWithoutArtworkStillDownloads() =
        runTest {
            seedLibrary()
            runBlocking {
                db.libraryDao().upsertAlbums(listOf(album(coverArt = null)))
                db.libraryDao().upsertArtists(listOf(artist(coverArt = null)))
            }

            runBlocking { repository.download(1, "s1") }

            assertTrue(requestedArt.isEmpty())
            assertEquals(1, engine.requests.size)
        }

    @Test
    fun aFailedArtworkFetchDoesNotStopTheDownload() =
        runTest {
            seedLibrary()
            failArtworkFetch = true

            runBlocking { repository.download(1, "s1") }

            assertEquals(1, engine.requests.size)
            assertEquals(DownloadStatus.Queued, row(1, "s1")?.status)
            assertNull(artwork.uri(1, coverArtKey(1, ALBUM_ART, false)))
        }

    @Test
    fun artworkThatIsAlreadyStoredIsNotFetchedAgain() =
        runTest {
            seedLibrary()
            listOf(ALBUM_ART, ARTIST_ART).forEach { cover ->
                listOf(false, true).forEach { thumbnail ->
                    artwork.write(1, coverArtKey(1, cover, thumbnail), byteArrayOf(9))
                }
            }

            runBlocking { repository.download(1, "s1") }

            assertTrue(requestedArt.isEmpty())
            assertEquals(1, engine.requests.size)
        }

    @Test
    fun removingTheLastDownloadOfAnAlbumRemovesItsArtwork() =
        runTest {
            seedLibrary()
            runBlocking { repository.download(1, "s1") }

            runBlocking { repository.remove(1, "s1") }

            assertNull(artwork.uri(1, coverArtKey(1, ALBUM_ART, false)))
            assertNull(artwork.uri(1, coverArtKey(1, ARTIST_ART, false)))
        }

    @Test
    fun artworkAnotherDownloadStillNeedsIsKept() =
        runTest {
            seedLibrary()
            runBlocking { repository.download(1, "s1") }
            runBlocking { repository.download(1, "s2") }

            runBlocking { repository.remove(1, "s1") }

            assertNotNull(artwork.uri(1, coverArtKey(1, ALBUM_ART, false)))
        }

    private fun album(coverArt: String?) =
        Album(
            sourceId = 1,
            id = "al1",
            artistId = "ar1",
            name = "Album",
            albumArtist = "Artist",
            created = 0,
            coverArt = coverArt,
            genre = null,
            year = 2000,
            starred = null,
            songCount = 2,
        )

    private fun artist(coverArt: String?) =
        Artist(sourceId = 1, id = "ar1", name = "Artist", albumCount = 1, starred = null, coverArt = coverArt)

    private fun seedLibrary() {
        runBlocking {
            db.sourcesDao().upsertSource(Source(1, "source", "http://localhost:4533/", isActive = true, createdAt = 0))
            db.sourcesDao().upsertSubsonicSource(SubsonicSource(1, "user", "pass"))
            db.libraryDao().upsertSongs(
                listOf(
                    Song(
                        sourceId = 1,
                        id = "s1",
                        albumId = "al1",
                        artistId = "ar1",
                        title = "Song 1",
                        album = "Album",
                        artist = "Artist",
                        duration = 100,
                        track = 1,
                        disc = 1,
                        starred = null,
                        genre = null,
                    ),
                    Song(
                        sourceId = 1,
                        id = "s2",
                        albumId = "al1",
                        artistId = "ar1",
                        title = "Song 2",
                        album = "Album",
                        artist = "Artist",
                        duration = 100,
                        track = 2,
                        disc = 1,
                        starred = null,
                        genre = null,
                    ),
                ),
            )
            db.libraryDao().upsertAlbums(listOf(album(ALBUM_ART)))
            db.libraryDao().upsertArtists(listOf(artist(ARTIST_ART)))
        }
        await { sources.downloadUri(1, "s1") != null }
    }

    private fun file(
        sourceId: Long,
        songId: String,
    ): File = File(File(dir, sourceId.toString()), songId)

    private fun row(
        sourceId: Long,
        songId: String,
    ): SongDownload? = runBlocking { db.downloadDao().find(sourceId, songId) }

    private fun allRows(): List<SongDownload> = runBlocking { db.downloadDao().all() }

    private fun writeFile(
        sourceId: Long,
        songId: String,
        contents: String,
    ) {
        val file = file(sourceId, songId)
        file.parentFile?.mkdirs()
        file.writeText(contents)
    }

    private fun await(predicate: () -> Boolean) {
        val deadline = System.nanoTime() + 5_000_000_000L
        while (!predicate() && System.nanoTime() < deadline) Thread.sleep(10)
        assertTrue("Timed out waiting for the expected download state", predicate())
    }

    private companion object {
        const val ALBUM_ART = "art-al1"
        const val ARTIST_ART = "art-ar1"
    }
}
