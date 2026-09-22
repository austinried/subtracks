package com.subtracks.ui

import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.subtracks.data.repo.SourceRepository
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.library.AlbumDetailRoute
import com.subtracks.ui.library.LibraryRoute
import com.subtracks.ui.library.PlaylistDetailRoute
import com.subtracks.ui.settings.AddSourceRoute
import com.subtracks.ui.settings.SettingsRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    NavHost(
        navController = navController,
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
}
