package com.subtracks.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.subtracks.ui.library.LibraryRoute
import com.subtracks.ui.library.PlaylistDetailRoute
import com.subtracks.ui.playback.MiniPlayer
import com.subtracks.ui.playback.NowPlayingRoute
import com.subtracks.ui.playback.QueueRoute
import com.subtracks.ui.settings.AddSourceRoute
import com.subtracks.ui.settings.SettingsRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
private const val NOW_PLAYING_DURATION_MS = 300

private object Routes {
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val ADD_SERVER = "add-server"
    const val ALBUM_DETAIL = "album/{albumId}"
    const val PLAYLIST_DETAIL = "playlist/{playlistId}"

    fun album(id: String) = "album/${Uri.encode(id)}"

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
    var showingNowPlaying by rememberSaveable { mutableStateOf(false) }
    var showingQueue by rememberSaveable { mutableStateOf(false) }
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
                        onAlbumClick = { album -> navController.navigate(Routes.album(album.id)) },
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
                    arguments = listOf(navArgument("albumId") { type = NavType.StringType }),
                ) { entry ->
                    AlbumDetailRoute(
                        albumId = entry.arguments?.getString("albumId").orEmpty(),
                        onBack = { navController.popBackStack() },
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
                MiniPlayer(
                    state = playback,
                    coverArt = playbackController.coverArt(playback.item, thumbnail = true),
                    onExpand = { showingNowPlaying = true },
                    onPlayPause = playbackController::togglePlayPause,
                    onNext = playbackController::next,
                )
            }
        }

        AnimatedVisibility(
            visible = showingNowPlaying,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(NOW_PLAYING_DURATION_MS)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(NOW_PLAYING_DURATION_MS)),
        ) {
            NowPlayingRoute(
                onBack = { showingNowPlaying = false },
                onQueue = { showingQueue = true },
                modifier = Modifier.fillMaxSize(),
            )
        }

        AnimatedVisibility(
            visible = showingQueue,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(NOW_PLAYING_DURATION_MS)),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(NOW_PLAYING_DURATION_MS)),
        ) {
            QueueRoute(
                onBack = { showingQueue = false },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    BackHandler(enabled = showingQueue) { showingQueue = false }
    BackHandler(enabled = showingNowPlaying && !showingQueue) { showingNowPlaying = false }
}
