package com.subtracks.ui.home

import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.data.model.Album
import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
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
    Rediscover("On repeat"),
    ;

    companion object {
        fun fromRoute(value: String?): HomeSection? = entries.firstOrNull { it.name == value }
    }
}

@Composable
fun HomeRoute(
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onViewAlbum: (String) -> Unit,
    onViewArtist: (String) -> Unit,
    onMore: (HomeSection) -> Unit,
    onGenreClick: (String) -> Unit,
    onDecadeClick: (Long) -> Unit,
    contextMenuHost: ContextMenuHost? = null,
    setStar: (StarType, String, Boolean) -> Unit,
    topInset: Dp,
    bottomInset: Dp,
    viewModel: HomeViewModel = koinViewModel(),
    playbackController: PlaybackController = koinInject(),
) {
    val feed by viewModel.feed.collectAsStateWithLifecycle()
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val actions =
        ItemActions(
            playNext = { playbackController.playNext(it.sourceId, it.kind, it.refId) },
            addToQueue = { playbackController.addToQueue(it.sourceId, it.kind, it.refId) },
            setStar = setStar,
            viewAlbum = onViewAlbum,
            viewArtist = onViewArtist,
        )
    HomeScreen(
        feed = feed,
        coverArt = viewModel::coverArt,
        playingSongId = playback.item?.id,
        topInset = topInset,
        bottomInset = bottomInset,
        onAlbumClick = onAlbumClick,
        onArtistClick = onArtistClick,
        onSongClick = viewModel::play,
        onLongClick = { contextMenuHost?.show(it, actions) },
        onMore = onMore,
        onGenreClick = onGenreClick,
        onDecadeClick = onDecadeClick,
        onSync = viewModel::sync,
    )
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
    onSongClick: (Song) -> Unit = {},
    onLongClick: (MenuTarget) -> Unit = {},
    onMore: (HomeSection) -> Unit = {},
    onGenreClick: (String) -> Unit = {},
    onDecadeClick: (Long) -> Unit = {},
    onSync: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (feed.loading) {
        LoadingState(modifier)
        return
    }
    if (feed.isEmpty()) {
        EmptyState(
            text = "Nothing to show yet.\nSync with your server to fill your library.",
            modifier = modifier,
            actionLabel = "Sync",
            onAction = onSync,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = topInset + 8.dp, bottom = bottomInset + 24.dp),
    ) {
        albumRow(
            HomeSection.RecentlyPlayedAlbums,
            feed.recentlyPlayedAlbums,
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
        albumRow(
            HomeSection.MostPlayedAlbums,
            feed.mostPlayedAlbums,
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
        genreBlock(HomeSection.Genres, feed.genres, onGenreClick, onMore)
        decadeRow(HomeSection.Decades, feed.decades, onDecadeClick, onMore)
        starredSongs(HomeSection.RecentlyStarredSongs, feed.recentlyStarredSongs, coverArt, playingSongId, onSongClick, onLongClick, onMore)
        albumRow(
            HomeSection.RecentlyAddedAlbums,
            feed.recentlyAddedAlbums,
            coverArt,
            onAlbumClick,
            onLongClick,
            onMore,
        )
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
    onMore: (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 20.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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

private val TILE_SIZE = 132.dp
private val ARTIST_TILE_SIZE = 104.dp
private val TILE_SPACING = 8.dp

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
        HomeSectionHeader(section.title) { onMore(section) }
    }
    item(key = "${section.name}-row") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(TILE_SPACING),
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
        HomeSectionHeader(section.title) { onMore(section) }
    }
    item(key = "${section.name}-row") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(TILE_SPACING),
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
        HomeSectionHeader(section.title) { onMore(section) }
    }
    item(key = "${section.name}-row") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(TILE_SPACING),
        ) {
            items(genres.chunked(GENRE_COLUMN_HEIGHT), key = { it.first() }) { column ->
                Column(verticalArrangement = Arrangement.spacedBy(TILE_SPACING)) {
                    column.forEach { genre ->
                        AssistChip(onClick = { onGenreClick(genre) }, label = { Text(genre) })
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
        HomeSectionHeader(section.title) { onMore(section) }
    }
    item(key = "${section.name}-row") {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(TILE_SPACING),
        ) {
            items(decades, key = { it }) { decade ->
                AssistChip(onClick = { onDecadeClick(decade) }, label = { Text("${decade}s") })
            }
        }
    }
}

private fun LazyListScope.starredSongs(
    section: HomeSection,
    songs: List<AlbumSongItem>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    playingSongId: String?,
    onSongClick: (Song) -> Unit,
    onLongClick: (MenuTarget) -> Unit,
    onMore: (HomeSection) -> Unit,
) {
    if (songs.isEmpty()) return
    item(key = "${section.name}-header") {
        HomeSectionHeader(section.title) { onMore(section) }
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
                    onClick = { onSongClick(item.song) },
                    onLongClick = { onLongClick(MenuTarget.Song(item.song, coverArt(item.coverArt, true))) },
                ),
        )
    }
}

private const val GENRE_COLUMN_HEIGHT = 3

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
            modifier = Modifier.size(TILE_SIZE).clip(RoundedCornerShape(4.dp)),
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
