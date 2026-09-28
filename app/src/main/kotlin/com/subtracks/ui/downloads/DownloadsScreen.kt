package com.subtracks.ui.downloads

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.data.model.AudioEncoding
import com.subtracks.data.model.DownloadList
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.DownloadedSong
import com.subtracks.ui.components.DeleteDownloadsDialog
import com.subtracks.ui.components.EmptyState
import org.koin.compose.viewmodel.koinViewModel

sealed interface DownloadDeleteTarget {
    data object All : DownloadDeleteTarget

    data class Node(
        val list: DownloadList,
        val refId: String,
        val name: String,
        val bytes: Long,
    ) : DownloadDeleteTarget

    data class Songs(
        val songIds: List<String>,
    ) : DownloadDeleteTarget
}

@Composable
fun DownloadsRoute(
    onBack: () -> Unit,
    viewModel: DownloadsViewModel = koinViewModel(),
) {
    val tree by viewModel.tree.collectAsStateWithLifecycle()
    var pending by remember { mutableStateOf<DownloadDeleteTarget?>(null) }
    DownloadsScreen(
        tree = tree,
        loadEncoding = viewModel::encoding,
        onBack = onBack,
        onDelete = { target ->
            when (target) {
                is DownloadDeleteTarget.Songs -> viewModel.deleteSongs(target.songIds)
                is DownloadDeleteTarget.All, is DownloadDeleteTarget.Node -> pending = target
            }
        },
    )
    when (val dialog = pending) {
        is DownloadDeleteTarget.All -> {
            DeleteDownloadsDialog(
                name = "all downloads",
                bytes = tree.bytes,
                onConfirm = viewModel::deleteAll,
                onDismiss = { pending = null },
            )
        }

        is DownloadDeleteTarget.Node -> {
            DeleteDownloadsDialog(
                name = dialog.name,
                bytes = dialog.bytes,
                onConfirm = { viewModel.delete(dialog.list, dialog.refId) },
                onDismiss = { pending = null },
            )
        }

        else -> {
            Unit
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    tree: DownloadTree,
    loadEncoding: suspend (DownloadedSong) -> AudioEncoding?,
    onBack: () -> Unit,
    onDelete: (DownloadDeleteTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(emptySet<String>()) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (tree.songs == 0) {
            EmptyState("No downloads yet.", Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                SummaryRow(tree, onDelete)
            }
            tree.artists.forEach { artist ->
                val artistKey = "artist:${artist.id}:${artist.name}"
                item(key = artistKey) {
                    NodeRow(
                        name = artist.name,
                        subtitle = "${artist.albums.size} albums · ${size(artist.bytes)}",
                        expanded = artistKey in expanded,
                        indent = 0,
                        onToggle = { expanded = expanded.toggle(artistKey) },
                        onDelete = {
                            onDelete(
                                if (artist.id.isBlank()) {
                                    DownloadDeleteTarget.Songs(artist.albums.flatMap { album -> album.songs.map { it.songId } })
                                } else {
                                    DownloadDeleteTarget.Node(DownloadList.Artist, artist.id, artist.name, artist.bytes)
                                },
                            )
                        },
                    )
                }
                if (artistKey in expanded) {
                    artist.albums.forEach { album ->
                        val albumKey = "album:$artistKey:${album.id}:${album.name}"
                        item(key = albumKey) {
                            NodeRow(
                                name = album.name,
                                subtitle = "${album.songs.size} songs · ${size(album.bytes)}",
                                expanded = albumKey in expanded,
                                indent = 1,
                                onToggle = { expanded = expanded.toggle(albumKey) },
                                onDelete = {
                                    onDelete(
                                        if (album.id.isBlank()) {
                                            DownloadDeleteTarget.Songs(album.songs.map { it.songId })
                                        } else {
                                            DownloadDeleteTarget.Node(DownloadList.Album, album.id, album.name, album.bytes)
                                        },
                                    )
                                },
                            )
                        }
                        if (albumKey in expanded) {
                            items(album.songs, key = { "$albumKey:${it.songId}" }) { song ->
                                SongRow(song, loadEncoding) { onDelete(DownloadDeleteTarget.Songs(listOf(song.songId))) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    tree: DownloadTree,
    onDelete: (DownloadDeleteTarget) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "${tree.songs} songs · ${size(tree.bytes)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = { onDelete(DownloadDeleteTarget.All) }) { Text("Delete all") }
    }
}

@Composable
private fun NodeRow(
    name: String,
    subtitle: String,
    expanded: Boolean,
    indent: Int,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingContent = {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                )
            }
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete downloads")
            }
        },
        modifier = Modifier.clickable(onClick = onToggle).padding(start = 16.dp * indent),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun SongRow(
    song: DownloadedSong,
    loadEncoding: suspend (DownloadedSong) -> AudioEncoding?,
    onDelete: () -> Unit,
) {
    var encoding by remember(song.songId) { mutableStateOf<AudioEncoding?>(null) }
    LaunchedEffect(song.songId, song.status) {
        encoding = if (song.status == DownloadStatus.Completed) loadEncoding(song) else null
    }
    ListItem(
        headlineContent = { Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text(songSubtitle(song, encoding), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete download")
            }
        },
        modifier = Modifier.padding(start = 32.dp),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun songSubtitle(
    song: DownloadedSong,
    encoding: AudioEncoding?,
): String {
    val size = size(song.size)
    return when (song.status) {
        DownloadStatus.Completed -> {
            listOfNotNull(
                encoding?.format,
                encoding?.bitrate?.takeIf { it > 0 }?.let { "${it / 1000} kbps" },
                size,
            ).joinToString(" · ")
        }

        DownloadStatus.Queued -> {
            "Queued · $size"
        }

        DownloadStatus.Running -> {
            "Downloading · $size"
        }

        DownloadStatus.Failed -> {
            "Failed · $size"
        }
    }
}

@Composable
private fun size(bytes: Long): String = Formatter.formatFileSize(LocalContext.current, bytes)

private fun Set<String>.toggle(key: String): Set<String> = if (key in this) this - key else this + key
