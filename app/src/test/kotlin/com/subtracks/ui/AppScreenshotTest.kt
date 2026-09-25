package com.subtracks.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.Image
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.asImage
import coil3.test.FakeImageLoaderEngine
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongListItem
import com.subtracks.data.model.Source
import com.subtracks.playback.PlaybackState
import com.subtracks.playback.QueueItem
import com.subtracks.playback.RepeatMode
import com.subtracks.ui.library.ALBUM_COVER_TAG
import com.subtracks.ui.library.AlbumDetailScreen
import com.subtracks.ui.library.ArtistDetailScreen
import com.subtracks.ui.library.LibraryScreen
import com.subtracks.ui.library.LibraryTab
import com.subtracks.ui.library.PlaylistDetailScreen
import com.subtracks.ui.playback.MiniPlayer
import com.subtracks.ui.playback.NowPlayingScreen
import com.subtracks.ui.playback.QueueRow
import com.subtracks.ui.playback.QueueScreen
import com.subtracks.ui.settings.AddSourceScreen
import com.subtracks.ui.settings.AddSourceState
import com.subtracks.ui.settings.SettingsScreen
import com.subtracks.ui.theme.SubtracksTheme
import com.subtracks.ui.theme.artworkColorsFromSeed
import com.subtracks.ui.theme.artworkColorsFromSeeds
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.random.Random

@OptIn(DelicateCoilApi::class)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.PixelXL)
class AppScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun installImageLoader() {
        SingletonImageLoader.reset()
        SingletonImageLoader.setUnsafe(
            ImageLoader
                .Builder(ApplicationProvider.getApplicationContext<Context>())
                .components { add(Fixtures.artEngine()) }
                .build(),
        )
    }

    @After
    fun resetImageLoader() {
        SingletonImageLoader.reset()
    }

    @Test
    fun libraryAlbums() {
        setLibraryContent(LibraryTab.Albums)
        awaitTag(ALBUM_COVER_TAG)
        composeRule.onRoot().captureRoboImage("src/test/screenshots/library_albums.png")
    }

    @Test
    fun libraryArtists() {
        setLibraryContent(LibraryTab.Artists)
        awaitText("Radiohead")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/library_artists.png")
    }

    @Test
    fun librarySongs() {
        setLibraryContent(LibraryTab.Songs)
        awaitText("Everything In Its Right Place")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/library_songs.png")
    }

    @Test
    fun libraryPlaylists() {
        setLibraryContent(LibraryTab.Playlists)
        awaitText("Late Night")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/library_playlists.png")
    }

    @Test
    fun libraryWithMiniPlayer() {
        composeRule.setContent {
            SubtracksTheme {
                val artwork = artworkColorsFromSeed(Color.rgb(120, 80, 200))
                Column(Modifier.fillMaxSize()) {
                    LibraryScreen(
                        selectedTab = LibraryTab.Albums,
                        onTabSelected = {},
                        albums = remember { flowOf(PagingData.from(Fixtures.albums)) }.collectAsLazyPagingItems(),
                        artists = remember { flowOf(PagingData.from(Fixtures.artists)) }.collectAsLazyPagingItems(),
                        songs = remember { flowOf(PagingData.from(Fixtures.songItems)) }.collectAsLazyPagingItems(),
                        playlists = remember { flowOf(PagingData.from(Fixtures.playlists)) }.collectAsLazyPagingItems(),
                        coverArt = { id, _ -> id?.let { CoverArtRef(it, "test:$it") } },
                        onAlbumClick = {},
                        onArtistClick = {},
                        onPlaylistClick = {},
                        onSongClick = {},
                        onSync = {},
                        onOpenSettings = {},
                        artwork = artwork,
                        modifier = Modifier.weight(1f),
                    )
                    MiniPlayer(
                        state = Fixtures.playbackState(),
                        coverArt = CoverArtRef("art-al-kid-a", "test:art-al-kid-a"),
                        artwork = artwork,
                        onExpand = {},
                        onPlayPause = {},
                        onNext = {},
                    )
                }
            }
        }
        awaitTag(ALBUM_COVER_TAG)
        composeRule.onRoot().captureRoboImage("src/test/screenshots/library_with_mini_player.png")
    }

    @Test
    fun albumDetail() {
        composeRule.setContent {
            SubtracksTheme {
                AlbumDetailScreen(
                    album = Fixtures.albums.first(),
                    songs = Fixtures.songs,
                    coverArt = { id, _ -> id?.let { CoverArtRef(it, "test:$it") } },
                    artwork = artworkColorsFromSeed(Color.rgb(120, 80, 200)),
                    onBack = {},
                    onSongClick = {},
                    playingSongId = Fixtures.songs.first().id,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        awaitText("Everything In Its Right Place")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/album_detail.png")
    }

    @Test
    fun albumDetailCool() {
        composeRule.setContent {
            SubtracksTheme {
                AlbumDetailScreen(
                    album = Fixtures.albums.first(),
                    songs = Fixtures.songs,
                    coverArt = { id, _ -> id?.let { CoverArtRef(it, "test:$it") } },
                    artwork = artworkColorsFromSeeds(Color.rgb(45, 115, 240), Color.rgb(40, 180, 140)),
                    onBack = {},
                    onSongClick = {},
                    playingSongId = Fixtures.songs.first().id,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        awaitText("Everything In Its Right Place")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/album_detail_cool.png")
    }

    @Test
    fun albumDetailMultiDisc() {
        val songs =
            listOf(
                Fixtures.song("s-eiirp", "al-kid-a", "ar-radiohead", "Everything In Its Right Place", "Kid A", "Radiohead", 1, 251),
                Fixtures.song("s-kid-a", "al-kid-a", "ar-radiohead", "Kid A", "Kid A", "Radiohead", 2, 274),
                Fixtures.song("s-anthem", "al-kid-a", "ar-radiohead", "The National Anthem", "Kid A", "Radiohead", 1, 351, disc = 2),
                Fixtures.song("s-htdc", "al-kid-a", "ar-radiohead", "How to Disappear Completely", "Kid A", "Radiohead", 2, 356, disc = 2),
            )
        composeRule.setContent {
            SubtracksTheme {
                AlbumDetailScreen(
                    album = Fixtures.albums.first(),
                    songs = songs,
                    coverArt = { id, _ -> id?.let { CoverArtRef(it, "test:$it") } },
                    artwork = artworkColorsFromSeed(Color.rgb(120, 80, 200)),
                    onBack = {},
                    onSongClick = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        awaitText("Everything In Its Right Place")
        composeRule.onRoot().performTouchInput { swipeUp(startY = centerY + 600f, endY = centerY - 600f, durationMillis = 400) }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Disc 2").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onRoot().captureRoboImage("src/test/screenshots/album_detail_multidisc.png")
    }

    @Test
    fun artistDetail() {
        composeRule.setContent {
            SubtracksTheme {
                ArtistDetailScreen(
                    artist = Fixtures.artists.first(),
                    albums = Fixtures.albums.filter { it.artistId == Fixtures.artists.first().id },
                    art = CoverArtRef("art-ar-radiohead", "test:art-ar-radiohead"),
                    artwork = artworkColorsFromSeed(Color.rgb(120, 80, 200)),
                    coverArt = { id, _ -> id?.let { CoverArtRef(it, "test:$it") } },
                    onBack = {},
                    onAlbumClick = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        awaitText("Kid A")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/artist_detail.png")
    }

    @Test
    fun playlistDetail() {
        composeRule.setContent {
            SubtracksTheme {
                PlaylistDetailScreen(
                    playlist = Fixtures.playlists.first(),
                    songs = remember { flowOf(PagingData.from(Fixtures.songItems)) }.collectAsLazyPagingItems(),
                    coverArt = { id, _ -> id?.let { CoverArtRef(it, "test:$it") } },
                    artwork = artworkColorsFromSeed(Color.rgb(60, 150, 90)),
                    onBack = {},
                    onSongClick = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        awaitText("Everything In Its Right Place")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/playlist_detail.png")
    }

    @Test
    fun settings() {
        composeRule.setContent {
            SubtracksTheme {
                SettingsScreen(
                    sources = Fixtures.sources,
                    activeSourceId = 1,
                    maxBitrate = 0,
                    streamFormat = null,
                    onSelectSource = {},
                    onDeleteSource = {},
                    onMaxBitrateChange = {},
                    onStreamFormatChange = {},
                    onAddServer = {},
                    onBack = {},
                )
            }
        }
        awaitText("Navidrome")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/settings.png")
    }

    @Test
    fun addSource() {
        captureAddSource(useTokenAuth = true, file = "src/test/screenshots/add_source.png")
    }

    @Test
    fun addSourceTokenAuthOff() {
        captureAddSource(useTokenAuth = false, file = "src/test/screenshots/add_source_off.png")
    }

    private fun captureAddSource(
        useTokenAuth: Boolean,
        file: String,
    ) {
        composeRule.setContent {
            SubtracksTheme {
                AddSourceScreen(
                    state =
                        AddSourceState(
                            name = "Home",
                            address = "https://music.example.com",
                            username = "austin",
                            password = "hunter2",
                            useTokenAuth = useTokenAuth,
                        ),
                    onNameChange = {},
                    onAddressChange = {},
                    onUsernameChange = {},
                    onPasswordChange = {},
                    onTokenAuthChange = {},
                    onTest = {},
                    onSave = {},
                    onBack = null,
                )
            }
        }
        awaitText("Add server")
        composeRule.onRoot().captureRoboImage(file)
    }

    @Test
    fun nowPlaying() {
        composeRule.setContent {
            SubtracksTheme {
                NowPlayingScreen(
                    state = Fixtures.playbackState(),
                    coverArt = CoverArtRef("art-al-kid-a", "test:art-al-kid-a"),
                    artwork = artworkColorsFromSeed(Color.rgb(120, 80, 200)),
                    onBack = {},
                    onQueue = {},
                    onPlayPause = {},
                    onNext = {},
                    onPrevious = {},
                    onSeek = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        awaitText("Everything In Its Right Place")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/now_playing.png")
    }

    @Test
    fun nowPlayingModes() {
        composeRule.setContent {
            SubtracksTheme {
                NowPlayingScreen(
                    state = Fixtures.playbackState().copy(shuffle = true, repeat = RepeatMode.One),
                    coverArt = CoverArtRef("art-al-kid-a", "test:art-al-kid-a"),
                    artwork = artworkColorsFromSeed(Color.rgb(120, 80, 200)),
                    onBack = {},
                    onQueue = {},
                    onPlayPause = {},
                    onNext = {},
                    onPrevious = {},
                    onSeek = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        awaitText("Everything In Its Right Place")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/now_playing_modes.png")
    }

    @Test
    fun queue() {
        composeRule.setContent {
            SubtracksTheme {
                QueueScreen(
                    rows =
                        Fixtures.songItems.mapIndexed { index, item -> QueueRow(index.toLong(), index.toLong(), item) },
                    ready = true,
                    currentSongId = Fixtures.songItems[1].song.id,
                    coverArt = { id, _ -> id?.let { CoverArtRef(it, "test:$it") } },
                    onBack = {},
                    onPlay = {},
                    onRemove = {},
                    onReorder = { _, _ -> },
                    onMove = { _, _ -> },
                    onLoadOlder = {},
                    onLoadNewer = {},
                    onUndo = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        awaitText("Everything In Its Right Place")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/queue.png")
    }

    @Test
    fun miniPlayer() {
        composeRule.setContent {
            SubtracksTheme {
                Column(Modifier.fillMaxSize()) {
                    Spacer(Modifier.weight(1f))
                    MiniPlayer(
                        state = Fixtures.playbackState(),
                        coverArt = CoverArtRef("art-al-kid-a", "test:art-al-kid-a"),
                        artwork = artworkColorsFromSeed(Color.rgb(120, 80, 200)),
                        onExpand = {},
                        onPlayPause = {},
                        onNext = {},
                    )
                }
            }
        }
        awaitText("Everything In Its Right Place")
        composeRule.onRoot().captureRoboImage("src/test/screenshots/mini_player.png")
    }

    private fun setLibraryContent(tab: LibraryTab) {
        composeRule.setContent {
            SubtracksTheme {
                LibraryScreen(
                    selectedTab = tab,
                    onTabSelected = {},
                    albums = remember { flowOf(PagingData.from(Fixtures.albums)) }.collectAsLazyPagingItems(),
                    artists = remember { flowOf(PagingData.from(Fixtures.artists)) }.collectAsLazyPagingItems(),
                    songs = remember { flowOf(PagingData.from(Fixtures.songItems)) }.collectAsLazyPagingItems(),
                    playlists = remember { flowOf(PagingData.from(Fixtures.playlists)) }.collectAsLazyPagingItems(),
                    coverArt = { id, _ -> id?.let { CoverArtRef(it, "test:$it") } },
                    onAlbumClick = {},
                    onArtistClick = {},
                    onPlaylistClick = {},
                    onSongClick = {},
                    onSync = {},
                    onOpenSettings = {},
                    artwork = artworkColorsFromSeed(Color.rgb(120, 80, 200)),
                )
            }
        }
    }

    private fun awaitText(text: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun awaitTag(tag: String) {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
}

private object Fixtures {
    val sources =
        listOf(
            Source(id = 1, name = "Navidrome", address = "https://navidrome.homelab.example.com:4533", isActive = true, createdAt = 0),
            Source(id = 2, name = "gonic", address = "http://192.168.1.10:4747", isActive = false, createdAt = 1),
        )

    val artists =
        listOf(
            artist("ar-radiohead", "Radiohead", 2),
            artist("ar-portishead", "Portishead", 1),
            artist("ar-massive", "Massive Attack", 1),
            artist("ar-aphex", "Aphex Twin", 1),
            artist("ar-boc", "Boards of Canada", 2),
        )

    val albums =
        listOf(
            album("al-kid-a", "ar-radiohead", "Kid A", "Radiohead", 2000, 10),
            album("al-amnesiac", "ar-radiohead", "Amnesiac", "Radiohead", 2001, 11),
            album("al-dummy", "ar-portishead", "Dummy", "Portishead", 1994, 11),
            album("al-mezzanine", "ar-massive", "Mezzanine", "Massive Attack", 1998, 11),
            album("al-saw", "ar-aphex", "Selected Ambient Works 85-92", "Aphex Twin", 1992, 13),
            album("al-mhtrtc", "ar-boc", "Music Has the Right to Children", "Boards of Canada", 1998, 17),
            album("al-geogaddi", "ar-boc", "Geogaddi", "Boards of Canada", 2002, 23),
        )

    val songs =
        listOf(
            song("s-eiirp", "al-kid-a", "ar-radiohead", "Everything In Its Right Place", "Kid A", "Radiohead", 1, 251),
            song("s-kid-a", "al-kid-a", "ar-radiohead", "Kid A", "Kid A", "Radiohead", 2, 274),
            song("s-pyramid", "al-amnesiac", "ar-radiohead", "Pyramid Song", "Amnesiac", "Radiohead", 2, 289),
            song("s-roads", "al-dummy", "ar-portishead", "Roads", "Dummy", "Portishead", 9, 302),
            song("s-teardrop", "al-mezzanine", "ar-massive", "Teardrop", "Mezzanine", "Massive Attack", 2, 328),
            song("s-xtal", "al-saw", "ar-aphex", "Xtal", "Selected Ambient Works 85-92", "Aphex Twin", 1, 293),
            song("s-roygbiv", "al-mhtrtc", "ar-boc", "Roygbiv", "Music Has the Right to Children", "Boards of Canada", 3, 149),
            song("s-gyroscope", "al-geogaddi", "ar-boc", "Gyroscope", "Geogaddi", "Boards of Canada", 5, 216),
        )

    val songItems =
        songs.map { song ->
            SongListItem(song = song, coverArt = albums.firstOrNull { it.id == song.albumId }?.coverArt)
        }

    val playlists =
        listOf(
            playlist("pl-late-night", "Late Night", 42, "Smooth late-night picks"),
            playlist("pl-road-trip", "Road Trip", 63),
            playlist("pl-rainy-day", "Rainy Day", 25),
            playlist("pl-focus", "Focus", 18),
            playlist("pl-morning", "Morning", 7),
            playlist("pl-one-song", "One Song", 1),
        )

    fun playbackState() =
        PlaybackState(
            item =
                QueueItem(
                    id = "s-eiirp",
                    title = "Everything In Its Right Place",
                    artist = "Radiohead",
                    album = "Kid A",
                    coverArtId = "art-al-kid-a",
                ),
            isPlaying = true,
            positionMs = 62_000,
            durationMs = 251_000,
            hasNext = true,
            hasPrevious = false,
        )

    private fun artist(
        id: String,
        name: String,
        albumCount: Long,
    ) = Artist(
        sourceId = 1,
        id = id,
        name = name,
        albumCount = albumCount,
        starred = null,
        coverArt = "art-$id",
    )

    private fun album(
        id: String,
        artistId: String,
        name: String,
        albumArtist: String,
        year: Long,
        songCount: Long,
    ) = Album(
        sourceId = 1,
        id = id,
        artistId = artistId,
        name = name,
        albumArtist = albumArtist,
        created = 0,
        coverArt = "art-$id",
        genre = null,
        year = year,
        starred = null,
        songCount = songCount,
        frequentRank = null,
        recentRank = null,
    )

    fun song(
        id: String,
        albumId: String,
        artistId: String,
        title: String,
        album: String,
        artist: String,
        track: Long,
        duration: Long,
        disc: Long? = 1,
    ) = Song(
        sourceId = 1,
        id = id,
        albumId = albumId,
        artistId = artistId,
        title = title,
        album = album,
        artist = artist,
        duration = duration,
        track = track,
        disc = disc,
        starred = null,
        genre = null,
    )

    private fun playlist(
        id: String,
        name: String,
        songCount: Long,
        comment: String? = null,
    ) = Playlist(
        sourceId = 1,
        id = id,
        name = name,
        comment = comment,
        coverArt = "art-$id",
        songCount = songCount,
        created = 0,
        duration = songCount * 210,
    )

    fun artEngine(): FakeImageLoaderEngine {
        val builder = FakeImageLoaderEngine.Builder()
        albums.mapNotNull { it.coverArt }.forEach { builder.intercept(it, art(it, ArtKind.Album)) }
        playlists.mapNotNull { it.coverArt }.forEach { builder.intercept(it, art(it, ArtKind.Playlist)) }
        artists.mapNotNull { it.coverArt }.forEach { builder.intercept(it, art(it, ArtKind.Artist)) }
        return builder.default(art("art-default", ArtKind.Album)).build()
    }

    private fun art(
        key: String,
        kind: ArtKind,
    ): Image {
        val size = 256
        val f = size.toFloat()
        val random = Random(key.hashCode())
        val base = random.nextFloat() * 360f

        fun color(offset: Float): Int =
            Color.HSVToColor(
                floatArrayOf(((base + offset) % 360f + 360f) % 360f, 0.88f, 0.92f),
            )

        val palette =
            if (random.nextBoolean()) {
                listOf(color(0f), color(120f + random.nextFloat() * 40f), color(230f + random.nextFloat() * 40f))
            } else {
                listOf(color(0f), color(170f + random.nextFloat() * 30f))
            }

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawColor(palette[0])

        when (kind) {
            ArtKind.Album -> {
                val stripe = f / 9f
                canvas.save()
                canvas.rotate(-30f, f / 2f, f / 2f)
                var x = -f
                var index = 1
                while (x < f * 2f) {
                    paint.color = palette[index % palette.size]
                    canvas.drawRect(x, -f, x + stripe * 0.6f, f * 2f, paint)
                    x += stripe
                    index++
                }
                canvas.restore()
            }

            ArtKind.Playlist -> {
                val step = f / 4f
                var row = 1
                var y = step / 2f
                while (y < f) {
                    var x = step / 2f
                    while (x < f) {
                        paint.color = palette[row % palette.size]
                        canvas.drawCircle(x, y, step * 0.3f, paint)
                        x += step
                        row++
                    }
                    y += step
                }
            }

            ArtKind.Artist -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = f / 13f
                var radius = f * 0.5f
                var index = 1
                while (radius > 0f) {
                    paint.color = palette[index % palette.size]
                    canvas.drawCircle(f / 2f, f / 2f, radius, paint)
                    radius -= paint.strokeWidth
                    index++
                }
                paint.style = Paint.Style.FILL
            }
        }

        return bitmap.asImage()
    }
}

private enum class ArtKind { Album, Playlist, Artist }
