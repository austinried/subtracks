package com.subtracks.ui.search

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.subtracks.awaitUntil
import com.subtracks.cancelAndJoinBlocking
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.download.FakeDownloadEngine
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.Song
import com.subtracks.data.prefs.fakeUserPreferences
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.NetworkServerActionSink
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.FakePlayerConnection
import com.subtracks.playback.FakePlayerHandle
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.Executors

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SearchViewModelTest {
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private lateinit var db: SubtracksDatabase
    private lateinit var sourceRepository: SourceRepository
    private lateinit var viewModel: SearchViewModel
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val controllerScope = CoroutineScope(SupervisorJob() + dispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .build()
        val artwork = ArtworkStore(File(context.cacheDir, "art-${System.nanoTime()}"))
        sourceRepository = SourceRepository(db, OkHttpClient(), fakeUserPreferences(), artwork, scope = repoScope)
        val downloads =
            DownloadRepository(
                db,
                sourceRepository,
                FakeDownloadEngine(),
                File(context.cacheDir, "downloads-${System.nanoTime()}"),
                artworkStore = artwork,
                artworkFetcher = { ByteArray(0) },
                scope = repoScope,
            )
        val controller =
            PlaybackController(
                sourceRepository,
                QueueRepository(db),
                FakePlayerConnection(FakePlayerHandle()),
                downloads,
                scope = controllerScope,
            )
        viewModel =
            SearchViewModel(
                LibraryRepository(db, sourceRepository, NetworkServerActionSink(sourceRepository), scope = repoScope),
                sourceRepository,
                controller,
            )
    }

    @After
    fun tearDown() {
        cancelAndJoinBlocking(controllerScope, repoScope)
        db.close()
        Dispatchers.resetMain()
        dispatcher.close()
    }

    @Test
    fun searchFindsEveryEntityTypeAndBlankClearsTheResults() {
        runBlocking {
            val id = sourceRepository.addSource("nav", "http://localhost/", "u", "p", false)
            db.libraryDao().upsertAlbums(listOf(album(id, "al-1", "Road Album")))
            db.libraryDao().upsertArtists(listOf(artist(id, "ar-1", "Road Artist")))
            db.libraryDao().upsertPlaylists(listOf(playlist(id, "pl-1", "Road Trip")))
            db.libraryDao().upsertSongs(listOf(song(id, "s1", "Road Song")))
        }

        viewModel.setQuery("oad")
        val results = runBlocking { withTimeout(5_000) { viewModel.results.first { !it.isEmpty } } }

        assertEquals(listOf("Road Album"), results.albums.map { it.name })
        assertEquals(listOf("ar-1"), results.artists.map { it.id })
        assertEquals(listOf("pl-1"), results.playlists.map { it.id })
        assertEquals(listOf("s1"), results.songs.map { it.song.id })

        viewModel.setQuery("oa")
        val short = runBlocking { withTimeout(5_000) { viewModel.results.first { it.isEmpty } } }

        assertTrue(short.isEmpty)

        viewModel.setQuery("")
        val blank = runBlocking { withTimeout(5_000) { viewModel.results.first { it.isEmpty } } }

        assertTrue(blank.isEmpty)
    }

    @Test
    fun playingASearchResultPlaysItsAlbumInsteadOfASearchQueue() {
        val sourceId =
            runBlocking {
                val id = sourceRepository.addSource("nav", "http://localhost/", "u", "p", false)
                db.libraryDao().upsertAlbums(listOf(album(id, "al-1", "Road Album")))
                db.libraryDao().upsertSongs(
                    listOf(
                        song(id, "s1", "Road Song"),
                        song(id, "s2", "Another").copy(track = 2),
                    ),
                )
                id
            }

        viewModel.play(song(sourceId, "s1", "Road Song"))
        awaitUntil("the queue is built") { runBlocking { db.queueDao().entries().isNotEmpty() } }

        val entries = runBlocking { db.queueDao().entries() }
        assertEquals(1, entries.size)
        assertEquals(QueueKind.Album, entries.first().kind)
        assertEquals("al-1", entries.first().refId)
    }

    private fun album(
        sourceId: Long,
        id: String,
        name: String,
    ) = Album(
        sourceId = sourceId,
        id = id,
        artistId = "ar-1",
        name = name,
        albumArtist = "Artist",
        created = 0,
        coverArt = null,
        genre = null,
        year = null,
        starred = null,
        songCount = 1,
    )

    private fun artist(
        sourceId: Long,
        id: String,
        name: String,
    ) = Artist(
        sourceId = sourceId,
        id = id,
        name = name,
        albumCount = 1,
        starred = null,
        coverArt = null,
    )

    private fun playlist(
        sourceId: Long,
        id: String,
        name: String,
    ) = Playlist(
        sourceId = sourceId,
        id = id,
        name = name,
        comment = null,
        coverArt = null,
        songCount = 1,
        created = 0,
        changed = 0,
        duration = 100,
    )

    private fun song(
        sourceId: Long,
        id: String,
        title: String,
    ) = Song(
        sourceId = sourceId,
        id = id,
        albumId = "al-1",
        artistId = "ar-1",
        title = title,
        album = "Album",
        artist = "Artist",
        duration = 100,
        track = 1,
        disc = 1,
        starred = null,
    )
}
