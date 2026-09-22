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
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.subtracks.data.model.Album
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.Song
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.CoverArt
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
    onBack: () -> Unit,
    onSongClick: (Int) -> Unit,
    playingSongId: String? = null,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(album?.name.orEmpty(), maxLines = 1) },
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
                        ref = coverArt(album?.coverArt, false),
                        name = album?.name.orEmpty(),
                        modifier = Modifier.size(120.dp).clip(RoundedCornerShape(2.dp)),
                    )
                    Column {
                        Text(album?.name.orEmpty(), style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = album?.albumArtist.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = listOfNotNull(album?.year?.takeIf { it > 0 }, "${songs.size} songs").joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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
