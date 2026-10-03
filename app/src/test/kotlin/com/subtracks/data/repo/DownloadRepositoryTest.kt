package com.subtracks.data.repo

import android.content.Context
import android.net.Uri
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.awaitUntil
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkFetcher
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.download.FakeDownloadEngine
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.DownloadError
import com.subtracks.data.model.DownloadList
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.Source
import com.subtracks.data.model.SubsonicSource
import com.subtracks.data.model.coverArtKey
import com.subtracks.data.prefs.fakeUserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private lateinit var scope: CoroutineScope
    private val prefs = fakeUserPreferences()
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
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        sources = SourceRepository(db, OkHttpClient(), prefs, artwork, scope = scope)
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
                scope = scope,
            )
    }

    @After
    fun tearDown() {
        cancelAndJoinBlocking(scope)
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
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Failed, engineId = 3, error = DownloadError.Failed))

            runBlocking { repository.download(1, "s1") }

            assertEquals(1, engine.requests.size)
            assertEquals(DownloadStatus.Queued, row(1, "s1")?.status)
        }

    @Test
    fun aFailedDownloadIsNotRestartedByReconcile() =
        runTest {
            seedLibrary()
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Failed, engineId = null, error = DownloadError.Failed))

            runBlocking { repository.reconcile() }
            runBlocking { repository.reconcile() }

            assertTrue(engine.requests.isEmpty())
            assertEquals(DownloadStatus.Failed, row(1, "s1")?.status)
            assertEquals(DownloadError.Failed, row(1, "s1")?.error)
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
            assertEquals(DownloadError.Failed, row(1, "s1")?.error)
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
    fun downloadingASongAlsoStoresTheArtworkOfPlaylistsThatContainIt() =
        runTest {
            seedLibrary()
            runBlocking {
                db.libraryDao().upsertPlaylists(
                    listOf(Playlist(1, "pl1", "Playlist", comment = null, coverArt = PLAYLIST_ART, songCount = 2, created = 0)),
                )
                db.libraryDao().upsertPlaylistSongs(listOf(PlaylistSong(1, "pl1", "s1", 0)))
            }

            runBlocking { repository.download(1, "s1") }

            listOf(false, true).forEach { thumbnail ->
                val key = coverArtKey(1, PLAYLIST_ART, thumbnail)
                assertNotNull("expected stored playlist art for $key", artwork.uri(1, key))
            }

            runBlocking { repository.reconcile() }
            assertNotNull(artwork.uri(1, coverArtKey(1, PLAYLIST_ART, false)))
        }

    @Test
    fun goingOfflineCancelsActiveDownloads() =
        runTest {
            seedLibrary()
            repository.start()
            val active: (SongDownload) -> Boolean = {
                it.status == DownloadStatus.Queued || it.status == DownloadStatus.Running
            }
            runBlocking { repository.download(1, "s1") }
            await { allRows().any(active) }

            runBlocking { sources.setOfflineMode(true) }

            await { allRows().none(active) }
            assertTrue(engine.cancelled.isNotEmpty())
        }

    @Test
    fun cancellingQueuedRowsWithoutEngineIdsLeavesTheEngineAlone() =
        runTest {
            seedLibrary()
            runBlocking {
                db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Queued))
                db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Queued))
            }
            // DownloadManager.remove throws on an empty id list; a queue of only waiting rows has
            // no engine ids, so cancelling must not call the engine at all.
            engine.failCancelOnEmpty = true

            runBlocking { repository.cancelActive() }

            assertTrue(allRows().isEmpty())
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
    fun artworkSurvivesALibraryRowGoingMissing() =
        runTest {
            seedLibrary()
            runBlocking { repository.download(1, "s1") }
            assertNotNull(artwork.uri(1, coverArtKey(1, ALBUM_ART, false)))

            runBlocking { db.libraryDao().deleteAlbums(1, listOf("al1")) }
            runBlocking { repository.reconcile() }

            assertNotNull(artwork.uri(1, coverArtKey(1, ALBUM_ART, false)))
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

    @Test
    fun downloadingAListQueuesEverySongAndStoresItsArtworkOnce() =
        runTest {
            seedLibrary()

            runBlocking { repository.downloadAll(1, DownloadList.Album, "al1") }

            assertEquals(2, engine.requests.size)
            assertEquals(4, requestedArt.size)
            assertNotNull(artwork.uri(1, coverArtKey(1, ALBUM_ART, true)))
        }

    @Test
    fun downloadingAListSkipsTheSongsItAlreadyHas() =
        runTest {
            seedLibrary()
            runBlocking { repository.download(1, "s1") }
            val before = engine.requests.size

            runBlocking { repository.downloadAll(1, DownloadList.Album, "al1") }

            assertEquals(before + 1, engine.requests.size)
        }

    @Test
    fun cancelAllStopsTheRunningSongsAndKeepsTheCompletedOnes() =
        runTest {
            seedLibrary()
            writeFile(1, "s1", "audio")
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed, engineId = 11))
            db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Running, engineId = 22))

            runBlocking { repository.cancelAll(1, DownloadList.Album, "al1") }

            assertNotNull(row(1, "s1"))
            assertNull(row(1, "s2"))
            assertTrue(engine.cancelled.contains(22L))
        }

    @Test
    fun deleteAllRemovesTheCompletedSongsAndKeepsTheRunningOnes() =
        runTest {
            seedLibrary()
            writeFile(1, "s1", "audio")
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed, engineId = 11))
            db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Running, engineId = 22))

            runBlocking { repository.deleteAll(1, DownloadList.Album, "al1") }

            assertNull(row(1, "s1"))
            assertFalse(file(1, "s1").exists())
            assertNotNull(row(1, "s2"))
        }

    @Test
    fun theListStatusCountsTheSongsAndTracksProgress() =
        runTest {
            seedLibrary()
            writeFile(1, "s1", "audio")
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed, engineId = 11))

            assertEquals(ListDownloadStatus(total = 2, downloaded = 1, downloading = 0), statusOf(DownloadList.Album, "al1"))
            assertEquals(ListDownloadStatus(total = 2, downloaded = 1, downloading = 0), statusOf(DownloadList.Artist, "ar1"))

            db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Queued, engineId = 22))

            assertEquals(ListDownloadStatus(total = 2, downloaded = 1, downloading = 1), statusOf(DownloadList.Album, "al1"))

            db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Running, engineId = 22))

            assertEquals(ListDownloadStatus(total = 2, downloaded = 1, downloading = 1), statusOf(DownloadList.Album, "al1"))
        }

    @Test
    fun thePlaylistStatusCountsASongThatAppearsTwiceOnce() =
        runTest {
            seedLibrary()
            seedPlaylist(listOf("s1", "s1"))

            assertEquals(ListDownloadStatus(total = 1, downloaded = 0, downloading = 0), statusOf(DownloadList.Playlist, "pl1"))
        }

    private fun statusOf(
        list: DownloadList,
        refId: String,
    ): ListDownloadStatus = runBlocking { repository.status(1, list, refId).first() }

    @Test
    fun downloadingAPlaylistQueuesEverySong() =
        runTest {
            seedLibrary()
            seedPlaylist(listOf("s1", "s2"))

            runBlocking { repository.downloadAll(1, DownloadList.Playlist, "pl1") }

            assertEquals(listOf("s1", "s2"), requestedSongIds())
        }

    @Test
    fun downloadingAnArtistQueuesTheSongsOfItsAlbums() =
        runTest {
            seedLibrary()
            // s2 is a guest on the album, so its own artistId differs: the artist page lists the
            // album, and the download follows what the page shows.
            runBlocking {
                db.libraryDao().upsertSongs(
                    listOf(
                        Song(
                            sourceId = 1,
                            id = "s2",
                            albumId = "al1",
                            artistId = "ar2",
                            title = "Song 2",
                            album = "Album",
                            artist = "Guest",
                            duration = 100,
                            track = 2,
                            disc = 1,
                            starred = null,
                            genre = null,
                        ),
                    ),
                )
            }

            runBlocking { repository.downloadAll(1, DownloadList.Artist, "ar1") }

            assertEquals(listOf("s1", "s2"), requestedSongIds())
        }

    @Test
    fun cancellingWhileAListIsStillQueuingRemovesTheWholeList() =
        runTest {
            seedLibrary()
            seedSongs(3..400)

            val queuing = launch(Dispatchers.IO) { repository.downloadAll(1, DownloadList.Album, "al1") }
            await { allRows().size >= 50 }
            val cancelling = launch(Dispatchers.IO) { repository.cancelAll(1, DownloadList.Album, "al1") }
            queuing.join()
            cancelling.join()

            assertTrue("expected the whole list cancelled, ${db.downloadDao().all().size} rows left", db.downloadDao().all().isEmpty())
        }

    @Test
    fun aLongListHandsThePlatformOnlyTheInFlightLimit() =
        runTest {
            seedLibrary()
            seedSongs(3..40)

            runBlocking { repository.downloadAll(1, DownloadList.Album, "al1") }

            assertEquals(8, engine.requests.size)
            assertEquals(32, db.downloadDao().all().count { it.status == DownloadStatus.Queued && it.engineId == null })
        }

    @Test
    fun finishingADownloadHandsOverTheNextSongInListOrder() =
        runTest {
            seedLibrary()
            seedSongs(3..12)
            runBlocking { repository.downloadAll(1, DownloadList.Album, "al1") }
            val first = engine.requests.first()

            engine.complete(first.first, total = 10)
            writeFile(1, first.second.path.substringAfterLast('/'), "audio")
            runBlocking { repository.reconcile() }

            assertEquals(9, engine.requests.size)
            assertEquals((1..9).map { "s$it" }, requestedSongIds())
        }

    @Test
    fun reconcilingAWaitingListDoesNotHandItOverWholesale() =
        runTest {
            seedLibrary()
            seedSongs(3..40)
            runBlocking {
                (1..40).forEach { track ->
                    db.downloadDao().upsert(SongDownload(1, "s$track", DownloadStatus.Queued))
                }
            }

            runBlocking { repository.reconcile() }

            assertEquals(8, engine.requests.size)
        }

    @Test
    fun oneSongFailingToQueueDoesNotAbandonTheRest() =
        runTest {
            seedLibrary()
            engine.failEnqueueFor = "s1"

            runBlocking { repository.downloadAll(1, DownloadList.Album, "al1") }

            assertEquals(listOf("s2"), requestedSongIds())
        }

    @Test
    fun downloadingAListWithNothingInItSaysSo() =
        runTest {
            seedLibrary()

            runBlocking { repository.downloadAll(1, DownloadList.Album, "missing-album") }

            assertEquals(listOf("Nothing left to download from this list"), messages.toList())
        }

    @Test
    fun applyActionRoutesToTheMatchingBulkAction() =
        runTest {
            seedLibrary()

            runBlocking { repository.applyAction(1, DownloadList.Album, "al1", BulkDownloadAction.Download) }
            assertEquals(2, engine.requests.size)

            runBlocking { repository.applyAction(1, DownloadList.Album, "al1", BulkDownloadAction.Cancel) }

            assertEquals(ListDownloadStatus(total = 2), statusOf(DownloadList.Album, "al1"))

            runBlocking { repository.applyAction(1, DownloadList.Album, "al1", BulkDownloadAction.Download) }
            runBlocking { db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Completed, engineId = 22)) }
            runBlocking { repository.applyAction(1, DownloadList.Album, "al1", BulkDownloadAction.Delete) }

            assertNotNull(row(1, "s1"))
            assertNull(row(1, "s2"))
        }

    @Test
    fun downloadedBytesCountsTheCompletedFilesOfTheList() =
        runTest {
            seedLibrary()
            writeFile(1, "s1", "12345")
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed, engineId = 11))
            writeFile(1, "s2", "1234")
            db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Running, engineId = 22))
            writeFile(1, "s3", "123")

            assertEquals(5L, repository.downloadedBytes(1, DownloadList.Album, "al1"))
            assertEquals(5L, repository.downloadedBytes(1, DownloadList.Artist, "ar1"))
        }

    @Test
    fun cancellingAListRemovesPartialFilesToo() =
        runTest {
            seedLibrary()
            runBlocking { repository.downloadAll(1, DownloadList.Album, "al1") }
            writeFile(1, "s1", "half")
            writeFile(1, "s1.part", "half")
            writeFile(1, "s2", "half")

            runBlocking { repository.cancelAll(1, DownloadList.Album, "al1") }

            assertFalse(file(1, "s1").exists())
            assertFalse(File(file(1, "s1").parentFile, "s1.part").exists())
            assertFalse(file(1, "s2").exists())
        }

    @Test
    fun cancellingAListCancelsThePlatformOnce() =
        runTest {
            seedLibrary()
            seedSongs(3..40)
            runBlocking { repository.downloadAll(1, DownloadList.Album, "al1") }

            runBlocking { repository.cancelAll(1, DownloadList.Album, "al1") }

            assertEquals(1, engine.cancelCalls)
            assertTrue(allRows().isEmpty())
        }

    @Test
    fun anotherSourcesWaitingRowsDoNotStarveTheActiveOne() =
        runTest {
            seedLibrary()
            runBlocking {
                db.sourcesDao().upsertSource(Source(2, "other", "http://other/", isActive = false, createdAt = 0))
                db.libraryDao().upsertSongs(
                    (1..8).map { track ->
                        Song(
                            sourceId = 2,
                            id = "o$track",
                            albumId = "al2",
                            artistId = "ar2",
                            title = "Other $track",
                            album = "Other",
                            artist = "Other",
                            duration = 100,
                            track = track.toLong(),
                            disc = 1,
                            starred = null,
                            genre = null,
                        )
                    },
                )
                (1..8).forEach { track -> db.downloadDao().upsert(SongDownload(2, "o$track", DownloadStatus.Queued)) }
            }

            runBlocking { repository.downloadAll(1, DownloadList.Album, "al1") }

            assertEquals(listOf("s1", "s2"), requestedSongIds())
        }

    @Test
    fun groupedStatusesCountEachAlbumArtistAndPlaylistSeparately() =
        runTest {
            seedLibrary()
            seedPlaylist(listOf("s1", "s2"))
            runBlocking {
                db.libraryDao().upsertSongs(
                    listOf(
                        Song(
                            sourceId = 1,
                            id = "s3",
                            albumId = "al2",
                            artistId = "ar2",
                            title = "Song 3",
                            album = "Other",
                            artist = "Other",
                            duration = 100,
                            track = 1,
                            disc = 1,
                            starred = null,
                            genre = null,
                        ),
                    ),
                )
                db.libraryDao().upsertAlbums(
                    listOf(
                        Album(
                            sourceId = 1,
                            id = "al2",
                            artistId = "ar2",
                            name = "Other",
                            albumArtist = "Other",
                            created = 0,
                            coverArt = null,
                            genre = null,
                            year = 2000,
                            starred = null,
                            songCount = 1,
                        ),
                    ),
                )
                db.libraryDao().upsertArtists(
                    listOf(Artist(sourceId = 1, id = "ar2", name = "Other", albumCount = 1, starred = null, coverArt = null)),
                )
                db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed))
                db.downloadDao().upsert(SongDownload(1, "s3", DownloadStatus.Queued))
            }

            val albums = runBlocking { repository.statuses(1, DownloadList.Album).first() }
            assertEquals(ListDownloadStatus(total = 2, downloaded = 1), albums["al1"])
            assertEquals(ListDownloadStatus(total = 1, downloading = 1), albums["al2"])

            val artists = runBlocking { repository.statuses(1, DownloadList.Artist).first() }
            assertEquals(ListDownloadStatus(total = 2, downloaded = 1), artists["ar1"])
            assertEquals(ListDownloadStatus(total = 1, downloading = 1), artists["ar2"])

            val playlists = runBlocking { repository.statuses(1, DownloadList.Playlist).first() }
            assertEquals(ListDownloadStatus(total = 2, downloaded = 1), playlists["pl1"])
        }

    @Test
    fun theManagementViewReportsFileSizesAndDeletesAWholeList() =
        runTest {
            seedLibrary()
            runBlocking {
                db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed, bytes = 10, total = 10))
                db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Queued))
            }
            writeFile(1, "s1", "audio")
            writeFile(1, "s2", "partial")

            val songs = runBlocking { repository.downloadedSongs(1).first() }
            assertEquals(setOf("s1", "s2"), songs.map { it.songId }.toSet())
            val completed = songs.first { it.songId == "s1" }
            assertEquals("Album", completed.albumName)
            assertEquals("Artist", completed.artistName)
            assertEquals(5L, completed.size)

            runBlocking { repository.removeList(1, DownloadList.Album, "al1") }

            assertTrue(allRows().isEmpty())
            assertFalse(file(1, "s1").exists())
            assertFalse(file(1, "s2").exists())
        }

    @Test
    fun theTreeGroupsAnAlbumUnderItsAlbumArtistNotEachTrackArtist() =
        runTest {
            seedLibrary()
            runBlocking {
                db.libraryDao().upsertSongs(
                    listOf(
                        Song(
                            sourceId = 1,
                            id = "s3",
                            albumId = "al1",
                            artistId = "ar-guest",
                            title = "Song 3",
                            album = "Album",
                            artist = "Guest",
                            duration = 100,
                            track = 3,
                            disc = 1,
                            starred = null,
                            genre = null,
                        ),
                    ),
                )
                db.libraryDao().upsertArtists(
                    listOf(Artist(sourceId = 1, id = "ar-guest", name = "Guest", albumCount = 0, starred = null, coverArt = null)),
                )
                db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed))
                db.downloadDao().upsert(SongDownload(1, "s3", DownloadStatus.Completed))
            }

            val songs = runBlocking { repository.downloadedSongs(1).first() }

            assertEquals(setOf("ar1" to "Artist"), songs.map { it.artistId to it.artistName }.toSet())
        }

    @Test
    fun activeDownloadsCoverEngineWorkInAnySourceButNotStalledWaitingRows() =
        runTest {
            seedLibrary()
            runBlocking {
                db.sourcesDao().upsertSource(Source(2, "other", "http://other/", isActive = false, createdAt = 0))
                val other =
                    Song(
                        sourceId = 2,
                        id = "o1",
                        albumId = "al2",
                        artistId = "ar2",
                        title = "Other",
                        album = "Other",
                        artist = "Other",
                        duration = 100,
                        track = 1,
                        disc = 1,
                        starred = null,
                        genre = null,
                    )
                db.libraryDao().upsertSongs(listOf(other, other.copy(id = "o2", track = 2)))
                db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Running, engineId = 1))
                db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Completed))
                db.downloadDao().upsert(SongDownload(2, "o1", DownloadStatus.Running, engineId = 2))
                db.downloadDao().upsert(SongDownload(2, "o2", DownloadStatus.Queued))
            }

            val rows = runBlocking { repository.activeDownloads().first() }

            assertEquals(setOf(1L to "s1", 2L to "o1"), rows.map { it.sourceId to it.songId }.toSet())
        }

    @Test
    fun aDownloadInASourceThatIsNotActiveIsStillReconciled() =
        runTest {
            seedLibrary()
            runBlocking {
                db.sourcesDao().upsertSource(Source(2, "other", "http://other/", isActive = false, createdAt = 0))
                db.libraryDao().upsertSongs(
                    listOf(
                        Song(
                            sourceId = 2,
                            id = "o1",
                            albumId = "al2",
                            artistId = "ar2",
                            title = "Other",
                            album = "Other",
                            artist = "Other",
                            duration = 100,
                            track = 1,
                            disc = 1,
                            starred = null,
                            genre = null,
                        ),
                    ),
                )
                db.downloadDao().upsert(SongDownload(2, "o1", DownloadStatus.Queued, engineId = 42))
            }
            engine.running(42, bytes = 1, total = 2)
            repository.start()

            await { row(2, "o1")?.status == DownloadStatus.Running }

            writeFile(2, "o1", "audio")
            engine.complete(42, total = 2)
            await { row(2, "o1")?.status == DownloadStatus.Completed }
        }

    @Test
    fun aServerErrorSavedAsThePayloadIsNotTreatedAsADownload() =
        runTest {
            seedLibrary()
            repository.start()
            runBlocking { repository.download(1, "s1") }
            val id = engine.requests.single().first

            engine.complete(id, total = 100)
            writeFile(
                1,
                "s1",
                "<?xml version=\"1.0\"?><subsonic-response status=\"failed\"><error code=\"70\"/></subsonic-response>",
            )
            runBlocking { repository.reconcile() }

            assertEquals(DownloadStatus.Failed, row(1, "s1")?.status)
            assertEquals(DownloadError.ServerError, row(1, "s1")?.error)
            assertFalse(file(1, "s1").exists())
            assertNull(repository.localUri("s1"))
        }

    @Test
    fun aCompletedRowLeftAsAnErrorPageIsFailedOnReconcile() =
        runTest {
            seedLibrary()
            runBlocking { db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed, bytes = 10, total = 10)) }
            writeFile(1, "s1", "<subsonic-response status=\"failed\"/>")

            runBlocking { repository.reconcile() }

            assertEquals(DownloadStatus.Failed, row(1, "s1")?.status)
            assertFalse(file(1, "s1").exists())
            assertNull(repository.localUri("s1"))
        }

    @Test
    fun downloadsAreWifiOnlyUntilThePreferenceAllowsMetered() =
        runTest {
            seedLibrary()
            runBlocking { repository.download(1, "s1") }
            assertFalse(
                engine.requests
                    .single()
                    .second.allowMetered,
            )

            prefs.setDownloadOverMetered(true)
            await { sources.downloadsAllowedOverMetered() }

            runBlocking { repository.download(1, "s2") }

            assertTrue(
                engine.requests
                    .last()
                    .second.allowMetered,
            )
        }

    private fun requestedSongIds(): List<String> = engine.requests.map { it.second.path.substringAfterLast('/') }

    private fun seedSongs(tracks: IntRange) {
        runBlocking {
            db.libraryDao().upsertSongs(
                tracks.map { track ->
                    Song(
                        sourceId = 1,
                        id = "s$track",
                        albumId = "al1",
                        artistId = "ar1",
                        title = "Song $track",
                        album = "Album",
                        artist = "Artist",
                        duration = 100,
                        track = track.toLong(),
                        disc = 1,
                        starred = null,
                        genre = null,
                    )
                },
            )
        }
    }

    private fun seedPlaylist(songIds: List<String>) {
        runBlocking {
            db.libraryDao().upsertPlaylists(listOf(Playlist(1, "pl1", "Playlist", null, null, songIds.size.toLong(), 0)))
            db.libraryDao().upsertPlaylistSongs(
                songIds.mapIndexed { index, songId -> PlaylistSong(1, "pl1", songId, index.toLong()) },
            )
        }
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

    private fun await(predicate: () -> Boolean) = awaitUntil("waiting for the expected download state", predicate)

    private companion object {
        const val ALBUM_ART = "art-al1"
        const val ARTIST_ART = "art-ar1"
        const val PLAYLIST_ART = "art-pl1"
    }
}
