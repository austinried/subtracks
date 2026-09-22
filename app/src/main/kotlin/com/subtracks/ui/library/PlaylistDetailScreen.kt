package com.subtracks.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.SongListItem
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.CoverArt
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
        onBack = onBack,
        onSongClick = viewModel::play,
        playingSongId = playback.item?.id.takeIf { context?.kind == QueueKind.Playlist && context.refId == playlistId },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlist: Playlist?,
    songs: LazyPagingItems<SongListItem>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onBack: () -> Unit,
    onSongClick: (Int) -> Unit,
    playingSongId: String? = null,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(playlist?.name.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CoverArt(
                        ref = coverArt(playlist?.coverArt, false),
                        name = playlist?.name.orEmpty(),
                        modifier = Modifier.size(120.dp).clip(RoundedCornerShape(2.dp)),
                    )
                    Column {
                        Text(playlist?.name.orEmpty(), style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${playlist?.songCount ?: 0} songs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            items(count = songs.itemCount, key = songs.itemKey { it.song.id }) { index ->
                val item = songs[index]
                if (item != null) {
                    SongRow(
                        song = item.song,
                        coverArtId = item.coverArt,
                        coverArt = coverArt,
                        isPlaying = item.song.id == playingSongId,
                        modifier = Modifier.clickable { onSongClick(index) },
                    )
                }
            }
        }
    }
}
