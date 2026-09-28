package com.subtracks.data.db

import android.content.Context
import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.room3.useReaderConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.Source
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun starredSortsKeepUnstarredLastInBothDirections() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a1", "Alpha", year = null, starred = 100),
                    album(sourceId, "a2", "Beta", year = null, starred = 200),
                    album(sourceId, "a3", "Gamma", year = null, starred = null),
                    album(sourceId, "a4", "Delta", year = null, starred = null),
                ),
            )
            dao.upsertArtists(
                listOf(
                    artist(sourceId, "r1", "Alpha", albumCount = 1).copy(starred = 100),
                    artist(sourceId, "r2", "Beta", albumCount = 1).copy(starred = 200),
                    artist(sourceId, "r3", "Gamma", albumCount = 1).copy(starred = null),
                    artist(sourceId, "r4", "Delta", albumCount = 1).copy(starred = null),
                ),
            )

            assertEquals(
                listOf("Beta", "Alpha", "Delta", "Gamma"),
                dao.albumsByStarred(sourceId, 0, "").page().map { it.name },
            )
            assertEquals(
                listOf("Alpha", "Beta", "Gamma", "Delta"),
                dao.albumsByStarredReversed(sourceId, 0, "").page().map { it.name },
            )
            assertEquals(
                listOf("Beta", "Alpha", "Delta", "Gamma"),
                dao.artistsByStarred(sourceId, 0, "").page().map { it.name },
            )
            assertEquals(
                listOf("Alpha", "Beta", "Gamma", "Delta"),
                dao.artistsByStarredReversed(sourceId, 0, "").page().map { it.name },
            )
        }

    @Test
    fun reversedAlbumSortsReverseEveryTiebreaker() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a1", "Same", year = 2000, starred = null, created = 100).copy(albumArtist = "Zed"),
                    album(sourceId, "a2", "Same", year = 2000, starred = null, created = 100).copy(albumArtist = "Amy"),
                    album(sourceId, "a3", "Same", year = 1990, starred = null, created = 200).copy(albumArtist = "Amy"),
                    album(sourceId, "a4", "Other", year = 2000, starred = null, created = 100).copy(albumArtist = "Amy"),
                ),
            )

            val pairs =
                listOf(
                    dao.albumsByName(sourceId, 0, "") to dao.albumsByNameReversed(sourceId, 0, ""),
                    dao.albumsByArtist(sourceId, 0, "") to dao.albumsByArtistReversed(sourceId, 0, ""),
                    dao.albumsByYear(sourceId, 0, "") to dao.albumsByYearReversed(sourceId, 0, ""),
                    dao.albumsByRecentlyAdded(sourceId, 0, "") to dao.albumsByRecentlyAddedReversed(sourceId, 0, ""),
                )
            for ((base, reversed) in pairs) {
                val ids = base.page().map { it.id }
                assertEquals(ids.reversed(), reversed.page().map { it.id })
            }
        }

    @Test
    fun reversedArtistAndPlaylistSortsReverseEveryTiebreaker() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertArtists(
                listOf(
                    artist(sourceId, "r1", "Same", albumCount = 2),
                    artist(sourceId, "r2", "Other", albumCount = 1),
                    artist(sourceId, "r3", "Same", albumCount = 1),
                ),
            )
            dao.upsertPlaylists(
                listOf(
                    playlist(sourceId, "l1", "Same", created = 100, changed = 100),
                    playlist(sourceId, "l2", "Other", created = 100, changed = 200),
                    playlist(sourceId, "l3", "Same", created = 200, changed = 100),
                ),
            )

            val artistPairs =
                listOf(
                    dao.artistsByName(sourceId, 0, "").page().map { it.id } to
                        dao.artistsByNameReversed(sourceId, 0, "").page().map { it.id },
                    dao.artistsByAlbumCount(sourceId, 0, "").page().map { it.id } to
                        dao.artistsByAlbumCountReversed(sourceId, 0, "").page().map { it.id },
                )
            val playlistPairs =
                listOf(
                    dao.playlistsByName(sourceId, "").page().map { it.id } to
                        dao.playlistsByNameReversed(sourceId, "").page().map { it.id },
                    dao.playlistsByAdded(sourceId, "").page().map { it.id } to
                        dao.playlistsByAddedReversed(sourceId, "").page().map { it.id },
                    dao.playlistsByUpdated(sourceId, "").page().map { it.id } to
                        dao.playlistsByUpdatedReversed(sourceId, "").page().map { it.id },
                )
            for ((base, reversed) in artistPairs + playlistPairs) {
                assertEquals(base.reversed(), reversed)
            }
        }

    // These plan assertions depend on the query planner in the pinned androidx.sqlite:sqlite-bundled
    // (see gradle/libs.versions.toml); re-check the expected index names when that engine is bumped.
    @Test
    fun albumOrdersAreIndexBacked() =
        runTest {
            val orders =
                listOf(
                    ALBUM_ORDER_BY_NAME to "index_albums_name",
                    ALBUM_ORDER_BY_NAME_REVERSED to "index_albums_name",
                    ALBUM_ORDER_BY_ARTIST to "index_albums_artist",
                    ALBUM_ORDER_BY_ARTIST_REVERSED to "index_albums_artist",
                    ALBUM_ORDER_BY_YEAR to "index_albums_year",
                    ALBUM_ORDER_BY_YEAR_REVERSED to "index_albums_year",
                    ALBUM_ORDER_BY_ADDED to "index_albums_added",
                    ALBUM_ORDER_BY_ADDED_REVERSED to "index_albums_added",
                    ALBUM_ORDER_BY_STARRED to "index_albums_starred",
                    ALBUM_ORDER_BY_STARRED_REVERSED to "index_albums_starred",
                )
            for ((order, index) in orders) {
                val plan = plan("SELECT * $ALBUMS_FILTER ORDER BY $order LIMIT 20 OFFSET 0")
                assertTrue(plan, plan.contains("USING INDEX $index"))
                assertFalse(plan, plan.contains("TEMP B-TREE"))
            }
        }

    @Test
    fun artistOrdersAreIndexBacked() =
        runTest {
            val orders =
                listOf(
                    ARTIST_ORDER_BY_NAME to "index_artists_name",
                    ARTIST_ORDER_BY_NAME_REVERSED to "index_artists_name",
                    ARTIST_ORDER_BY_ALBUM_COUNT to "index_artists_albumCount",
                    ARTIST_ORDER_BY_ALBUM_COUNT_REVERSED to "index_artists_albumCount",
                    ARTIST_ORDER_BY_STARRED to "index_artists_starred",
                    ARTIST_ORDER_BY_STARRED_REVERSED to "index_artists_starred",
                )
            for ((order, index) in orders) {
                val plan = plan("SELECT * $ARTISTS_FILTER ORDER BY $order LIMIT 20 OFFSET 0")
                assertTrue(plan, plan.contains("USING INDEX $index"))
                assertFalse(plan, plan.contains("TEMP B-TREE"))
            }
        }

    @Test
    fun playlistOrdersAreIndexBacked() =
        runTest {
            val orders =
                listOf(
                    PLAYLIST_ORDER_BY_NAME to "index_playlists_name",
                    PLAYLIST_ORDER_BY_NAME_REVERSED to "index_playlists_name",
                    PLAYLIST_ORDER_BY_ADDED to "index_playlists_added",
                    PLAYLIST_ORDER_BY_ADDED_REVERSED to "index_playlists_added",
                    PLAYLIST_ORDER_BY_UPDATED to "index_playlists_updated",
                    PLAYLIST_ORDER_BY_UPDATED_REVERSED to "index_playlists_updated",
                )
            for ((order, index) in orders) {
                val plan = plan("SELECT * $PLAYLISTS_FILTER ORDER BY $order LIMIT 20 OFFSET 0")
                assertTrue(plan, plan.contains("USING INDEX $index"))
                assertFalse(plan, plan.contains("TEMP B-TREE"))
            }
        }

    @Test
    fun anUnindexedAlbumOrderStillShowsATempSort() =
        runTest {
            val plan = plan("SELECT * $ALBUMS_FILTER ORDER BY songCount, id LIMIT 20 OFFSET 0")

            assertTrue(plan, plan.contains("TEMP B-TREE"))
        }

    @Test
    fun theDownloadedFilterKeepsOnlyEntitiesWithACompletedDownload() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "al-1", "Downloaded", year = 2000, starred = null),
                    album(sourceId, "al-2", "Empty", year = 2001, starred = null),
                    album(sourceId, "al-3", "Elsewhere", year = 2002, starred = null).copy(artistId = "ar-2"),
                ),
            )
            dao.upsertArtists(
                listOf(
                    artist(sourceId, "ar-1", "Artist", albumCount = 2),
                    artist(sourceId, "ar-2", "Other", albumCount = 1),
                ),
            )
            dao.upsertSongs(
                listOf(
                    song(sourceId, "s1", "One", starred = null),
                    song(sourceId, "s2", "Two", starred = null).copy(albumId = "al-2"),
                    song(sourceId, "s3", "Three", starred = null).copy(albumId = "al-3", artistId = "ar-2"),
                ),
            )
            dao.upsertPlaylists(
                listOf(
                    playlist(sourceId, "pl-1", "Has download", created = 0, changed = 0),
                    playlist(sourceId, "pl-2", "No download", created = 1, changed = 1),
                ),
            )
            dao.upsertPlaylistSongs(
                listOf(
                    PlaylistSong(sourceId, "pl-1", "s1", 0),
                    PlaylistSong(sourceId, "pl-2", "s2", 0),
                ),
            )
            db.downloadDao().upsert(SongDownload(sourceId, "s1", DownloadStatus.Completed))

            assertEquals(
                listOf("Downloaded"),
                dao.albumsByName(sourceId, 0, "", downloadedFilter = 1).page().map { it.name },
            )
            assertEquals(
                listOf("ar-1"),
                dao.artistsByName(sourceId, 0, "", downloadedFilter = 1).page().map { it.id },
            )
            assertEquals(
                listOf("pl-1"),
                dao.playlistsByName(sourceId, "", downloadedFilter = 1).page().map { it.id },
            )
            assertEquals(3, dao.albumsByName(sourceId, 0, "").page().size)
        }

    @Test
    fun offlineListQueriesReturnOnlyCompletedDownloads() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(listOf(album(sourceId, "al-1", "Album", year = null, starred = null)))
            dao.upsertSongs(
                listOf(
                    song(sourceId, "s1", "One", starred = null, track = 1),
                    song(sourceId, "s2", "Two", starred = null, track = 2),
                    song(sourceId, "s3", "Three", starred = null, track = 3),
                ),
            )
            dao.upsertPlaylists(listOf(playlist(sourceId, "pl-1", "List", created = 0, changed = 0)))
            dao.upsertPlaylistSongs(
                listOf(
                    PlaylistSong(sourceId, "pl-1", "s1", 0),
                    PlaylistSong(sourceId, "pl-1", "s2", 1),
                    PlaylistSong(sourceId, "pl-1", "s3", 2),
                ),
            )
            db.downloadDao().upsert(SongDownload(sourceId, "s1", DownloadStatus.Completed))
            db.downloadDao().upsert(SongDownload(sourceId, "s2", DownloadStatus.Queued))
            db.downloadDao().upsert(SongDownload(sourceId, "s3", DownloadStatus.Completed))

            assertEquals(listOf("s1", "s3"), dao.songsByAlbumDownloaded(sourceId, "al-1").first().map { it.id })
            assertEquals(listOf("s1", "s3"), dao.downloadedAlbumSongIds(sourceId, "al-1"))
            assertEquals(listOf("s1", "s3"), dao.downloadedPlaylistSongIds(sourceId, "pl-1"))
            assertEquals(
                listOf("s1", "s3"),
                dao.playlistSongsDownloaded(sourceId, "pl-1").page().map { it.song.id },
            )
            assertEquals(
                listOf("al-1"),
                dao.albumsForArtistDownloaded(sourceId, "ar-1").first().map { it.id },
            )
        }

    @Test
    fun searchMatchesInfixCaseInsensitivelyAndFallsBackToAScanBelowThreeCharacters() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a", "Gamma", year = null, starred = null).copy(albumArtist = "The Artist"),
                    album(sourceId, "b", "Alpha", year = null, starred = null).copy(albumArtist = "Someone Else"),
                ),
            )

            assertEquals(listOf("Gamma"), dao.albumsByName(sourceId, 0, "amm").page().map { it.name })
            assertEquals(listOf("Gamma"), dao.albumsByName(sourceId, 0, "GAM").page().map { it.name })
            assertEquals(listOf("Gamma"), dao.albumsByName(sourceId, 0, "ga").page().map { it.name })
            assertEquals(listOf("Alpha"), dao.albumsByName(sourceId, 0, "Else").page().map { it.name })
            assertTrue(dao.albumsByName(sourceId, 0, "zzz").page().isEmpty())
        }

    @Test
    fun searchIndexFollowsRenamesUpdatesAndDeletes() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(listOf(album(sourceId, "a", "Gamma", year = null, starred = null)))
            assertEquals(listOf("Gamma"), dao.albumsByName(sourceId, 0, "gam").page().map { it.name })

            dao.setAlbumStar(sourceId, "a", 7)
            assertEquals(listOf("Gamma"), dao.albumsByName(sourceId, 0, "gam").page().map { it.name })

            dao.upsertAlbums(listOf(album(sourceId, "a", "Delta", year = null, starred = null)))
            assertTrue(dao.albumsByName(sourceId, 0, "gam").page().isEmpty())
            assertEquals(listOf("Delta"), dao.albumsByName(sourceId, 0, "elt").page().map { it.name })

            dao.deleteAlbums(sourceId, listOf("a"))
            assertTrue(dao.albumsByName(sourceId, 0, "elt").page().isEmpty())
        }

    @Test
    fun deletingASourceClearsItsSearchRows() =
        runTest {
            val first = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(listOf(album(first, "a", "Gamma", year = null, starred = null)))
            db.sourcesDao().deleteSource(first)

            val second = source()
            dao.upsertAlbums(listOf(album(second, "a", "Beta", year = null, starred = null)))

            assertEquals(listOf("Beta"), dao.albumsByName(second, 0, "eta").page().map { it.name })
            assertTrue(dao.albumsByName(second, 0, "gam").page().isEmpty())
        }

    @Test
    fun artistAndPlaylistSearchUseTheirOwnIndices() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertArtists(listOf(artist(sourceId, "r1", "The Beatles", albumCount = 1)))
            dao.upsertPlaylists(listOf(playlist(sourceId, "l1", "Road Trip", created = 0, changed = 0)))

            assertEquals(listOf("r1"), dao.artistsByName(sourceId, 0, "eat").page().map { it.id })
            assertEquals(listOf("l1"), dao.playlistsByName(sourceId, "rip").page().map { it.id })
            assertTrue(dao.artistsByName(sourceId, 0, "rip").page().isEmpty())
            assertTrue(dao.playlistsByName(sourceId, "eat").page().isEmpty())
        }

    @Test
    fun aQuoteInTheSearchTermIsTreatedLiterally() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(
                listOf(
                    album(sourceId, "a", "Say \"Hello\"", year = null, starred = null),
                    album(sourceId, "b", "Plain", year = null, starred = null),
                ),
            )

            assertEquals(listOf("Say \"Hello\""), dao.albumsByName(sourceId, 0, "\"Hel").page().map { it.name })
        }

    @Test
    fun searchHandlesANullAlbumArtist() =
        runTest {
            val sourceId = source()
            val dao = db.libraryDao()
            dao.upsertAlbums(listOf(album(sourceId, "a", "Gamma", year = null, starred = null).copy(albumArtist = null)))

            assertEquals(listOf("Gamma"), dao.albumsByName(sourceId, 0, "amm").page().map { it.name })
        }

    @Test
    fun searchRunsThroughTheTrigramIndex() =
        runTest {
            val plan =
                plan(
                    "SELECT * ${ALBUMS_FILTER.replace(":search", "'abc'")} " +
                        "ORDER BY $ALBUM_ORDER_BY_NAME LIMIT 20 OFFSET 0",
                )

            assertTrue(plan, plan.contains("album_search"))
        }

    private suspend fun plan(sql: String): String =
        db.useReaderConnection { connection ->
            connection.usePrepared("EXPLAIN QUERY PLAN $sql") { statement ->
                val details = mutableListOf<String>()
                while (statement.step()) details += statement.getText(3)
                details.joinToString("\n")
            }
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

    private fun artist(
        sourceId: Long,
        id: String,
        name: String,
        albumCount: Long,
    ) = Artist(
        sourceId = sourceId,
        id = id,
        name = name,
        albumCount = albumCount,
        starred = null,
        coverArt = null,
    )

    private fun playlist(
        sourceId: Long,
        id: String,
        name: String,
        created: Long,
        changed: Long,
    ) = Playlist(
        sourceId = sourceId,
        id = id,
        name = name,
        comment = null,
        coverArt = null,
        songCount = 1,
        created = created,
        changed = changed,
        duration = 100,
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
