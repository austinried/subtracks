package com.subtracks.data.repo

import android.content.Context
import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.model.Source
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueueKeysetTest {
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
    fun albumKeysetFromMatchesOffsetWindowAtADeepPosition() =
        runTest {
            seedLibrary()
            val all = allAlbumRows("big")
            val anchor = all[200]

            val expected = all.drop(200).take(50).map { it.song.id }
            val actual =
                db
                    .queueDao()
                    .albumSongsFrom(1, "big", anchor.song.disc!!, anchor.song.track!!, anchor.song.id, skip = 0, limit = 50)
                    .map { it.song.id }

            assertEquals(expected, actual)
        }

    @Test
    fun albumKeysetBeforeMatchesOffsetWindowAtADeepPosition() =
        runTest {
            seedLibrary()
            val all = allAlbumRows("big")
            val anchor = all[200]

            val expected = all.drop(150).take(50).map { it.song.id }
            val actual =
                db
                    .queueDao()
                    .albumSongsBefore(1, "big", anchor.song.disc!!, anchor.song.track!!, anchor.song.id, skip = 0, limit = 50)
                    .asReversed()
                    .map { it.song.id }

            assertEquals(expected, actual)
        }

    @Test
    fun albumKeysetRespectsDiscThenTrackThenId() =
        runTest {
            seedLibrary()
            val all = allAlbumRows("big").map { it.song.id }

            assertEquals(300, all.size)
            assertEquals("b0", all.first())
            assertEquals("b150", all[150])

            val anchor = allAlbumRows("big")[150]
            val actual =
                db
                    .queueDao()
                    .albumSongsFrom(1, "big", anchor.song.disc!!, anchor.song.track!!, anchor.song.id, skip = 0, limit = 3)
                    .map { it.song.id }
            assertEquals(all.drop(150).take(3), actual)
        }

    @Test
    fun albumKeysetBreaksTiesOnId() =
        runTest {
            seedLibrary()
            val all = allAlbumRows("tie").map { it.song.id }

            assertEquals(listOf("t1a", "t1z"), all)
        }

    @Test
    fun playlistKeysetMatchesOffsetWindowWithGapsAndGhosts() =
        runTest {
            seedLibrary()
            val all = allPlaylistRows().map { it.song.id }
            assertTrue(all.size >= 12)

            val anchor = allPlaylistRows()[10]
            val expected = all.drop(10).take(5)
            val actual =
                db
                    .queueDao()
                    .playlistSongsFrom(1, "pl1", anchor.position!!, skip = 0, limit = 5)
                    .map { it.song.id }
            assertEquals(expected, actual)
        }

    @Test
    fun scanningTheWholeAlbumThroughTheRepositoryUsesKeysetSeeks() =
        runTest {
            seedLibrary()
            repository.replace(listOf(repository.albumEntry(1, "big")))
            val snapshot = repository.snapshot()
            val expected = allAlbumRows("big").map { it.song.id }

            val resolved = (0 until snapshot.size).map { repository.itemAt(snapshot, it)?.song?.id }
            assertEquals(expected, resolved)
            assertTrue("expected keyset seeks, saw ${repository.keysetSeeks}", repository.keysetSeeks >= snapshot.size - 1)
            assertEquals(1L, repository.offsetSeeks)
        }

    @Test
    fun scanningBackwardsThroughTheRepositoryUsesKeysetSeeks() =
        runTest {
            seedLibrary()
            repository.replace(listOf(repository.albumEntry(1, "big")))
            val snapshot = repository.snapshot()
            val expected = allAlbumRows("big").map { it.song.id }

            val resolved =
                (snapshot.size - 1 downTo 0).map { repository.itemAt(snapshot, it)?.song?.id }.asReversed()
            assertEquals(expected, resolved)
            assertTrue(repository.keysetSeeks >= snapshot.size - 1)
        }

    @Test
    fun aWindowStraddlingTheCachedAnchorResolvesLikeOffset() =
        runTest {
            seedLibrary()
            repository.replace(listOf(repository.albumEntry(1, "big")))
            val snapshot = repository.snapshot()
            repository.itemAt(snapshot, 200)

            val range = repository.range(snapshot, first = 190, last = 210)

            val expected = allAlbumRows("big").drop(190).take(21).map { it.song.id }
            assertEquals(expected, range.map { it.item.song.id })
            assertEquals((190L..210L).toList(), range.map { it.position })
        }

    @Test
    fun aSplitRangeAfterRemovingAMiddleTrackMatchesTheOracle() =
        runTest {
            seedLibrary()
            repository.replace(listOf(repository.albumEntry(1, "big")))
            val before = repository.snapshot()
            repository.removeAt(before, 100)

            val after = repository.snapshot()
            val resolved = (0 until after.size).map { repository.itemAt(after, it)?.song?.id }
            val all = allAlbumRows("big").map { it.song.id }
            val expected = all.filterIndexed { index, _ -> index != 100 }

            assertEquals(expected, resolved)
            assertTrue(repository.keysetSeeks > 0)
        }

    @Test
    fun aShuffledWindowResolvesEveryTrackExactlyOnce() =
        runTest {
            seedLibrary()
            repository.replace(listOf(repository.albumEntry(1, "big")))
            repository.setShuffle(4242L)
            val snapshot = repository.snapshot()

            val window = repository.window(snapshot, center = snapshot.size / 2, radius = 20)
            val ids = window.map { it.item.song.id }

            assertEquals(window.size, ids.distinct().size)
            val expected = allAlbumRows("big").map { it.song.id }
            assertEquals(expected.size, snapshot.size.toInt())
            assertTrue(ids.all { it in expected })
        }

    @Test
    fun theAlbumKeysetQuerySeeksTheOrderIndex() =
        runTest {
            seedLibrary()
            val plan =
                queryPlan(
                    "SELECT songs.*, albums.coverArt AS coverArt, NULL AS position FROM songs " +
                        "LEFT JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId " +
                        "WHERE songs.sourceId = 1 AND songs.albumId = 'big' " +
                        "AND (songs.disc, songs.track, songs.id) >= (1, 1, 'b0') " +
                        "ORDER BY songs.disc, songs.track, songs.id LIMIT 50 OFFSET 0",
                )

            assertTrue(plan.joinToString("\n"), plan.any { it.contains("index_songs_sourceId_albumId_order") })
        }

    private suspend fun queryPlan(sql: String): List<String> =
        db.useReaderConnection { transactor ->
            transactor.usePrepared("EXPLAIN QUERY PLAN $sql") { statement ->
                buildList {
                    while (statement.step()) add(statement.getText(3))
                }
            }
        }

    private suspend fun allAlbumRows(albumId: String) = db.queueDao().albumSongs(1, albumId, 0, 1000)

    private suspend fun allPlaylistRows() = db.queueDao().playlistSongs(1, "pl1", 0, 1000)

    private suspend fun seedLibrary() {
        db.sourcesDao().upsertSource(
            Source(id = 1, name = "test", address = "http://localhost", isActive = true, createdAt = 0),
        )
        db.libraryDao().upsertAlbums(
            listOf(
                album("big", "Big Album"),
                album("tie", "Tie Album"),
            ),
        )
        val big =
            (0 until 300).map { index ->
                val disc = if (index < 150) 1L else 2L
                val track = (index % 150 + 1).toLong()
                song(id = "b$index", albumId = "big", disc = disc, track = track)
            }
        val tie =
            listOf(
                song(id = "t1z", albumId = "tie", disc = 1, track = 1),
                song(id = "t1a", albumId = "tie", disc = 1, track = 1),
            )
        db.libraryDao().upsertSongs(big + tie)

        val positions = (0 until 20).map { it * 2L }
        val playlistSongs =
            positions.flatMapIndexed { index, position ->
                if (index == 5) {
                    listOf(PlaylistSong(1, "pl1", "ghost", position))
                } else {
                    listOf(PlaylistSong(1, "pl1", "b${index * 7}", position))
                }
            }
        db.libraryDao().upsertPlaylistSongs(playlistSongs)
    }

    private fun album(
        id: String,
        name: String,
    ) = Album(
        sourceId = 1,
        id = id,
        artistId = "ar1",
        name = name,
        albumArtist = "Artist",
        created = 0,
        coverArt = null,
        genre = null,
        year = null,
        starred = null,
        songCount = 1,
    )

    private fun song(
        id: String,
        albumId: String,
        disc: Long,
        track: Long,
    ) = Song(
        sourceId = 1,
        id = id,
        albumId = albumId,
        artistId = "ar1",
        title = "Song $id",
        album = "Album",
        artist = "Artist",
        duration = 100,
        track = track,
        disc = disc,
        starred = null,
        genre = null,
        created = 0,
    )
}
