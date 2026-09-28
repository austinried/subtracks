package com.subtracks.ui.library

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FiberSmartRecord
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.data.model.Album
import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Disc
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.source.StarType
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.ContextMenuHost
import com.subtracks.ui.components.DeleteDownloadsDialog
import com.subtracks.ui.components.HeroDetailScaffold
import com.subtracks.ui.components.HeroHeader
import com.subtracks.ui.components.ItemActions
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.rememberArtworkColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val DOT = "\u00B7"
private const val THEME_TRANSITION_MS = 100

@Composable
fun AlbumDetailRoute(
    albumId: String,
    coverArtId: String?,
    onBack: () -> Unit,
    onViewArtist: (String) -> Unit,
    contextMenuHost: ContextMenuHost? = null,
    setStar: (StarType, String, Boolean) -> Unit,
    viewModel: AlbumDetailViewModel = koinViewModel(key = albumId) { parametersOf(albumId) },
    playbackController: PlaybackController = koinInject(),
) {
    val album by viewModel.album.collectAsStateWithLifecycle(initialValue = null)
    val songs by viewModel.songs.collectAsStateWithLifecycle(initialValue = emptyList())
    val discs by viewModel.discs.collectAsStateWithLifecycle(initialValue = emptyList())
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val downloadStatus by viewModel.downloadStatus.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<Long?>(null) }
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val context = playback.context
    val shortcutArt = remember(coverArtId) { coverArtId?.let { viewModel.coverArt(it, true) } }
    val requestDownloadAction: (BulkDownloadAction) -> Unit = { action ->
        if (action == BulkDownloadAction.Delete) {
            scope.launch { pendingDelete = viewModel.downloadedBytes() }
        } else {
            viewModel.onDownloadAction(action)
        }
    }
    val actions =
        ItemActions(
            playSong = { song -> songs.indexOfFirst { it.id == song.id }.takeIf { it >= 0 }?.let(viewModel::play) },
            playAlbum = { viewModel.playAll() },
            shuffleAlbum = { viewModel.shuffle() },
            playNext = { playbackController.playNext(it.sourceId, it.kind, it.refId) },
            addToQueue = { playbackController.addToQueue(it.sourceId, it.kind, it.refId) },
            download = viewModel::download,
            cancelDownload = viewModel::cancelDownload,
            deleteDownload = viewModel::deleteDownload,
            bulkDownload = { _, action -> requestDownloadAction(action) },
            setStar = setStar,
            viewArtist = onViewArtist,
        )
    AlbumDetailScreen(
        album = album,
        songs = songs,
        discs = discs,
        coverArt = viewModel::coverArt,
        artwork = rememberArtworkColors(shortcutArt ?: viewModel.coverArt(album?.coverArt, true), THEME_TRANSITION_MS),
        downloads = downloads,
        downloadStatus = downloadStatus,
        onDownloadAction = requestDownloadAction,
        onBack = onBack,
        onSongClick = viewModel::play,
        onSongLongClick = { contextMenuHost?.show(it, actions) },
        onShuffle = viewModel::shuffle,
        onPlay = viewModel::playAll,
        onMore = {
            album?.let {
                contextMenuHost?.show(
                    MenuTarget.Album(it, viewModel.coverArt(it.coverArt, true), downloadStatus),
                    actions,
                )
            }
        },
        onArtistClick =
            album
                ?.takeIf { !it.albumArtist.isNullOrBlank() }
                ?.let { it.artistId }
                ?.let { id -> { onViewArtist(id) } },
        playingSongId = playback.item?.id.takeIf { context?.kind == QueueKind.Album && context.refId == albumId },
    )

    pendingDelete?.let { bytes ->
        DeleteDownloadsDialog(
            name = album?.name.orEmpty(),
            bytes = bytes,
            onConfirm = { viewModel.onDownloadAction(BulkDownloadAction.Delete) },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
fun AlbumDetailScreen(
    album: Album?,
    songs: List<Song>,
    discs: List<Disc> = emptyList(),
    coverArt: (String?, Boolean) -> CoverArtRef?,
    artwork: ArtworkColors?,
    downloads: Map<String, SongDownload> = emptyMap(),
    onBack: () -> Unit,
    onSongClick: (Int) -> Unit,
    onSongLongClick: (MenuTarget) -> Unit = {},
    onShuffle: () -> Unit = {},
    onPlay: () -> Unit = { onSongClick(0) },
    downloadStatus: ListDownloadStatus = ListDownloadStatus(),
    onDownloadAction: (BulkDownloadAction) -> Unit = {},
    onMore: () -> Unit = {},
    onArtistClick: (() -> Unit)? = null,
    playingSongId: String? = null,
    modifier: Modifier = Modifier,
) {
    val multiDisc = songs.map { it.disc ?: 1L }.distinct().size > 1
    val discLabels =
        remember(discs) {
            val repeated =
                discs
                    .map { it.title }
                    .filter { it.isNotBlank() }
                    .groupingBy { it }
                    .eachCount()
                    .filterValues { it > 1 }
                    .keys
            discs.associate { disc ->
                disc.disc to
                    disc.title.takeIf { it.isNotBlank() }?.let { title ->
                        if (title in repeated) "$title: Disc ${disc.disc}" else title
                    }
            }
        }
    val discOrdinals =
        remember(songs) {
            val counters = mutableMapOf<Long, Long>()
            songs.associate { song ->
                val disc = song.disc ?: 1L
                val ordinal = (counters[disc] ?: 0L) + 1
                counters[disc] = ordinal
                song.id to ordinal
            }
        }
    HeroDetailScaffold(
        artwork = artwork,
        title = album?.name.orEmpty(),
        onBack = onBack,
        header = { controlsModifier, topInset ->
            val artistName = album?.albumArtist?.takeIf { it.isNotBlank() }
            HeroHeader(
                art = coverArt(album?.coverArt, false),
                thumbnailRef = coverArt(album?.coverArt, true),
                name = album?.name.orEmpty(),
                subtitle =
                    listOfNotNull(
                        artistName,
                        album?.year?.takeIf { it > 0 }?.toString(),
                    ).joinToString(" $DOT "),
                onSubtitleClick = onArtistClick,
                hasSongs = songs.isNotEmpty(),
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
            var lastDisc: Long? = null
            songs.forEachIndexed { index, song ->
                val disc = song.disc ?: 1L
                val discLabel = discLabels[disc]
                if (disc != lastDisc && (multiDisc || discLabel != null)) {
                    val isFirst = lastDisc == null
                    item(key = "disc:$disc") { DiscHeader(discLabel ?: "Disc $disc", first = isFirst) }
                }
                lastDisc = disc
                item(key = song.id) {
                    SongRow(
                        song = song,
                        isPlaying = song.id == playingSongId,
                        trackNumber = song.track ?: discOrdinals[song.id],
                        durationSeconds = song.duration,
                        download = downloads[song.id],
                        modifier =
                            rowModifier.combinedClickable(
                                onClick = { onSongClick(index) },
                                onLongClick = {
                                    onSongLongClick(MenuTarget.Song(song, coverArt(album?.coverArt, true), downloads[song.id]))
                                },
                            ),
                    )
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun DiscHeader(
    text: String,
    first: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = if (first) 16.dp else 40.dp, bottom = 4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.FiberSmartRecord,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
