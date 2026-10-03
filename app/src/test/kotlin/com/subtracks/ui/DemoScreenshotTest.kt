package com.subtracks.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.paging.PagingData
import androidx.palette.graphics.Palette
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.asImage
import coil3.test.FakeImageLoaderEngine
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.Song
import com.subtracks.data.model.Source
import com.subtracks.data.model.coverArtKey
import com.subtracks.data.prefs.ListQuery
import com.subtracks.data.source.subsonic.SubsonicSource
import com.subtracks.data.source.subsonic.TestServer
import com.subtracks.data.source.subsonic.TestServers
import com.subtracks.data.sync.SyncService
import com.subtracks.playback.PlaybackState
import com.subtracks.playback.QueueItem
import com.subtracks.ui.home.HomeFeed
import com.subtracks.ui.home.HomeScreen
import com.subtracks.ui.library.AlbumDetailScreen
import com.subtracks.ui.library.ArtistDetailScreen
import com.subtracks.ui.library.LibraryScreen
import com.subtracks.ui.library.LibraryTab
import com.subtracks.ui.library.sortOptionsFor
import com.subtracks.ui.playback.NowPlayingScreen
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.SubtracksTheme
import com.subtracks.ui.theme.artworkColorsFromSeeds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Records the store screenshots from a real sync of the public Navidrome demo library. It is not run
 * by `:*UnitTest` or the integration suite; run it explicitly with `gradle :app:demoScreenshots`.
 * The synced database and the downloaded cover art live only in memory and are never written down.
 */
@OptIn(DelicateCoilApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelXL)
class DemoScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var screen by mutableStateOf(Screen.Home)

    @Before
    fun resetImageLoader() {
        SingletonImageLoader.reset()
    }

    @After
    fun clearImageLoader() {
        SingletonImageLoader.reset()
    }

    @Test
    fun recordDemo() {
        val demo = DemoData.load()

        SingletonImageLoader.setUnsafe(
            ImageLoader
                .Builder(ApplicationProvider.getApplicationContext())
                .components { add(demo.engine) }
                .build(),
        )

        composeRule.setContent {
            SubtracksTheme {
                Surface(Modifier.fillMaxSize()) {
                    when (screen) {
                        Screen.Home -> {
                            HomeScreen(
                                feed = demo.feed,
                                coverArt = demo.coverArt,
                                playingSongId = demo.song.id,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        Screen.NowPlaying -> {
                            NowPlayingScreen(
                                state = demo.playbackState,
                                positionMs = demo.positionMs,
                                title = demo.album.name,
                                coverArt = demo.coverArt(demo.album.coverArt, false),
                                artwork = demo.artwork(demo.album.coverArt),
                                onBack = {},
                                onQueue = {},
                                onPlayPause = {},
                                onNext = {},
                                onPrevious = {},
                                onSeek = {},
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        Screen.Albums -> {
                            AlbumsScreen(demo.albums, demo.coverArt, demo.artwork(demo.albums.first().coverArt))
                        }

                        Screen.AlbumDetail -> {
                            AlbumDetailScreen(
                                album = demo.album,
                                songs = demo.albumSongs,
                                coverArt = demo.coverArt,
                                artwork = demo.artwork(demo.album.coverArt),
                                onBack = {},
                                onSongClick = {},
                                playingSongId = demo.song.id,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        Screen.ArtistDetail -> {
                            ArtistDetailScreen(
                                artist = demo.artist,
                                albums = demo.artistAlbums,
                                art = demo.coverArt(demo.artist.coverArt, false),
                                artThumbnail = demo.coverArt(demo.artist.coverArt, true),
                                artwork = demo.artwork(demo.artist.coverArt),
                                coverArt = demo.coverArt,
                                onBack = {},
                                onAlbumClick = {},
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        Screen.Artists -> {
                            LibraryScreen(
                                selectedTab = LibraryTab.Artists,
                                onTabSelected = {},
                                albums = remember { flowOf(PagingData.empty<Album>()) },
                                artists = remember { flowOf(PagingData.from(demo.artists)) },
                                playlists = remember { flowOf(PagingData.empty<Playlist>()) },
                                coverArt = demo.coverArt,
                                onAlbumClick = {},
                                onArtistClick = {},
                                onPlaylistClick = {},
                                onSync = {},
                                onOpenSettings = {},
                                artwork = demo.artwork(demo.artists.first().coverArt),
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        Screen.Search -> {
                            LibraryScreen(
                                selectedTab = LibraryTab.Albums,
                                onTabSelected = {},
                                albums = remember { flowOf(PagingData.from(demo.searchAlbums)) },
                                artists = remember { flowOf(PagingData.empty<Artist>()) },
                                playlists = remember { flowOf(PagingData.empty<Playlist>()) },
                                coverArt = demo.coverArt,
                                onAlbumClick = {},
                                onArtistClick = {},
                                onPlaylistClick = {},
                                onSync = {},
                                onOpenSettings = {},
                                listQuery = ListQuery("Name"),
                                sortOptions = sortOptionsFor(LibraryTab.Albums),
                                starredSupported = true,
                                search = DEMO_SEARCH,
                                onSearchChange = {},
                                artwork = demo.artwork(demo.searchAlbums.first().coverArt),
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }

        capture(Screen.Home, "01_home.png")
        capture(Screen.NowPlaying, "02_now-playing.png")
        capture(Screen.Albums, "03_library-albums.png")
        capture(Screen.AlbumDetail, "04_album.png")
        capture(Screen.ArtistDetail, "05_artist.png")
        capture(Screen.Artists, "06_library-artists.png")
        capture(Screen.Search, "07_search.png")
    }

    @Composable
    private fun AlbumsScreen(
        albums: List<Album>,
        coverArt: (String?, Boolean) -> CoverArtRef?,
        artwork: ArtworkColors,
    ) {
        LibraryScreen(
            selectedTab = LibraryTab.Albums,
            onTabSelected = {},
            albums = remember { flowOf(PagingData.from(albums)) },
            artists = remember { flowOf(PagingData.empty<Artist>()) },
            playlists = remember { flowOf(PagingData.empty<Playlist>()) },
            coverArt = coverArt,
            onAlbumClick = {},
            onArtistClick = {},
            onPlaylistClick = {},
            onSync = {},
            onOpenSettings = {},
            listQuery = ListQuery("Name"),
            sortOptions = sortOptionsFor(LibraryTab.Albums),
            starredSupported = true,
            artwork = artwork,
            modifier = Modifier.fillMaxSize(),
        )
    }

    private fun capture(
        target: Screen,
        file: String,
    ) {
        composeRule.runOnIdle { screen = target }
        repeat(3) {
            composeRule.mainClock.advanceTimeBy(500)
            composeRule.waitForIdle()
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/demo/$file")
    }

    private enum class Screen { Home, NowPlaying, Albums, AlbumDetail, ArtistDetail, Artists, Search }

    private class DemoData(
        val albums: List<Album>,
        val artists: List<Artist>,
        val feed: HomeFeed,
        val song: Song,
        val album: Album,
        val albumSongs: List<Song>,
        val artist: Artist,
        val artistAlbums: List<Album>,
        val searchAlbums: List<Album>,
        val coverArt: (String?, Boolean) -> CoverArtRef?,
        val engine: FakeImageLoaderEngine,
        private val bitmaps: Map<String, Bitmap>,
    ) {
        val playbackState =
            PlaybackState(
                item =
                    QueueItem(
                        id = song.id,
                        title = song.title,
                        artist = song.artist,
                        album = song.album,
                        coverArtId = album.coverArt,
                    ),
                isPlaying = true,
                durationMs = (song.duration ?: 0) * 1000,
                hasNext = true,
                hasPrevious = false,
            )

        val positionMs: Long = (song.duration ?: 0) * 1000 * 35 / 100

        fun artwork(coverArtId: String?): ArtworkColors {
            val bitmap = coverArtId?.let(bitmaps::get) ?: return artworkColorsFromSeeds(FALLBACK_SEED, null)
            val palette = Palette.from(bitmap).generate()
            val primary =
                palette.vibrantSwatch
                    ?: palette.lightVibrantSwatch
                    ?: palette.darkVibrantSwatch
                    ?: palette.mutedSwatch
                    ?: palette.dominantSwatch
            return artworkColorsFromSeeds(primary?.rgb ?: FALLBACK_SEED, null)
        }

        companion object {
            private const val FALLBACK_SEED = 0xFF3A7BD5.toInt()
            private const val DEMO_BASE = "https://demo.navidrome.org/"

            fun load(): DemoData =
                runBlocking {
                    val context = ApplicationProvider.getApplicationContext<Context>()
                    val http = OkHttpClient()
                    val server =
                        TestServer(
                            name = "demo",
                            baseUrl = DEMO_BASE,
                            username = "demo",
                            password = "demo",
                            supportsTokenAuth = true,
                            supportsGenres = true,
                        )
                    val source: SubsonicSource = TestServers.source(server)

                    val db =
                        Room
                            .inMemoryDatabaseBuilder(context, SubtracksDatabase::class.java)
                            .setDriver(BundledSQLiteDriver())
                            .build()
                    db.sourcesDao().upsertSource(
                        Source(id = 1, name = "Navidrome Demo", address = server.baseUrl, isActive = true, createdAt = 0),
                    )
                    SyncService(db, source).sync()

                    val dao = db.libraryDao()
                    val albums = dao.recentlyAddedAlbums(1, 500).first().sortedBy { it.name.lowercase() }
                    val artists = dao.artistIds(1).mapNotNull { dao.artist(1, it).first() }
                    val playlists = dao.playlistIds(1).mapNotNull { dao.playlist(1, it).first() }

                    val album = albums.firstOrNull { it.name == "First Words" } ?: albums.first()
                    val albumSongs = dao.songsByAlbum(1, album.id).first()
                    val song = albumSongs.getOrNull(1) ?: albumSongs.first()
                    val artist =
                        artists.maxByOrNull { it.albumCount }
                            ?: artists.first()
                    val artistAlbums = dao.albumsForArtist(1, artist.id).first()

                    val feed =
                        HomeFeed(
                            recentlyPlayedAlbums = dao.recentlyPlayedAlbums(1, 10).first(),
                            recentlyPlayedArtists = dao.recentlyPlayedArtists(1, 10).first(),
                            mostPlayedAlbums = dao.mostPlayedAlbums(1, 10).first(),
                            mostPlayedArtists = dao.mostPlayedArtists(1, 10).first(),
                            genres = dao.genresByMostPlayed(1).first(),
                            decades = dao.decades(1).first(),
                            recentlyStarredSongs = dao.recentlyStarredSongs(1, 10).first(),
                            recentlyAddedAlbums = albums.take(10),
                            rediscoverAlbums = dao.rediscoverAlbums(1, System.currentTimeMillis() / 1000, 10).first(),
                        )

                    val searchAlbums =
                        albums
                            .filter {
                                it.name.contains(DEMO_SEARCH, ignoreCase = true) ||
                                    (it.albumArtist ?: "").contains(DEMO_SEARCH, ignoreCase = true)
                            }.ifEmpty { albums.take(4) }

                    val coverArtIds =
                        (albums.map { it.coverArt } + artists.map { it.coverArt } + playlists.map { it.coverArt })
                            .filterNotNull()
                            .distinct()

                    val bitmaps = mutableMapOf<String, Bitmap>()
                    val builder = FakeImageLoaderEngine.Builder()
                    coverArtIds.forEach { id ->
                        val bitmap = fetchBitmap(http, source, id) ?: return@forEach
                        bitmaps[id] = bitmap
                        val image = bitmap.asImage()
                        listOf(false, true).forEach { thumbnail ->
                            source.coverArtUri(id, thumbnail)?.let { builder.intercept(it.toString(), image) }
                        }
                    }
                    val fallback =
                        Bitmap
                            .createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                            .apply { eraseColor(Color.GRAY) }
                            .asImage()
                    val engine = builder.default(fallback).build()

                    val coverArt: (String?, Boolean) -> CoverArtRef? = { id, thumbnail ->
                        id?.let {
                            source.coverArtUri(it, thumbnail)?.let { uri ->
                                CoverArtRef(uri.toString(), coverArtKey(1, it, thumbnail))
                            }
                        }
                    }

                    db.close()
                    DemoData(
                        albums = albums,
                        artists = artists,
                        feed = feed,
                        song = song,
                        album = album,
                        albumSongs = albumSongs,
                        artist = artist,
                        artistAlbums = artistAlbums,
                        searchAlbums = searchAlbums,
                        coverArt = coverArt,
                        engine = engine,
                        bitmaps = bitmaps,
                    )
                }

            private fun fetchBitmap(
                http: OkHttpClient,
                source: SubsonicSource,
                id: String,
            ): Bitmap? =
                try {
                    val url = source.coverArtUri(id, false) ?: return null
                    val bytes = http.newCall(Request.Builder().url(url).build()).execute().use { it.body.bytes() }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (_: Exception) {
                    null
                }
        }
    }
}

private const val DEMO_SEARCH = "the"
