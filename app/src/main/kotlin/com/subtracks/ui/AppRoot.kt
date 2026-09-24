package com.subtracks.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.library.AlbumDetailRoute
import com.subtracks.ui.library.ArtistDetailRoute
import com.subtracks.ui.library.LibraryRoute
import com.subtracks.ui.library.PlaylistDetailRoute
import com.subtracks.ui.playback.MiniPlayer
import com.subtracks.ui.playback.NowPlayingRoute
import com.subtracks.ui.playback.QueueRoute
import com.subtracks.ui.settings.AddSourceRoute
import com.subtracks.ui.settings.SettingsRoute
import com.subtracks.ui.theme.rememberArtworkColors
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
private const val EXPAND_FADE = 0.1f

private object Routes {
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val ADD_SERVER = "add-server"
    const val ALBUM_DETAIL = "album/{albumId}?coverArt={coverArt}"
    const val ARTIST_DETAIL = "artist/{artistId}?coverArt={coverArt}"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}"

    fun album(
        id: String,
        coverArt: String? = null,
    ) = "album/${Uri.encode(id)}?coverArt=${Uri.encode(coverArt.orEmpty())}"

    fun artist(
        id: String,
        coverArt: String? = null,
    ) = "artist/${Uri.encode(id)}?coverArt=${Uri.encode(coverArt.orEmpty())}"

    fun playlist(id: String) = "playlist/${Uri.encode(id)}"
}

@Composable
fun SubtracksRoot(root: RootViewModel = koinViewModel()) {
    when (val hasSource = root.hasSource.collectAsStateWithLifecycle().value) {
        null -> LoadingState()
        false -> AddSourceRoute(onSaved = {}, onBack = null)
        true -> MainNavigation()
    }
}

@Composable
private fun MainNavigation() {
    val navController = rememberNavController()
    val playbackController = koinInject<PlaybackController>()
    val playback by playbackController.state.collectAsStateWithLifecycle()
    var showingQueue by rememberSaveable { mutableStateOf(false) }
    var nowPlayingProgress by remember { mutableFloatStateOf(0f) }
    var miniPlayerTopPx by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    fun settleNowPlaying(open: Boolean) {
        val target = if (open) 1f else 0f
        scope.launch {
            animate(nowPlayingProgress, target, animationSpec = tween(OVERLAY_DURATION_MS)) { value, _ ->
                nowPlayingProgress = value
            }
        }
    }
    LaunchedEffect(Unit) { playbackController.connect() }

    val playerVisible = playback.item != null
    val density = LocalDensity.current
    val navBarInset = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
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
                        onAlbumClick = { album -> navController.navigate(Routes.album(album.id, album.coverArt)) },
                        onArtistClick = { artist -> navController.navigate(Routes.artist(artist.id, artist.coverArt)) },
                        onPlaylistClick = { playlist -> navController.navigate(Routes.playlist(playlist.id)) },
                        onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                        bottomInset = if (playerVisible) 0.dp else navBarInset,
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsRoute(
                        onAddServer = { navController.navigate(Routes.ADD_SERVER) },
                        onBack = { navController.popBackStack() },
                    )
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
                        onAlbumClick = { album -> navController.navigate(Routes.album(album.id, album.coverArt)) },
                    )
                }
                composable(
                    route = Routes.PLAYLIST_DETAIL,
                    arguments = listOf(navArgument("playlistId") { type = NavType.StringType }),
                ) { entry ->
                    PlaylistDetailRoute(
                        playlistId = entry.arguments?.getString("playlistId").orEmpty(),
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.ADD_SERVER) {
                    AddSourceRoute(
                        onSaved = { navController.popBackStack() },
                        onBack = { navController.popBackStack() },
                    )
                }
            }

            if (playerVisible) {
                val miniArt = playbackController.coverArt(playback.item, thumbnail = true)
                MiniPlayer(
                    state = playback,
                    coverArt = miniArt,
                    artwork = rememberArtworkColors(miniArt),
                    onExpand = { settleNowPlaying(true) },
                    onPlayPause = playbackController::togglePlayPause,
                    onNext = playbackController::next,
                    onExpandDrag = { dragUpPx ->
                        if (miniPlayerTopPx > 0f) {
                            nowPlayingProgress = (dragUpPx / miniPlayerTopPx).coerceIn(0f, 1f)
                        }
                    },
                    onExpandRelease = { settleNowPlaying(nowPlayingProgress > 0.05f) },
                    modifier = Modifier.onGloballyPositioned { miniPlayerTopPx = it.positionInRoot().y },
                )
            }
        }

        if (nowPlayingProgress > 0f) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationY = (1f - nowPlayingProgress) * miniPlayerTopPx
                            alpha = (nowPlayingProgress / EXPAND_FADE).coerceAtMost(1f)
                        }.draggable(
                            orientation = Orientation.Vertical,
                            state =
                                rememberDraggableState { delta ->
                                    if (miniPlayerTopPx > 0f) {
                                        nowPlayingProgress = (nowPlayingProgress - delta / miniPlayerTopPx).coerceIn(0f, 1f)
                                    }
                                },
                            onDragStopped = { settleNowPlaying(nowPlayingProgress > 0.9f) },
                        ),
            ) {
                NowPlayingRoute(
                    onBack = { settleNowPlaying(false) },
                    onQueue = { showingQueue = true },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        AnimatedVisibility(
            visible = showingQueue,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(OVERLAY_DURATION_MS)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(OVERLAY_DURATION_MS)),
        ) {
            QueueRoute(
                onBack = { showingQueue = false },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    BackHandler(enabled = showingQueue) { showingQueue = false }
    BackHandler(enabled = nowPlayingProgress > 0f && !showingQueue) {
        settleNowPlaying(false)
    }
}
