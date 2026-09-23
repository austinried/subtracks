package com.subtracks.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.data.model.Album
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.Song
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.ArtworkTheme
import com.subtracks.ui.theme.HeroGradient
import com.subtracks.ui.theme.rememberArtworkColors
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val ART_SIZE = 240.dp
private const val DOT = "\u00B7"

@Composable
fun AlbumDetailRoute(
    albumId: String,
    onBack: () -> Unit,
    viewModel: AlbumDetailViewModel = koinViewModel(key = albumId) { parametersOf(albumId) },
    playbackController: PlaybackController = koinInject(),
) {
    val album by viewModel.album.collectAsStateWithLifecycle(initialValue = null)
    val songs by viewModel.songs.collectAsStateWithLifecycle(initialValue = emptyList())
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val context = playback.context
    AlbumDetailScreen(
        album = album,
        songs = songs,
        coverArt = viewModel::coverArt,
        artwork = rememberArtworkColors(viewModel.coverArt(album?.coverArt, false)),
        onBack = onBack,
        onSongClick = viewModel::play,
        playingSongId = playback.item?.id.takeIf { context?.kind == QueueKind.Album && context.refId == albumId },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
    ArtworkTheme(artwork) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
            Box(modifier.fillMaxSize()) {
                HeroGradient(artwork, Modifier.fillMaxSize())
                Scaffold(
                    containerColor = Color.Transparent,
                    topBar = {
                        TopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        )
                    },
                ) { padding ->
                    LazyColumn(Modifier.padding(padding).fillMaxSize()) {
                        item {
                            AlbumHeader(
                                album = album,
                                hasSongs = songs.isNotEmpty(),
                                coverArt = coverArt,
                                onPlay = { onSongClick(0) },
                                onShuffle = onShuffle,
                                onDownload = onDownload,
                                onMore = onMore,
                            )
                        }
                        itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                            SongRow(
                                song = song,
                                isPlaying = song.id == playingSongId,
                                modifier = Modifier.clickable { onSongClick(index) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumHeader(
    album: Album?,
    hasSongs: Boolean,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    onDownload: () -> Unit,
    onMore: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CoverArt(
            ref = coverArt(album?.coverArt, false),
            name = album?.name.orEmpty(),
            modifier = Modifier.size(ART_SIZE).clip(RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = album?.name.orEmpty(),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val subtitle =
            listOfNotNull(
                album?.albumArtist?.takeIf { it.isNotBlank() },
                album?.year?.takeIf { it > 0 }?.toString(),
            ).joinToString(" $DOT ")
        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(20.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDownload) {
                Icon(Icons.Rounded.Download, contentDescription = "Download")
            }
            Button(onClick = onPlay, enabled = hasSongs) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Play")
            }
            IconButton(onClick = onShuffle, enabled = hasSongs) {
                Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle play")
            }
            IconButton(onClick = onMore) {
                Icon(Icons.Rounded.MoreHoriz, contentDescription = "More options")
            }
        }
    }
}
