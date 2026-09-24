package com.subtracks.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.data.model.Album
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.Song
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
fun AlbumDetailRoute(
    albumId: String,
    coverArtId: String?,
    onBack: () -> Unit,
    viewModel: AlbumDetailViewModel = koinViewModel(key = albumId) { parametersOf(albumId) },
    playbackController: PlaybackController = koinInject(),
) {
    val album by viewModel.album.collectAsStateWithLifecycle(initialValue = null)
    val songs by viewModel.songs.collectAsStateWithLifecycle(initialValue = emptyList())
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val context = playback.context
    val shortcutArt = remember(coverArtId) { coverArtId?.let { viewModel.coverArt(it, true) } }
    AlbumDetailScreen(
        album = album,
        songs = songs,
        coverArt = viewModel::coverArt,
        artwork = rememberArtworkColors(shortcutArt ?: viewModel.coverArt(album?.coverArt, true), THEME_TRANSITION_MS),
        onBack = onBack,
        onSongClick = viewModel::play,
        playingSongId = playback.item?.id.takeIf { context?.kind == QueueKind.Album && context.refId == albumId },
    )
}

@Composable
fun AlbumDetailScreen(
    album: Album?,
    songs: List<Song>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    artwork: ArtworkColors?,
    onBack: () -> Unit,
    onSongClick: (Int) -> Unit,
    onDownload: () -> Unit = {},
    onShuffle: () -> Unit = {},
    onMore: () -> Unit = {},
    playingSongId: String? = null,
    modifier: Modifier = Modifier,
) {
    HeroDetailScaffold(
        artwork = artwork,
        title = album?.name.orEmpty(),
        onBack = onBack,
        header = { controlsModifier, topInset ->
            HeroHeader(
                art = coverArt(album?.coverArt, false),
                thumbnailRef = coverArt(album?.coverArt, true),
                name = album?.name.orEmpty(),
                subtitle =
                    listOfNotNull(
                        album?.albumArtist?.takeIf { it.isNotBlank() },
                        album?.year?.takeIf { it > 0 }?.toString(),
                    ).joinToString(" $DOT "),
                hasSongs = songs.isNotEmpty(),
                onPlay = { onSongClick(0) },
                onShuffle = onShuffle,
                onDownload = onDownload,
                onMore = onMore,
                topInset = topInset,
                controlsModifier = controlsModifier,
            )
        },
        content = { rowModifier ->
            itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                SongRow(
                    song = song,
                    isPlaying = song.id == playingSongId,
                    modifier = rowModifier.clickable { onSongClick(index) },
                )
            }
        },
        modifier = modifier,
    )
}
