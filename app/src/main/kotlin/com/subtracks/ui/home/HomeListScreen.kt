package com.subtracks.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.subtracks.data.model.Album
import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.Song
import com.subtracks.data.source.StarType
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.ContextMenuHost
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.components.ItemActions
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.library.PlaylistsContent
import com.subtracks.ui.library.SongRow
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun HomeListRoute(
    request: HomeListRequest,
    onBack: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit = {},
    onViewAlbum: (String) -> Unit,
    onViewArtist: (String) -> Unit,
    onGenreClick: (String) -> Unit,
    onDecadeClick: (Long) -> Unit,
    contextMenuHost: ContextMenuHost? = null,
    setStar: (StarType, String, Boolean) -> Unit,
    viewModel: HomeListViewModel = koinViewModel { parametersOf(request) },
    playbackController: PlaybackController = koinInject(),
) {
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val genresState = if (request.section == HomeSection.Genres) viewModel.genres.collectAsStateWithLifecycle() else null
    val genres = genresState?.value.orEmpty()
    val decadesState = if (request.section == HomeSection.Decades) viewModel.decades.collectAsStateWithLifecycle() else null
    val decades = decadesState?.value.orEmpty()
    val actions =
        ItemActions(
            playNext = { playbackController.playNext(it.sourceId, it.kind, it.refId) },
            addToQueue = { playbackController.addToQueue(it.sourceId, it.kind, it.refId) },
            setStar = setStar,
            viewAlbum = onViewAlbum,
            viewArtist = onViewArtist,
        )
    HomeListScreen(
        request = request,
        albums = viewModel.albums.collectAsLazyPagingItems(),
        artists = viewModel.artists.collectAsLazyPagingItems(),
        songs = viewModel.songs.collectAsLazyPagingItems(),
        playlists = viewModel.playlists.collectAsLazyPagingItems(),
        genres = genres,
        decades = decades,
        coverArt = viewModel::coverArt,
        playingSongId = playback.item?.id,
        onBack = onBack,
        onAlbumClick = onAlbumClick,
        onArtistClick = onArtistClick,
        onPlaylistClick = onPlaylistClick,
        onSongClick = viewModel::play,
        onLongClick = { contextMenuHost?.show(it, actions) },
        onGenreClick = onGenreClick,
        onDecadeClick = onDecadeClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeListScreen(
    request: HomeListRequest,
    albums: LazyPagingItems<Album>,
    artists: LazyPagingItems<Artist>,
    songs: LazyPagingItems<AlbumSongItem>,
    playlists: LazyPagingItems<Playlist>,
    genres: List<String> = emptyList(),
    decades: List<Long> = emptyList(),
    coverArt: (String?, Boolean) -> CoverArtRef? = { _, _ -> null },
    playingSongId: String? = null,
    onBack: () -> Unit = {},
    onAlbumClick: (Album) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    onLongClick: (MenuTarget) -> Unit = {},
    onGenreClick: (String) -> Unit = {},
    onDecadeClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val now = remember { System.currentTimeMillis() }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(request.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        val downloaded = request.downloaded
        if (downloaded != null) {
            when (downloaded) {
                OfflineListKind.Albums -> {
                    AlbumsList(
                        albums = albums,
                        coverArt = coverArt,
                        info = { albumListInfo(request, it, now) },
                        onAlbumClick = onAlbumClick,
                        onLongClick = onLongClick,
                        modifier = Modifier.padding(padding),
                    )
                }

                OfflineListKind.Artists -> {
                    ArtistsList(
                        artists = artists,
                        coverArt = coverArt,
                        info = { artistListInfo(request, it, now) },
                        onArtistClick = onArtistClick,
                        onLongClick = onLongClick,
                        modifier = Modifier.padding(padding),
                    )
                }

                OfflineListKind.Songs -> {
                    SongsList(
                        songs = songs,
                        coverArt = coverArt,
                        playingSongId = playingSongId,
                        onSongClick = onSongClick,
                        onLongClick = onLongClick,
                        modifier = Modifier.padding(padding),
                    )
                }

                OfflineListKind.Playlists -> {
                    PlaylistsContent(
                        items = playlists,
                        coverArt = coverArt,
                        bottomInset = 0.dp,
                        onPlaylistClick = onPlaylistClick,
                        onLongClick = onLongClick,
                        modifier = Modifier.padding(padding),
                    )
                }
            }
        } else {
            when (request.section) {
                HomeSection.Genres -> {
                    PlainList(
                        items = genres,
                        label = { it },
                        onClick = onGenreClick,
                        modifier = Modifier.padding(padding),
                    )
                }

                HomeSection.Decades -> {
                    PlainList(
                        items = decades,
                        label = { "${it}s" },
                        onClick = onDecadeClick,
                        modifier = Modifier.padding(padding),
                    )
                }

                HomeSection.RecentlyPlayedArtists, HomeSection.MostPlayedArtists -> {
                    ArtistsList(
                        artists = artists,
                        coverArt = coverArt,
                        info = { artistListInfo(request, it, now) },
                        onArtistClick = onArtistClick,
                        onLongClick = onLongClick,
                        modifier = Modifier.padding(padding),
                    )
                }

                HomeSection.RecentlyStarredSongs -> {
                    SongsList(
                        songs = songs,
                        coverArt = coverArt,
                        playingSongId = playingSongId,
                        onSongClick = onSongClick,
                        onLongClick = onLongClick,
                        modifier = Modifier.padding(padding),
                    )
                }

                HomeSection.RecentlyPlayedAlbums,
                HomeSection.MostPlayedAlbums,
                HomeSection.RecentlyAddedAlbums,
                HomeSection.Rediscover,
                null,
                -> {
                    if (request.genre != null) {
                        SongsList(
                            songs = songs,
                            coverArt = coverArt,
                            playingSongId = playingSongId,
                            onSongClick = onSongClick,
                            onLongClick = onLongClick,
                            modifier = Modifier.padding(padding),
                        )
                    } else {
                        AlbumsList(
                            albums = albums,
                            coverArt = coverArt,
                            info = { albumListInfo(request, it, now) },
                            onAlbumClick = onAlbumClick,
                            onLongClick = onLongClick,
                            modifier = Modifier.padding(padding),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> PlainList(
    items: List<T>,
    label: (T) -> String,
    onClick: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(items) { item ->
            ListItem(
                headlineContent = { Text(label(item)) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { onClick(item) },
            )
        }
    }
}

@Composable
private fun SongsList(
    songs: LazyPagingItems<AlbumSongItem>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    playingSongId: String?,
    onSongClick: (Song) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (songs.itemCount == 0 && songs.loadState.refresh is LoadState.Loading) {
        LoadingState(modifier)
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        items(count = songs.itemCount, key = songs.itemKey { it.song.id }) { index ->
            val item = songs[index]
            if (item != null) {
                SongRow(
                    song = item.song,
                    coverArtId = item.coverArt,
                    coverArt = coverArt,
                    isPlaying = item.song.id == playingSongId,
                    durationSeconds = item.song.duration,
                    modifier =
                        Modifier.combinedClickable(
                            onClick = { onSongClick(item.song) },
                            onLongClick = { onLongClick(MenuTarget.Song(item.song, coverArt(item.coverArt, true))) },
                        ),
                )
            }
        }
    }
}

private val ALBUM_ROW_COVER = 48.dp

@Composable
private fun AlbumsList(
    albums: LazyPagingItems<Album>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    info: (Album) -> String?,
    onAlbumClick: (Album) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (albums.itemCount == 0 && albums.loadState.refresh is LoadState.Loading) {
        LoadingState(modifier)
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        items(count = albums.itemCount, key = albums.itemKey { it.id }) { index ->
            val album = albums[index]
            if (album != null) {
                AlbumListRow(
                    album = album,
                    coverArt = coverArt,
                    info = info(album),
                    onClick = { onAlbumClick(album) },
                    onLongClick = { onLongClick(MenuTarget.Album(album, coverArt(album.coverArt, true))) },
                )
            }
        }
    }
}

@Composable
private fun AlbumListRow(
    album: Album,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    info: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverArt(
            ref = coverArt(album.coverArt, true),
            name = album.name,
            modifier = Modifier.size(ALBUM_ROW_COVER).clip(RoundedCornerShape(2.dp)),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = album.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = album.albumArtist.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (info != null) {
                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistsList(
    artists: LazyPagingItems<Artist>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    info: (Artist) -> String?,
    onArtistClick: (Artist) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (artists.itemCount == 0 && artists.loadState.refresh is LoadState.Loading) {
        LoadingState(modifier)
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        items(count = artists.itemCount, key = artists.itemKey { it.id }) { index ->
            val artist = artists[index]
            if (artist != null) {
                ArtistListRow(
                    artist = artist,
                    coverArt = coverArt,
                    info = info(artist),
                    onClick = { onArtistClick(artist) },
                    onLongClick = { onLongClick(MenuTarget.Artist(artist, coverArt(artist.coverArt, true))) },
                )
            }
        }
    }
}

@Composable
private fun ArtistListRow(
    artist: Artist,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    info: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoverArt(
            ref = coverArt(artist.coverArt, true),
            name = artist.name,
            modifier = Modifier.size(ALBUM_ROW_COVER).clip(CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${artist.albumCount} ${if (artist.albumCount == 1L) "album" else "albums"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                if (info != null) {
                    Text(
                        text = info,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

internal fun artistListInfo(
    request: HomeListRequest,
    artist: Artist,
    now: Long,
): String? =
    when (request.section) {
        HomeSection.RecentlyPlayedArtists -> artist.played?.let { relativeTimeLabel(it, now) }
        HomeSection.MostPlayedArtists -> playCountLabel(artist.playCount)
        else -> null
    }

internal fun albumListInfo(
    request: HomeListRequest,
    album: Album,
    now: Long,
): String? =
    when {
        request.section == HomeSection.RecentlyAddedAlbums -> {
            relativeTimeLabel(album.created, now)
        }

        request.section == HomeSection.RecentlyPlayedAlbums || request.section == HomeSection.Rediscover -> {
            album.played?.let { relativeTimeLabel(it, now) }
        }

        request.section == HomeSection.MostPlayedAlbums -> {
            playCountLabel(album.playCount)
        }

        request.decade != null -> {
            album.year?.takeIf { it > 0 }?.toString()
        }

        else -> {
            null
        }
    }

private fun playCountLabel(count: Long): String? = if (count <= 0L) null else "$count ${if (count == 1L) "play" else "plays"}"

internal fun relativeTimeLabel(
    epochSeconds: Long,
    nowMillis: Long,
): String? {
    if (epochSeconds <= 0L) return null
    val elapsedMillis = nowMillis - epochSeconds * 1000L
    if (elapsedMillis < 0L) return "Just now"
    val days = elapsedMillis / 86_400_000L
    return when {
        elapsedMillis < 60_000L -> "Just now"
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days < 7L -> "$days days ago"
        days < 14L -> "Last week"
        days < 30L -> "${days / 7L} weeks ago"
        days < 60L -> "Last month"
        days < 365L -> "${days / 30L} months ago"
        days < 730L -> "Last year"
        else -> "${days / 365L} years ago"
    }
}
