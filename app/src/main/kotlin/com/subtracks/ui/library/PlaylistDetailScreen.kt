package com.subtracks.ui.library

import android.content.Context
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.subtracks.R
import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSongItem
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.SongDownload
import com.subtracks.data.source.StarType
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.CloseWhenDownloadsGone
import com.subtracks.ui.components.ContextMenuHost
import com.subtracks.ui.components.DeleteDownloadsDialog
import com.subtracks.ui.components.DismissOnRequest
import com.subtracks.ui.components.HeroDetailScaffold
import com.subtracks.ui.components.HeroHeader
import com.subtracks.ui.components.ItemActions
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.rememberArtworkColors
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
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
    setStar: (StarType, String, Boolean) -> Unit,
    viewModel: PlaylistDetailViewModel = koinViewModel(key = playlistId) { parametersOf(playlistId) },
    playbackController: PlaybackController = koinInject(),
) {
    val playlist by viewModel.playlist.collectAsStateWithLifecycle(initialValue = null)
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val downloadStatus by viewModel.downloadStatus.collectAsStateWithLifecycle()
    val offline by viewModel.offline.collectAsStateWithLifecycle()
    CloseWhenDownloadsGone(downloadStatus.downloaded, offline, onBack)
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<Long?>(null) }
    DismissOnRequest(contextMenuHost?.dismissals ?: emptyFlow()) { pendingDelete = null }
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val context = playback.context
    val requestDownloadAction: (BulkDownloadAction) -> Unit = { action ->
        if (action == BulkDownloadAction.Delete) {
            scope.launch { pendingDelete = viewModel.downloadedBytes() }
        } else {
            viewModel.onDownloadAction(action)
        }
    }
    val actions =
        ItemActions(
            playNext = { playbackController.playNext(it.sourceId, it.kind, it.refId) },
            addToQueue = { playbackController.addToQueue(it.sourceId, it.kind, it.refId) },
            download = viewModel::download,
            cancelDownload = viewModel::cancelDownload,
            deleteDownload = viewModel::deleteDownload,
            bulkDownload = { _, action -> requestDownloadAction(action) },
            setStar = setStar,
            viewAlbum = onViewAlbum,
            viewArtist = onViewArtist,
        )
    PlaylistDetailScreen(
        playlist = playlist,
        songs = viewModel.songs.collectAsLazyPagingItems(),
        coverArt = viewModel::coverArt,
        artwork = rememberArtworkColors(viewModel.coverArt(playlist?.coverArt, true), THEME_TRANSITION_MS, fallbackName = playlist?.name),
        downloads = downloads,
        downloadStatus = downloadStatus,
        offline = offline,
        onDownloadAction = requestDownloadAction,
        onBack = onBack,
        onSongClick = viewModel::play,
        onSongLongClick = { contextMenuHost?.show(it, actions) },
        onShuffle = viewModel::shuffle,
        onPlay = viewModel::playAll,
        onMore = {
            playlist?.let { contextMenuHost?.show(MenuTarget.Playlist(it, viewModel.coverArt(it.coverArt, true), downloadStatus), actions) }
        },
        playingSongId = playback.item?.id.takeIf { context?.kind == QueueKind.Playlist && context.refId == playlistId },
    )

    pendingDelete?.let { bytes ->
        DeleteDownloadsDialog(
            name = playlist?.name.orEmpty(),
            bytes = bytes,
            onConfirm = { viewModel.onDownloadAction(BulkDownloadAction.Delete) },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist?,
    songs: LazyPagingItems<PlaylistSongItem>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    artwork: ArtworkColors?,
    modifier: Modifier = Modifier,
    downloads: Map<String, SongDownload> = emptyMap(),
    downloadStatus: ListDownloadStatus = ListDownloadStatus(),
    offline: Boolean = false,
    onDownloadAction: (BulkDownloadAction) -> Unit = {},
    onBack: () -> Unit,
    onSongClick: (Long) -> Unit,
    onSongLongClick: (MenuTarget) -> Unit = {},
    onShuffle: () -> Unit = {},
    onPlay: () -> Unit = {},
    onMore: () -> Unit = {},
    playingSongId: String? = null,
) {
    val context = LocalContext.current
    HeroDetailScaffold(
        artwork = artwork,
        title = playlist?.name.orEmpty(),
        onBack = onBack,
        header = { controlsModifier, topInset ->
            HeroHeader(
                art = coverArt(playlist?.coverArt, false),
                thumbnailRef = coverArt(playlist?.coverArt, true),
                name = playlist?.name.orEmpty(),
                subtitle = playlistSummary(context, playlist, offline, downloadStatus.downloaded),
                comment = playlist?.comment,
                hasSongs = songs.itemCount > 0,
                onPlay = onPlay,
                onShuffle = onShuffle,
                downloadStatus = downloadStatus,
                onDownloadAction = onDownloadAction,
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
                        durationSeconds = item.song.duration,
                        download = downloads[item.song.id],
                        modifier =
                            rowModifier.combinedClickable(
                                onClick = { onSongClick(item.position) },
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

private fun playlistSummary(
    context: Context,
    playlist: Playlist?,
    offline: Boolean,
    downloadedSongs: Long,
): String {
    playlist ?: return ""
    val songs = if (offline) downloadedSongs else playlist.songCount
    return listOfNotNull(
        context.resources.getQuantityString(R.plurals.resources_song_count, songs.toInt(), songs),
        formatDuration(context, playlist.duration),
    ).joinToString(" $DOT ")
}

private fun formatDuration(
    context: Context,
    seconds: Long,
): String? {
    if (seconds < 60) return null
    val totalMinutes = seconds / 60
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0L -> context.getString(R.string.playlist_duration_minutes, totalMinutes)
        minutes == 0L -> context.getString(R.string.playlist_duration_hours, hours)
        else -> context.getString(R.string.playlist_duration_hours_minutes, hours, minutes)
    }
}
