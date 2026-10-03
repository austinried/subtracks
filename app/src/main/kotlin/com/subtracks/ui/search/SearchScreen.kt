package com.subtracks.ui.search

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.R
import com.subtracks.data.db.SEARCH_MIN_LENGTH
import com.subtracks.data.model.Album
import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.Song
import com.subtracks.data.source.StarType
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.ContextMenuHost
import com.subtracks.ui.components.EmptyState
import com.subtracks.ui.components.ItemActions
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.home.AlbumListRow
import com.subtracks.ui.home.ArtistListRow
import com.subtracks.ui.library.PlaylistRow
import com.subtracks.ui.library.SongRow
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onViewAlbum: (String) -> Unit,
    onViewArtist: (String) -> Unit,
    contextMenuHost: ContextMenuHost? = null,
    setStar: (StarType, String, Boolean) -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
    playbackController: PlaybackController = koinInject(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val actions =
        ItemActions(
            playNext = { playbackController.playNext(it.sourceId, it.kind, it.refId) },
            addToQueue = { playbackController.addToQueue(it.sourceId, it.kind, it.refId) },
            setStar = setStar,
            viewAlbum = onViewAlbum,
            viewArtist = onViewArtist,
        )
    SearchScreen(
        query = query,
        onQueryChange = viewModel::setQuery,
        results = results,
        coverArt = viewModel::coverArt,
        playingSongId = playback.item?.id,
        onBack = onBack,
        onAlbumClick = onAlbumClick,
        onArtistClick = onArtistClick,
        onPlaylistClick = onPlaylistClick,
        onSongClick = viewModel::play,
        onLongClick = { contextMenuHost?.show(it, actions) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    results: SearchResults,
    coverArt: (String?, Boolean) -> CoverArtRef? = { _, _ -> null },
    playingSongId: String? = null,
    onBack: () -> Unit = {},
    onAlbumClick: (Album) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    onLongClick: (MenuTarget) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val backLabel = stringResource(R.string.navigation_back)
    val clearLabel = stringResource(R.string.close_search)
    val hint = stringResource(R.string.search_input_placeholder)
    val minLength = stringResource(R.string.search_min_length, SEARCH_MIN_LENGTH)
    val songsLabel = stringResource(R.string.search_songs)
    val albumsLabel = stringResource(R.string.resources_album_name)
    val artistsLabel = stringResource(R.string.resources_artist_name)
    val playlistsLabel = stringResource(R.string.resources_playlist_name)
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = backLabel)
                    }
                },
                title = {
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        placeholder = { Text(hint) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = clearLabel)
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors =
                            TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .semantics { contentDescription = hint },
                    )
                },
            )
        },
    ) { padding ->
        when {
            !searchReady(query) -> {
                EmptyState(
                    text = minLength,
                    modifier = Modifier.padding(padding),
                )
            }

            results.loading -> {
                LoadingState(Modifier.padding(padding))
            }

            results.isEmpty -> {
                EmptyState(
                    text = stringResource(R.string.search_no_results, query),
                    modifier = Modifier.padding(padding),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    songResults(results.songs, songsLabel, coverArt, playingSongId, onSongClick, onLongClick)
                    albumResults(results.albums, albumsLabel, coverArt, onAlbumClick, onLongClick)
                    artistResults(results.artists, artistsLabel, coverArt, onArtistClick, onLongClick)
                    playlistResults(results.playlists, playlistsLabel, coverArt, onPlaylistClick, onLongClick)
                }
            }
        }
    }
}

private fun LazyListScope.resultHeader(title: String) {
    item(key = "search-header-$title") {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
        )
    }
}

private fun LazyListScope.songResults(
    songs: List<AlbumSongItem>,
    header: String,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    playingSongId: String?,
    onClick: (Song) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
) {
    if (songs.isEmpty()) return
    resultHeader(header)
    items(songs, key = { "song-${it.song.id}" }) { item ->
        SongRow(
            song = item.song,
            coverArtId = item.coverArt,
            coverArt = coverArt,
            isPlaying = item.song.id == playingSongId,
            durationSeconds = item.song.duration,
            modifier =
                Modifier.combinedClickable(
                    onClick = { onClick(item.song) },
                    onLongClick = { onLongClick(MenuTarget.Song(item.song, coverArt(item.coverArt, true))) },
                ),
        )
    }
}

private fun LazyListScope.albumResults(
    albums: List<Album>,
    header: String,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onClick: (Album) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
) {
    if (albums.isEmpty()) return
    resultHeader(header)
    items(albums, key = { "album-${it.id}" }) { album ->
        AlbumListRow(
            album = album,
            coverArt = coverArt,
            info = null,
            onClick = { onClick(album) },
            onLongClick = { onLongClick(MenuTarget.Album(album, coverArt(album.coverArt, true))) },
        )
    }
}

private fun LazyListScope.artistResults(
    artists: List<Artist>,
    header: String,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onClick: (Artist) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
) {
    if (artists.isEmpty()) return
    resultHeader(header)
    items(artists, key = { "artist-${it.id}" }) { artist ->
        ArtistListRow(
            artist = artist,
            coverArt = coverArt,
            info = null,
            onClick = { onClick(artist) },
            onLongClick = { onLongClick(MenuTarget.Artist(artist, coverArt(artist.coverArt, true))) },
        )
    }
}

private fun LazyListScope.playlistResults(
    playlists: List<Playlist>,
    header: String,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onClick: (Playlist) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
) {
    if (playlists.isEmpty()) return
    resultHeader(header)
    items(playlists, key = { "playlist-${it.id}" }) { playlist ->
        PlaylistRow(
            playlist = playlist,
            coverArt = coverArt,
            download = null,
            onClick = { onClick(playlist) },
            onLongClick = { onLongClick(MenuTarget.Playlist(playlist, coverArt(playlist.coverArt, true))) },
        )
    }
}
