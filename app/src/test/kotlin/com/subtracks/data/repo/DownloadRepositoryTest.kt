package com.subtracks.data.repo

import android.content.Context
import android.net.Uri
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.FakeDownloadEngine
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.Source
import com.subtracks.data.model.SubsonicSource
import com.subtracks.data.prefs.fakeUserPreferences
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

@RunWith(AndroidJUnit4::class)
class DownloadRepositoryTest {
    private lateinit var db: SubtracksDatabase
    private lateinit var sources: SourceRepository
    private lateinit var engine: FakeDownloadEngine
    private lateinit var repository: DownloadRepository
    private lateinit var dir: File
    private val messages = CopyOnWriteArrayList<String>()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        sources = SourceRepository(db, OkHttpClient(), fakeUserPreferences())
        engine = FakeDownloadEngine()
        dir = File(context.cacheDir, "downloads-${System.nanoTime()}")
        repository = DownloadRepository(db, sources, engine, dir, showMessage = { messages += it })
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
    fun anEngineFailureIsMirroredWithItsMessage() =
        runTest {
            seedLibrary()
            runBlocking { repository.download(1, "s1") }
            val id = engine.requests.single().first

            engine.fail(id)
            runBlocking { repository.reconcile() }

            assertEquals(DownloadStatus.Failed, row(1, "s1")?.status)
            assertEquals("Download failed", row(1, "s1")?.error)
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
                ),
            )
        }
        await { sources.downloadUri("s1") != null }
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
}
