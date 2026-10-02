package com.subtracks.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.subtracks.data.model.Song
import com.subtracks.data.source.StarType
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.ContextMenuHost
import com.subtracks.ui.components.ItemActions
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.library.AlbumsContent
import com.subtracks.ui.library.ArtistsContent
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
        genres = genres,
        decades = decades,
        coverArt = viewModel::coverArt,
        playingSongId = playback.item?.id,
        onBack = onBack,
        onAlbumClick = onAlbumClick,
        onArtistClick = onArtistClick,
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
    genres: List<String> = emptyList(),
    decades: List<Long> = emptyList(),
    coverArt: (String?, Boolean) -> CoverArtRef? = { _, _ -> null },
    playingSongId: String? = null,
    onBack: () -> Unit = {},
    onAlbumClick: (Album) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    onLongClick: (MenuTarget) -> Unit = {},
    onGenreClick: (String) -> Unit = {},
    onDecadeClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
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
                ArtistsContent(
                    items = artists,
                    coverArt = coverArt,
                    bottomInset = 0.dp,
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
                    AlbumsContent(
                        items = albums,
                        coverArt = coverArt,
                        bottomInset = 0.dp,
                        onAlbumClick = onAlbumClick,
                        onLongClick = onLongClick,
                        modifier = Modifier.padding(padding),
                    )
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
