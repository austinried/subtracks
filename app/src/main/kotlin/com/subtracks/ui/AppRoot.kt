package com.subtracks.ui

import android.net.Uri
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.subtracks.data.model.Song
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.ContextMenuHost
import com.subtracks.ui.components.ItemContextMenu
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.components.SongInfoDialog
import com.subtracks.ui.components.statusBarScrim
import com.subtracks.ui.downloads.DownloadsRoute
import com.subtracks.ui.home.HomeListRequest
import com.subtracks.ui.home.HomeListRoute
import com.subtracks.ui.home.HomeSection
import com.subtracks.ui.library.AlbumDetailRoute
import com.subtracks.ui.library.ArtistDetailRoute
import com.subtracks.ui.library.LibraryRoute
import com.subtracks.ui.library.PlaylistDetailRoute
import com.subtracks.ui.playback.MiniPlayer
import com.subtracks.ui.playback.NowPlayingRoute
import com.subtracks.ui.playback.QueueRoute
import com.subtracks.ui.settings.AddSourceRoute
import com.subtracks.ui.settings.LicensesRoute
import com.subtracks.ui.settings.SettingsRoute
import com.subtracks.ui.theme.ArtworkTheme
import com.subtracks.ui.theme.rememberArtworkColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

class RootViewModel(
    sourceRepository: SourceRepository,
) : ViewModel() {
    val hasSource =
        sourceRepository
            .activeSourceId()
            .map { it != null }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

private const val NAVIGATION_DURATION_MS = 260
private const val OVERLAY_DURATION_MS = 200
private const val FADE_OUT_MS = 150
private const val EXPAND_FADE = 0.1f
private const val FLING_VELOCITY = 1000f
private const val MINI_PLAYER_ANIM_MS = 200
private const val SCRIM_FADE_START = 0.85f

private object Routes {
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val DOWNLOADS = "downloads"
    const val LICENSES = "licenses"
    const val ADD_SERVER = "add-server"
    const val EDIT_SERVER = "edit-server/{sourceId}"
    const val ALBUM_DETAIL = "album/{albumId}?coverArt={coverArt}"
    const val ARTIST_DETAIL = "artist/{artistId}?coverArt={coverArt}"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}"
    const val HOME_SECTION = "home/more/{section}"
    const val HOME_GENRE = "home/genre/{genre}"
    const val HOME_DECADE = "home/decade/{decade}"

    fun album(
        id: String,
        coverArt: String? = null,
    ) = "album/${Uri.encode(id)}?coverArt=${Uri.encode(coverArt.orEmpty())}"

    fun artist(
        id: String,
        coverArt: String? = null,
    ) = "artist/${Uri.encode(id)}?coverArt=${Uri.encode(coverArt.orEmpty())}"

    fun playlist(id: String) = "playlist/${Uri.encode(id)}"

    fun editServer(id: Long) = "edit-server/$id"

    fun homeSection(section: HomeSection) = "home/more/${section.name}"

    fun homeGenre(genre: String) = "home/genre/${Uri.encode(genre)}"

    fun homeDecade(decade: Long) = "home/decade/$decade"
}

private val DETAIL_ROUTES =
    setOf(Routes.ALBUM_DETAIL, Routes.ARTIST_DETAIL, Routes.PLAYLIST_DETAIL)

internal fun resetOnSourceSwitch(
    previousSourceId: Long?,
    currentSourceId: Long?,
    route: String?,
    closeNowPlaying: () -> Unit,
    closeQueue: () -> Unit,
    popDetail: () -> Unit,
) {
    if (previousSourceId == null || currentSourceId == null || previousSourceId == currentSourceId) return
    closeNowPlaying()
    closeQueue()
    if (route in DETAIL_ROUTES) popDetail()
}

internal data class BackStackKey(
    val route: String?,
    val argument: String?,
)

internal fun focusPops(
    stack: List<BackStackKey>,
    pattern: String,
    value: String,
): Int? {
    val index = stack.indexOfLast { it.route == pattern && it.argument == value }
    return if (index < 0) null else stack.size - 1 - index
}

@Composable
fun SubtracksRoot(root: RootViewModel = koinViewModel()) {
    when (val hasSource = root.hasSource.collectAsStateWithLifecycle().value) {
        null -> {
            LoadingState()
        }

        false -> {
            AddSourceRoute(onSaved = {}, onBack = null)
        }

        true -> {
            val playbackController = koinInject<PlaybackController>()
            LaunchedEffect(Unit) { playbackController.connect() }
            val playbackReady by playbackController.ready.collectAsStateWithLifecycle()
            if (playbackReady) MainNavigation() else LoadingState()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MainNavigation() {
    val navController = rememberNavController()
    val currentRoute =
        navController
            .currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route
    val tabBarVisible = currentRoute == Routes.LIBRARY
    val playbackController = koinInject<PlaybackController>()
    val libraryRepository = koinInject<LibraryRepository>()
    val downloadRepository = koinInject<DownloadRepository>()
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val downloads by downloadRepository.states().collectAsStateWithLifecycle()
    var showingQueue by rememberSaveable { mutableStateOf(false) }
    var nowPlayingOpen by rememberSaveable { mutableStateOf(false) }
    var nowPlayingProgress by remember { mutableFloatStateOf(0f) }
    var miniPlayerTopPx by remember { mutableFloatStateOf(0f) }
    var nowPlayingFadeOut by remember { mutableStateOf(false) }
    var settleJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val contextMenuHost = remember { ContextMenuHost() }
    var infoSong by remember { mutableStateOf<Song?>(null) }

    val activeSourceId by libraryRepository.activeSourceId.collectAsStateWithLifecycle(initialValue = null)
    var previousSourceId by rememberSaveable { mutableStateOf<Long?>(null) }
    LaunchedEffect(activeSourceId) {
        val id = activeSourceId
        resetOnSourceSwitch(
            previousSourceId = previousSourceId,
            currentSourceId = id,
            route = currentRoute,
            closeNowPlaying = {
                settleJob?.cancel()
                nowPlayingOpen = false
                nowPlayingProgress = 0f
            },
            closeQueue = { showingQueue = false },
            popDetail = { navController.popBackStack(Routes.LIBRARY, inclusive = false) },
        )
        if (id != null) previousSourceId = id
    }

    fun settleNowPlaying(open: Boolean) {
        if (open) {
            nowPlayingOpen = true
            nowPlayingFadeOut = false
        }
        settleJob?.cancel()
        settleJob =
            scope.launch {
                animate(nowPlayingProgress, if (open) 1f else 0f, animationSpec = tween(OVERLAY_DURATION_MS)) { value, _ ->
                    nowPlayingProgress = value
                }
                if (!open) nowPlayingOpen = false
            }
    }

    fun fadeNowPlaying() {
        nowPlayingFadeOut = true
        settleJob?.cancel()
        settleJob =
            scope.launch {
                animate(nowPlayingProgress, 0f, animationSpec = tween(FADE_OUT_MS)) { value, _ ->
                    nowPlayingProgress = value
                }
                nowPlayingOpen = false
                nowPlayingFadeOut = false
            }
    }

    // currentBackStack is restricted to androidx.navigation, but it is the only way to find a
    // detail entry by its arguments; popBackStack(route) matches the destination and ignores them.
    @Suppress("RestrictedApi")
    fun navigateDetail(
        pattern: String,
        argument: String,
        value: String,
        route: String,
    ) {
        val stack =
            navController.currentBackStack.value.map {
                BackStackKey(it.destination.route, it.arguments?.getString(argument))
            }
        when (val pops = focusPops(stack, pattern, value)) {
            null -> navController.navigate(route)
            0 -> Unit
            else -> repeat(pops) { navController.popBackStack() }
        }
    }
    LaunchedEffect(Unit) { if (nowPlayingOpen) nowPlayingProgress = 1f }

    val playerVisible = playback.item != null
    LaunchedEffect(playerVisible) {
        if (!playerVisible) {
            settleJob?.cancel()
            nowPlayingOpen = false
            nowPlayingProgress = 0f
        }
    }
    val density = LocalDensity.current
    val navBarInset = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
    val keyboardUp = WindowInsets.imeAnimationTarget.getBottom(density) > 0
    val showMiniPlayer = playerVisible && !keyboardUp
    val bottomInset by animateDpAsState(
        targetValue = if (showMiniPlayer) 0.dp else navBarInset,
        animationSpec = tween(MINI_PLAYER_ANIM_MS),
        label = "libraryBottomInset",
    )

    val artwork =
        rememberArtworkColors(
            playbackController.coverArt(playback.item, thumbnail = true),
            markActive = true,
            fallbackName = playback.item?.title,
        )
    ArtworkTheme(artwork) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().imePadding()) {
                NavHost(
                    navController = navController,
                    modifier =
                        Modifier
                            .weight(1f)
                            .then(
                                if (playerVisible) {
                                    Modifier.consumeWindowInsets(WindowInsets.navigationBars)
                                } else {
                                    Modifier
                                },
                            ),
                    startDestination = Routes.LIBRARY,
                    enterTransition = {
                        slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = tween(NAVIGATION_DURATION_MS)) +
                            fadeIn(tween(NAVIGATION_DURATION_MS))
                    },
                    exitTransition = {
                        slideOutHorizontally(targetOffsetX = { -it / 4 }, animationSpec = tween(NAVIGATION_DURATION_MS)) +
                            fadeOut(tween(NAVIGATION_DURATION_MS))
                    },
                    popEnterTransition = {
                        slideInHorizontally(initialOffsetX = { -it / 4 }, animationSpec = tween(NAVIGATION_DURATION_MS)) +
                            fadeIn(tween(NAVIGATION_DURATION_MS))
                    },
                    popExitTransition = {
                        slideOutHorizontally(targetOffsetX = { it / 4 }, animationSpec = tween(NAVIGATION_DURATION_MS)) +
                            fadeOut(tween(NAVIGATION_DURATION_MS))
                    },
                ) {
                    composable(Routes.LIBRARY) {
                        LibraryRoute(
                            onAlbumClick = { album ->
                                navigateDetail(Routes.ALBUM_DETAIL, "albumId", album.id, Routes.album(album.id, album.coverArt))
                            },
                            onArtistClick = { artist ->
                                navigateDetail(Routes.ARTIST_DETAIL, "artistId", artist.id, Routes.artist(artist.id, artist.coverArt))
                            },
                            onPlaylistClick = { playlist ->
                                navigateDetail(Routes.PLAYLIST_DETAIL, "playlistId", playlist.id, Routes.playlist(playlist.id))
                            },
                            onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                            onViewAlbum = { albumId -> navigateDetail(Routes.ALBUM_DETAIL, "albumId", albumId, Routes.album(albumId)) },
                            onViewArtist = { artistId ->
                                navigateDetail(Routes.ARTIST_DETAIL, "artistId", artistId, Routes.artist(artistId))
                            },
                            onHomeMore = { section -> navController.navigate(Routes.homeSection(section)) },
                            onGenreClick = { genre -> navController.navigate(Routes.homeGenre(genre)) },
                            onDecadeClick = { decade -> navController.navigate(Routes.homeDecade(decade)) },
                            contextMenuHost = contextMenuHost,
                            setStar = libraryRepository::star,
                            bottomInset = bottomInset,
                        )
                    }
                    composable(Routes.SETTINGS) {
                        SettingsRoute(
                            onAddServer = { navController.navigate(Routes.ADD_SERVER) },
                            onOpenDownloads = { navController.navigate(Routes.DOWNLOADS) },
                            onOpenLicenses = { navController.navigate(Routes.LICENSES) },
                            onEditServer = { id -> navController.navigate(Routes.editServer(id)) },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(Routes.DOWNLOADS) {
                        DownloadsRoute(onBack = { navController.popBackStack() })
                    }
                    composable(Routes.LICENSES) {
                        LicensesRoute(onBack = { navController.popBackStack() })
                    }
                    composable(
                        route = Routes.ALBUM_DETAIL,
                        arguments =
                            listOf(
                                navArgument("albumId") { type = NavType.StringType },
                                navArgument("coverArt") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                            ),
                    ) { entry ->
                        AlbumDetailRoute(
                            albumId = entry.arguments?.getString("albumId").orEmpty(),
                            coverArtId =
                                entry.arguments
                                    ?.getString("coverArt")
                                    .orEmpty()
                                    .ifEmpty { null },
                            onBack = { navController.popBackStack() },
                            onViewArtist = { artistId ->
                                navigateDetail(Routes.ARTIST_DETAIL, "artistId", artistId, Routes.artist(artistId))
                            },
                            contextMenuHost = contextMenuHost,
                            setStar = libraryRepository::star,
                        )
                    }
                    composable(
                        route = Routes.ARTIST_DETAIL,
                        arguments =
                            listOf(
                                navArgument("artistId") { type = NavType.StringType },
                                navArgument("coverArt") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                },
                            ),
                    ) { entry ->
                        ArtistDetailRoute(
                            artistId = entry.arguments?.getString("artistId").orEmpty(),
                            coverArtId =
                                entry.arguments
                                    ?.getString("coverArt")
                                    .orEmpty()
                                    .ifEmpty { null },
                            onBack = { navController.popBackStack() },
                            onAlbumClick = { album ->
                                navigateDetail(Routes.ALBUM_DETAIL, "albumId", album.id, Routes.album(album.id, album.coverArt))
                            },
                            onViewAlbum = { albumId -> navigateDetail(Routes.ALBUM_DETAIL, "albumId", albumId, Routes.album(albumId)) },
                            contextMenuHost = contextMenuHost,
                            setStar = libraryRepository::star,
                        )
                    }
                    composable(
                        route = Routes.PLAYLIST_DETAIL,
                        arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
                    ) { entry ->
                        PlaylistDetailRoute(
                            playlistId = entry.arguments?.getString("playlistId").orEmpty(),
                            onBack = { navController.popBackStack() },
                            onViewAlbum = { albumId -> navigateDetail(Routes.ALBUM_DETAIL, "albumId", albumId, Routes.album(albumId)) },
                            onViewArtist = { artistId ->
                                navigateDetail(Routes.ARTIST_DETAIL, "artistId", artistId, Routes.artist(artistId))
                            },
                            contextMenuHost = contextMenuHost,
                            setStar = libraryRepository::star,
                        )
                    }
                    composable(
                        route = Routes.HOME_SECTION,
                        arguments = listOf(navArgument("section") { type = NavType.StringType }),
                    ) { entry ->
                        val section = HomeSection.fromRoute(entry.arguments?.getString("section")) ?: return@composable
                        HomeListRoute(
                            request = HomeListRequest(title = section.title, section = section),
                            onBack = { navController.popBackStack() },
                            onAlbumClick = { album ->
                                navigateDetail(Routes.ALBUM_DETAIL, "albumId", album.id, Routes.album(album.id, album.coverArt))
                            },
                            onArtistClick = { artist ->
                                navigateDetail(Routes.ARTIST_DETAIL, "artistId", artist.id, Routes.artist(artist.id, artist.coverArt))
                            },
                            onViewAlbum = { albumId -> navigateDetail(Routes.ALBUM_DETAIL, "albumId", albumId, Routes.album(albumId)) },
                            onViewArtist = { artistId ->
                                navigateDetail(Routes.ARTIST_DETAIL, "artistId", artistId, Routes.artist(artistId))
                            },
                            onGenreClick = { genre -> navController.navigate(Routes.homeGenre(genre)) },
                            onDecadeClick = { decade -> navController.navigate(Routes.homeDecade(decade)) },
                            contextMenuHost = contextMenuHost,
                            setStar = libraryRepository::star,
                        )
                    }
                    composable(
                        route = Routes.HOME_GENRE,
                        arguments = listOf(navArgument("genre") { type = NavType.StringType }),
                    ) { entry ->
                        val genre = entry.arguments?.getString("genre").orEmpty()
                        HomeListRoute(
                            request = HomeListRequest(title = genre, genre = genre),
                            onBack = { navController.popBackStack() },
                            onAlbumClick = { album ->
                                navigateDetail(Routes.ALBUM_DETAIL, "albumId", album.id, Routes.album(album.id, album.coverArt))
                            },
                            onArtistClick = { artist ->
                                navigateDetail(Routes.ARTIST_DETAIL, "artistId", artist.id, Routes.artist(artist.id, artist.coverArt))
                            },
                            onViewAlbum = { albumId -> navigateDetail(Routes.ALBUM_DETAIL, "albumId", albumId, Routes.album(albumId)) },
                            onViewArtist = { artistId ->
                                navigateDetail(Routes.ARTIST_DETAIL, "artistId", artistId, Routes.artist(artistId))
                            },
                            onGenreClick = { value -> navController.navigate(Routes.homeGenre(value)) },
                            onDecadeClick = { decade -> navController.navigate(Routes.homeDecade(decade)) },
                            contextMenuHost = contextMenuHost,
                            setStar = libraryRepository::star,
                        )
                    }
                    composable(
                        route = Routes.HOME_DECADE,
                        arguments = listOf(navArgument("decade") { type = NavType.LongType }),
                    ) { entry ->
                        val decade = entry.arguments?.getLong("decade") ?: return@composable
                        HomeListRoute(
                            request = HomeListRequest(title = "${decade}s", decade = decade),
                            onBack = { navController.popBackStack() },
                            onAlbumClick = { album ->
                                navigateDetail(Routes.ALBUM_DETAIL, "albumId", album.id, Routes.album(album.id, album.coverArt))
                            },
                            onArtistClick = { artist ->
                                navigateDetail(Routes.ARTIST_DETAIL, "artistId", artist.id, Routes.artist(artist.id, artist.coverArt))
                            },
                            onViewAlbum = { albumId -> navigateDetail(Routes.ALBUM_DETAIL, "albumId", albumId, Routes.album(albumId)) },
                            onViewArtist = { artistId ->
                                navigateDetail(Routes.ARTIST_DETAIL, "artistId", artistId, Routes.artist(artistId))
                            },
                            onGenreClick = { genre -> navController.navigate(Routes.homeGenre(genre)) },
                            onDecadeClick = { value -> navController.navigate(Routes.homeDecade(value)) },
                            contextMenuHost = contextMenuHost,
                            setStar = libraryRepository::star,
                        )
                    }
                    composable(Routes.ADD_SERVER) {
                        AddSourceRoute(
                            onSaved = { navController.popBackStack() },
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(
                        route = Routes.EDIT_SERVER,
                        arguments = listOf(navArgument("sourceId") { type = NavType.LongType }),
                    ) { entry ->
                        AddSourceRoute(
                            onSaved = { navController.popBackStack() },
                            onBack = { navController.popBackStack() },
                            sourceId = entry.arguments?.getLong("sourceId"),
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showMiniPlayer,
                    enter = expandVertically(tween(MINI_PLAYER_ANIM_MS), expandFrom = Alignment.Bottom),
                    exit = shrinkVertically(tween(MINI_PLAYER_ANIM_MS), shrinkTowards = Alignment.Bottom),
                ) {
                    val positionMs by playbackController.positionMs.collectAsStateWithLifecycle()
                    val miniArt = playbackController.coverArt(playback.item, thumbnail = true)
                    MiniPlayer(
                        state = playback,
                        positionMs = positionMs,
                        coverArt = miniArt,
                        artwork = artwork,
                        onExpand = { settleNowPlaying(true) },
                        onPlayPause = playbackController::togglePlayPause,
                        onNext = playbackController::next,
                        progressInset = tabBarVisible,
                        onExpandDrag = { dragUpPx ->
                            nowPlayingOpen = true
                            settleJob?.cancel()
                            if (miniPlayerTopPx > 0f) {
                                nowPlayingProgress = (dragUpPx / miniPlayerTopPx).coerceIn(0f, 1f)
                            }
                        },
                        onExpandRelease = { velocity -> settleNowPlaying(nowPlayingProgress > 0.05f || velocity < -FLING_VELOCITY) },
                        modifier = Modifier.onGloballyPositioned { miniPlayerTopPx = it.positionInRoot().y },
                    )
                }
            }

            if (nowPlayingOpen) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationY = if (nowPlayingFadeOut) 0f else (1f - nowPlayingProgress) * miniPlayerTopPx
                                alpha = if (nowPlayingFadeOut) nowPlayingProgress else (nowPlayingProgress / EXPAND_FADE).coerceAtMost(1f)
                            }.draggable(
                                orientation = Orientation.Vertical,
                                state =
                                    rememberDraggableState { delta ->
                                        settleJob?.cancel()
                                        if (miniPlayerTopPx > 0f) {
                                            nowPlayingProgress = (nowPlayingProgress - delta / miniPlayerTopPx).coerceIn(0f, 1f)
                                        }
                                    },
                                onDragStopped = { velocity -> settleNowPlaying(nowPlayingProgress > 0.9f && velocity < FLING_VELOCITY) },
                            ),
                ) {
                    NowPlayingRoute(
                        onBack = { settleNowPlaying(false) },
                        onQueue = { showingQueue = true },
                        onViewAlbum = { albumId ->
                            navigateDetail(Routes.ALBUM_DETAIL, "albumId", albumId, Routes.album(albumId))
                            fadeNowPlaying()
                        },
                        onViewArtist = { artistId ->
                            navigateDetail(Routes.ARTIST_DETAIL, "artistId", artistId, Routes.artist(artistId))
                            fadeNowPlaying()
                        },
                        contextMenuHost = contextMenuHost,
                        setStar = libraryRepository::star,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .graphicsLayer {
                                alpha = ((nowPlayingProgress - SCRIM_FADE_START) / (1f - SCRIM_FADE_START)).coerceIn(0f, 1f)
                            }.statusBarScrim(),
                )
            }

            AnimatedVisibility(
                visible = showingQueue,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(OVERLAY_DURATION_MS)),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(OVERLAY_DURATION_MS)),
            ) {
                val queueStoreOwner =
                    remember {
                        object : ViewModelStoreOwner {
                            override val viewModelStore = ViewModelStore()
                        }
                    }
                DisposableEffect(queueStoreOwner) { onDispose { queueStoreOwner.viewModelStore.clear() } }
                CompositionLocalProvider(LocalViewModelStoreOwner provides queueStoreOwner) {
                    QueueRoute(
                        open = showingQueue,
                        onBack = { showingQueue = false },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            contextMenuHost.target?.let { target ->
                ItemContextMenu(
                    target = target,
                    actions = contextMenuHost.actions,
                    onDismiss = { contextMenuHost.dismiss() },
                    onInfo = { infoSong = it },
                )
            }

            infoSong?.let { song ->
                SongInfoDialog(
                    song = song,
                    download = downloads[song.id],
                    localFile = downloadRepository.localFile(song.id),
                    streamEncoding = playbackController.currentAudioEncoding().takeIf { playback.item?.id == song.id },
                    onDismiss = { infoSong = null },
                )
            }
        }
    }

    // The NavHost registers its back callback after ours, so on a pushed route it wins and pops the
    // route under the now playing overlay. Re-register ours on every navigation so it is the most
    // recent callback in the dispatcher and gets the back first.
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val backCallback =
        remember {
            object : OnBackPressedCallback(false) {
                override fun handleOnBackPressed() {
                    when {
                        showingQueue -> showingQueue = false
                        nowPlayingOpen -> settleNowPlaying(false)
                        else -> navController.popBackStack()
                    }
                }
            }
        }
    SideEffect {
        backCallback.isEnabled = showingQueue || nowPlayingOpen || navController.previousBackStackEntry != null
    }
    DisposableEffect(backDispatcher, currentRoute, showingQueue, nowPlayingOpen) {
        backDispatcher?.addCallback(backCallback)
        onDispose { backCallback.remove() }
    }
}
