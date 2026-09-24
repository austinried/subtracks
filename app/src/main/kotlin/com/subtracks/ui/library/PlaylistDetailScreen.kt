package com.subtracks.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.SongListItem
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.HeroDetailScaffold
import com.subtracks.ui.components.HeroHeader
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.rememberArtworkColors
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val DOT = "\u00B7"
private const val THEME_TRANSITION_MS = 100

@Composable
fun PlaylistDetailRoute(
    playlistId: String,
    onBack: () -> Unit,
    viewModel: PlaylistDetailViewModel = koinViewModel(key = playlistId) { parametersOf(playlistId) },
    playbackController: PlaybackController = koinInject(),
) {
    val playlist by viewModel.playlist.collectAsStateWithLifecycle(initialValue = null)
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val context = playback.context
    PlaylistDetailScreen(
        playlist = playlist,
        songs = viewModel.songs.collectAsLazyPagingItems(),
        coverArt = viewModel::coverArt,
        artwork = rememberArtworkColors(viewModel.coverArt(playlist?.coverArt, true), THEME_TRANSITION_MS),
        onBack = onBack,
        onSongClick = viewModel::play,
        playingSongId = playback.item?.id.takeIf { context?.kind == QueueKind.Playlist && context.refId == playlistId },
    )
}

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist?,
    songs: LazyPagingItems<SongListItem>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    artwork: ArtworkColors?,
    onBack: () -> Unit,
    onSongClick: (Int) -> Unit,
    playingSongId: String? = null,
    modifier: Modifier = Modifier,
) {
    HeroDetailScaffold(
        artwork = artwork,
        title = playlist?.name.orEmpty(),
        onBack = onBack,
        header = { controlsModifier, topInset ->
            HeroHeader(
                art = coverArt(playlist?.coverArt, false),
                thumbnailRef = coverArt(playlist?.coverArt, true),
                name = playlist?.name.orEmpty(),
                subtitle = playlistSummary(playlist),
                comment = playlist?.comment,
                hasSongs = songs.itemCount > 0,
                onPlay = { onSongClick(0) },
                onShuffle = {},
                onDownload = {},
                onMore = {},
                topInset = topInset,
                controlsModifier = controlsModifier,
            )
        },
        content = { rowModifier ->
            items(count = songs.itemCount, key = songs.itemKey { it.song.id }) { index ->
                val item = songs[index]
                if (item != null) {
                    SongRow(
                        song = item.song,
                        coverArtId = item.coverArt,
                        coverArt = coverArt,
                        isPlaying = item.song.id == playingSongId,
                        modifier = rowModifier.clickable { onSongClick(index) },
                    )
                }
            }
        },
        modifier = modifier,
    )
}

private fun playlistSummary(playlist: Playlist?): String {
    playlist ?: return ""
    return listOfNotNull(
        "${playlist.songCount} ${if (playlist.songCount == 1L) "song" else "songs"}",
        formatDuration(playlist.duration),
    ).joinToString(" $DOT ")
}

private fun formatDuration(seconds: Long): String? {
    if (seconds < 60) return null
    val totalMinutes = seconds / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0L -> "$totalMinutes min"
        minutes == 0L -> "$hours hr"
        else -> "$hours hr $minutes min"
    }
}
