package com.subtracks.data.repo

import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.db.createTestDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.QueueOrigin
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.Source
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueueRepositoryTest {
    private lateinit var db: SubtracksDatabase
    private lateinit var repository: QueueRepository

    @Before
    fun setUp() {
        db = createTestDatabase()
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
    fun wholeGenreEntryResolvesEveryTrackInTitleOrder() =
        runTest {
            seedLibrary()
            insertGenre("s4", "Rock")
            insertGenre("s1", "Rock")
            insertGenre("s3", "Rock")
            insertGenre("s2", "Jazz")

            val snapshot = repository.snapshotAfter(listOf(repository.genreEntry(1, "Rock")))

            assertEquals(3, snapshot.size)
            assertEquals(listOf("s1", "s3", "s4"), resolveAll(snapshot))
        }

    @Test
    fun downloadedEntryResolvesOnlyCompletedDownloads() =
        runTest {
            seedLibrary()
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed))
            db.downloadDao().upsert(SongDownload(1, "s3", DownloadStatus.Completed))
            db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Running))

            val snapshot = repository.snapshotAfter(listOf(repository.downloadedEntry(1)))

            assertEquals(2, snapshot.size)
            assertEquals(listOf("s1", "s3"), resolveAll(snapshot))
        }

    @Test
    fun downloadedEntryIgnoresRetainedRowsWhoseSongsArePruned() =
        runTest {
            seedLibrary()
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed))
            db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Completed))
            db.libraryDao().deleteSongs(1, listOf("s2"))

            val snapshot = repository.snapshotAfter(listOf(repository.downloadedEntry(1)))

            assertEquals(1, snapshot.size)
            assertEquals(listOf("s1"), resolveAll(snapshot))
        }

    @Test
    fun starredEntryResolvesStarredTracksNewestFirst() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertSongs(
                listOf(
                    song("s1", "al1", track = 1, starred = 100),
                    song("s2", "al1", track = 2, starred = 300),
                    song("s3", "al1", track = 3, starred = 200),
                ),
            )

            val snapshot = repository.snapshotAfter(listOf(repository.starredEntry(1)))

            assertEquals(3, snapshot.size)
            assertEquals(listOf("s2", "s3", "s1"), resolveAll(snapshot))
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

    @Test
    fun rangeResolvesASliceSpanningEntries() =
        runTest {
            seedLibrary()
            val entries = listOf(repository.albumEntry(1, "al1"), repository.songEntry(1, "s4"))
            val snapshot = repository.snapshotAfter(entries)

            val range = repository.range(snapshot, first = 1, last = 3)

            assertEquals(listOf(1L, 2L, 3L), range.map { it.position })
            assertEquals(listOf("s2", "s3", "s4"), range.map { it.item.song.id })
        }

    @Test
    fun removingInsideAnEntrySplitsItIntoTwoRanges() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))

            repository.removeAt(snapshot, 1)

            assertEquals(listOf("s1", "s3"), resolveAll(repository.snapshot()))
        }

    @Test
    fun removingTheFirstOrLastTrackKeepsTheRemainingRange() =
        runTest {
            seedLibrary()
            val first = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.removeAt(first, 0)
            assertEquals(listOf("s2", "s3"), resolveAll(repository.snapshot()))

            val last = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.removeAt(last, 2)
            assertEquals(listOf("s1", "s2"), resolveAll(repository.snapshot()))
        }

    @Test
    fun removingASongEntryDropsIt() =
        runTest {
            seedLibrary()
            val snapshot =
                repository.snapshotAfter(listOf(repository.albumEntry(1, "al1"), repository.songEntry(1, "s4")))

            repository.removeAt(snapshot, 3)

            assertEquals(listOf("s1", "s2", "s3"), resolveAll(repository.snapshot()))
        }

    @Test
    fun movingWithinAnEntryReordersTheQueue() =
        runTest {
            seedLibrary()
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))

            repository.move(repository.snapshot(), from = 0, to = 2)

            assertEquals(listOf("s2", "s3", "s1"), resolveAll(repository.snapshot()))
        }

    @Test
    fun movingAcrossEntriesKeepsTheRestInOrder() =
        runTest {
            seedLibrary()
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1"), repository.songEntry(1, "s4")))

            repository.move(repository.snapshot(), from = 0, to = 3)

            assertEquals(listOf("s2", "s3", "s4", "s1"), resolveAll(repository.snapshot()))
        }

    @Test
    fun movingFromOutsideTheQueueChangesNothing() =
        runTest {
            seedLibrary()
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))

            assertFalse(repository.move(repository.snapshot(), from = 5, to = 0))
            assertTrue(repository.move(repository.snapshot(), from = 0, to = 2))
            assertEquals(listOf("s2", "s3", "s1"), resolveAll(repository.snapshot()))
        }

    @Test
    fun removingASongBetweenContiguousRangesMergesThem() =
        runTest {
            seedLibrary()
            repository.replace(
                listOf(
                    albumRange(0, 0),
                    repository.songEntry(1, "s4"),
                    albumRange(1, 2),
                ),
            )
            assertEquals(listOf("s1", "s4", "s2", "s3"), resolveAll(repository.snapshot()))

            repository.removeAt(repository.snapshot(), 1)

            val compacted = repository.snapshot()
            assertEquals(1, compacted.entries.size)
            assertEquals(listOf("s1", "s2", "s3"), resolveAll(compacted))
        }

    @Test
    fun rangesSeparatedByAGapAreNotMerged() =
        runTest {
            seedLibrary()
            repository.replace(
                listOf(
                    albumRange(0, 0),
                    repository.songEntry(1, "s4"),
                    albumRange(2, 2),
                ),
            )
            assertEquals(listOf("s1", "s4", "s3"), resolveAll(repository.snapshot()))

            repository.removeAt(repository.snapshot(), 1)

            val kept = repository.snapshot()
            assertEquals(2, kept.entries.size)
            assertEquals(listOf("s1", "s3"), resolveAll(kept))
        }

    @Test
    fun movingATrackOutAndBackCompactsTheQueue() =
        runTest {
            seedLibrary()
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1"), repository.songEntry(1, "s4")))

            repository.move(repository.snapshot(), from = 3, to = 1)
            assertEquals(listOf("s1", "s4", "s2", "s3"), resolveAll(repository.snapshot()))

            repository.move(repository.snapshot(), from = 1, to = 3)

            val compacted = repository.snapshot()
            assertEquals(listOf("s1", "s2", "s3", "s4"), resolveAll(compacted))
            assertEquals(2, compacted.entries.size)
        }

    @Test
    fun resolvedLengthsSurviveQueueEditsUntilTheLibraryIsInvalidated() =
        runTest {
            seedLibrary()
            repository.replace(listOf(repository.albumEntry(1, "al1"), repository.songEntry(1, "s5")))
            assertEquals(4, repository.snapshot().size)

            repository.removeAt(repository.snapshot(), 3)
            assertEquals(3, repository.snapshot().size)

            db.libraryDao().upsertSongs(listOf(song("s6", "al1", track = 4, album = "First Album")))
            assertEquals(3, repository.snapshot().size)

            repository.invalidateLibraryCache()
            assertEquals(4, repository.snapshot().size)
        }

    @Test
    fun aChangedOriginResetMergesEditsAndRemapsTheCurrentItem() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertSongs(listOf(song("s0", "al1", track = 0, album = "First Album")))
            setPlaylist("p1", "s1", "s2", "s3")
            val snapshot = repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))
            repository.removeAt(snapshot, 1)
            repository.setCursor(1)
            val point = repository.captureResetPoint()

            setPlaylist("p1", "s0", "s1", "s2", "s3")
            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            val updated = repository.snapshot()
            val entry = updated.entries.single().entry
            assertEquals(QueueKind.Playlist, entry.kind)
            assertEquals("p1", entry.refId)
            assertNull(entry.rangeStart)
            assertNull(entry.rangeEnd)
            assertEquals(3L, repository.remapPosition(updated, point.current!!))
        }

    @Test
    fun anUnrelatedOriginResetRemapsWithinTheSecondUnchangedFragment() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertSongs(listOf(song("s0", "al1", track = 0, album = "First Album")))
            setPlaylist("p2", "s4")
            setPlaylist("p1", "s1", "s2", "s3")
            val snapshot = repository.snapshotAfter(listOf(repository.playlistEntry(1, "p2"), repository.playlistEntry(1, "p1")))
            repository.removeAt(snapshot, 2)
            repository.setCursor(2)
            val point = repository.captureResetPoint()

            setPlaylist("p2", "s0", "s4")
            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p2")), point)

            assertEquals(3L, repository.remapPosition(repository.snapshot(), point.current!!))
            assertEquals(3L, repository.cursor())
        }

    @Test
    fun anUnrelatedOriginResetPreservesInterleavedFragmentStarts() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1"), repository.albumEntry(1, "al2")))
            repository.move(snapshot, 3, 1)
            repository.setCursor(3)
            val point = repository.captureResetPoint()

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Album, 1, "al2")), point)

            assertEquals(4L, repository.remapPosition(repository.snapshot(), point.current!!))
            assertEquals(4L, repository.cursor())
        }

    @Test
    fun aMovedPlaylistOccurrenceSurvivesReconstructionAndReset() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s1")
            val snapshot = repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))
            repository.move(snapshot, 2, 0)
            repository.setCursor(0)
            val point = repository.captureResetPoint()

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            assertEquals(2L, repository.remapPosition(repository.snapshot(), point.current!!))
            assertEquals(2L, repository.cursor())
        }

    @Test
    fun aRemovedDuplicateOccurrenceDoesNotLeaveItsOffsetOnTheSurvivor() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s1")
            repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))
            repository.setCursor(1)
            repository.setPosition(42_000L)
            val point = repository.captureResetPoint()

            setPlaylist("p1", "s1")
            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            assertEquals(0L, repository.cursorPositionMs())
        }

    @Test
    fun anUnrelatedOriginResetKeepsTheEditedQueue() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s3")
            val snapshot = repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))
            repository.removeAt(snapshot, 1)
            val edited = repository.snapshot().entries.map { it.entry }
            val point = repository.captureResetPoint()

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p2")), point)

            assertEquals(edited, repository.snapshot().entries.map { it.entry })
        }

    @Test
    fun anOriginResetRebuildsOnlyTheChangedUpNextBlock() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s3")
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(0, repository.playlistEntry(1, "p1").copy(rangeStart = 0, rangeEnd = 1), playNext = false)
            repository.addUpNext(
                0,
                repository.albumEntry(1, "al2").copy(rangeStart = 1, rangeEnd = 1),
                playNext = false,
            )
            val point = repository.captureResetPoint()

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            val upNext = repository.snapshot().upNext.map { it.entry }
            assertEquals(QueueKind.Playlist, upNext[0].kind)
            assertNull(upNext[0].rangeStart)
            assertEquals(QueueKind.Album, upNext[1].kind)
            assertEquals(1L, upNext[1].rangeStart)
            assertEquals(1L, upNext[1].rangeEnd)
        }

    @Test
    fun anOriginResetPreservesDuplicateWholeUpNextEntries() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s3")
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(0, repository.playlistEntry(1, "p1"), playNext = false)
            repository.addUpNext(0, repository.playlistEntry(1, "p1"), playNext = false)
            val point = repository.captureResetPoint()

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            val updated = repository.snapshot()
            assertEquals(2, updated.upNext.size)
            assertEquals(6L, updated.upNextSize)
            assertTrue(updated.upNext.all { it.entry.rangeStart == null && it.entry.rangeEnd == null })
        }

    @Test
    fun aResetRemapsCurrentWithinTheSecondDuplicateUpNextEntry() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2")
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(0, repository.playlistEntry(1, "p1"), playNext = false)
            repository.addUpNext(0, repository.playlistEntry(1, "p1"), playNext = false)
            repository.setCursor(4)
            val point = repository.captureResetPoint()

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            assertEquals(4L, repository.cursor())
        }

    @Test
    fun aResetRemapsCurrentAndAnchorWithinTheSecondDuplicateContextEntry() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2")
            repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1"), repository.playlistEntry(1, "p1")))
            repository.addUpNext(3, repository.songEntry(1, "s4"), playNext = false)
            repository.setCursor(3)
            val point = repository.captureResetPoint()

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            assertEquals(3L, repository.cursor())
            assertEquals(3L, repository.snapshot().upNextAnchor)
        }

    @Test
    fun aChangedOriginResetRestoresFullyFragmentedDuplicateEntries() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2")
            val initial =
                repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1"), repository.playlistEntry(1, "p1")))
            repository.removeAt(initial, 0)
            repository.removeAt(repository.snapshot(), 1)
            repository = QueueRepository(db)
            repository.setCursor(1)
            repository.setPosition(42_000L)
            val point = repository.captureResetPoint()

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            val updated = repository.snapshot()
            assertEquals(2, updated.entries.size)
            assertTrue(updated.entries.all { it.entry.rangeStart == null && it.entry.rangeEnd == null })
            assertEquals(listOf("s1", "s2", "s1", "s2"), resolveAll(updated))
            assertEquals(3L, repository.remapPosition(updated, point.current!!))
            assertEquals(3L, repository.cursor())
            assertEquals(42_000L, repository.cursorPositionMs())
        }

    @Test
    fun anUnrelatedOriginResetRemapsWithinTheSecondFullyFragmentedDuplicateEntry() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertSongs(listOf(song("s0", "al1", track = 0, album = "First Album")))
            setPlaylist("p1", "s1", "s2")
            setPlaylist("p2", "s4")
            val initial =
                repository.snapshotAfter(
                    listOf(
                        repository.playlistEntry(1, "p2"),
                        repository.playlistEntry(1, "p1"),
                        repository.playlistEntry(1, "p1"),
                    ),
                )
            repository.removeAt(initial, 1)
            repository.removeAt(repository.snapshot(), 2)
            repository.setCursor(2)
            val point = repository.captureResetPoint()

            setPlaylist("p2", "s0", "s4")
            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p2")), point)

            val updated = repository.snapshot()
            assertEquals(3L, repository.remapPosition(updated, point.current!!))
            assertEquals(3L, repository.cursor())
        }

    @Test
    fun aResetRemapsTheSecondOccurrenceOfASongWithinAPlaylist() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s1")
            repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))
            repository.setCursor(2)
            val point = repository.captureResetPoint()

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            assertEquals(2L, repository.cursor())
        }

    @Test
    fun aRemovedShuffledAnchorFallbackStaysInPlayCoordinates() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s3", "s4", "s5")
            val snapshot = repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))
            repository.addUpNext(2, repository.songEntry(1, "s4"), playNext = false)
            val seed = (1L..100L).first { Shuffle.toSequence(it, 4, 2) != 2L }
            repository.setShuffle(seed)
            val point = repository.captureResetPoint()

            setPlaylist("p1", "s1", "s2", "s4", "s5")
            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)
            repository.setShuffle(seed)

            val updated = repository.snapshot()
            assertEquals(updated.flatContext(point.anchor!!.position), updated.upNextAnchor)
        }

    @Test
    fun aResetDoesNotRestoreShuffleAfterTheUserDisablesIt() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s3")
            val snapshot = repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))
            repository.removeAt(snapshot, 1)
            repository.setShuffle(42L)
            val point = repository.captureResetPoint()
            repository.setShuffle(null)

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            assertFalse(repository.snapshot().shuffled)
        }

    @Test
    fun aResetPreservesShuffleEnabledDuringSync() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s3")
            val snapshot = repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))
            repository.removeAt(snapshot, 1)
            val point = repository.captureResetPoint()
            repository.setShuffle(99L)

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            assertTrue(repository.snapshot().shuffled)
            assertEquals(99L, repository.snapshot().shuffleSeed)
        }

    @Test
    fun aConcurrentUpNextEditSurvivesAReset() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s3")
            val snapshot = repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))
            repository.removeAt(snapshot, 1)
            val point = repository.captureResetPoint()

            val reset = launch { repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point) }
            val edit = launch { repository.addUpNext(0, repository.songEntry(1, "s4"), playNext = false) }
            reset.join()
            edit.join()

            assertEquals(1L, repository.snapshot().upNextSize)
        }

    @Test
    fun staleContextEditsCannotOverwriteACompletedReset() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s3")
            val initial = repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))
            repository.removeAt(initial, 1)
            val stale = repository.snapshot()
            val point = repository.captureResetPoint()
            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)
            val canonical = repository.snapshot().entries.map { it.entry }

            repository.removeAt(stale, 0)
            assertFalse(repository.move(stale, 0, 1))

            assertEquals(canonical, repository.snapshot().entries.map { it.entry })
        }

    @Test
    fun staleUpNextRemovalCannotOverwriteACompletedReset() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s3")
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(0, repository.playlistEntry(1, "p1").copy(rangeStart = 0, rangeEnd = 1), playNext = false)
            val stale = repository.snapshot()
            val point = repository.captureResetPoint()
            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)
            val canonical = repository.snapshot().upNext.map { it.entry }

            repository.removeUpNextAt(stale, 0)

            assertEquals(canonical, repository.snapshot().upNext.map { it.entry })
        }

    @Test
    fun cacheInvalidationDoesNotAdvanceTheQueueContentVersion() =
        runTest {
            seedLibrary()
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            val before = repository.snapshot().version

            repository.invalidateLibraryCache()

            assertEquals(before, repository.snapshot().version)
        }

    @Test
    fun aResetPrefersTheLatestUpNextAnchorOverTheCapturedOne() =
        runTest {
            seedLibrary()
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(0, repository.albumEntry(1, "al2").copy(rangeStart = 0, rangeEnd = 0), playNext = false)
            val point = repository.captureResetPoint()
            val navigated = repository.snapshot()
            repository.reanchorFor(navigated, 2)
            repository.setCursor(repository.snapshot().anchorPlay + 1)

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Album, 1, "al2")), point)

            assertEquals(1L, repository.snapshot().upNextAnchor)
        }

    @Test
    fun aResetPreservesANewBeforeFirstUpNextAnchor() =
        runTest {
            seedLibrary()
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(0, repository.albumEntry(1, "al2").copy(rangeStart = 0, rangeEnd = 0), playNext = false)
            val point = repository.captureResetPoint()
            repository.removeAt(repository.snapshot(), 0)
            assertEquals(-1L, repository.snapshot().upNextAnchor)

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Album, 1, "al2")), point)

            assertEquals(-1L, repository.snapshot().upNextAnchor)
        }

    @Test
    fun aDeletedCurrentSongDoesNotLeaveItsPositionOnTheSuccessor() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s3")
            repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1").copy(rangeStart = 0, rangeEnd = 2)))
            repository.setCursor(0)
            repository.setPosition(50_000)
            val point = repository.captureResetPoint()
            setPlaylist("p1", "s2", "s3")

            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            assertEquals(0L, repository.cursorPositionMs())
        }

    @Test
    fun resetWaitsForCursorUpdatesBeforeWritingItsCursor() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2", "s3")
            val snapshot = repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))
            repository.removeAt(snapshot, 0)
            val point = repository.captureResetPoint()

            repository.cursorMutex.lock()
            val reset = launch { repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point) }
            val repeat = launch { repository.setRepeat(2) }
            try {
                yield()
                assertFalse(reset.isCompleted)
                assertFalse(repeat.isCompleted)
            } finally {
                repository.cursorMutex.unlock()
            }
            reset.join()
            repeat.join()

            assertEquals(2, repository.modes().repeat)
        }

    @Test
    fun upNextResolvesAfterTheCurrentTrack() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))

            repository.addUpNext(currentPosition = 0, entry = repository.songEntry(1, "s4"), playNext = false)

            val updated = repository.snapshot()
            assertEquals(4, updated.size)
            assertEquals(listOf("s1", "s4", "s2", "s3"), resolveAll(updated))
        }

    @Test
    fun reAddingUpNextAfterItsLibraryOriginChangedResolvesFreshRows() =
        runTest {
            seedLibrary()
            setPlaylist("p1", "s1", "s2")
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(0, repository.playlistEntry(1, "p1"), playNext = false)
            val initial = repository.snapshot()
            assertEquals(listOf("s1", "s2"), (1L..2L).map { repository.itemAt(initial, it)?.song?.id })
            repository.clearUpNext()
            setPlaylist("p1", "s1", "s2", "s3")
            val point = repository.captureResetPoint()
            repository.resetChangedOrigins(setOf(QueueOrigin(QueueKind.Playlist, 1, "p1")), point)

            repository.addUpNext(0, repository.playlistEntry(1, "p1"), playNext = false)

            val updated = repository.snapshot()
            assertEquals(3L, updated.upNextSize)
            assertEquals(listOf("s1", "s2", "s3"), (1L..3L).map { repository.itemAt(updated, it)?.song?.id })
        }

    @Test
    fun playNextGoesToTheFrontOfTheUpNextQueue() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(currentPosition = 0, entry = repository.songEntry(1, "s4"), playNext = false)

            repository.addUpNext(
                currentPosition = 0,
                entry = repository.songEntry(1, "s5"),
                playNext = true,
            )

            assertEquals(listOf("s1", "s5", "s4", "s2", "s3"), resolveAll(repository.snapshot()))
        }

    @Test
    fun removingAQueuedTrackDropsItFromTheCombinedQueue() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(currentPosition = 0, entry = repository.songEntry(1, "s4"), playNext = false)

            repository.removeAt(repository.snapshot(), 1)

            val updated = repository.snapshot()
            assertEquals(3, updated.size)
            assertEquals(listOf("s1", "s2", "s3"), resolveAll(updated))
        }

    @Test
    fun movingAQueuedTrackReordersTheUpNextQueue() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(currentPosition = 0, entry = repository.songEntry(1, "s4"), playNext = false)
            repository.addUpNext(
                currentPosition = 0,
                entry = repository.songEntry(1, "s5"),
                playNext = false,
            )
            assertEquals(listOf("s1", "s4", "s5", "s2", "s3"), resolveAll(repository.snapshot()))

            repository.move(repository.snapshot(), from = 2, to = 1)

            assertEquals(listOf("s1", "s5", "s4", "s2", "s3"), resolveAll(repository.snapshot()))
            val (entry, _) = repository.snapshot().locate(1)!!
            assertEquals(QueueKind.Song, entry.kind)
        }

    @Test
    fun movingAQueuedAlbumTrackKeepsItsAlbumEntry() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(currentPosition = 0, entry = repository.albumEntry(1, "al2"), playNext = false)

            repository.move(repository.snapshot(), from = 2, to = 1)

            val (entry, _) = repository.snapshot().locate(1)!!
            assertEquals(QueueKind.Album, entry.kind)
            assertEquals("al2", entry.refId)
            assertEquals(1L, entry.rangeStart)
            assertEquals(1L, entry.rangeEnd)
        }

    @Test
    fun movingARangedAlbumTrackInUpNextKeepsTheAbsoluteOffsets() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertSongs(
                listOf(
                    song("s6", "al1", track = 4, album = "First Album"),
                    song("s7", "al1", track = 5, album = "First Album"),
                    song("x1", "al3", track = 1),
                ),
            )
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al2")))
            repository.addUpNext(currentPosition = 0, entry = albumRange(1, 4), playNext = false)
            repository.addUpNext(
                currentPosition = 0,
                entry = repository.songEntry(1, "x1"),
                playNext = false,
            )
            assertEquals(listOf("s4", "s2", "s3", "s6", "s7", "x1", "s5"), resolveAll(repository.snapshot()))

            repository.move(repository.snapshot(), from = 4, to = 5)

            var updated = repository.snapshot()
            assertEquals(listOf("s4", "s2", "s3", "s6", "x1", "s7", "s5"), resolveAll(updated))
            val moved = updated.locate(5)!!.first
            assertEquals(QueueKind.Album, moved.kind)
            assertEquals(4L, moved.rangeStart)
            assertEquals(4L, moved.rangeEnd)

            repository.move(repository.snapshot(), from = 5, to = 4)

            updated = repository.snapshot()
            assertEquals(listOf("s4", "s2", "s3", "s6", "s7", "x1", "s5"), resolveAll(updated))
            assertTrue(updated.isAlbumContext(4))
        }

    @Test
    fun movingAPlaylistTrackKeepsItAPinnedSong() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertPlaylistSongs(
                listOf(
                    PlaylistSong(1, "p1", "s1", 0),
                    PlaylistSong(1, "p1", "s4", 1),
                ),
            )
            repository.snapshotAfter(listOf(repository.playlistEntry(1, "p1")))

            repository.move(repository.snapshot(), from = 0, to = 1)

            val (entry, _) = repository.snapshot().locate(1)!!
            assertEquals(QueueKind.Song, entry.kind)
            assertEquals("s1", entry.refId)
        }

    @Test
    fun queuedTracksNeverJoinAShuffledContext() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.setShuffle(12345L)
            val shuffled = repository.snapshot()
            val contextBefore = (0 until shuffled.contextSize).mapNotNull { repository.itemAt(shuffled, it)?.song?.id }

            repository.addUpNext(currentPosition = 0, entry = repository.songEntry(1, "s5"), playNext = false)

            val updated = repository.snapshot()
            assertEquals(4, updated.size)
            assertEquals("s5", repository.itemAt(updated, updated.anchorPlay + 1)?.song?.id)
            val contextAfter =
                (0 until updated.size)
                    .filterNot { updated.isUpNext(it) }
                    .mapNotNull { repository.itemAt(updated, it)?.song?.id }
            assertEquals(contextBefore, contextAfter)
            assertTrue(updated.shuffled)
        }

    @Test
    fun consumingASliceOfAQueuedAlbumRemovesTheRightTrack() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(currentPosition = 0, entry = repository.albumEntry(1, "al2"), playNext = false)
            assertEquals(listOf("s1", "s4", "s5", "s2", "s3"), resolveAll(repository.snapshot()))

            repository.removeAt(repository.snapshot(), 2)

            assertEquals(listOf("s1", "s4", "s2", "s3"), resolveAll(repository.snapshot()))
        }

    @Test
    fun removingTheAnchorTrackPutsTheQueueFirst() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(currentPosition = 0, entry = repository.songEntry(1, "s4"), playNext = false)
            assertEquals(listOf("s1", "s4", "s2", "s3"), resolveAll(repository.snapshot()))

            repository.removeAt(repository.snapshot(), 0)

            assertEquals(listOf("s4", "s2", "s3"), resolveAll(repository.snapshot()))
        }

    @Test
    fun removingTheLastAnchorTrackKeepsTheQueueResolvable() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(currentPosition = 2, entry = repository.songEntry(1, "s4"), playNext = false)
            assertEquals(listOf("s1", "s2", "s3", "s4"), resolveAll(repository.snapshot()))

            repository.removeAt(repository.snapshot(), 2)

            val updated = repository.snapshot()
            assertEquals(3, updated.size)
            assertEquals(listOf("s1", "s2", "s4"), resolveAll(updated))
        }

    @Test
    fun removingAContextTrackBeforeTheAnchorKeepsTheQueueAttached() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(currentPosition = 1, entry = repository.songEntry(1, "s4"), playNext = false)
            assertEquals(listOf("s1", "s2", "s4", "s3"), resolveAll(repository.snapshot()))

            repository.removeAt(repository.snapshot(), 0)

            assertEquals(listOf("s2", "s4", "s3"), resolveAll(repository.snapshot()))
        }

    @Test
    fun replacingTheContextClearsTheUpNextQueue() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(currentPosition = 0, entry = repository.songEntry(1, "s4"), playNext = false)

            repository.replace(listOf(repository.songEntry(1, "s5")))

            val updated = repository.snapshot()
            assertTrue(updated.upNext.isEmpty())
            assertEquals(1, updated.size)
            assertEquals(listOf("s5"), resolveAll(updated))
        }

    private fun albumRange(
        start: Long,
        end: Long,
    ) = QueueEntry(position = 0, sourceId = 1, kind = QueueKind.Album, refId = "al1", rangeStart = start, rangeEnd = end)

    private suspend fun QueueRepository.snapshotAfter(entries: List<QueueEntry>): QueueSnapshot {
        replace(entries)
        return snapshot()
    }

    private suspend fun resolveAll(snapshot: QueueSnapshot): List<String> =
        (0 until snapshot.size).mapNotNull { repository.itemAt(snapshot, it)?.song?.id }

    @Test
    fun aStaleShuffleSizeIsIgnoredWhenTheQueueLengthChanges() =
        runTest {
            seedLibrary()
            repository.replace(listOf(repository.albumEntry(1, "al1")))
            repository.setShuffle(1234L)
            assertTrue(repository.snapshot().shuffled)

            db.libraryDao().upsertSongs(listOf(song("s6", "al1", track = 4, album = "First Album")))
            repository.invalidateLibraryCache()

            assertFalse(repository.snapshot().shuffled)
        }

    @Test
    fun enablingShuffleStoresNoPerTrackRows() =
        runTest {
            seedLibrary()
            repository.replace(listOf(repository.albumEntry(1, "al1")))
            val entriesBefore = db.queueDao().entries().size

            repository.setShuffle(5678L)
            val snapshot = repository.snapshot()

            assertTrue(snapshot.shuffled)
            assertEquals(3L, snapshot.contextSize)
            assertEquals(entriesBefore, db.queueDao().entries().size)
            assertFalse(shuffleOrderTableExists())
        }

    private suspend fun shuffleOrderTableExists(): Boolean =
        db.useReaderConnection { transactor ->
            transactor.usePrepared(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'shuffle_order'",
            ) { statement ->
                if (statement.step()) statement.getLong(0) > 0 else false
            }
        }

    @Test
    fun shuffledPositionsResolveToEveryTrackExactlyOnce() =
        runTest {
            seedLibrary()
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.setShuffle(42L)
            val shuffled = repository.snapshot()

            val resolved = (0 until shuffled.size).mapNotNull { repository.itemAt(shuffled, it)?.song?.id }

            assertEquals(3, resolved.distinct().size)
            assertEquals(listOf("s1", "s2", "s3"), resolved.sorted())
            val flatPositions = (0 until shuffled.size).map { shuffled.flatContext(it)!! }
            assertEquals((0L until shuffled.size).toList(), flatPositions.sorted())
        }

    @Test
    fun togglingShuffleOffRestoresTheOriginalOrder() =
        runTest {
            seedLibrary()
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.setShuffle(99L)
            assertTrue(repository.snapshot().shuffled)

            repository.setShuffle(null)

            val restored = repository.snapshot()
            assertFalse(restored.shuffled)
            assertEquals(listOf("s1", "s2", "s3"), resolveAll(restored))
        }

    @Test
    fun shuffledLargeContextWindowOnlyResolvesRequestedRows() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertPlaylists(listOf(playlist("huge", "Huge")))
            db.useWriterConnection { transactor ->
                transactor.usePrepared(
                    "WITH RECURSIVE n(x) AS (VALUES(0) UNION ALL SELECT x + 1 FROM n WHERE x < 199999) " +
                        "INSERT INTO playlist_songs SELECT 1, 'huge', 's1', x FROM n",
                ) { statement ->
                    statement.step()
                }
            }
            repository.snapshotAfter(listOf(repository.playlistEntry(1, "huge")))
            repository.setShuffle(42L)
            val snapshot = repository.snapshot()

            val window = repository.window(snapshot, center = 100_000, radius = 25)

            assertEquals(200_000L, snapshot.contextSize)
            assertEquals(51, window.size)
            assertEquals(51L, repository.resolvedRowCount)
            assertEquals((99_975L..100_025L).toList(), window.map { it.position })
            assertTrue(window.all { it.item.song.id == "s1" })
        }

    @Test
    fun shuffledRangeResolvesEntriesInPositionOrder() =
        runTest {
            seedLibrary()
            repository.snapshotAfter(listOf(repository.albumEntry(1, "al1"), repository.songEntry(1, "s4")))
            repository.setShuffle(42L)
            val snapshot = repository.snapshot()

            val range = repository.range(snapshot, 0, snapshot.size - 1)

            assertEquals((0L until snapshot.size).toList(), range.map { it.position })
            assertEquals(setOf("s1", "s2", "s3", "s4"), range.map { it.item.song.id }.toSet())
        }

    @Test
    fun shuffledRangeResolvesASplitEntryWithoutRepeatingOrSkipping() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.removeAt(snapshot, 1)
            repository.setShuffle(7L)
            val shuffled = repository.snapshot()

            val range = repository.range(shuffled, 0, shuffled.size - 1)

            assertEquals(listOf(0L, 1L), range.map { it.position })
            assertEquals(setOf("s1", "s3"), range.map { it.item.song.id }.toSet())
        }

    @Test
    fun removingAContextTrackWhileShuffledIsRejected() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.setShuffle(42L)
            val before = repository.snapshot()
            val entriesBefore = before.entries.map { it.entry }
            val orderBefore = (0 until before.contextSize).mapNotNull { repository.itemAt(before, it)?.song?.id }

            repository.removeAt(before, 1)

            val after = repository.snapshot()
            assertTrue(after.shuffled)
            assertEquals(before.contextSize, after.contextSize)
            assertEquals(entriesBefore, after.entries.map { it.entry })
            assertEquals(orderBefore, (0 until after.contextSize).mapNotNull { repository.itemAt(after, it)?.song?.id })
        }

    @Test
    fun contextIndexUsesTheFirstDuplicateOrdinalAcrossSparsePositions() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertPlaylists(listOf(playlist("duplicates", "Duplicates")))
            db.libraryDao().upsertPlaylistSongs(
                listOf(
                    PlaylistSong(1, "duplicates", "s2", 10),
                    PlaylistSong(1, "duplicates", "s1", 20),
                    PlaylistSong(1, "duplicates", "s2", 30),
                ),
            )
            val snapshot = repository.snapshotAfter(listOf(repository.playlistEntry(1, "duplicates")))

            assertEquals(0L, repository.combinedContextIndexOf(snapshot, "s2"))
            assertEquals(1L, repository.combinedContextIndexOf(snapshot, "s1"))
        }

    @Test
    fun contextIndexFindsADuplicateInsideTheEntryRange() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertPlaylists(listOf(playlist("duplicates", "Duplicates")))
            db.libraryDao().upsertPlaylistSongs(
                listOf(
                    PlaylistSong(1, "duplicates", "s2", 10),
                    PlaylistSong(1, "duplicates", "s1", 20),
                    PlaylistSong(1, "duplicates", "s2", 30),
                ),
            )
            val entry = repository.playlistEntry(1, "duplicates").copy(rangeStart = 1, rangeEnd = 2)
            val snapshot = repository.snapshotAfter(listOf(entry))

            assertEquals(1L, repository.combinedContextIndexOf(snapshot, "s2"))
        }

    @Test
    fun contextIndexFallsBackToBoundedRowsForGenre() =
        runTest {
            seedLibrary()
            insertGenre("s1", "Rock")
            insertGenre("s3", "Rock")
            insertGenre("s4", "Rock")
            val snapshot = repository.snapshotAfter(listOf(repository.genreEntry(1, "Rock")))

            assertEquals(2L, repository.combinedContextIndexOf(snapshot, "s4"))
            assertTrue(repository.resolvedRowCount <= QUEUE_CHUNK)
        }

    @Test
    fun upNextCombinedIndexOfCountsFromTheAnchorIntoTheBlock() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))

            repository.addUpNext(currentPosition = 1, entry = repository.songEntry(1, "s4"), playNext = false)

            val updated = repository.snapshot()
            assertEquals(1L, updated.anchorPlay)
            assertEquals(1L, updated.upNextSize)
            assertEquals(2L, repository.combinedIndexOf(updated, "s4"))
            assertEquals(1L, repository.combinedContextIndexOf(updated, "s2"))
            assertEquals(3L, repository.combinedContextIndexOf(updated, "s3"))
        }

    @Test
    fun combinedContextIndexOfTracksTheAnchorAsItMoves() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))
            repository.addUpNext(currentPosition = 0, entry = repository.songEntry(1, "s4"), playNext = false)

            val before = repository.snapshot()
            assertEquals(0L, before.anchorPlay)
            assertEquals(3L, repository.combinedContextIndexOf(before, "s3"))

            repository.reanchorFor(before, 3)

            val after = repository.snapshot()
            assertEquals(2L, after.anchorPlay)
            assertEquals(2L, repository.combinedContextIndexOf(after, "s3"))
            assertEquals(3L, repository.combinedIndexOf(after, "s4"))
        }

    @Test
    fun combinedIndexOfPrefersTheUpNextCopyOverTheContextCopy() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.songEntry(1, "s4")))
            repository.addUpNext(currentPosition = 0, entry = repository.songEntry(1, "s4"), playNext = false)

            val updated = repository.snapshot()
            assertEquals(0L, repository.combinedContextIndexOf(updated, "s4"))
            assertEquals(1L, repository.combinedIndexOf(updated, "s4"))
        }

    @Test
    fun sourceNameReadsTheAlbumPlaylistOrSongAlbum() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertPlaylists(listOf(playlist("p1", "Road Trip")))

            assertEquals("First Album", repository.sourceName(QueueKind.Album, 1, "al1"))
            assertEquals("Road Trip", repository.sourceName(QueueKind.Playlist, 1, "p1"))
            assertEquals("Second Album", repository.sourceName(QueueKind.Song, 1, "s4"))
            assertEquals(null, repository.sourceName(QueueKind.Song, 1, "ghost"))
        }

    @Test
    fun aReorderedAlbumTrackKeepsItsAlbumAsSourceName() =
        runTest {
            seedLibrary()
            repository.replace(listOf(repository.albumEntry(1, "al1")))

            repository.move(repository.snapshot(), from = 0, to = 2)

            val (entry, _) = repository.snapshot().locate(2)!!
            assertEquals(QueueKind.Album, entry.kind)
            assertEquals("al1", entry.refId)
            assertEquals(0L, entry.rangeStart)
            assertEquals(0L, entry.rangeEnd)
            assertEquals("First Album", repository.sourceName(entry.kind, entry.sourceId, entry.refId))
        }

    @Test
    fun movingWithinARangedAlbumKeepsTheAbsoluteOffsets() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertSongs(
                listOf(
                    song("s6", "al1", track = 4, album = "First Album"),
                    song("s7", "al1", track = 5, album = "First Album"),
                ),
            )
            repository.snapshotAfter(listOf(albumRange(1, 4)))
            assertEquals(listOf("s2", "s3", "s6", "s7"), resolveAll(repository.snapshot()))

            repository.move(repository.snapshot(), from = 0, to = 2)

            val snapshot = repository.snapshot()
            assertEquals(listOf("s3", "s6", "s2", "s7"), resolveAll(snapshot))
            val (entry, _) = snapshot.locate(2)!!
            assertEquals(QueueKind.Album, entry.kind)
            assertEquals(1L, entry.rangeStart)
            assertEquals(1L, entry.rangeEnd)
        }

    @Test
    fun movingBackwardWithinARangedAlbumKeepsTheAbsoluteOffsets() =
        runTest {
            seedLibrary()
            db.libraryDao().upsertSongs(
                listOf(
                    song("s6", "al1", track = 4, album = "First Album"),
                    song("s7", "al1", track = 5, album = "First Album"),
                ),
            )
            repository.snapshotAfter(listOf(albumRange(1, 4)))

            repository.move(repository.snapshot(), from = 3, to = 0)

            val snapshot = repository.snapshot()
            assertEquals(listOf("s7", "s2", "s3", "s6"), resolveAll(snapshot))
            val (entry, _) = snapshot.locate(0)!!
            assertEquals(QueueKind.Album, entry.kind)
            assertEquals(4L, entry.rangeStart)
            assertEquals(4L, entry.rangeEnd)
        }

    @Test
    fun movingAnAlbumTrackBackCompactsTheAlbumEntry() =
        runTest {
            seedLibrary()
            repository.replace(listOf(repository.albumEntry(1, "al1")))

            repository.move(repository.snapshot(), from = 0, to = 2)
            repository.move(repository.snapshot(), from = 2, to = 0)

            val snapshot = repository.snapshot()
            assertEquals(listOf("s1", "s2", "s3"), resolveAll(snapshot))
            val entry = snapshot.entries.single().entry
            assertEquals(QueueKind.Album, entry.kind)
            assertEquals(0L, entry.rangeStart)
            assertEquals(2L, entry.rangeEnd)
        }

    @Test
    fun anAlbumSplitByRemovalIsStillLabeledByItsAlbum() =
        runTest {
            seedLibrary()
            val snapshot = repository.snapshotAfter(listOf(repository.albumEntry(1, "al1")))

            repository.removeAt(snapshot, 1)

            val (entry, _) = repository.snapshot().locate(1)!!
            assertEquals(QueueKind.Album, entry.kind)
            assertEquals("First Album", repository.sourceName(entry.kind, entry.sourceId, entry.refId))
        }

    @Test
    fun queueModesRoundTrip() =
        runTest {
            seedLibrary()
            repository.replace(listOf(repository.albumEntry(1, "al1")))
            assertEquals(QueueModes(shuffle = false, repeat = 0), repository.modes())

            repository.setShuffle(4321L)
            repository.setRepeat(2)
            assertEquals(QueueModes(shuffle = true, repeat = 2), repository.modes())

            repository.setShuffle(null)
            repository.setRepeat(0)
            assertEquals(QueueModes(shuffle = false, repeat = 0), repository.modes())
        }

    private fun playlist(
        id: String,
        name: String,
    ) = Playlist(
        sourceId = 1,
        id = id,
        name = name,
        comment = null,
        coverArt = null,
        songCount = 0,
        created = 0,
    )

    private suspend fun setPlaylist(
        id: String,
        vararg songIds: String,
    ) {
        db.libraryDao().deletePlaylistSongsFrom(1, id, 0)
        db.libraryDao().upsertPlaylistSongs(
            songIds.mapIndexed { position, songId -> PlaylistSong(1, id, songId, position.toLong()) },
        )
    }

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
    )

    private fun song(
        id: String,
        albumId: String,
        track: Long,
        album: String = "Album",
        title: String = "Song $id",
        starred: Long? = null,
        created: Long = 0,
    ) = Song(
        sourceId = 1,
        id = id,
        albumId = albumId,
        artistId = "ar1",
        title = title,
        album = album,
        artist = "Artist",
        duration = 100,
        track = track,
        disc = 1,
        starred = starred,
        genre = null,
        created = created,
    )

    private suspend fun insertGenre(
        songId: String,
        genre: String,
    ) {
        db.useWriterConnection { connection ->
            connection.usePrepared(
                "INSERT INTO song_genres (sourceId, songId, position, genre) VALUES (1, '$songId', 0, '$genre')",
            ) { it.step() }
        }
    }
}
