package com.subtracks.data.sync

import android.content.Context
import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.LoggingSQLiteDriver
import com.subtracks.data.db.SqlExecution
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.db.artistsDefaultQuery
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.DiscKey
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.Source
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.source.MusicSource
import com.subtracks.data.source.StarType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Collections

@RunWith(AndroidJUnit4::class)
class SyncServiceTest {
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
    fun syncInsertsAndPrunesRemovedRows() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1"), artist("a2")),
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1"), song("s2").copy(artistId = "a2")),
                    playlists = listOf(playlist("p1")),
                    playlistSongs = listOf(PlaylistSong(1, "p1", "s1", 0)),
                )

            SyncService(db, source).sync()

            assertEquals(
                2,
                db
                    .libraryDao()
                    .artistsByName(1, 0, "")
                    .allRows()
                    .size,
            )
            assertEquals(2, songIds(1).size)
            assertEquals(
                1,
                db
                    .libraryDao()
                    .playlistSongs(1, "p1")
                    .allRows()
                    .size,
            )

            source.artists = listOf(artist("a1"))
            source.songs = listOf(song("s1"))

            SyncService(db, source).sync()

            assertEquals(
                listOf("a1"),
                db
                    .libraryDao()
                    .artistsByName(1, 0, "")
                    .allRows()
                    .map { it.id },
            )
            assertEquals(listOf("s1"), songIds(1))
        }

    @Test
    fun aSecondIdenticalSyncLeavesTheLibraryUntouched() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1"), artist("a2")),
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1")),
                    playlists = listOf(playlist("p1")),
                    playlistSongs = listOf(PlaylistSong(1, "p1", "s1", 0)),
                )

            SyncService(db, source).sync()
            SyncService(db, source).sync()

            assertEquals(2, db.libraryDao().artistIds(1).size)
            assertEquals(1, db.libraryDao().albumIds(1).size)
            assertEquals(1, songIds(1).size)
            assertEquals(1, db.libraryDao().playlistIds(1).size)
            assertEquals(
                1,
                db
                    .libraryDao()
                    .playlistSongs(1, "p1")
                    .allRows()
                    .size,
            )
        }

    @Test
    fun albumsAreUpsertedAndPruned() =
        runTest {
            insertSource()
            val source = FakeMusicSource(albums = listOf(album("al1"), album("al2")))

            SyncService(db, source).sync()
            SyncService(db, source).sync()

            assertEquals(listOf("al1", "al2"), db.libraryDao().albumIds(1).sorted())

            source.albums = listOf(album("al1").copy(name = "Renamed"))

            SyncService(db, source).sync()

            assertEquals(listOf("al1"), db.libraryDao().albumIds(1))
            assertEquals(
                "Renamed",
                db
                    .libraryDao()
                    .album(1, "al1")
                    .first()
                    ?.name,
            )
        }

    @Test
    fun syncUpdatesExistingRows() =
        runTest {
            insertSource()
            val source = FakeMusicSource(artists = listOf(artist("a1", name = "Old")))

            SyncService(db, source).sync()
            assertEquals(
                "Old",
                db
                    .libraryDao()
                    .artistsByName(1, 0, "")
                    .allRows()
                    .single()
                    .name,
            )

            source.artists = listOf(artist("a1", name = "New"))
            SyncService(db, source).sync()

            assertEquals(
                "New",
                db
                    .libraryDao()
                    .artistsByName(1, 0, "")
                    .allRows()
                    .single()
                    .name,
            )
        }

    @Test
    fun aCaseOnlyNameChangeRewritesTheRow() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", name = "the beatles")),
                    albums = listOf(album("al1").copy(name = "abbey road", albumArtist = "the beatles")),
                    playlists = listOf(playlist("p1").copy(name = "driving")),
                )

            SyncService(db, source).sync()

            source.artists = listOf(artist("a1", name = "The Beatles"))
            source.albums = listOf(album("al1").copy(name = "Abbey Road", albumArtist = "The Beatles"))
            source.playlists = listOf(playlist("p1").copy(name = "Driving"))

            SyncService(db, source).sync()

            assertEquals(
                "The Beatles",
                db
                    .libraryDao()
                    .artist(1, "a1")
                    .first()
                    ?.name,
            )
            assertEquals(
                "Abbey Road",
                db
                    .libraryDao()
                    .album(1, "al1")
                    .first()
                    ?.name,
            )
            assertEquals(
                "The Beatles",
                db
                    .libraryDao()
                    .album(1, "al1")
                    .first()
                    ?.albumArtist,
            )
            assertEquals(
                "Driving",
                db
                    .libraryDao()
                    .playlist(1, "p1")
                    .first()
                    ?.name,
            )
        }

    @Test
    fun syncStoresStarredAndAddedTimestamps() =
        runTest {
            insertSource()
            val source = FakeMusicSource(albums = listOf(album("al1").copy(created = 1234, starred = 5678)))

            SyncService(db, source).sync()

            val stored = db.libraryDao().album(1, "al1").first()
            assertEquals(1234L, stored?.created)
            assertEquals(5678L, stored?.starred)
        }

    @Test
    fun syncStoresPlaylistOwnerAndSharing() =
        runTest {
            insertSource()
            val source = FakeMusicSource(playlists = listOf(playlist("p1").copy(owner = "austin", shared = true)))

            SyncService(db, source).sync()

            val stored = db.libraryDao().playlist(1, "p1").first()
            assertEquals("austin", stored?.owner)
            assertEquals(true, stored?.shared)
        }

    @Test
    fun syncStoresSongGenresInOrder() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1").copy(genres = listOf("Rock", "Electronic"))),
                )

            SyncService(db, source).sync()

            assertEquals(listOf("Rock", "Electronic"), db.libraryDao().songGenres(1, "s1").first())
        }

    @Test
    fun genresShrinkAndClearWithTheSong() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1").copy(genres = listOf("Rock", "Electronic", "Pop"))),
                )

            SyncService(db, source).sync()
            assertEquals(listOf("Rock", "Electronic", "Pop"), db.libraryDao().songGenres(1, "s1").first())

            source.songs = listOf(song("s1").copy(genres = listOf("Rock")))

            SyncService(db, source).sync()
            assertEquals(listOf("Rock"), db.libraryDao().songGenres(1, "s1").first())

            source.songs = listOf(song("s1"))

            SyncService(db, source).sync()
            assertEquals(emptyList<String>(), db.libraryDao().songGenres(1, "s1").first())
        }

    @Test
    fun anIdenticalSyncDoesNotRewriteGenreRows() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1").copy(genres = listOf("Rock", "Electronic"))),
                )

            SyncService(db, source).sync()
            val before = totalChanges()

            SyncService(db, source).sync()

            assertEquals(before, totalChanges())
            assertEquals(listOf("Rock", "Electronic"), db.libraryDao().songGenres(1, "s1").first())
        }

    @Test
    fun removingASongRemovesItsGenres() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1").copy(genres = listOf("Rock"))),
                )

            SyncService(db, source).sync()
            assertEquals(listOf("Rock"), db.libraryDao().songGenres(1, "s1").first())

            source.songs = emptyList()

            SyncService(db, source).sync()
            assertEquals(emptyList<String>(), db.libraryDao().songGenres(1, "s1").first())
        }

    @Test
    fun syncNormalizesSingularGenresIntoTheGenreRows() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1")),
                    songs =
                        listOf(
                            song("s1").copy(genres = listOf("Rock", "Pop")),
                            song("s2").copy(genre = "Jazz"),
                            song("s3").copy(genre = "Rock"),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals(listOf("Rock", "Pop"), db.libraryDao().songGenres(1, "s1").first())
            assertEquals(listOf("Jazz"), db.libraryDao().songGenres(1, "s2").first())
            assertEquals(listOf("Rock"), db.libraryDao().songGenres(1, "s3").first())
        }

    @Test
    fun genresSplitOnEveryNavidromeSeparator() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1")),
                    songs =
                        listOf(
                            song("s1").copy(genre = "Classical;Minimal/Rock,Pop"),
                            song("s2").copy(genres = listOf("Jazz/Blues", "Soul, Funk")),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals(
                listOf("Classical", "Minimal", "Rock", "Pop"),
                db.libraryDao().songGenres(1, "s1").first(),
            )
            assertEquals(listOf("Jazz", "Blues", "Soul", "Funk"), db.libraryDao().songGenres(1, "s2").first())
        }

    @Test
    fun numericGenresMapToId3v1Names() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1")),
                    songs =
                        listOf(
                            song("s1").copy(genre = "17"),
                            song("s2").copy(genre = "(52)"),
                            song("s3").copy(genre = "999"),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals(listOf("Rock"), db.libraryDao().songGenres(1, "s1").first())
            assertEquals(listOf("Electronic"), db.libraryDao().songGenres(1, "s2").first())
            assertEquals(listOf("999"), db.libraryDao().songGenres(1, "s3").first())
        }

    @Test
    fun genresThatMapToTheSameNameCollapse() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1")),
                    songs =
                        listOf(
                            song("s1").copy(genre = "17;Rock"),
                            song("s2").copy(genres = listOf("17", "Rock")),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals(listOf("Rock"), db.libraryDao().songGenres(1, "s1").first())
            assertEquals(listOf("Rock"), db.libraryDao().songGenres(1, "s2").first())
        }

    @Test
    fun concatenatedAndIndividualGenresCollapseOnTheHomeFeed() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1")),
                    songs =
                        listOf(
                            song("s1").copy(genre = "Classical;Minimal"),
                            song("s2").copy(genre = "Classical"),
                            song("s3").copy(genre = "Minimal"),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals(
                listOf("Classical", "Minimal"),
                db.libraryDao().genresByMostPlayed(1).first(),
            )
        }

    @Test
    fun thePluralGenresWinOverTheSingularAttribute() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1").copy(genres = listOf("Rock", "Pop"), genre = "Jazz")),
                )

            SyncService(db, source).sync()

            assertEquals(listOf("Rock", "Pop"), db.libraryDao().songGenres(1, "s1").first())
        }

    @Test
    fun syncStoresAlbumDiscTitles() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1").copy(discTitles = mapOf(1L to "The Calm", 2L to "The Storm"))),
                )

            SyncService(db, source).sync()

            assertEquals(mapOf(1L to "The Calm", 2L to "The Storm"), storedDiscs("al1"))
        }

    @Test
    fun syncClearsDiscTitlesWhenTheServerStopsSendingThem() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1").copy(discTitles = mapOf(1L to "The Calm"))),
                )

            SyncService(db, source).sync()
            assertEquals(mapOf(1L to "The Calm"), storedDiscs("al1"))

            source.albums = listOf(album("al1"))

            SyncService(db, source).sync()
            assertEquals(emptyMap<Long, String>(), storedDiscs("al1"))
        }

    @Test
    fun discTitlesAreKeptPerAlbum() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums =
                        listOf(
                            album("al1").copy(discTitles = mapOf(1L to "First Album Disc")),
                            album("al2"),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals(mapOf(1L to "First Album Disc"), storedDiscs("al1"))
            assertEquals(emptyMap<Long, String>(), storedDiscs("al2"))
        }

    @Test
    fun removingAnAlbumRemovesItsDiscTitles() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums =
                        listOf(
                            album("al1").copy(discTitles = mapOf(1L to "The Calm")),
                            album("al2").copy(discTitles = mapOf(1L to "The Storm")),
                        ),
                )

            SyncService(db, source).sync()

            source.albums = listOf(album("al2").copy(discTitles = mapOf(1L to "The Storm")))

            SyncService(db, source).sync()

            assertEquals(emptyMap<Long, String>(), storedDiscs("al1"))
            assertEquals(mapOf(1L to "The Storm"), storedDiscs("al2"))
        }

    @Test
    fun syncUpdatesADiscTitle() =
        runTest {
            insertSource()
            val source = FakeMusicSource(albums = listOf(album("al1").copy(discTitles = mapOf(1L to "Old"))))

            SyncService(db, source).sync()
            assertEquals(mapOf(1L to "Old"), storedDiscs("al1"))

            source.albums = listOf(album("al1").copy(discTitles = mapOf(1L to "New")))

            SyncService(db, source).sync()
            assertEquals(mapOf(1L to "New"), storedDiscs("al1"))
        }

    @Test
    fun syncCommitsMultipleBatchesAndPrunes() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = (1..5).map { artist("a$it") },
                    songs = (1..5).map { song("s$it") },
                    batchSize = 2,
                )

            SyncService(db, source).sync()

            assertEquals((1..5).map { "a$it" }, db.libraryDao().artistIds(1).sorted())
            assertEquals((1..5).map { "s$it" }, songIds(1).sorted())

            source.artists = (1..3).map { artist("a$it") }
            source.songs = (1..3).map { song("s$it") }

            SyncService(db, source).sync()

            assertEquals((1..3).map { "a$it" }, db.libraryDao().artistIds(1).sorted())
            assertEquals((1..3).map { "s$it" }, songIds(1).sorted())
        }

    @Test
    fun playlistSongsSplitAcrossBatchesKeepTheirTail() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    songs = (1..5).map { song("s$it") },
                    playlists = listOf(playlist("p1")),
                    playlistSongs = (1..5).map { PlaylistSong(1, "p1", "s$it", (it - 1).toLong()) },
                    batchSize = 2,
                )

            SyncService(db, source).sync()
            assertEquals(
                5,
                db
                    .libraryDao()
                    .playlistSongs(1, "p1")
                    .allRows()
                    .size,
            )

            source.playlistSongs = listOf(PlaylistSong(1, "p1", "s1", 0), PlaylistSong(1, "p1", "s2", 1))

            SyncService(db, source).sync()
            assertEquals(
                2,
                db
                    .libraryDao()
                    .playlistSongs(1, "p1")
                    .allRows()
                    .size,
            )
        }

    @Test
    fun removingAPlaylistRemovesItsSongs() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    songs = listOf(song("s1")),
                    playlists = listOf(playlist("p1")),
                    playlistSongs = listOf(PlaylistSong(1, "p1", "s1", 0)),
                )

            SyncService(db, source).sync()
            assertEquals(
                1,
                db
                    .libraryDao()
                    .playlistSongs(1, "p1")
                    .allRows()
                    .size,
            )

            source.playlists = emptyList()
            source.playlistSongs = emptyList()

            SyncService(db, source).sync()

            assertEquals(0, db.libraryDao().playlistIds(1).size)
            assertEquals(
                0,
                db
                    .libraryDao()
                    .playlistSongs(1, "p1")
                    .allRows()
                    .size,
            )
        }

    @Test
    fun playlistSongsAreFetchedOnlyForSurvivingPlaylists() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    songs = listOf(song("s1"), song("s2")),
                    playlists = listOf(playlist("p1"), playlist("p2")),
                    playlistSongs = listOf(PlaylistSong(1, "p1", "s1", 0), PlaylistSong(1, "p2", "s2", 0)),
                )

            SyncService(db, source).sync()

            source.playlists = listOf(playlist("p1"))
            source.playlistSongs = listOf(PlaylistSong(1, "p1", "s1", 0))

            SyncService(db, source).sync()

            assertEquals(listOf("p1"), source.requestedPlaylistIds.last())
        }

    @Test
    fun playlistEmptiedToZeroRemovesAllSongs() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    songs = listOf(song("s1"), song("s2")),
                    playlists = listOf(playlist("p1")),
                    playlistSongs = listOf(PlaylistSong(1, "p1", "s1", 0), PlaylistSong(1, "p1", "s2", 1)),
                )

            SyncService(db, source).sync()
            assertEquals(
                2,
                db
                    .libraryDao()
                    .playlistSongs(1, "p1")
                    .allRows()
                    .size,
            )

            source.playlistSongs = emptyList()

            SyncService(db, source).sync()

            assertEquals(1, db.libraryDao().playlistIds(1).size)
            assertEquals(
                0,
                db
                    .libraryDao()
                    .playlistSongs(1, "p1")
                    .allRows()
                    .size,
            )
        }

    @Test
    fun discTitleSetShrinksForARetainedAlbum() =
        runTest {
            insertSource()
            val source = FakeMusicSource(albums = listOf(album("al1").copy(discTitles = mapOf(1L to "A", 2L to "B"))))

            SyncService(db, source).sync()
            assertEquals(mapOf(1L to "A", 2L to "B"), storedDiscs("al1"))

            source.albums = listOf(album("al1").copy(discTitles = mapOf(1L to "A")))

            SyncService(db, source).sync()

            assertEquals(mapOf(1L to "A"), storedDiscs("al1"))
        }

    @Test
    fun syncingOneSourceDoesNotTouchAnother() =
        runTest {
            insertSource()
            insertSource(id = 2, name = "other")
            val sourceOne =
                FakeMusicSource(
                    artists = listOf(artist("a1"), artist("a2")),
                    songs = listOf(song("s1")),
                    playlists = listOf(playlist("p1")),
                    playlistSongs = listOf(PlaylistSong(1, "p1", "s1", 0)),
                )
            val sourceTwo =
                FakeMusicSource(
                    id = 2,
                    artists = listOf(artist("b1").copy(sourceId = 2)),
                    songs = listOf(song("s2").copy(sourceId = 2)),
                    playlists = listOf(playlist("p2").copy(sourceId = 2)),
                    playlistSongs = listOf(PlaylistSong(2, "p2", "s2", 0)),
                )
            SyncService(db, sourceOne).sync()
            SyncService(db, sourceTwo).sync()

            sourceOne.artists = listOf(artist("a1"))
            sourceOne.songs = emptyList()
            sourceOne.playlists = emptyList()
            sourceOne.playlistSongs = emptyList()

            SyncService(db, sourceOne).sync()

            assertEquals(listOf("b1"), db.libraryDao().artistIds(2))
            assertEquals(listOf("s2"), songIds(2))
            assertEquals(listOf("p2"), db.libraryDao().playlistIds(2))
            assertEquals(
                1,
                db
                    .libraryDao()
                    .playlistSongs(2, "p2")
                    .allRows()
                    .size,
            )
        }

    @Test
    fun prunesMoreThanOneDeleteChunk() =
        runTest {
            insertSource()
            val source = FakeMusicSource(artists = (1..1100).map { artist("a$it") })

            SyncService(db, source).sync()
            assertEquals(1100, db.libraryDao().artistIds(1).size)

            source.artists = emptyList()

            SyncService(db, source).sync()

            assertEquals(0, db.libraryDao().artistIds(1).size)
        }

    @Test
    fun partialPruneAcrossManyPagesKeepsTheSurvivors() =
        runTest {
            insertSource()
            val source = FakeMusicSource(artists = (1..1100).map { artist("a$it") })

            SyncService(db, source).sync()
            assertEquals(1100, db.libraryDao().artistIds(1).size)

            val survivors = (1..1100).filter { it % 2 == 0 }.map { "a$it" }
            source.artists = survivors.map { artist(it) }

            SyncService(db, source).sync()

            assertEquals(survivors.sorted(), db.libraryDao().artistIds(1).sorted())
        }

    @Test
    fun partialDiscPruneAcrossManyPagesKeepsTheSurvivors() =
        runTest {
            insertSource()
            val total = 1100

            fun albumsWithDiscTitles(predicate: (Int) -> Boolean) =
                (1..total).map { index ->
                    if (predicate(index)) {
                        album("al$index").copy(discTitles = mapOf(1L to "Disc $index"))
                    } else {
                        album("al$index")
                    }
                }
            val source = FakeMusicSource(albums = albumsWithDiscTitles { true })

            SyncService(db, source).sync()
            assertEquals(total, storedDiscKeys().size)

            source.albums = albumsWithDiscTitles { it % 2 == 0 }

            SyncService(db, source).sync()

            val survivors = (1..total).filter { it % 2 == 0 }.map { "al$it" }.toSet()
            assertEquals(survivors.size, storedDiscKeys().size)
            assertEquals(survivors, storedDiscKeys().map { it.albumId }.toSet())
        }

    @Test
    fun albumAndArtistPlayRanksAreDerivedFromTheSongRows() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1")),
                    albums = listOf(album("al1")),
                    songs =
                        listOf(
                            song("s1").copy(playCount = 5, played = 100),
                            song("s2").copy(playCount = 2, played = 300),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals(7L, db.libraryDao().albumOnce(1, "al1")!!.playCount)
            assertEquals(300L, db.libraryDao().albumOnce(1, "al1")!!.played)
            assertEquals(7L, db.libraryDao().artistOnce(1, "a1")!!.playCount)
            assertEquals(300L, db.libraryDao().artistOnce(1, "a1")!!.played)

            source.songs = listOf(song("s1").copy(playCount = 0, played = null))

            SyncService(db, source).sync()

            assertEquals(0L, db.libraryDao().albumOnce(1, "al1")!!.playCount)
            assertEquals(null, db.libraryDao().albumOnce(1, "al1")!!.played)
            assertEquals(0L, db.libraryDao().artistOnce(1, "a1")!!.playCount)
            assertEquals(null, db.libraryDao().artistOnce(1, "a1")!!.played)
        }

    @Test
    fun anUnchangedSyncWithPlayDataDoesNotRewriteTheRanks() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1")),
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1").copy(playCount = 5, played = 100)),
                )

            SyncService(db, source).sync()
            val before = totalChanges()

            SyncService(db, source).sync()

            assertEquals(before, totalChanges())
            assertEquals(5L, db.libraryDao().albumOnce(1, "al1")!!.playCount)
            assertEquals(5L, db.libraryDao().artistOnce(1, "a1")!!.playCount)
        }

    @Test
    fun anIdenticalSyncDoesNotRewriteRows() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1")),
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1")),
                    playlists = listOf(playlist("p1")),
                    playlistSongs = listOf(PlaylistSong(1, "p1", "s1", 0)),
                )

            SyncService(db, source).sync()
            val before = totalChanges()

            SyncService(db, source).sync()

            assertEquals(before, totalChanges())
        }

    @Test
    fun storesReplayGainAndClearsItWhenTheServerStopsSendingIt() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    albums = listOf(album("al1")),
                    songs =
                        listOf(
                            song("s1").copy(
                                trackGain = -3.2f,
                                albumGain = -2.1f,
                                trackPeak = 0.98f,
                                albumPeak = 0.99f,
                            ),
                        ),
                )

            SyncService(db, source).sync()

            val stored = db.libraryDao().song(1, "s1").first()!!
            assertEquals(-3.2f, stored.trackGain)
            assertEquals(-2.1f, stored.albumGain)
            assertEquals(0.98f, stored.trackPeak)
            assertEquals(0.99f, stored.albumPeak)

            val before = totalChanges()
            SyncService(db, source).sync()
            assertEquals(before, totalChanges())

            source.songs = listOf(song("s1"))

            SyncService(db, source).sync()

            val cleared = db.libraryDao().song(1, "s1").first()!!
            assertEquals(null, cleared.trackGain)
            assertEquals(null, cleared.albumGain)
            assertEquals(null, cleared.trackPeak)
            assertEquals(null, cleared.albumPeak)
        }

    @Test
    fun albumArtistSortPrefersTheSingularArtistOverThePluralList() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists =
                        listOf(
                            artist("various", "Various Artists").copy(sortName = "Various Artists"),
                            artist("utada", "宇多田ヒカル").copy(sortName = "Utada Hikaru"),
                        ),
                    albums =
                        listOf(
                            album("al1").copy(
                                artistId = "various",
                                albumArtist = "Various Artists",
                                artists = listOf(ArtistRef("utada", "宇多田ヒカル")),
                            ),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals("Various Artists", db.libraryDao().albumOnce(1, "al1")!!.albumArtistSort)
        }

    @Test
    fun syncStoresSortNamesAndResolvesArtistSorts() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "宇多田ヒカル").copy(sortName = "Utada Hikaru")),
                    albums =
                        listOf(
                            album("al1").copy(
                                name = "First Love",
                                sortName = "First Love",
                                albumArtist = "宇多田ヒカル",
                            ),
                        ),
                    songs =
                        listOf(
                            song("s1").copy(
                                title = "Automatic",
                                sortName = "Automatic",
                                artist = "宇多田ヒカル",
                                artistSort = "",
                            ),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals("Utada Hikaru", db.libraryDao().artistOnce(1, "a1")!!.sortName)
            assertEquals("First Love", db.libraryDao().albumOnce(1, "al1")!!.sortName)
            assertEquals("Utada Hikaru", db.libraryDao().albumOnce(1, "al1")!!.albumArtistSort)
            assertEquals("Automatic", db.libraryDao().songOnce(1, "s1")!!.sortName)
            assertEquals("Utada Hikaru", db.libraryDao().songOnce(1, "s1")!!.artistSort)
        }

    @Test
    fun songArtistSortUsesTheSingularArtistOverThePluralList() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists =
                        listOf(
                            artist("a1", "A").copy(sortName = "Alpha"),
                            artist("a2", "B").copy(sortName = "Beta"),
                        ),
                    albums = listOf(album("al1")),
                    songs =
                        listOf(
                            song("s1").copy(
                                artistId = "a2",
                                artists = listOf(ArtistRef("a1", "A"), ArtistRef("a2", "B")),
                            ),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals("Beta", db.libraryDao().songOnce(1, "s1")!!.artistSort)
        }

    @Test
    fun albumArtistSortFallsBackToAPluralRefWhenTheSingularArtistIsMissing() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "A").copy(sortName = "Alpha")),
                    albums =
                        listOf(
                            album("al1").copy(
                                artistId = "missing",
                                albumArtist = "A",
                                artists = listOf(ArtistRef("a1", "A")),
                            ),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals("Alpha", db.libraryDao().albumOnce(1, "al1")!!.albumArtistSort)
        }

    @Test
    fun changingAnArtistSortNameUpdatesAlbumAndSongSortKeys() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "A").copy(sortName = "Alpha")),
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1")),
                )

            SyncService(db, source).sync()
            assertEquals("Alpha", db.libraryDao().albumOnce(1, "al1")!!.albumArtistSort)
            assertEquals("Alpha", db.libraryDao().songOnce(1, "s1")!!.artistSort)

            source.artists = listOf(artist("a1", "A").copy(sortName = "Zulu"))

            SyncService(db, source).sync()

            assertEquals("Zulu", db.libraryDao().albumOnce(1, "al1")!!.albumArtistSort)
            assertEquals("Zulu", db.libraryDao().songOnce(1, "s1")!!.artistSort)
        }

    @Test
    fun theArtistsListHidesNamesThatAreOnlyDisplayStrings() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists =
                        listOf(
                            artist("a1", "Alpha").copy(playCount = 5, played = 100),
                            artist("combo", "Alpha & Beta").copy(playCount = 5, played = 100),
                        ),
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1").copy(playCount = 5, played = 100)),
                )

            SyncService(db, source).sync()

            assertEquals(false, db.libraryDao().artistOnce(1, "combo")!!.credited)
            assertEquals(true, db.libraryDao().artistOnce(1, "a1")!!.credited)

            assertEquals(
                listOf("a1"),
                db
                    .libraryDao()
                    .artistsByName(1, 0, "")
                    .allRows()
                    .map { it.id },
            )
            val query = artistsDefaultQuery(1, 0, "alpha", ArtistSort.Name, false)
            assertEquals(
                listOf("a1"),
                db
                    .libraryDao()
                    .artistsByName(1, 0, "alpha")
                    .allRows()
                    .map { it.id },
            )
            assertEquals(
                listOf("a1"),
                db
                    .libraryDao()
                    .artistsPaging(query)
                    .allRows()
                    .map { it.id },
            )
            assertEquals(
                listOf("a1"),
                db
                    .libraryDao()
                    .searchArtists(1, "alpha", 10)
                    .first()
                    .map { it.id },
            )
            assertEquals(
                listOf("a1"),
                db
                    .libraryDao()
                    .homeMostPlayedArtists(1)
                    .allRows()
                    .map { it.id },
            )
        }

    @Test
    fun downloadedArtistsHidesNamesThatAreOnlyDisplayStrings() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "Alpha"), artist("combo", "Alpha & Beta")),
                    albums =
                        listOf(
                            album("al1").copy(
                                artistId = "combo",
                                albumArtist = "Alpha & Beta",
                                artists = listOf(ArtistRef("a1", "Alpha")),
                            ),
                        ),
                    songs =
                        listOf(
                            song("s1").copy(
                                artistId = "combo",
                                artist = "Alpha & Beta",
                                artists = listOf(ArtistRef("a1", "Alpha")),
                            ),
                        ),
                )

            SyncService(db, source).sync()
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed))

            assertEquals(
                listOf("a1"),
                db
                    .libraryDao()
                    .downloadedArtists(1)
                    .allRows()
                    .map { it.id },
            )
        }

    @Test
    fun anInterruptedSyncLeavesTheCreditMarkerStale() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "Alpha"), artist("combo", "Alpha & Beta")),
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1")),
                    songsFailure = java.io.IOException("boom"),
                )

            runCatching { SyncService(db, source).sync() }

            assertEquals(true, db.sourcesDao().creditsStale(1))
            assertEquals(
                listOf("a1", "combo"),
                db
                    .libraryDao()
                    .artistsByName(1, 0, "")
                    .allRows()
                    .map { it.id },
            )

            source.songsFailure = null
            SyncService(db, source).sync()

            assertEquals(false, db.sourcesDao().creditsStale(1))
            assertEquals(
                listOf("a1"),
                db
                    .libraryDao()
                    .artistsByName(1, 0, "")
                    .allRows()
                    .map { it.id },
            )
        }

    @Test
    fun aStaleCreditMarkerForcesARecomputeOnTheNextSync() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "A")),
                    albums = listOf(album("al1")),
                    songs = listOf(song("s1")),
                )

            SyncService(db, source).sync()
            assertEquals(false, db.sourcesDao().creditsStale(1))

            db.sourcesDao().setCreditsStale(1, true)
            db.libraryDao().upsertArtists(
                listOf(db.libraryDao().artistOnce(1, "a1")!!.copy(songCount = 0, credited = false)),
            )

            SyncService(db, source).sync()

            val artist = db.libraryDao().artistOnce(1, "a1")!!
            assertEquals(1L, artist.songCount)
            assertEquals(true, artist.credited)
            assertEquals(false, db.sourcesDao().creditsStale(1))
        }

    @Test
    fun everyAlbumArtistAndTrackArtistReachesTheSharedAlbum() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "A"), artist("a2", "B")),
                    albums =
                        listOf(
                            album("al1").copy(
                                artistId = "a1",
                                albumArtist = "A • B",
                                artists = listOf(ArtistRef("a1", "A"), ArtistRef("a2", "B")),
                            ),
                        ),
                    songs =
                        listOf(
                            song("s1").copy(
                                artistId = "a1",
                                artist = "A • B",
                                artists = listOf(ArtistRef("a1", "A"), ArtistRef("a2", "B")),
                            ),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals(
                listOf("al1"),
                db
                    .libraryDao()
                    .albumsForArtist(1, "a1")
                    .first()
                    .map { it.id },
            )
            assertEquals(
                listOf("al1"),
                db
                    .libraryDao()
                    .albumsForArtist(1, "a2")
                    .first()
                    .map { it.id },
            )
            assertEquals(listOf("s1"), db.queueDao().artistSongIds(1, "a1"))
            assertEquals(listOf("s1"), db.queueDao().artistSongIds(1, "a2"))
            assertEquals(1L, db.queueDao().artistLength(1, "a2"))
            assertEquals("B", db.libraryDao().artistName(1, "a2").first())
            assertEquals(1L, db.libraryDao().artistOnce(1, "a1")!!.songCount)
            assertEquals(1L, db.libraryDao().artistOnce(1, "a2")!!.songCount)

            source.albums = listOf(album("al1").copy(artistId = "a1", artists = listOf(ArtistRef("a1", "A"))))
            source.songs = listOf(song("s1").copy(artistId = "a1", artists = listOf(ArtistRef("a1", "A"))))

            SyncService(db, source).sync()

            assertEquals(
                emptyList<String>(),
                db
                    .libraryDao()
                    .albumsForArtist(1, "a2")
                    .first()
                    .map { it.id },
            )
            assertEquals(emptyList<String>(), db.queueDao().artistSongIds(1, "a2"))
            assertEquals(0L, db.libraryDao().artistOnce(1, "a2")!!.songCount)
        }

    @Test
    fun artistSongCountsAndDownloadsIncludeAlbumlessTracks() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "A")),
                    songs =
                        listOf(
                            song("s1").copy(albumId = null),
                            song("s2").copy(albumId = null),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals(2L, db.libraryDao().artistOnce(1, "a1")!!.songCount)
            assertEquals(listOf("s1", "s2"), db.queueDao().artistSongIds(1, "a1"))
            assertEquals(2L, db.queueDao().artistLength(1, "a1"))
            assertEquals(2, db.queueDao().artistSongs(1, "a1", 0, 10).size)
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed))
            db.downloadDao().upsert(SongDownload(1, "s2", DownloadStatus.Completed))

            assertEquals(
                ListDownloadStatus(total = 2, downloaded = 2),
                db.downloadDao().artistStatus(1, "a1").first(),
            )
            assertEquals(
                2L,
                db
                    .downloadDao()
                    .artistStatuses(1)
                    .first()
                    .single { it.id == "a1" }
                    .downloaded,
            )
        }

    @Test
    fun movingASongBetweenAlbumsUpdatesAlbumArtistCounts() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "A"), artist("a2", "B"), artist("a3", "Track")),
                    albums =
                        listOf(
                            album("al1").copy(artistId = "a1"),
                            album("al2").copy(artistId = "a2"),
                        ),
                    songs = listOf(song("s1").copy(artistId = "a3", albumId = "al1")),
                )

            SyncService(db, source).sync()

            assertEquals(1L, db.libraryDao().artistOnce(1, "a1")!!.songCount)
            assertEquals(0L, db.libraryDao().artistOnce(1, "a2")!!.songCount)
            assertEquals(1L, db.libraryDao().artistOnce(1, "a3")!!.songCount)

            source.songs = listOf(song("s1").copy(artistId = "a3", albumId = "al2"))

            SyncService(db, source).sync()

            assertEquals(0L, db.libraryDao().artistOnce(1, "a1")!!.songCount)
            assertEquals(1L, db.libraryDao().artistOnce(1, "a2")!!.songCount)
            assertEquals(1L, db.libraryDao().artistOnce(1, "a3")!!.songCount)
        }

    @Test
    fun anIdenticalSyncDoesNotRewriteArtistRows() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists =
                        listOf(
                            artist("a1", "Artist").copy(albumCount = 9),
                            artist("a2", "Other").copy(albumCount = 9),
                        ),
                    albums =
                        listOf(
                            album("al1").copy(artists = listOf(ArtistRef("a1", "Artist"), ArtistRef("a2", "Other"))),
                        ),
                    songs =
                        listOf(
                            song("s1").copy(artists = listOf(ArtistRef("a1", "Artist"), ArtistRef("a2", "Other"))),
                        ),
                )

            SyncService(db, source).sync()
            val before = totalChanges()

            SyncService(db, source).sync()

            assertEquals(before, totalChanges())
        }

    @Test
    fun theDownloadedArtistFilterIncludesEveryAlbumArtist() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "A"), artist("a2", "B"), artist("a3", "C")),
                    albums =
                        listOf(
                            album("al1").copy(
                                artistId = "a1",
                                albumArtist = "A • B",
                                artists = listOf(ArtistRef("a1", "A"), ArtistRef("a2", "B")),
                            ),
                        ),
                    songs = listOf(song("s1").copy(artistId = "a1")),
                )

            SyncService(db, source).sync()
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed))

            val filtered = db.libraryDao().artistsByName(1, 0, "", downloadedFilter = 1).allRows()
            assertEquals(listOf("A", "B"), filtered.map { it.name })
            assertEquals(listOf(1L, 1L), filtered.map { it.albumCount })
            assertEquals(ListDownloadStatus(total = 1, downloaded = 1), db.downloadDao().artistStatus(1, "a2").first())
            assertEquals(
                setOf("A", "B"),
                db
                    .libraryDao()
                    .downloadedArtists(1)
                    .allRows()
                    .map { it.name }
                    .toSet(),
            )

            val statuses =
                db
                    .downloadDao()
                    .artistStatuses(1)
                    .first()
                    .associateBy { it.id }
            assertEquals(setOf("a1", "a2"), statuses.keys)
            assertEquals(1L, statuses.getValue("a2").downloaded)
        }

    @Test
    fun playsOnASharedAlbumReachEveryAlbumArtist() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "A"), artist("a2", "B")),
                    albums =
                        listOf(
                            album("al1").copy(
                                artistId = "a1",
                                artists = listOf(ArtistRef("a1", "A"), ArtistRef("a2", "B")),
                            ),
                        ),
                    songs = listOf(song("s1").copy(albumId = "al1", playCount = 3, played = 100)),
                )

            SyncService(db, source).sync()

            assertEquals(3L, db.libraryDao().artistOnce(1, "a2")!!.playCount)
            assertEquals(100L, db.libraryDao().artistOnce(1, "a2")!!.played)

            db.libraryDao().recordPlay(1, "s1", 500)

            assertEquals(4L, db.libraryDao().artistOnce(1, "a1")!!.playCount)
            assertEquals(4L, db.libraryDao().artistOnce(1, "a2")!!.playCount)
            assertEquals(500L, db.libraryDao().artistOnce(1, "a2")!!.played)
        }

    @Test
    fun trackOnlyCreditsReachTheArtistBadgeAndPlays() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "Album Artist"), artist("a2", "Guest").copy(albumCount = 7)),
                    albums =
                        listOf(
                            album("al1").copy(
                                artistId = "a1",
                                artists = listOf(ArtistRef("a1", "Album Artist")),
                            ),
                            album("al2").copy(
                                artistId = "a2",
                                artists = listOf(ArtistRef("a2", "Guest")),
                            ),
                        ),
                    songs =
                        listOf(
                            song("s1").copy(
                                albumId = "al1",
                                artistId = "a1",
                                artists = listOf(ArtistRef("a1", "Album Artist"), ArtistRef("a2", "Guest")),
                                playCount = 3,
                                played = 100,
                            ),
                            song("s2").copy(
                                albumId = "al1",
                                artistId = "a2",
                                artists = listOf(ArtistRef("a2", "Guest")),
                                playCount = 4,
                                played = 200,
                            ),
                        ),
                )

            SyncService(db, source).sync()

            val guest = db.libraryDao().artistOnce(1, "a2")!!
            assertEquals(2L, guest.albumCount)
            assertEquals(2L, guest.songCount)
            assertEquals(7L, guest.playCount)
            assertEquals(200L, guest.played)
            val albums = db.libraryDao().albumsForArtist(1, "a2").first()
            assertEquals(listOf("al1", "al2"), albums.map { it.id }.sorted())
            assertEquals(albums.size.toLong(), guest.albumCount)
            assertEquals(
                albums.size.toLong(),
                db
                    .libraryDao()
                    .artistsByName(1, 0, "")
                    .allRows()
                    .first { it.id == "a2" }
                    .albumCount,
            )
            assertEquals(guest.songCount, db.queueDao().artistLength(1, "a2"))
        }

    @Test
    fun songsOnMissingAlbumsAreSongsButNotAlbums() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "Artist")),
                    albums = emptyList(),
                    songs =
                        listOf(
                            song("s1").copy(
                                albumId = "missing",
                                artistId = "a1",
                                artists = listOf(ArtistRef("a1", "Artist")),
                            ),
                        ),
                )

            SyncService(db, source).sync()

            val artist = db.libraryDao().artistOnce(1, "a1")!!
            assertEquals(1L, artist.songCount)
            assertEquals(0L, artist.albumCount)
            assertEquals(
                emptyList<String>(),
                db
                    .libraryDao()
                    .albumsForArtist(1, "a1")
                    .first()
                    .map { it.id },
            )
        }

    @Test
    fun albumlessTrackCreditsCountAndBumpOnPlay() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "Album Artist"), artist("a2", "Guest")),
                    albums = emptyList(),
                    songs =
                        listOf(
                            song("s1").copy(
                                albumId = null,
                                artistId = "a1",
                                artists = listOf(ArtistRef("a1", "Album Artist"), ArtistRef("a2", "Guest")),
                                playCount = 2,
                                played = 50,
                            ),
                        ),
                )

            SyncService(db, source).sync()

            val guest = db.libraryDao().artistOnce(1, "a2")!!
            assertEquals(0L, guest.albumCount)
            assertEquals(1L, guest.songCount)
            assertEquals(2L, guest.playCount)
            assertEquals(50L, guest.played)

            db.libraryDao().recordPlay(1, "s1", 300)

            assertEquals(3L, db.libraryDao().artistOnce(1, "a2")!!.playCount)
            assertEquals(300L, db.libraryDao().artistOnce(1, "a2")!!.played)
        }

    @Test
    fun leadIdsOutsideThePluralRefsKeepArtistTotalsAndShufflesAligned() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "Lead"), artist("a2", "Ref")),
                    albums =
                        listOf(
                            album("al1").copy(
                                artistId = "a1",
                                artists = listOf(ArtistRef("a2", "Ref")),
                            ),
                        ),
                    songs =
                        listOf(
                            song("s1").copy(
                                albumId = "al1",
                                artistId = "a1",
                                artists = listOf(ArtistRef("a2", "Ref")),
                            ),
                        ),
                )

            SyncService(db, source).sync()

            val lead = db.libraryDao().artistOnce(1, "a1")!!
            assertEquals(1L, lead.songCount)
            assertEquals(1L, lead.albumCount)
            assertEquals(db.queueDao().artistLength(1, "a1"), lead.songCount)
            assertEquals(
                lead.songCount,
                db
                    .downloadDao()
                    .artistStatus(1, "a1")
                    .first()
                    .total,
            )
        }

    @Test
    fun albumCreditIdsFollowPluralCredits() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "A"), artist("a2", "B")),
                    albums =
                        listOf(
                            album("al1").copy(artistId = "a1", artists = listOf(ArtistRef("a1", "A"))),
                            album("al2").copy(artistId = "a2", artists = listOf(ArtistRef("a2", "B"))),
                            album("al3").copy(
                                artistId = "a2",
                                artists = listOf(ArtistRef("a1", "A"), ArtistRef("a2", "B")),
                            ),
                        ),
                    songs =
                        listOf(
                            song("s1").copy(
                                albumId = "al1",
                                artistId = "a1",
                                artists = listOf(ArtistRef("a1", "A")),
                            ),
                            song("s2").copy(
                                albumId = "al2",
                                artistId = "a2",
                                artists = listOf(ArtistRef("a2", "B"), ArtistRef("a1", "A")),
                            ),
                            song("s3").copy(
                                albumId = "al3",
                                artistId = "a2",
                                artists = listOf(ArtistRef("a1", "A"), ArtistRef("a2", "B")),
                            ),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals(
                listOf("al1", "al3"),
                db
                    .libraryDao()
                    .albumCreditAlbumIds(1, "a1")
                    .first()
                    .sorted(),
            )
            assertEquals(
                listOf("al2", "al3"),
                db
                    .libraryDao()
                    .albumCreditAlbumIds(1, "a2")
                    .first()
                    .sorted(),
            )
            assertEquals(
                listOf("al1", "al2", "al3"),
                db
                    .libraryDao()
                    .albumsForArtist(1, "a1")
                    .first()
                    .map { it.id }
                    .sorted(),
            )
        }

    @Test
    fun everyArtistConsumerAgreesOnTheCreditedSet() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "Album Artist"), artist("a2", "Guest"), artist("a3", "Track Only")),
                    albums =
                        listOf(
                            album("al1").copy(artistId = "a1", artists = listOf(ArtistRef("a1", "Album Artist"))),
                            album("al2").copy(artistId = "a2", artists = listOf(ArtistRef("a2", "Guest"))),
                        ),
                    songs =
                        listOf(
                            song("s1").copy(
                                albumId = "al1",
                                artistId = "a1",
                                artists = listOf(ArtistRef("a1", "Album Artist"), ArtistRef("a2", "Guest")),
                                playCount = 1,
                                played = 100,
                            ),
                            song("s2").copy(
                                albumId = "al1",
                                artistId = "a2",
                                artists = listOf(ArtistRef("a2", "Guest"), ArtistRef("a3", "Track Only")),
                            ),
                            song("s3").copy(
                                albumId = null,
                                artistId = "a3",
                                artists = listOf(ArtistRef("a3", "Track Only")),
                            ),
                            song("s4").copy(
                                albumId = "missing",
                                artistId = "a2",
                                artists = listOf(ArtistRef("a2", "Guest")),
                            ),
                        ),
                )

            SyncService(db, source).sync()
            db.downloadDao().upsert(SongDownload(1, "s1", DownloadStatus.Completed))
            db.downloadDao().upsert(SongDownload(1, "s3", DownloadStatus.Completed))
            db.downloadDao().upsert(SongDownload(1, "s4", DownloadStatus.Completed))

            for (artistId in listOf("a1", "a2", "a3")) {
                val artist = db.libraryDao().artistOnce(1, artistId)!!
                val songIds = db.queueDao().artistSongIds(1, artistId)
                val albums = db.libraryDao().albumsForArtist(1, artistId).first()
                val status = db.downloadDao().artistStatus(1, artistId).first()
                val downloaded =
                    db
                        .downloadDao()
                        .all()
                        .count { it.status == DownloadStatus.Completed && it.songId in songIds }

                assertEquals("$artistId albums", albums.size.toLong(), artist.albumCount)
                assertEquals("$artistId songs", songIds.size.toLong(), artist.songCount)
                assertEquals("$artistId length", songIds.size.toLong(), db.queueDao().artistLength(1, artistId))
                assertEquals("$artistId total", songIds.size.toLong(), status.total)
                assertEquals("$artistId downloaded", downloaded.toLong(), status.downloaded)
            }

            val downloadedArtists = db.libraryDao().artistsByName(1, 0, "", downloadedFilter = 1).allRows()
            for (artist in downloadedArtists) {
                assertEquals(
                    "${artist.id} downloaded albums",
                    db
                        .libraryDao()
                        .albumsForArtistDownloaded(1, artist.id)
                        .first()
                        .size
                        .toLong(),
                    artist.albumCount,
                )
            }
        }

    @Test
    fun changingOnlyTheLeadArtistRecomputesTheCreditedCounts() =
        runTest {
            insertSource()
            val source =
                FakeMusicSource(
                    artists = listOf(artist("a1", "Lead"), artist("a2", "Ref")),
                    albums =
                        listOf(
                            album("al1").copy(
                                artistId = "a1",
                                artists = listOf(ArtistRef("a2", "Ref")),
                            ),
                        ),
                    songs =
                        listOf(
                            song("s1").copy(
                                albumId = "al1",
                                artistId = "a1",
                                artists = listOf(ArtistRef("a2", "Ref")),
                            ),
                        ),
                )

            SyncService(db, source).sync()

            assertEquals(1L, db.libraryDao().artistOnce(1, "a1")!!.songCount)
            assertEquals(1L, db.libraryDao().artistOnce(1, "a1")!!.albumCount)

            source.albums = listOf(album("al1").copy(artistId = "a2", artists = listOf(ArtistRef("a2", "Ref"))))
            source.songs =
                listOf(
                    song("s1").copy(
                        albumId = "al1",
                        artistId = "a2",
                        artists = listOf(ArtistRef("a2", "Ref")),
                    ),
                )

            SyncService(db, source).sync()

            assertEquals(0L, db.libraryDao().artistOnce(1, "a1")!!.songCount)
            assertEquals(0L, db.libraryDao().artistOnce(1, "a1")!!.albumCount)
            assertEquals(1L, db.libraryDao().artistOnce(1, "a2")!!.songCount)
            assertEquals(1L, db.libraryDao().artistOnce(1, "a2")!!.albumCount)
        }

    @Test
    fun anIdenticalSyncSkipsTheAggregateQueries() =
        runTest {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val executions = Collections.synchronizedList(mutableListOf<SqlExecution>())
            val loggingDb =
                Room
                    .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                    .setDriver(LoggingSQLiteDriver(BundledSQLiteDriver()) { executions += it })
                    .build()
            try {
                loggingDb.sourcesDao().upsertSource(
                    Source(id = 1, name = "test", address = "http://localhost", isActive = true, createdAt = 0),
                )
                val source =
                    FakeMusicSource(
                        artists = listOf(artist("a1", "Artist")),
                        albums = listOf(album("al1")),
                        songs = listOf(song("s1")),
                    )

                SyncService(loggingDb, source).sync()
                val firstSyncExecutions = synchronized(executions) { executions.toList() }
                assertTrue(firstSyncExecutions.any { it.sql.contains("credited_albums") })
                assertTrue(firstSyncExecutions.any { it.sql.contains("UPDATE albums SET playCount") })

                synchronized(executions) { executions.clear() }
                SyncService(loggingDb, source).sync()

                val secondSyncExecutions = synchronized(executions) { executions.toList() }
                assertTrue(secondSyncExecutions.none { it.sql.contains("credited_albums") })
                assertTrue(secondSyncExecutions.none { it.sql.contains("UPDATE albums SET playCount") })
            } finally {
                loggingDb.close()
            }
        }

    private suspend fun totalChanges(): Long =
        db.useWriterConnection { connection ->
            connection.usePrepared("SELECT total_changes()") { statement ->
                statement.step()
                statement.getLong(0)
            }
        }

    private suspend fun songIds(sourceId: Long): List<String> =
        db
            .libraryDao()
            .songIdsAfter(sourceId, "", Int.MAX_VALUE)

    private suspend fun storedDiscKeys(): List<DiscKey> =
        db
            .libraryDao()
            .discKeysAfter(1, "", 0L, 100_000)

    private suspend fun storedDiscs(albumId: String): Map<Long, String> =
        db
            .libraryDao()
            .discs(1, albumId)
            .first()
            .associate { it.disc to it.title }

    private suspend fun <T : Any> PagingSource<Int, T>.allRows(): List<T> {
        val page = load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
        return (page as PagingSource.LoadResult.Page).data
    }

    private suspend fun insertSource(
        id: Long = 1,
        name: String = "test",
    ) {
        db.sourcesDao().upsertSource(
            Source(id = id, name = name, address = "http://localhost/$id", isActive = id == 1L, createdAt = 0),
        )
    }

    private fun artist(
        id: String,
        name: String = "Artist $id",
    ) = Artist(sourceId = 1, id = id, name = name, albumCount = 1, starred = null)

    private fun album(id: String) =
        Album(
            sourceId = 1,
            id = id,
            artistId = "a1",
            name = "Album $id",
            albumArtist = "Artist",
            created = 0,
            coverArt = null,
            genre = null,
            year = null,
            starred = null,
            songCount = 1,
        )

    private fun song(id: String) =
        Song(
            sourceId = 1,
            id = id,
            albumId = "al1",
            artistId = "a1",
            title = "Song $id",
            album = "Album",
            artist = "Artist",
            duration = 100,
            track = 1,
            disc = 1,
            starred = null,
            genre = null,
        )

    private fun playlist(id: String) =
        Playlist(sourceId = 1, id = id, name = "Playlist $id", comment = null, coverArt = null, songCount = 1, created = 0)
}

private class FakeMusicSource(
    override val id: Long = 1,
    var artists: List<Artist> = emptyList(),
    var albums: List<Album> = emptyList(),
    var songs: List<Song> = emptyList(),
    var playlists: List<Playlist> = emptyList(),
    var playlistSongs: List<PlaylistSong> = emptyList(),
    var songsFailure: Throwable? = null,
    private val batchSize: Int = 1000,
) : MusicSource {
    val requestedPlaylistIds = mutableListOf<List<String>>()

    override suspend fun ping() = Unit

    override suspend fun setStar(
        type: StarType,
        id: String,
        starred: Boolean,
    ) = Unit

    override suspend fun scrobble(
        songId: String,
        submission: Boolean,
        time: Long?,
    ) = Unit

    override fun artists(): Flow<List<Artist>> = batches(artists)

    override fun albums(): Flow<List<Album>> = batches(albums)

    override fun songs(): Flow<List<Song>> =
        if (songsFailure == null) {
            batches(songs)
        } else {
            flow { throw songsFailure!! }
        }

    override fun playlists(): Flow<List<Playlist>> = batches(playlists)

    override fun playlistSongs(playlistIds: List<String>): Flow<List<PlaylistSong>> {
        requestedPlaylistIds += playlistIds
        return batches(playlistSongs)
    }

    private fun <T> batches(items: List<T>): Flow<List<T>> =
        flow {
            items.chunked(batchSize).forEach { emit(it) }
        }
}
