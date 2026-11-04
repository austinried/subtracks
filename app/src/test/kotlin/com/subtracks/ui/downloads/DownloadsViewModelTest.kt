package com.subtracks.ui.downloads

import androidx.lifecycle.viewModelScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.awaitUntil
import com.subtracks.data.model.DownloadList
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.ui.ViewModelRepositoryFixture
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DownloadsViewModelTest {
    private lateinit var fixture: ViewModelRepositoryFixture
    private val db get() = fixture.db
    private val sourceRepository get() = fixture.sourceRepository
    private val downloadRepository get() = fixture.downloadRepository
    private val controller get() = fixture.controller
    private lateinit var viewModel: DownloadsViewModel
    private var sourceId: Long = 0

    @Before
    fun setUp() {
        fixture = ViewModelRepositoryFixture()
        viewModel =
            DownloadsViewModel(
                fixture.libraryRepository,
                downloadRepository,
                controller,
                "Unknown artist",
                "Unknown album",
            )
    }

    @After
    fun tearDown() {
        runBlocking { viewModel.viewModelScope.coroutineContext[Job]?.cancelAndJoin() }
        fixture.close()
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

    @Test
    fun cancelSongsRemovesOnlyTheActiveDownload() {
        seedDownloads()
        runBlocking { db.downloadDao().upsert(SongDownload(sourceId, "s1", DownloadStatus.Running, engineId = 7)) }

        viewModel.cancelSongs(listOf("s1", "s2"))
        await { row("s1") == null }

        assertNull(row("s1"))
        assertNotNull("a completed download should survive a cancel", row("s2"))
    }

    @Test
    fun cancelSongsRemovesAQueuedDownloadWithoutAnEngineId() {
        seedDownloads()
        runBlocking { db.downloadDao().upsert(SongDownload(sourceId, "s1", DownloadStatus.Queued, engineId = null)) }

        viewModel.cancelSongs(listOf("s1"))
        await { row("s1") == null }

        assertNull(row("s1"))
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

    private fun await(predicate: () -> Boolean) = awaitUntil("waiting for the expected state", predicate)
}
