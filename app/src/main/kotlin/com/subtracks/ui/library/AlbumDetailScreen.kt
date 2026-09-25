package com.subtracks.ui.library

import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.data.model.Album
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Disc
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
    val discs by viewModel.discs.collectAsStateWithLifecycle(initialValue = emptyList())
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val context = playback.context
    val shortcutArt = remember(coverArtId) { coverArtId?.let { viewModel.coverArt(it, true) } }
    AlbumDetailScreen(
        album = album,
        songs = songs,
        discs = discs,
        coverArt = viewModel::coverArt,
        artwork = rememberArtworkColors(shortcutArt ?: viewModel.coverArt(album?.coverArt, true), THEME_TRANSITION_MS),
        onBack = onBack,
        onSongClick = viewModel::play,
        onShuffle = viewModel::shuffle,
        onPlay = viewModel::playAll,
        playingSongId = playback.item?.id.takeIf { context?.kind == QueueKind.Album && context.refId == albumId },
    )
}

@Composable
fun AlbumDetailScreen(
    album: Album?,
    songs: List<Song>,
    discs: List<Disc> = emptyList(),
    coverArt: (String?, Boolean) -> CoverArtRef?,
    artwork: ArtworkColors?,
    onBack: () -> Unit,
    onSongClick: (Int) -> Unit,
    onShuffle: () -> Unit = {},
    onPlay: () -> Unit = { onSongClick(0) },
    onDownload: () -> Unit = {},
    onMore: () -> Unit = {},
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
                onPlay = onPlay,
                onShuffle = onShuffle,
                onDownload = onDownload,
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
                    item(key = "disc:$disc") { DiscHeader(discLabel ?: "Disc $disc") }
                }
                lastDisc = disc
                item(key = song.id) {
                    SongRow(
                        song = song,
                        isPlaying = song.id == playingSongId,
                        trackNumber = song.track,
                        modifier = rowModifier.clickable { onSongClick(index) },
                    )
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun DiscHeader(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 28.dp, bottom = 4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.FiberSmartRecord,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
