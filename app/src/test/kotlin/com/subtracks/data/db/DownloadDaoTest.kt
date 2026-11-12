package com.subtracks.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.Source
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DownloadDaoTest {
    private lateinit var db: SubtracksDatabase

    @Before
    fun setUp() {
        db = createTestDatabase()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun completedDownloadsUseTheDownloadedQueueVisibilityAndOrder() =
        runTest {
            val sourceId =
                db.sourcesDao().insertSource(
                    Source(name = "source", address = "http://localhost", isActive = true, createdAt = 0),
                )
            db.libraryDao().upsertSongs(
                listOf(
                    song(sourceId, "s1", "Zulu"),
                    song(sourceId, "s2", "Alpha"),
                    song(sourceId, "s3", "Running"),
                    song(sourceId, "s4", "Pruned"),
                ),
            )
            db.downloadDao().upsert(SongDownload(sourceId, "s1", DownloadStatus.Completed, downloadedAt = 1))
            db.downloadDao().upsert(SongDownload(sourceId, "s2", DownloadStatus.Completed, downloadedAt = 1))
            db.downloadDao().upsert(SongDownload(sourceId, "s3", DownloadStatus.Running, downloadedAt = 2))
            db.downloadDao().upsert(SongDownload(sourceId, "s4", DownloadStatus.Completed, downloadedAt = 3))
            db.libraryDao().deleteSongs(sourceId, listOf("s4"))

            assertEquals(listOf("s2", "s1"), db.downloadDao().completedDownloads(sourceId).map { it.songId })
            assertEquals(
                setOf("s1", "s2", "s3"),
                db.downloadDao().queueVisibleSongIds(sourceId, listOf("s1", "s2", "s3", "s4")).toSet(),
            )
        }

    private fun song(
        sourceId: Long,
        id: String,
        sortName: String,
    ) = Song(
        sourceId = sourceId,
        id = id,
        albumId = "al1",
        artistId = "ar1",
        title = sortName,
        sortName = sortName,
        album = "Album",
        artist = "Artist",
        duration = 100,
        track = 1,
        disc = 1,
        starred = null,
        genre = null,
    )
}
