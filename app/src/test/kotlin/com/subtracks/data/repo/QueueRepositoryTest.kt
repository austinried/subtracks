package com.subtracks.data.repo

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.Song
import com.subtracks.data.model.Source
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueueRepositoryTest {
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
    fun wholeAlbumEntryResolvesEveryTrackInOrder() =
        runTest {
            seedLibrary()

            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))

            assertEquals(3, snapshot.size)
            assertEquals(listOf("s1", "s2", "s3"), resolveAll(snapshot))
        }

    @Test
    fun rangeLimitsAnEntryToASlice() =
        runTest {
            seedLibrary()

            val entry =
                QueueEntry(
                    position = 0,
                    sourceId = 1,
                    kind = QueueKind.Album,
                    refId = "al1",
                    rangeStart = 1,
                    rangeEnd = 2,
                )
            val snapshot = repository.snapshotAfter(listOf(entry))

            assertEquals(2, snapshot.size)
            assertEquals(listOf("s2", "s3"), resolveAll(snapshot))
        }

    @Test
    fun theSongsEntryResolvesTheWholeLibraryInListOrder() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertAlbums(listOf(album("al0", "Zeroth Album", "Aardvark")))
            db.libraryDao().upsertSongs(listOf(song("s0", "al0", track = 1, album = "Zeroth Album")))

            val snapshot = repository.snapshotAfter(listOf(repository.songsEntry(1)))

            assertEquals(6, snapshot.size)
            assertEquals(listOf("s0", "s1", "s2", "s3", "s4", "s5"), resolveAll(snapshot))
        }

    @Test
    fun windowSpansEntryBoundaries() =
        runTest {
            seedLibrary()

            val entries =
                listOf(
                    repository.albumEntry(1, "al1"),
                    repository.songEntry(1, "s4"),
                )
            val snapshot = repository.snapshotAfter(entries)

            assertEquals(4, snapshot.size)
            val window = repository.window(snapshot, center = 3, radius = 1)
            assertEquals(listOf(2L, 3L), window.map { it.position })
            assertEquals(listOf("s3", "s4"), window.map { it.item.song.id })
        }

    @Test
    fun locateIdentifiesTheEntryBehindEachPosition() =
        runTest {
            seedLibrary()
            val entries = listOf(repository.albumEntry(1, "al1"), repository.songEntry(1, "s4"))
            val snapshot = repository.snapshotAfter(entries)

            val first = snapshot.locate(0)!!
            assertEquals(QueueKind.Album, first.first.kind)
            assertEquals("al1", first.first.refId)
            assertEquals(0L, first.second)

            val last = snapshot.locate(3)!!
            assertEquals(QueueKind.Song, last.first.kind)
            assertEquals("s4", last.first.refId)
            assertEquals(0L, last.second)
        }

    @Test
    fun aRangePastTheEndIsClamped() =
        runTest {
            seedLibrary()

            val entry =
                QueueEntry(
                    position = 0,
                    sourceId = 1,
                    kind = QueueKind.Album,
                    refId = "al1",
                    rangeStart = 1,
                    rangeEnd = 99,
                )
            val snapshot = repository.snapshotAfter(listOf(entry))

            assertEquals(2, snapshot.size)
            assertEquals(listOf("s2", "s3"), resolveAll(snapshot))
        }

    @Test
    fun anInvertedRangeIsEmpty() =
        runTest {
            seedLibrary()

            val entry =
                QueueEntry(
                    position = 0,
                    sourceId = 1,
                    kind = QueueKind.Album,
                    refId = "al1",
                    rangeStart = 2,
                    rangeEnd = 1,
                )
            val snapshot = repository.snapshotAfter(listOf(entry))

            assertEquals(0, snapshot.size)
            assertEquals(null, repository.itemAt(snapshot, 0))
        }

    @Test
    fun positionsPastTheEndResolveNothing() =
        runTest {
            seedLibrary()

            val snapshot = repository.snapshotAfter(listOf(repository.songEntry(1, "s4")))

            assertEquals(null, snapshot.locate(1))
            assertEquals(null, repository.itemAt(snapshot, 1))
        }

    @Test
    fun playlistIgnoresRowsWithoutABackingSong() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertPlaylistSongs(
                listOf(
                    PlaylistSong(1, "p1", "s1", 0),
                    PlaylistSong(1, "p1", "ghost", 1),
                ),
            )

            val snapshot = repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))

            assertEquals(1, snapshot.size)
            assertEquals(listOf("s1"), resolveAll(snapshot))
        }

    @Test
    fun anEntryForAMissingSongIsEmpty() =
        runTest {
            seedLibrary()

            val snapshot = repository.snapshotAfter(listOf(repository.songEntry(1, "ghost")))

            assertEquals(0, snapshot.size)
        }

    @Test
    fun playlistEntryFollowsThePlaylistOrder() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertPlaylistSongs(
                listOf(
                    PlaylistSong(1, "p1", "s3", 0),
                    PlaylistSong(1, "p1", "s1", 1),
                ),
            )

            val snapshot = repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))

            assertEquals(2, snapshot.size)
            assertEquals(listOf("s3", "s1"), resolveAll(snapshot))
        }

    @Test
    fun replaceDropsThePreviousQueue() =
        runTest {
            seedLibrary()
            repository.replace(listOf(repository.albumEntry(1, "al1")))
            repository.replace(listOf(repository.songEntry(1, "s4")))

            val snapshot = repository.snapshot()

            assertEquals(1, snapshot.size)
            assertEquals(listOf("s4"), resolveAll(snapshot))
        }

    private suspend fun QueueRepository.snapshotAfter(entries: List<QueueEntry>): QueueSnapshot {
        replace(entries)
        return snapshot()
    }

    private suspend fun resolveAll(snapshot: QueueSnapshot): List<String> =
        (0 until snapshot.size).mapNotNull { repository.itemAt(snapshot, it)?.song?.id }

    private suspend fun seedLibrary() {
        db.sourcesDao().upsertSource(
            Source(id = 1, name = "test", address = "http://localhost", isActive = true, createdAt = 0),
        )
        db.libraryDao().upsertAlbums(
            listOf(
                album("al1", "First Album", "Artist One"),
                album("al2", "Second Album", "Artist Two"),
            ),
        )
        db.libraryDao().upsertSongs(
            listOf(
                song("s1", "al1", track = 1, album = "First Album"),
                song("s2", "al1", track = 2, album = "First Album"),
                song("s3", "al1", track = 3, album = "First Album"),
                song("s4", "al2", track = 1, album = "Second Album"),
                song("s5", "al2", track = 2, album = "Second Album"),
            ),
        )
    }

    private fun album(
        id: String,
        name: String,
        albumArtist: String,
    ) = Album(
        sourceId = 1,
        id = id,
        artistId = "ar1",
        name = name,
        albumArtist = albumArtist,
        created = 0,
        coverArt = null,
        genre = null,
        year = null,
        starred = null,
        songCount = 1,
        frequentRank = null,
        recentRank = null,
    )

    private fun song(
        id: String,
        albumId: String,
        track: Long,
        album: String = "Album",
    ) = Song(
        sourceId = 1,
        id = id,
        albumId = albumId,
        artistId = "ar1",
        title = "Song $id",
        album = album,
        artist = "Artist",
        duration = 100,
        track = track,
        disc = 1,
        starred = null,
        genre = null,
    )
}
