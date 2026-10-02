package com.subtracks.data.db

import android.content.Context
import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
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
class LibraryHomeDaoTest {
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
    fun recentlyPlayedAlbumsOrderByMostRecentFirstAndLimit() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a", "Alpha", played = 100),
                    album(sourceId, "b", "Beta", played = 300),
                    album(sourceId, "c", "Gamma", played = 200),
                    album(sourceId, "d", "Never", played = null),
                ),
            )

            assertEquals(
                listOf("Beta", "Gamma"),
                dao.recentlyPlayedAlbums(sourceId, limit = 2).first().map { it.name },
            )
            assertEquals(
                listOf("Beta", "Gamma", "Alpha"),
                dao.recentlyPlayedAlbums(sourceId, limit = 10).first().map { it.name },
            )
        }

    @Test
    fun mostPlayedAlbumsIgnoreUnplayedAndOrderByCount() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a", "Alpha", playCount = 3),
                    album(sourceId, "b", "Beta", playCount = 9),
                    album(sourceId, "c", "Gamma", playCount = 0),
                ),
            )

            assertEquals(
                listOf("Beta", "Alpha"),
                dao.mostPlayedAlbums(sourceId, limit = 10).first().map { it.name },
            )
        }

    @Test
    fun artistsUseAggregatedPlayData() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertArtists(
                listOf(
                    artist(sourceId, "r1", "Alpha", played = 50, playCount = 1),
                    artist(sourceId, "r2", "Beta", played = 200, playCount = 5),
                    artist(sourceId, "r3", "Gamma", played = null, playCount = 0),
                ),
            )

            assertEquals(
                listOf("Beta", "Alpha"),
                dao.recentlyPlayedArtists(sourceId, limit = 10).first().map { it.name },
            )
            assertEquals(
                listOf("Beta", "Alpha"),
                dao.mostPlayedArtists(sourceId, limit = 10).first().map { it.name },
            )
        }

    @Test
    fun recentlyStarredSongsAreNewestFirstJoinAlbumCoverArtAndLimit() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(listOf(album(sourceId, "al1", "Album", coverArt = "cover-1")))
            dao.upsertSongs(
                listOf(
                    song(sourceId, "s1", "First", starred = 100),
                    song(sourceId, "s2", "Second", starred = 300),
                    song(sourceId, "s3", "Third", starred = null),
                ),
            )

            val starred = dao.recentlyStarredSongs(sourceId, limit = 1).first()
            assertEquals(listOf("s2"), starred.map { it.song.id })
            assertEquals("cover-1", starred.single().coverArt)
        }

    @Test
    fun genresOrderByMostRecentPlayWithinTheGenre() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertSongs(
                listOf(
                    song(sourceId, "s1", "One", genre = "Rock", played = 100),
                    song(sourceId, "s2", "Two", genre = "Jazz", played = 400),
                    song(sourceId, "s3", "Three", genre = "Rock", played = 300),
                    song(sourceId, "s4", "Four", genre = "Classical", played = null),
                ),
            )

            assertEquals(
                listOf("Jazz", "Rock", "Classical"),
                dao.genresByRecentPlay(sourceId).first(),
            )
        }

    @Test
    fun genresComeFromTheGenreRelationsAndFallBackPerSong() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertSongs(
                listOf(
                    song(sourceId, "s1", "One", genre = "Legacy", played = 100),
                    song(sourceId, "s2", "Two", genre = "Legacy", played = 300),
                ),
            )
            insertGenre(sourceId, "s2", 0, "Rock")
            insertGenre(sourceId, "s2", 1, "Electronic")

            assertEquals(
                listOf("Electronic", "Rock", "Legacy"),
                dao.genresByRecentPlay(sourceId).first(),
            )
        }

    @Test
    fun songsByGenreMatchesTheGenreRelationsWithFallback() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(listOf(album(sourceId, "al1", "Album", coverArt = "cover-1")))
            dao.upsertSongs(
                listOf(
                    song(sourceId, "s1", "Fallback", genre = "Rock"),
                    song(sourceId, "s2", "Relation", genre = "Jazz"),
                    song(sourceId, "s3", "NotRock", genre = "Rock"),
                ),
            )
            insertGenre(sourceId, "s2", 0, "Rock")
            insertGenre(sourceId, "s3", 0, "Jazz")

            val page = dao.songsByGenre(sourceId, "Rock").page()
            assertEquals(listOf("s1", "s2"), page.map { it.song.id })
            assertEquals("cover-1", page.first().coverArt)
        }

    private suspend fun insertGenre(
        sourceId: Long,
        songId: String,
        position: Long,
        genre: String,
    ) {
        db.useWriterConnection { connection ->
            connection.usePrepared(
                "INSERT INTO song_genres (sourceId, songId, position, genre) " +
                    "VALUES ($sourceId, '$songId', $position, '$genre')",
            ) { it.step() }
        }
    }

    @Test
    fun decadesAreDistinctAndOldestFirstExcludingMissingYears() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a", "A", year = 1975),
                    album(sourceId, "b", "B", year = 1979),
                    album(sourceId, "c", "C", year = 2001),
                    album(sourceId, "d", "D", year = null),
                    album(sourceId, "e", "E", year = 0),
                ),
            )

            assertEquals(listOf(1970L, 2000L), dao.decades(sourceId).first())
        }

    @Test
    fun rediscoverOnlyIncludesStalePlaysOldestFirst() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a", "Recent", played = 900),
                    album(sourceId, "b", "Old", played = 100),
                    album(sourceId, "c", "Older", played = 50),
                    album(sourceId, "d", "Never", played = null),
                ),
            )

            assertEquals(
                listOf("Older", "Old"),
                dao.rediscoverAlbums(sourceId, cutoff = 500, limit = 10).first().map { it.name },
            )
        }

    @Test
    fun homeMorePagingSourcesMatchTheirRowsWithoutLimits() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a", "Alpha", created = 100, playCount = 3, played = 100),
                    album(sourceId, "b", "Beta", created = 300, playCount = 9, played = 300),
                    album(sourceId, "c", "Gamma", created = 200, playCount = 0, played = null),
                ),
            )
            dao.upsertArtists(
                listOf(
                    artist(sourceId, "r1", "Alpha", playCount = 3, played = 100),
                    artist(sourceId, "r2", "Beta", playCount = 9, played = 300),
                    artist(sourceId, "r3", "Gamma", playCount = 0, played = null),
                ),
            )

            assertEquals(
                listOf("Beta", "Alpha"),
                dao.homeRecentlyPlayedAlbums(sourceId).page().map { it.name },
            )
            assertEquals(
                listOf("Beta", "Alpha"),
                dao.homeMostPlayedAlbums(sourceId).page().map { it.name },
            )
            assertEquals(
                listOf("Beta", "Gamma", "Alpha"),
                dao.homeRecentlyAddedAlbums(sourceId).page().map { it.name },
            )
            assertEquals(
                listOf("Beta", "Alpha"),
                dao.homeRecentlyPlayedArtists(sourceId).page().map { it.name },
            )
            assertEquals(
                listOf("Beta", "Alpha"),
                dao.homeMostPlayedArtists(sourceId).page().map { it.name },
            )
        }

    @Test
    fun rediscoverPagingSourceOrdersOldestFirst() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a", "Old", played = 100),
                    album(sourceId, "b", "Older", played = 50),
                    album(sourceId, "c", "Recent", played = 900),
                ),
            )

            assertEquals(
                listOf("Older", "Old"),
                dao.albumsByRediscover(sourceId, starredFilter = 0, search = "", cutoff = 500).page().map { it.name },
            )
        }

    @Test
    fun starredSongsPagingJoinsAlbumCoverArt() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(listOf(album(sourceId, "al1", "Album", coverArt = "cover-1")))
            dao.upsertSongs(
                listOf(
                    song(sourceId, "s1", "First", starred = 100),
                    song(sourceId, "s2", "Second", starred = 300),
                    song(sourceId, "s3", "Third", starred = null),
                ),
            )

            val page = dao.starredSongs(sourceId).page()
            assertEquals(listOf("s2", "s1"), page.map { it.song.id })
            assertEquals("cover-1", page.first().coverArt)
        }

    @Test
    fun songsByGenrePagingFiltersAndJoinsAlbumCoverArt() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(listOf(album(sourceId, "al1", "Album", coverArt = "cover-1")))
            dao.upsertSongs(
                listOf(
                    song(sourceId, "s1", "Rock One", genre = "Rock"),
                    song(sourceId, "s2", "Jazz One", genre = "Jazz"),
                    song(sourceId, "s3", "Rock Two", genre = "Rock"),
                ),
            )

            val page = dao.songsByGenre(sourceId, "Rock").page()
            assertEquals(listOf("s1", "s3"), page.map { it.song.id })
            assertEquals("cover-1", page.first().coverArt)
        }

    @Test
    fun albumsByDecadePagingFiltersTheYearRange() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a", "A", year = 1969),
                    album(sourceId, "b", "B", year = 1970),
                    album(sourceId, "c", "C", year = 1979),
                    album(sourceId, "d", "D", year = 1980),
                ),
            )

            assertEquals(
                listOf("C", "B"),
                dao.albumsByDecade(sourceId, start = 1970, end = 1980).page().map { it.name },
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
        year: Long? = null,
        coverArt: String? = null,
        created: Long = 0,
        playCount: Long = 0,
        played: Long? = null,
    ) = Album(
        sourceId = sourceId,
        id = id,
        artistId = "ar-1",
        name = name,
        albumArtist = "Artist",
        created = created,
        coverArt = coverArt,
        genre = null,
        year = year,
        starred = null,
        songCount = 1,
        playCount = playCount,
        played = played,
    )

    private fun artist(
        sourceId: Long,
        id: String,
        name: String,
        playCount: Long = 0,
        played: Long? = null,
    ) = Artist(
        sourceId = sourceId,
        id = id,
        name = name,
        albumCount = 1,
        starred = null,
        coverArt = null,
        playCount = playCount,
        played = played,
    )

    private fun song(
        sourceId: Long,
        id: String,
        title: String,
        starred: Long? = null,
        genre: String? = null,
        playCount: Long = 0,
        played: Long? = null,
    ) = Song(
        sourceId = sourceId,
        id = id,
        albumId = "al1",
        artistId = "ar-1",
        title = title,
        album = "Album",
        artist = "Artist",
        duration = 200,
        track = 1,
        disc = 1,
        starred = starred,
        genre = genre,
        created = 0,
        playCount = playCount,
        played = played,
    )
}
