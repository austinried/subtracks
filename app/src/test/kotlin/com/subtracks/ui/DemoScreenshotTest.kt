package com.subtracks.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.subtracks.ui.playback.MiniPlayer
import com.subtracks.ui.playback.NowPlayingScreen
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.ArtworkTheme
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
import java.io.File

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
                            ArtworkTheme(demo.artwork(demo.homePlayingAlbum.coverArt)) {
                                HomeScreen(
                                    feed = demo.feed,
                                    coverArt = demo.coverArt,
                                    playingSongId = demo.homePlayingSong.id,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }

                        Screen.NowPlaying -> {
                            NowPlayingScreen(
                                state = demo.playbackState,
                                positionMs = demo.positionMs,
                                title = demo.nowPlayingAlbum.name,
                                coverArt = demo.coverArt(demo.nowPlayingAlbum.coverArt, false),
                                artwork = demo.artwork(demo.nowPlayingAlbum.coverArt),
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
                            Box(Modifier.fillMaxSize()) {
                                AlbumsScreen(demo.albums, demo.coverArt, demo.artwork(demo.albums.first().coverArt))
                                demo.MiniPlayerOverlay(Modifier.align(Alignment.BottomCenter))
                            }
                        }

                        Screen.AlbumDetail -> {
                            Box(Modifier.fillMaxSize()) {
                                AlbumDetailScreen(
                                    album = demo.album,
                                    songs = demo.albumSongs,
                                    coverArt = demo.coverArt,
                                    artwork = demo.artwork(demo.album.coverArt),
                                    onBack = {},
                                    onSongClick = {},
                                    playingSongId = demo.nowPlayingSong.id,
                                    modifier = Modifier.fillMaxSize(),
                                )
                                demo.MiniPlayerOverlay(Modifier.align(Alignment.BottomCenter))
                            }
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
                            Box(Modifier.fillMaxSize()) {
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
                                demo.MiniPlayerOverlay(Modifier.align(Alignment.BottomCenter))
                            }
                        }

                        Screen.Search -> {
                            Box(Modifier.fillMaxSize()) {
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
                                demo.MiniPlayerOverlay(Modifier.align(Alignment.BottomCenter))
                            }
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

    @Composable
    private fun DemoData.MiniPlayerOverlay(modifier: Modifier = Modifier) {
        MiniPlayer(
            state = playbackState,
            positionMs = positionMs,
            coverArt = coverArt(nowPlayingAlbum.coverArt, true),
            artwork = artwork(nowPlayingAlbum.coverArt),
            onExpand = {},
            onPlayPause = {},
            onNext = {},
            modifier = modifier,
        )
    }

    private enum class Screen { Home, NowPlaying, Albums, AlbumDetail, ArtistDetail, Artists, Search }

    private class DemoData(
        val albums: List<Album>,
        val artists: List<Artist>,
        val feed: HomeFeed,
        val nowPlayingSong: Song,
        val nowPlayingAlbum: Album,
        val album: Album,
        val albumSongs: List<Song>,
        val homePlayingAlbum: Album,
        val homePlayingSong: Song,
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
                        id = nowPlayingSong.id,
                        title = nowPlayingSong.title,
                        artist = nowPlayingSong.artist,
                        album = nowPlayingSong.album,
                        coverArtId = nowPlayingAlbum.coverArt,
                    ),
                isPlaying = true,
                durationMs = (nowPlayingSong.duration ?: 0) * 1000,
                hasNext = true,
                hasPrevious = false,
            )

        val positionMs: Long = (nowPlayingSong.duration ?: 0) * 1000 * 35 / 100

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

                    context.deleteDatabase(DEMO_DB_NAME)
                    val db =
                        Room
                            .databaseBuilder<SubtracksDatabase>(context, DEMO_DB_NAME)
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

                    val nowPlayingAlbum = albums.firstOrNull { it.name == "Chillhop Essentials - Winter 2016" } ?: albums.first()
                    val nowPlayingSongs = dao.songsByAlbum(1, nowPlayingAlbum.id).first()
                    val nowPlayingSong = nowPlayingSongs.getOrNull(1) ?: nowPlayingSongs.first()
                    val album = albums.firstOrNull { it.name == "My latin way" } ?: albums.first()
                    val albumSongs = dao.songsByAlbum(1, album.id).first()
                    val homePlayingAlbum = albums.firstOrNull { it.name == "Shaking The Habitual" } ?: albums.last()
                    val homePlayingSong = dao.songsByAlbum(1, homePlayingAlbum.id).first().first()
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

                    val fullSizeIds =
                        buildSet {
                            addAll(listOfNotNull(album.coverArt, nowPlayingAlbum.coverArt, artist.coverArt))
                            artistAlbums.mapNotNullTo(this) { it.coverArt }
                        }

                    val bitmaps = mutableMapOf<String, Bitmap>()
                    val builder = FakeImageLoaderEngine.Builder()
                    val coverDir = DEMO_COVER_DIR.apply { mkdirs() }
                    coverArtIds.forEach { id ->
                        val thumbnail = fetchBitmap(http, source, id, thumbnail = true, coverDir) ?: return@forEach
                        source.coverArtUri(id, true)?.let { builder.intercept(it.toString(), thumbnail.asImage()) }
                        val image =
                            if (id in fullSizeIds) {
                                (fetchBitmap(http, source, id, thumbnail = false, coverDir) ?: thumbnail).also {
                                    bitmaps[id] = it
                                }
                            } else {
                                thumbnail.also { bitmaps[id] = it }
                            }
                        source.coverArtUri(id, false)?.let { builder.intercept(it.toString(), image.asImage()) }
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
                        nowPlayingSong = nowPlayingSong,
                        nowPlayingAlbum = nowPlayingAlbum,
                        album = album,
                        albumSongs = albumSongs,
                        homePlayingAlbum = homePlayingAlbum,
                        homePlayingSong = homePlayingSong,
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
                thumbnail: Boolean,
                coverDir: File,
            ): Bitmap? =
                try {
                    val url = source.coverArtUri(id, thumbnail) ?: return null
                    val file = File(coverDir, "${url.hashCode().toUInt().toString(16)}.img")
                    if (!file.exists()) {
                        http.newCall(Request.Builder().url(url).build()).execute().use { response ->
                            response.body.byteStream().use { input ->
                                file.outputStream().use { output -> input.copyTo(output) }
                            }
                        }
                    }
                    val bytes = file.readBytes()
                    if (thumbnail) {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    } else {
                        decodeDownsampled(bytes, MAX_ART_DIM)
                    }
                } catch (_: Exception) {
                    null
                }

            private fun decodeDownsampled(
                bytes: ByteArray,
                maxDim: Int,
            ): Bitmap? {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= maxDim) sample *= 2
                val decoded =
                    BitmapFactory.decodeByteArray(
                        bytes,
                        0,
                        bytes.size,
                        BitmapFactory.Options().apply { inSampleSize = sample },
                    ) ?: return null
                val largest = maxOf(decoded.width, decoded.height)
                if (largest <= maxDim) return decoded
                val scale = maxDim.toFloat() / largest
                val scaled =
                    Bitmap.createScaledBitmap(
                        decoded,
                        (decoded.width * scale).toInt().coerceAtLeast(1),
                        (decoded.height * scale).toInt().coerceAtLeast(1),
                        true,
                    )
                if (scaled !== decoded) decoded.recycle()
                return scaled
            }
        }
    }
}

private const val DEMO_SEARCH = "chill"
private const val MAX_ART_DIM = 1200
private const val DEMO_DB_NAME = "demo-screens.db"
private val DEMO_COVER_DIR = File(System.getProperty("java.io.tmpdir"), "subtracks-demo-covers")
