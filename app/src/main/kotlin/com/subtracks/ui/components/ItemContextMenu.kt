package com.subtracks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.subtracks.data.source.StarType
import com.subtracks.data.model.Album as AlbumModel
import com.subtracks.data.model.Artist as ArtistModel
import com.subtracks.data.model.Playlist as PlaylistModel
import com.subtracks.data.model.Song as SongModel

sealed interface MenuTarget {
    val title: String
    val subtitle: String?

    data class Song(
        val song: SongModel,
    ) : MenuTarget {
        override val title: String get() = song.title

        override val subtitle: String? get() = song.artist
    }

    data class Album(
        val album: AlbumModel,
    ) : MenuTarget {
        override val title: String get() = album.name

        override val subtitle: String? get() = album.albumArtist
    }

    data class Artist(
        val artist: ArtistModel,
    ) : MenuTarget {
        override val title: String get() = artist.name

        override val subtitle: String? get() = null
    }

    data class Playlist(
        val playlist: PlaylistModel,
    ) : MenuTarget {
        override val title: String get() = playlist.name

        override val subtitle: String? get() = null
    }
}

class ItemActions(
    val playSong: ((SongModel) -> Unit)? = null,
    val playAlbum: (AlbumModel) -> Unit = {},
    val shuffleAlbum: (AlbumModel) -> Unit = {},
    val playPlaylist: (PlaylistModel) -> Unit = {},
    val shufflePlaylist: (PlaylistModel) -> Unit = {},
    val setStar: (StarType, String, Boolean) -> Unit = { _, _, _ -> },
    val viewAlbum: ((String) -> Unit)? = null,
    val viewArtist: ((String) -> Unit)? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemContextMenu(
    target: MenuTarget,
    actions: ItemActions,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        val dismissThen: (() -> Unit) -> Unit = { action ->
            onDismiss()
            action()
        }
        MenuHeader(target)
        when (target) {
            is MenuTarget.Song -> {
                actions.playSong?.let { play ->
                    MenuItem(Icons.Rounded.PlayArrow, "Play") { dismissThen { play(target.song) } }
                }
                MenuItem(starIcon(target.song.starred), starLabel(target.song.starred)) {
                    dismissThen { actions.setStar(StarType.Song, target.song.id, target.song.starred == null) }
                }
                target.song.albumId?.let { albumId ->
                    actions.viewAlbum?.let { view ->
                        MenuItem(Icons.Rounded.Album, "View album") { dismissThen { view(albumId) } }
                    }
                }
                target.song.artistId?.let { artistId ->
                    actions.viewArtist?.let { view ->
                        MenuItem(Icons.Rounded.Person, "View artist") { dismissThen { view(artistId) } }
                    }
                }
            }

            is MenuTarget.Album -> {
                MenuItem(Icons.Rounded.PlayArrow, "Play") { dismissThen { actions.playAlbum(target.album) } }
                MenuItem(Icons.Rounded.Shuffle, "Shuffle") { dismissThen { actions.shuffleAlbum(target.album) } }
                MenuItem(starIcon(target.album.starred), starLabel(target.album.starred)) {
                    dismissThen { actions.setStar(StarType.Album, target.album.id, target.album.starred == null) }
                }
                target.album.artistId?.let { artistId ->
                    actions.viewArtist?.let { view ->
                        MenuItem(Icons.Rounded.Person, "View artist") { dismissThen { view(artistId) } }
                    }
                }
            }

            is MenuTarget.Artist -> {
                MenuItem(starIcon(target.artist.starred), starLabel(target.artist.starred)) {
                    dismissThen { actions.setStar(StarType.Artist, target.artist.id, target.artist.starred == null) }
                }
            }

            is MenuTarget.Playlist -> {
                MenuItem(Icons.Rounded.PlayArrow, "Play") { dismissThen { actions.playPlaylist(target.playlist) } }
                MenuItem(Icons.Rounded.Shuffle, "Shuffle") { dismissThen { actions.shufflePlaylist(target.playlist) } }
            }
        }
    }
}

@Composable
private fun MenuHeader(target: MenuTarget) {
    ListItem(
        headlineContent = {
            Text(
                text = target.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent =
            target.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                {
                    Text(
                        text = subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

private fun starIcon(starred: Long?): ImageVector = if (starred == null) Icons.Rounded.StarBorder else Icons.Rounded.Star

private fun starLabel(starred: Long?): String = if (starred == null) "Star" else "Unstar"
