package com.subtracks.data.repo

import android.content.Context
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongListItem
import com.subtracks.data.model.Source
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueuePagingSourceTest {
    private lateinit var db: SubtracksDatabase
    private lateinit var repository: QueueRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        repository = QueueRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun pagesAreContiguousAndDoNotOverlap() =
        runTest {
            seedSongs(130)
            repository.replace(listOf(repository.songsEntry(1)))
            val source = repository.pagingSource(initialPosition = 0)

            val first = load(source, key = null)
            assertEquals((0L until 60L).toList(), first.data.map { it.position })
            assertEquals(1L, first.nextKey)
            assertEquals(null, first.prevKey)

            val second = load(source, key = 1L)
            assertEquals((60L until 120L).toList(), second.data.map { it.position })
            assertEquals(2L, second.nextKey)
            assertEquals(0L, second.prevKey)

            val third = load(source, key = 2L)
            assertEquals((120L until 130L).toList(), third.data.map { it.position })
            assertEquals(null, third.nextKey)
            assertEquals(1L, third.prevKey)
        }

    @Test
    fun theFirstPageStartsAtTheInitialPosition() =
        runTest {
            seedSongs(130)
            repository.replace(listOf(repository.songsEntry(1)))
            val source = repository.pagingSource(initialPosition = 100)

            val first = load(source, key = null)

            assertEquals((60L until 120L).toList(), first.data.map { it.position })
            assertEquals(2L, first.nextKey)
            assertEquals(0L, first.prevKey)
        }

    @Test
    fun theRefreshKeyUsesTheLoadedPositions() =
        runTest {
            val source = repository.pagingSource(initialPosition = 100)
            val state =
                PagingState(
                    pages = listOf(PagingSource.LoadResult.Page(listOf(itemAt(60)), prevKey = 0L, nextKey = 2L)),
                    anchorPosition = 5,
                    config = PagingConfig(pageSize = QUEUE_PAGE_SIZE, enablePlaceholders = false),
                    leadingPlaceholderCount = 0,
                )

            assertEquals(1L, source.getRefreshKey(state))
        }

    @Test
    fun anEmptyQueueLoadsNothing() =
        runTest {
            val result = load(repository.pagingSource(0), key = null)

            assertEquals(emptyList<QueueWindowItem>(), result.data)
            assertEquals(null, result.nextKey)
            assertEquals(null, result.prevKey)
        }

    private fun itemAt(position: Long) =
        QueueWindowItem(
            position,
            SongListItem(
                song =
                    Song(
                        sourceId = 1,
                        id = "s$position",
                        albumId = "al1",
                        artistId = "ar1",
                        title = "Song $position",
                        album = "Album",
                        artist = "Artist",
                        duration = 100,
                        track = position,
                        disc = 1,
                        starred = null,
                        genre = null,
                    ),
                coverArt = null,
            ),
        )

    private suspend fun load(
        source: PagingSource<Long, QueueWindowItem>,
        key: Long?,
    ): PagingSource.LoadResult.Page<Long, QueueWindowItem> {
        val params = PagingSource.LoadParams.Refresh<Long>(key, QUEUE_PAGE_SIZE, false)
        return source.load(params) as PagingSource.LoadResult.Page<Long, QueueWindowItem>
    }

    private suspend fun seedSongs(count: Int) {
        db.sourcesDao().upsertSource(
            Source(id = 1, name = "test", address = "http://localhost", isActive = true, createdAt = 0),
        )
        db.libraryDao().upsertSongs(
            (1..count).map { track ->
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
