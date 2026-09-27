package com.subtracks.ui.library

import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.SongListItem
import com.subtracks.data.source.StarType
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.ContextMenuHost
import com.subtracks.ui.components.HeroDetailScaffold
import com.subtracks.ui.components.HeroHeader
import com.subtracks.ui.components.ItemActions
import com.subtracks.ui.components.MenuTarget
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
    onViewAlbum: (String) -> Unit,
    onViewArtist: (String) -> Unit,
    contextMenuHost: ContextMenuHost? = null,
    setStar: suspend (StarType, String, Boolean) -> Result<Unit>,
    viewModel: PlaylistDetailViewModel = koinViewModel(key = playlistId) { parametersOf(playlistId) },
    playbackController: PlaybackController = koinInject(),
) {
    val playlist by viewModel.playlist.collectAsStateWithLifecycle(initialValue = null)
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val context = playback.context
    val actions =
        ItemActions(
            playPlaylist = { viewModel.playAll() },
            shufflePlaylist = { viewModel.shuffle() },
            playNext = { playbackController.playNext(it.sourceId, it.kind, it.refId) },
            addToQueue = { playbackController.addToQueue(it.sourceId, it.kind, it.refId) },
            download = viewModel::download,
            cancelDownload = viewModel::cancelDownload,
            deleteDownload = viewModel::deleteDownload,
            setStar = setStar,
            viewAlbum = onViewAlbum,
            viewArtist = onViewArtist,
        )
    PlaylistDetailScreen(
        playlist = playlist,
        songs = viewModel.songs.collectAsLazyPagingItems(),
        coverArt = viewModel::coverArt,
        artwork = rememberArtworkColors(viewModel.coverArt(playlist?.coverArt, true), THEME_TRANSITION_MS),
        downloads = downloads,
        onBack = onBack,
        onSongClick = viewModel::play,
        onSongLongClick = { contextMenuHost?.show(it, actions) },
        onShuffle = viewModel::shuffle,
        onPlay = viewModel::playAll,
        onMore = { playlist?.let { contextMenuHost?.show(MenuTarget.Playlist(it, viewModel.coverArt(it.coverArt, true)), actions) } },
        playingSongId = playback.item?.id.takeIf { context?.kind == QueueKind.Playlist && context.refId == playlistId },
    )
}

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist?,
    songs: LazyPagingItems<SongListItem>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    artwork: ArtworkColors?,
    downloads: Map<String, SongDownload> = emptyMap(),
    onBack: () -> Unit,
    onSongClick: (Int) -> Unit,
    onSongLongClick: (MenuTarget) -> Unit = {},
    onShuffle: () -> Unit = {},
    onPlay: () -> Unit = { onSongClick(0) },
    onMore: () -> Unit = {},
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
                onPlay = onPlay,
                onShuffle = onShuffle,
                onDownload = {},
                onMore = onMore,
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
                        download = downloads[item.song.id],
                        modifier =
                            rowModifier.combinedClickable(
                                onClick = { onSongClick(index) },
                                onLongClick = {
                                    onSongLongClick(MenuTarget.Song(item.song, coverArt(item.coverArt, true), downloads[item.song.id]))
                                },
                            ),
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
