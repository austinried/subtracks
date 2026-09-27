package com.subtracks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.SongDownload
import com.subtracks.data.source.StarType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.subtracks.data.model.Album as AlbumModel
import com.subtracks.data.model.Artist as ArtistModel
import com.subtracks.data.model.Playlist as PlaylistModel
import com.subtracks.data.model.Song as SongModel

sealed interface MenuTarget {
    val title: String
    val subtitle: String?
    val coverArt: CoverArtRef?

    data class Song(
        val song: SongModel,
        override val coverArt: CoverArtRef? = null,
        val download: SongDownload? = null,
    ) : MenuTarget {
        override val title: String get() = song.title

        override val subtitle: String? get() = song.artist
    }

    data class Album(
        val album: AlbumModel,
        override val coverArt: CoverArtRef? = null,
    ) : MenuTarget {
        override val title: String get() = album.name

        override val subtitle: String? get() = album.albumArtist
    }

    data class Artist(
        val artist: ArtistModel,
        override val coverArt: CoverArtRef? = null,
    ) : MenuTarget {
        override val title: String get() = artist.name

        override val subtitle: String? get() = null
    }

    data class Playlist(
        val playlist: PlaylistModel,
        override val coverArt: CoverArtRef? = null,
    ) : MenuTarget {
        override val title: String get() = playlist.name

        override val subtitle: String? get() = null
    }
}

data class QueueRef(
    val sourceId: Long,
    val kind: QueueKind,
    val refId: String,
)

fun MenuTarget.queueRef(): QueueRef? =
    when (this) {
        is MenuTarget.Song -> QueueRef(song.sourceId, QueueKind.Song, song.id)
        is MenuTarget.Album -> QueueRef(album.sourceId, QueueKind.Album, album.id)
        is MenuTarget.Playlist -> QueueRef(playlist.sourceId, QueueKind.Playlist, playlist.id)
        is MenuTarget.Artist -> null
    }

class ItemActions(
    val playSong: ((SongModel) -> Unit)? = null,
    val playAlbum: (AlbumModel) -> Unit = {},
    val shuffleAlbum: (AlbumModel) -> Unit = {},
    val playPlaylist: (PlaylistModel) -> Unit = {},
    val shufflePlaylist: (PlaylistModel) -> Unit = {},
    val playNext: ((QueueRef) -> Unit)? = null,
    val addToQueue: ((QueueRef) -> Unit)? = null,
    val download: ((SongModel) -> Unit)? = null,
    val cancelDownload: ((SongModel) -> Unit)? = null,
    val deleteDownload: ((SongModel) -> Unit)? = null,
    val setStar: (StarType, String, Boolean) -> Unit = { _, _, _ -> },
    val viewAlbum: ((String) -> Unit)? = null,
    val viewArtist: ((String) -> Unit)? = null,
)

class ContextMenuHost {
    var target by mutableStateOf<MenuTarget?>(null)
        private set
    var actions by mutableStateOf(ItemActions())
        private set

    fun show(
        target: MenuTarget,
        actions: ItemActions,
    ) {
        this.target = target
        this.actions = actions
    }

    fun dismiss() {
        target = null
        actions = ItemActions()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemContextMenu(
    target: MenuTarget,
    actions: ItemActions,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun dismiss(after: () -> Unit = {}) {
        after()
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    val queueRef = target.queueRef()

    @Composable
    fun queueItems() {
        if (queueRef == null) return
        actions.playNext?.let { next ->
            MenuItem(Icons.Rounded.SkipNext, "Play next") { dismiss { next(queueRef) } }
        }
        actions.addToQueue?.let { add ->
            MenuItem(Icons.AutoMirrored.Rounded.QueueMusic, "Add to queue") { dismiss { add(queueRef) } }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { dismiss() },
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        MenuHeader(target)
        when (target) {
            is MenuTarget.Song -> {
                actions.playSong?.let { play ->
                    MenuItem(Icons.Rounded.PlayArrow, "Play") { dismiss { play(target.song) } }
                }
                queueItems()
                DownloadItem(target.download, target.song, actions, ::dismiss)
                StarItem(
                    current = target.song.starred,
                    onSet = { starring -> actions.setStar(StarType.Song, target.song.id, starring) },
                    onDismiss = { dismiss() },
                )
                target.song.albumId?.let { albumId ->
                    actions.viewAlbum?.let { view ->
                        MenuItem(Icons.Rounded.Album, target.song.album?.takeIf { it.isNotBlank() } ?: "Album") {
                            dismiss { view(albumId) }
                        }
                    }
                }
                target.song.artistId?.let { artistId ->
                    actions.viewArtist?.let { view ->
                        MenuItem(Icons.Rounded.Person, target.song.artist?.takeIf { it.isNotBlank() } ?: "Artist") {
                            dismiss { view(artistId) }
                        }
                    }
                }
            }

            is MenuTarget.Album -> {
                MenuItem(Icons.Rounded.PlayArrow, "Play") { dismiss { actions.playAlbum(target.album) } }
                MenuItem(Icons.Rounded.Shuffle, "Shuffle") { dismiss { actions.shuffleAlbum(target.album) } }
                queueItems()
                StarItem(
                    current = target.album.starred,
                    onSet = { starring -> actions.setStar(StarType.Album, target.album.id, starring) },
                    onDismiss = { dismiss() },
                )
                target.album.artistId?.let { artistId ->
                    actions.viewArtist?.let { view ->
                        MenuItem(Icons.Rounded.Person, target.album.albumArtist?.takeIf { it.isNotBlank() } ?: "Artist") {
                            dismiss { view(artistId) }
                        }
                    }
                }
            }

            is MenuTarget.Artist -> {
                StarItem(
                    current = target.artist.starred,
                    onSet = { starring -> actions.setStar(StarType.Artist, target.artist.id, starring) },
                    onDismiss = { dismiss() },
                )
            }

            is MenuTarget.Playlist -> {
                MenuItem(Icons.Rounded.PlayArrow, "Play") { dismiss { actions.playPlaylist(target.playlist) } }
                MenuItem(Icons.Rounded.Shuffle, "Shuffle") { dismiss { actions.shufflePlaylist(target.playlist) } }
                queueItems()
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MenuHeader(target: MenuTarget) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverArt(
            ref = target.coverArt,
            name = target.title,
            modifier = Modifier.size(48.dp).clip(if (target is MenuTarget.Artist) CircleShape else RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = target.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            target.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DownloadItem(
    download: SongDownload?,
    song: SongModel,
    actions: ItemActions,
    dismiss: (() -> Unit) -> Unit,
) {
    when (download?.status) {
        DownloadStatus.Completed -> {
            actions.deleteDownload?.let { delete ->
                MenuItem(Icons.Rounded.Delete, "Delete download") { dismiss { delete(song) } }
            }
        }

        DownloadStatus.Queued, DownloadStatus.Running -> {
            actions.cancelDownload?.let { cancel ->
                MenuItem(Icons.Rounded.Cancel, "Cancel download") { dismiss { cancel(song) } }
            }
        }

        DownloadStatus.Failed -> {
            actions.download?.let { start ->
                MenuItem(Icons.Rounded.Download, "Retry download") { dismiss { start(song) } }
            }
        }

        null -> {
            actions.download?.let { start ->
                MenuItem(Icons.Rounded.Download, "Download") { dismiss { start(song) } }
            }
        }
    }
}

@Composable
private fun StarItem(
    current: Long?,
    onSet: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var starred by remember(current) { mutableStateOf(current) }
    val scope = rememberCoroutineScope()
    MenuItem(
        starIcon(starred),
        starLabel(starred),
        tint = if (starred == null) null else MaterialTheme.colorScheme.primary,
    ) {
        val starring = starred == null
        starred = if (starring) System.currentTimeMillis() else null
        onSet(starring)
        scope.launch {
            delay(STAR_DISMISS_DELAY_MS)
            onDismiss()
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    label: String,
    tint: Color? = null,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null, tint = tint ?: LocalContentColor.current) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

private fun starIcon(starred: Long?): ImageVector = if (starred == null) Icons.Rounded.StarBorder else Icons.Rounded.Star

private fun starLabel(starred: Long?): String = if (starred == null) "Star" else "Unstar"

private const val STAR_DISMISS_DELAY_MS = 400L
