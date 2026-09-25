package com.subtracks.data.sync

import android.content.Context
import androidx.paging.PagingSource
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.model.Source
import com.subtracks.data.source.MusicSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
                    songs = listOf(song("s1"), song("s2")),
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
            assertEquals(
                2,
                db
                    .libraryDao()
                    .songs(1, 0, "")
                    .allRows()
                    .size,
            )
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
            assertEquals(
                listOf("s1"),
                db
                    .libraryDao()
                    .songs(1, 0, "")
                    .allRows()
                    .map { it.song.id },
            )
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
            assertEquals(1, db.libraryDao().songIds(1).size)
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
    fun syncStoresStarredAndAddedTimestamps() =
        runTest {
            insertSource()
            val source = FakeMusicSource(albums = listOf(album("al1").copy(created = 1234, starred = 5678)))

            SyncService(db, source).sync()

            val stored = db.libraryDao().album(1, "al1").first()
            assertEquals(1234L, stored?.created)
            assertEquals(5678L, stored?.starred)
        }

    private suspend fun <T : Any> PagingSource<Int, T>.allRows(): List<T> {
        val page = load(PagingSource.LoadParams.Refresh(key = null, loadSize = 100, placeholdersEnabled = false))
        return (page as PagingSource.LoadResult.Page).data
    }

    private suspend fun insertSource() {
        db.sourcesDao().upsertSource(
            Source(id = 1, name = "test", address = "http://localhost", isActive = true, createdAt = 0),
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
            frequentRank = null,
            recentRank = null,
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
    var artists: List<Artist> = emptyList(),
    var albums: List<Album> = emptyList(),
    var songs: List<Song> = emptyList(),
    var playlists: List<Playlist> = emptyList(),
    var playlistSongs: List<PlaylistSong> = emptyList(),
) : MusicSource {
    override val id: Long = 1

    override suspend fun ping() = Unit

    override suspend fun getArtists(): List<Artist> = artists

    override suspend fun getAlbums(): List<Album> = albums

    override suspend fun getSongs(): List<Song> = songs

    override suspend fun getPlaylists(): List<Playlist> = playlists

    override suspend fun getPlaylistSongs(playlists: List<Playlist>): List<PlaylistSong> = playlistSongs
}
