package com.subtracks.ui.downloads

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.download.FakeDownloadEngine
import com.subtracks.data.model.DownloadList
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.NetworkServerActionSink
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.FakePlayerConnection
import com.subtracks.playback.FakePlayerHandle
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class DownloadsViewModelTest {
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private lateinit var db: SubtracksDatabase
    private lateinit var sourceRepository: SourceRepository
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var controller: PlaybackController
    private lateinit var viewModel: DownloadsViewModel
    private var sourceId: Long = 0

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        val artwork = ArtworkStore(File(context.cacheDir, "art-${System.nanoTime()}"))
        sourceRepository = SourceRepository(db, OkHttpClient(), fakeUserPreferences(), artwork)
        downloadRepository =
            DownloadRepository(
                db,
                sourceRepository,
                FakeDownloadEngine(),
                File(context.cacheDir, "downloads-${System.nanoTime()}"),
                artworkStore = artwork,
                artworkFetcher = { ByteArray(0) },
            )
        controller =
            PlaybackController(
                sourceRepository,
                QueueRepository(db),
                FakePlayerConnection(FakePlayerHandle()),
                downloadRepository,
                dispatcher = dispatcher,
            )
        viewModel =
            DownloadsViewModel(
                LibraryRepository(db, sourceRepository, NetworkServerActionSink(sourceRepository)),
                downloadRepository,
                controller,
            )
    }

    @After
    fun tearDown() {
        runBlocking { viewModel.viewModelScope.coroutineContext[Job]?.cancelAndJoin() }
        controller.close()
        runBlocking { controller.awaitStopped() }
        downloadRepository.close()
        sourceRepository.close()
        db.close()
        Dispatchers.resetMain()
        dispatcher.close()
    }

    @Test
    fun deleteAllRemovesEveryDownloadOfTheSource() {
        seedDownloads()

        viewModel.deleteAll()
        await { allRows().isEmpty() }

        assertTrue("every download row should be gone", allRows().isEmpty())
    }

    @Test
    fun deleteRemovesOnlyTheSongsOfThatAlbum() {
        seedDownloads()

        viewModel.delete(DownloadList.Album, "al1")
        await { row("s1") == null && row("s2") == null }

        assertNull(row("s1"))
        assertNull(row("s2"))
        assertNotNull("a song from another album should survive", row("s3"))
    }

    @Test
    fun deleteSongsRemovesOnlyTheGivenSongs() {
        seedDownloads()

        viewModel.deleteSongs(listOf("s1"))
        await { row("s1") == null }

        assertNull(row("s1"))
        assertNotNull("an unnamed song should survive", row("s2"))
    }

    private fun seedDownloads() {
        sourceId =
            runBlocking {
                val id = sourceRepository.addSource("nav", "http://localhost/", "u", "p", false)
                db.libraryDao().upsertSongs(
                    listOf(
                        song(id, "s1", "al1"),
                        song(id, "s2", "al1"),
                        song(id, "s3", "al2"),
                    ),
                )
                listOf("s1", "s2", "s3").forEach { db.downloadDao().upsert(SongDownload(id, it, DownloadStatus.Completed)) }
                id
            }
    }

    private fun song(
        sourceId: Long,
        songId: String,
        albumId: String,
    ) = Song(
        sourceId = sourceId,
        id = songId,
        albumId = albumId,
        artistId = "ar1",
        title = songId,
        album = "Album",
        artist = "Artist",
        duration = 100,
        track = 1,
        disc = 1,
        starred = null,
        genre = null,
    )

    private fun row(songId: String): SongDownload? = runBlocking { db.downloadDao().find(sourceId, songId) }

    private fun allRows(): List<SongDownload> = runBlocking { db.downloadDao().all() }

    private fun await(predicate: () -> Boolean) {
        val deadline = System.nanoTime() + 5_000_000_000L
        while (!predicate() && System.nanoTime() < deadline) Thread.sleep(10)
        assertTrue("Timed out waiting for the expected state", predicate())
    }
}
