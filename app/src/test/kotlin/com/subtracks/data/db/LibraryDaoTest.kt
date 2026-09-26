package com.subtracks.data.db

import android.content.Context
import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Album
import com.subtracks.data.model.Song
import com.subtracks.data.model.Source
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryDaoTest {
    private lateinit var db: SubtracksDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun albumSortsAndStarredFilter() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a", "Beta", year = 2000, starred = null),
                    album(sourceId, "b", "Alpha", year = 2010, starred = 5),
                    album(sourceId, "c", "Gamma", year = 1990, starred = null),
                ),
            )

            assertEquals(
                listOf("Alpha", "Beta", "Gamma"),
                dao.albumsByName(sourceId, starredFilter = 0, search = "").page().map { it.name },
            )
            assertEquals(
                listOf("Gamma", "Beta", "Alpha"),
                dao.albumsByNameReversed(sourceId, starredFilter = 0, search = "").page().map { it.name },
            )
            assertEquals(
                listOf("Alpha", "Beta", "Gamma"),
                dao.albumsByYear(sourceId, starredFilter = 0, search = "").page().map { it.name },
            )
            assertEquals(listOf("Alpha"), dao.albumsByName(sourceId, starredFilter = 1, search = "").page().map { it.name })
        }

    @Test
    fun albumAddedAndStarredSortsAndSearch() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a", "Beta", year = 2000, starred = null, created = 300),
                    album(sourceId, "b", "Alpha", year = 2010, starred = 200, created = 100),
                    album(sourceId, "c", "Gamma", year = 1990, starred = 100, created = 200),
                ),
            )

            assertEquals(
                listOf("Beta", "Gamma", "Alpha"),
                dao.albumsByRecentlyAdded(sourceId, starredFilter = 0, search = "").page().map { it.name },
            )
            assertEquals(
                listOf("Alpha", "Gamma", "Beta"),
                dao.albumsByStarred(sourceId, starredFilter = 0, search = "").page().map { it.name },
            )
            assertEquals(listOf("Gamma"), dao.albumsByName(sourceId, starredFilter = 0, search = "gam").page().map { it.name })
            assertEquals(listOf("Beta"), dao.albumsByName(sourceId, starredFilter = 2, search = "").page().map { it.name })
        }

    @Test
    fun albumSongsAreOrderedByDiscAndTrack() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertSongs(
                listOf(
                    song(sourceId, "s2", "Second", starred = null, track = 2),
                    song(sourceId, "s3", "Third", starred = null, track = 1, disc = 2),
                    song(sourceId, "s1", "First", starred = null, track = 1),
                ),
            )

            assertEquals(
                listOf("s1", "s2", "s3"),
                dao.songsByAlbum(sourceId, "al-1").first().map { it.id },
            )
        }

    private suspend fun source(): Long =
        db.sourcesDao().insertSource(Source(name = "Navidrome", address = "http://localhost", isActive = true, createdAt = 0))

    private suspend fun <T : Any> PagingSource<Int, T>.page(): List<T> =
        (
            load(
                PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false),
            ) as PagingSource.LoadResult.Page
        ).data

    private fun album(
        sourceId: Long,
        id: String,
        name: String,
        year: Long?,
        starred: Long?,
        created: Long = 0,
    ) = Album(
        sourceId = sourceId,
        id = id,
        artistId = "ar-1",
        name = name,
        albumArtist = "Artist",
        created = created,
        coverArt = null,
        genre = null,
        year = year,
        starred = starred,
        songCount = 1,
    )

    private fun song(
        sourceId: Long,
        id: String,
        title: String,
        starred: Long?,
        created: Long = 0,
        track: Long = 1,
        disc: Long = 1,
    ) = Song(
        sourceId = sourceId,
        id = id,
        albumId = "al-1",
        artistId = "ar-1",
        title = title,
        album = "Album",
        artist = "Artist",
        duration = 200,
        track = track,
        disc = disc,
        starred = starred,
        genre = null,
        created = created,
    )
}
