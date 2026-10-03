package com.subtracks.ui.home

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LibraryAdd
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.subtracks.R
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
import com.subtracks.ui.components.EmptyState
import com.subtracks.ui.components.ItemActions
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.library.SongRow
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

enum class HomeSection(
    val title: String,
) {
    RecentlyPlayedAlbums("Recently played"),
    RecentlyPlayedArtists("Recently played artists"),
    MostPlayedAlbums("Most played"),
    MostPlayedArtists("Most played artists"),
    Genres("Genres"),
    Decades("Decades"),
    RecentlyStarredSongs("Recently starred"),
    RecentlyAddedAlbums("Recently added"),
    Rediscover("Rediscover"),
    ;

    companion object {
        fun fromRoute(value: String?): HomeSection? = entries.firstOrNull { it.name == value }
    }
}

@Composable
fun HomeRoute(
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onViewAlbum: (String) -> Unit,
    onViewArtist: (String) -> Unit,
    onMore: (HomeSection) -> Unit,
    onDownloadedMore: (OfflineListKind) -> Unit,
    onGenreClick: (String) -> Unit,
    onDecadeClick: (Long) -> Unit,
    contextMenuHost: ContextMenuHost? = null,
    setStar: (StarType, String, Boolean) -> Unit,
    topInset: Dp,
    bottomInset: Dp,
    viewModel: HomeViewModel = koinViewModel(),
    playbackController: PlaybackController = koinInject(),
) {
    val offline by viewModel.offline.collectAsStateWithLifecycle()
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val actions =
        ItemActions(
            playNext = { playbackController.playNext(it.sourceId, it.kind, it.refId) },
            addToQueue = { playbackController.addToQueue(it.sourceId, it.kind, it.refId) },
            setStar = setStar,
            viewAlbum = onViewAlbum,
            viewArtist = onViewArtist,
        )
    val onLongClick: (MenuTarget) -> Unit = { contextMenuHost?.show(it, actions) }
    if (offline) {
        OfflineHomeScreen(
            albums = viewModel.downloadedAlbums.collectAsLazyPagingItems(),
            artists = viewModel.downloadedArtists.collectAsLazyPagingItems(),
            playlists = viewModel.downloadedPlaylists.collectAsLazyPagingItems(),
            songs = viewModel.downloadedSongs.collectAsStateWithLifecycle(initialValue = emptyList()).value,
            coverArt = viewModel::coverArt,
            playingSongId = playback.item?.id,
            topInset = topInset,
            bottomInset = bottomInset,
            onAlbumClick = onAlbumClick,
            onArtistClick = onArtistClick,
            onPlaylistClick = onPlaylistClick,
            onSongPlay = viewModel::playDownloaded,
            onLongClick = onLongClick,
            onMore = onDownloadedMore,
        )
    } else {
        val feed by viewModel.feed.collectAsStateWithLifecycle()
        HomeScreen(
            feed = feed,
            coverArt = viewModel::coverArt,
            playingSongId = playback.item?.id,
            topInset = topInset,
            bottomInset = bottomInset,
            onAlbumClick = onAlbumClick,
            onArtistClick = onArtistClick,
            onPlayStarred = viewModel::playStarred,
            onLongClick = onLongClick,
            onMore = onMore,
            onGenreClick = onGenreClick,
            onDecadeClick = onDecadeClick,
            onSync = viewModel::sync,
        )
    }
}

@Composable
fun HomeScreen(
    feed: HomeFeed,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    playingSongId: String?,
    topInset: Dp = 0.dp,
    bottomInset: Dp = 0.dp,
    onAlbumClick: (Album) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onPlayStarred: (Song) -> Unit = {},
    onLongClick: (MenuTarget) -> Unit = {},
    onMore: (HomeSection) -> Unit = {},
    onGenreClick: (String) -> Unit = {},
    onDecadeClick: (Long) -> Unit = {},
    onSync: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        if (feed.loading) {
            LoadingState(modifier)
            return@CompositionLocalProvider
        }
        if (feed.isEmpty()) {
            EmptyState(
                text = "Nothing to show yet.\nSync with your server to fill your library.",
                modifier = modifier,
                actionLabel = "Sync",
                onAction = onSync,
            )
            return@CompositionLocalProvider
        }
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = topInset + 8.dp, bottom = bottomInset + 24.dp),
        ) {
            homeTitle()
            albumRow(
                HomeSection.RecentlyAddedAlbums,
                feed.recentlyAddedAlbums,
                coverArt,
                onAlbumClick,
                onLongClick,
                onMore,
            )
            artistRow(
                HomeSection.RecentlyPlayedArtists,
                feed.recentlyPlayedArtists,
                coverArt,
                onArtistClick,
                onLongClick,
                onMore,
            )
            homeSongs(
                HomeSection.RecentlyStarredSongs,
                feed.recentlyStarredSongs,
                coverArt,
                playingSongId,
                onPlayStarred,
                onLongClick,
                onMore,
            )
            albumRow(
                HomeSection.MostPlayedAlbums,
                feed.mostPlayedAlbums,
                coverArt,
                onAlbumClick,
                onLongClick,
                onMore,
            )
            genreBlock(HomeSection.Genres, feed.genres, onGenreClick, onMore)
            albumRow(
                HomeSection.RecentlyPlayedAlbums,
                feed.recentlyPlayedAlbums,
                coverArt,
                onAlbumClick,
                onLongClick,
                onMore,
            )
            artistRow(
                HomeSection.MostPlayedArtists,
                feed.mostPlayedArtists,
                coverArt,
                onArtistClick,
                onLongClick,
                onMore,
            )
            decadeRow(HomeSection.Decades, feed.decades, onDecadeClick, onMore)
            albumRow(
                HomeSection.Rediscover,
                feed.rediscoverAlbums,
                coverArt,
                onAlbumClick,
                onLongClick,
                onMore,
            )
        }
    }
}

private fun LazyListScope.homeTitle() {
    item(key = "home-title") {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_subtracks_logo),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(width = 34.dp, height = 25.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(text = "subtracks", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
fun OfflineHomeScreen(
    albums: LazyPagingItems<Album>,
    artists: LazyPagingItems<Artist>,
    playlists: LazyPagingItems<Playlist>,
    songs: List<AlbumSongItem>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    playingSongId: String?,
    topInset: Dp = 0.dp,
    bottomInset: Dp = 0.dp,
    onAlbumClick: (Album) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onSongPlay: (Song) -> Unit = {},
    onLongClick: (MenuTarget) -> Unit = {},
    onMore: (OfflineListKind) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = topInset + 8.dp, bottom = bottomInset + 24.dp),
        ) {
            homeTitle()
            downloadedAlbumRow(albums, coverArt, onAlbumClick, onLongClick) { onMore(OfflineListKind.Albums) }
            downloadedArtistRow(artists, coverArt, onArtistClick, onLongClick) { onMore(OfflineListKind.Artists) }
            downloadedPlaylistRow(playlists, coverArt, onPlaylistClick, onLongClick) { onMore(OfflineListKind.Playlists) }
            songList(
                key = "downloaded-songs",
                title = "Downloaded songs",
                icon = Icons.Rounded.MusicNote,
                songs = songs,
                coverArt = coverArt,
                playingSongId = playingSongId,
                onSongPlay = onSongPlay,
                onLongClick = onLongClick,
                onMore = { onMore(OfflineListKind.Songs) },
            )
        }
    }
}

private fun LazyListScope.downloadedAlbumRow(
    albums: LazyPagingItems<Album>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onAlbumClick: (Album) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
    onMore: () -> Unit,
) {
    if (albums.itemCount == 0) return
    item(key = "downloaded-albums-header") {
        HomeSectionHeader("Downloaded albums", Icons.Rounded.Album) { onMore() }
    }
    item(key = "downloaded-albums-row") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(TILE_SPACING),
            modifier = Modifier.padding(top = SECTION_CONTENT_TOP),
        ) {
            items(count = albums.itemCount, key = albums.itemKey { it.id }) { index ->
                val album = albums[index]
                if (album != null) {
                    val ref = coverArt(album.coverArt, true)
                    AlbumTile(
                        album = album,
                        ref = ref,
                        onClick = { onAlbumClick(album) },
                        onLongClick = { onLongClick(MenuTarget.Album(album, ref)) },
                    )
                }
            }
        }
    }
}

private fun LazyListScope.downloadedArtistRow(
    artists: LazyPagingItems<Artist>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onArtistClick: (Artist) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
    onMore: () -> Unit,
) {
    if (artists.itemCount == 0) return
    item(key = "downloaded-artists-header") {
        HomeSectionHeader("Downloaded artists", Icons.Rounded.Person) { onMore() }
    }
    item(key = "downloaded-artists-row") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(ARTIST_SPACING),
            modifier = Modifier.padding(top = SECTION_CONTENT_TOP),
        ) {
            items(count = artists.itemCount, key = artists.itemKey { it.id }) { index ->
                val artist = artists[index]
                if (artist != null) {
                    val ref = coverArt(artist.coverArt, true)
                    ArtistTile(
                        artist = artist,
                        ref = ref,
                        onClick = { onArtistClick(artist) },
                        onLongClick = { onLongClick(MenuTarget.Artist(artist, ref)) },
                    )
                }
            }
        }
    }
}

private fun LazyListScope.downloadedPlaylistRow(
    playlists: LazyPagingItems<Playlist>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onPlaylistClick: (Playlist) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
    onMore: () -> Unit,
) {
    if (playlists.itemCount == 0) return
    item(key = "downloaded-playlists-header") {
        HomeSectionHeader("Downloaded playlists", Icons.AutoMirrored.Rounded.PlaylistPlay) { onMore() }
    }
    item(key = "downloaded-playlists-row") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(TILE_SPACING),
            modifier = Modifier.padding(top = SECTION_CONTENT_TOP),
        ) {
            items(count = playlists.itemCount, key = playlists.itemKey { it.id }) { index ->
                val playlist = playlists[index]
                if (playlist != null) {
                    val ref = coverArt(playlist.coverArt, true)
                    PlaylistTile(
                        playlist = playlist,
                        ref = ref,
                        onClick = { onPlaylistClick(playlist) },
                        onLongClick = { onLongClick(MenuTarget.Playlist(playlist, ref)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistTile(
    playlist: Playlist,
    ref: CoverArtRef?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Column(
        modifier = Modifier.width(TILE_SIZE).combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        CoverArt(
            ref = ref,
            name = playlist.name,
            modifier = Modifier.size(TILE_SIZE).clip(RoundedCornerShape(2.dp)),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun HomeFeed.isEmpty(): Boolean =
    recentlyPlayedAlbums.isEmpty() &&
        recentlyPlayedArtists.isEmpty() &&
        mostPlayedAlbums.isEmpty() &&
        mostPlayedArtists.isEmpty() &&
        genres.isEmpty() &&
        decades.isEmpty() &&
        recentlyStarredSongs.isEmpty() &&
        recentlyAddedAlbums.isEmpty() &&
        rediscoverAlbums.isEmpty()

@Composable
private fun HomeSectionHeader(
    title: String,
    icon: ImageVector,
    onMore: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 24.dp, bottom = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        if (onMore != null) {
            TextButton(onClick = onMore) {
                Text("More")
            }
        }
    }
}

private val HomeSection.icon: ImageVector
    get() =
        when (this) {
            HomeSection.RecentlyPlayedAlbums -> Icons.Rounded.History
            HomeSection.RecentlyPlayedArtists -> Icons.Rounded.Groups
            HomeSection.MostPlayedAlbums -> Icons.AutoMirrored.Rounded.TrendingUp
            HomeSection.MostPlayedArtists -> Icons.Rounded.LocalFireDepartment
            HomeSection.Genres -> Icons.Rounded.Category
            HomeSection.Decades -> Icons.Rounded.CalendarMonth
            HomeSection.RecentlyStarredSongs -> Icons.Rounded.Star
            HomeSection.RecentlyAddedAlbums -> Icons.Rounded.LibraryAdd
            HomeSection.Rediscover -> Icons.Rounded.Replay
        }

private val TILE_SIZE = 132.dp
private val ARTIST_TILE_SIZE = 104.dp
private val TILE_SPACING = 8.dp
private val ARTIST_SPACING = 16.dp
private val SECTION_CONTENT_TOP = 4.dp
private val GENRE_ROW_SPACING = 2.dp
private const val GENRE_ROWS = 3
private const val GENRE_CHIP_WIDTH = 6

internal fun brickRows(
    genres: List<String>,
    rows: Int,
): List<List<String>> {
    if (rows < 2) return listOf(genres)
    val balanced = List(rows) { mutableListOf<String>() }
    val widths = LongArray(rows)
    genres.forEach { genre ->
        val shortest = widths.indices.minBy { widths[it] }
        balanced[shortest] += genre
        widths[shortest] += genre.length + GENRE_CHIP_WIDTH
    }
    return balanced
}

private const val REVEAL_NEAR_FRONT = 1

internal fun shouldRevealNewFront(
    previousKey: Any?,
    firstKey: Any?,
    insertedCount: Int,
    firstVisibleItemIndex: Int,
): Boolean =
    firstKey != null &&
        previousKey != null &&
        previousKey != firstKey &&
        insertedCount >= 0 &&
        firstVisibleItemIndex - insertedCount <= REVEAL_NEAR_FRONT

/**
 * Scrolls a row back to the front when a new first item appears while it is already at/near the
 * start. The lazy layout anchors to the previously visible key, so a row at the start advances by
 * the number of prepended items; subtracting that recovers the position the user was actually at.
 */
@Composable
internal fun <T> RevealNewFrontItem(
    items: List<T>,
    state: LazyListState,
    key: (T) -> Any?,
) {
    val firstKey = items.firstOrNull()?.let(key)
    var previous by remember { mutableStateOf<Any?>(null) }
    LaunchedEffect(firstKey) {
        val oldFront = previous
        val inserted = if (oldFront != null) items.indexOfFirst { key(it) == oldFront } else -1
        if (shouldRevealNewFront(oldFront, firstKey, inserted, state.firstVisibleItemIndex)) {
            state.animateScrollToItem(0)
        }
        if (firstKey != null) previous = firstKey
    }
}

private fun LazyListScope.albumRow(
    section: HomeSection,
    albums: List<Album>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onAlbumClick: (Album) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
    onMore: (HomeSection) -> Unit,
) {
    if (albums.isEmpty()) return
    item(key = "${section.name}-header") {
        HomeSectionHeader(section.title, section.icon) { onMore(section) }
    }
    item(key = "${section.name}-row") {
        val rowState = rememberLazyListState()
        RevealNewFrontItem(albums, rowState) { it.id }
        LazyRow(
            state = rowState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(TILE_SPACING),
            modifier = Modifier.padding(top = SECTION_CONTENT_TOP),
        ) {
            items(albums, key = { it.id }) { album ->
                val ref = coverArt(album.coverArt, true)
                AlbumTile(
                    album = album,
                    ref = ref,
                    onClick = { onAlbumClick(album) },
                    onLongClick = { onLongClick(MenuTarget.Album(album, ref)) },
                )
            }
        }
    }
}

private fun LazyListScope.artistRow(
    section: HomeSection,
    artists: List<Artist>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onArtistClick: (Artist) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
    onMore: (HomeSection) -> Unit,
) {
    if (artists.isEmpty()) return
    item(key = "${section.name}-header") {
        HomeSectionHeader(section.title, section.icon) { onMore(section) }
    }
    item(key = "${section.name}-row") {
        val rowState = rememberLazyListState()
        RevealNewFrontItem(artists, rowState) { it.id }
        LazyRow(
            state = rowState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(ARTIST_SPACING),
            modifier = Modifier.padding(top = SECTION_CONTENT_TOP),
        ) {
            items(artists, key = { it.id }) { artist ->
                val ref = coverArt(artist.coverArt, true)
                ArtistTile(
                    artist = artist,
                    ref = ref,
                    onClick = { onArtistClick(artist) },
                    onLongClick = { onLongClick(MenuTarget.Artist(artist, ref)) },
                )
            }
        }
    }
}

private fun LazyListScope.genreBlock(
    section: HomeSection,
    genres: List<String>,
    onGenreClick: (String) -> Unit,
    onMore: (HomeSection) -> Unit,
) {
    if (genres.isEmpty()) return
    item(key = "${section.name}-header") {
        HomeSectionHeader(section.title, section.icon) { onMore(section) }
    }
    item(key = "${section.name}-row") {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = SECTION_CONTENT_TOP),
        ) {
            val rows = remember(genres) { brickRows(genres, GENRE_ROWS) }
            Column(verticalArrangement = Arrangement.spacedBy(GENRE_ROW_SPACING)) {
                rows.forEach { rowGenres ->
                    if (rowGenres.isEmpty()) return@forEach
                    Row(horizontalArrangement = Arrangement.spacedBy(TILE_SPACING)) {
                        rowGenres.forEach { genre ->
                            AssistChip(onClick = { onGenreClick(genre) }, label = { Text(genre) })
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.decadeRow(
    section: HomeSection,
    decades: List<Long>,
    onDecadeClick: (Long) -> Unit,
    onMore: (HomeSection) -> Unit,
) {
    if (decades.isEmpty()) return
    item(key = "${section.name}-header") {
        HomeSectionHeader(section.title, section.icon) { onMore(section) }
    }
    item(key = "${section.name}-row") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(TILE_SPACING),
            modifier = Modifier.padding(top = SECTION_CONTENT_TOP),
        ) {
            items(decades, key = { it }) { decade ->
                AssistChip(onClick = { onDecadeClick(decade) }, label = { Text("${decade}s") })
            }
        }
    }
}

private fun LazyListScope.homeSongs(
    section: HomeSection,
    songs: List<AlbumSongItem>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    playingSongId: String?,
    onSongPlay: (Song) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
    onMore: (HomeSection) -> Unit,
) = songList(
    key = section.name,
    title = section.title,
    icon = section.icon,
    songs = songs,
    coverArt = coverArt,
    playingSongId = playingSongId,
    onSongPlay = onSongPlay,
    onLongClick = onLongClick,
    onMore = { onMore(section) },
)

private fun LazyListScope.songList(
    key: String,
    title: String,
    icon: ImageVector,
    songs: List<AlbumSongItem>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    playingSongId: String?,
    onSongPlay: (Song) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
    onMore: (() -> Unit)?,
) {
    if (songs.isEmpty()) return
    item(key = "$key-header") {
        HomeSectionHeader(title, icon, onMore)
    }
    items(songs, key = { it.song.id }) { item ->
        SongRow(
            song = item.song,
            coverArtId = item.coverArt,
            coverArt = coverArt,
            isPlaying = item.song.id == playingSongId,
            durationSeconds = item.song.duration,
            modifier =
                Modifier.combinedClickable(
                    onClick = { onSongPlay(item.song) },
                    onLongClick = { onLongClick(MenuTarget.Song(item.song, coverArt(item.coverArt, true))) },
                ),
        )
    }
}

@Composable
private fun AlbumTile(
    album: Album,
    ref: CoverArtRef?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Column(
        modifier = Modifier.width(TILE_SIZE).combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        CoverArt(
            ref = ref,
            name = album.name,
            modifier = Modifier.size(TILE_SIZE).clip(RoundedCornerShape(2.dp)),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = album.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = album.albumArtist.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ArtistTile(
    artist: Artist,
    ref: CoverArtRef?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Column(
        modifier = Modifier.width(ARTIST_TILE_SIZE).combinedClickable(onClick = onClick, onLongClick = onLongClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CoverArt(
            ref = ref,
            name = artist.name,
            modifier = Modifier.size(ARTIST_TILE_SIZE).clip(CircleShape),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = artist.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
