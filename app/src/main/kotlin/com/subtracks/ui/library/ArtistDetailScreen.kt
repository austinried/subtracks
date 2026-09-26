package com.subtracks.ui.library

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.source.StarType
import com.subtracks.ui.components.ContextMenuHost
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.components.ItemActions
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.components.StarredBadge
import com.subtracks.ui.components.rememberViewportFill
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.ArtworkTheme
import com.subtracks.ui.theme.HeroGradient
import com.subtracks.ui.theme.PrefetchArtworkSeeds
import com.subtracks.ui.theme.baseArtworkColors
import com.subtracks.ui.theme.heroBarColor
import com.subtracks.ui.theme.rememberArtworkColors
import com.subtracks.ui.theme.rememberOverlaidNameBusy
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val ART_HEIGHT = 420.dp
private val TITLE_INSET = 16.dp
private val FADE_LEAD = 24.dp
private const val THEME_TRANSITION_MS = 100
private const val SCRIM_FADE_MS = 180
internal const val ARTIST_NAME_SCRIM_TAG = "artistNameScrim"

@Composable
fun ArtistDetailRoute(
    artistId: String,
    coverArtId: String?,
    onBack: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onViewAlbum: (String) -> Unit,
    contextMenuHost: ContextMenuHost? = null,
    setStar: suspend (StarType, String, Boolean) -> Result<Unit>,
    viewModel: ArtistDetailViewModel = koinViewModel(key = artistId) { parametersOf(artistId) },
) {
    val artist by viewModel.artist.collectAsStateWithLifecycle(initialValue = null)
    val albums by viewModel.albums.collectAsStateWithLifecycle(initialValue = emptyList())
    val art by viewModel.art.collectAsStateWithLifecycle()
    val artThumbnail by viewModel.artThumbnail.collectAsStateWithLifecycle()
    val shortcutArt = remember(coverArtId) { coverArtId?.let { viewModel.coverArt(it, true) } }
    val actions =
        ItemActions(
            playAlbum = { viewModel.playAlbum(it.id) },
            shuffleAlbum = { viewModel.shuffleAlbum(it.id) },
            setStar = setStar,
            viewAlbum = onViewAlbum,
        )
    ArtistDetailScreen(
        artist = artist,
        albums = albums,
        art = art,
        artThumbnail = artThumbnail,
        artwork = rememberArtworkColors(shortcutArt ?: artThumbnail ?: art, THEME_TRANSITION_MS),
        coverArt = viewModel::coverArt,
        onBack = onBack,
        onAlbumClick = onAlbumClick,
        onAlbumLongClick = { contextMenuHost?.show(it, actions) },
        onMore = { artist?.let { contextMenuHost?.show(MenuTarget.Artist(it, artThumbnail ?: art), actions) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artist: Artist?,
    albums: List<Album>,
    art: CoverArtRef?,
    artThumbnail: CoverArtRef? = null,
    artwork: ArtworkColors? = null,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onBack: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onAlbumLongClick: (MenuTarget) -> Unit = {},
    onMore: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyGridState()
    val fill = rememberViewportFill(listState)
    val density = LocalDensity.current
    val lineHeight =
        with(density) {
            MaterialTheme.typography.headlineLarge.lineHeight
                .toDp()
        }
    val statusBarTop = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
    val navBarBottom = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
    val barHeight = statusBarTop + TopAppBarDefaults.TopAppBarExpandedHeight
    val imageTitleTop = ART_HEIGHT - TITLE_INSET - lineHeight
    val fadeStartPx = with(density) { (imageTitleTop - barHeight - FADE_LEAD).toPx() }
    val fadeEndPx = with(density) { (imageTitleTop + lineHeight / 2 - barHeight).toPx() }
    val barFraction by remember(fadeStartPx, fadeEndPx) {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                ((listState.firstVisibleItemScrollOffset - fadeStartPx) / (fadeEndPx - fadeStartPx)).coerceIn(0f, 1f)
            }
        }
    }
    var imageHeightPx by remember { mutableFloatStateOf(0f) }
    var albumHeightPx by remember { mutableFloatStateOf(0f) }
    val albumRowGapPx = with(density) { 16.dp.toPx() }
    val scrollPx by remember {
        derivedStateOf {
            val index = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset.toFloat()
            if (index <= 0) {
                offset
            } else {
                imageHeightPx + (albumHeightPx + albumRowGapPx) * ((index - 1) / 2) + offset
            }
        }
    }
    val nameTextStyle = MaterialTheme.typography.headlineLarge
    val imageNameStyle =
        nameTextStyle.copy(
            shadow =
                Shadow(
                    color = Color.Black.copy(alpha = 0.8f),
                    offset = Offset(0f, 2f),
                    blurRadius = 10f,
                ),
        )
    val nameBusy = rememberOverlaidNameBusy(artThumbnail ?: art)
    val nameScrimAlpha by animateFloatAsState(if (nameBusy) 1f else 0f, tween(SCRIM_FADE_MS), label = "artistNameScrim")
    val effectiveArtwork = artwork ?: baseArtworkColors

    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        ArtworkTheme(effectiveArtwork) {
            BoxWithConstraints(modifier.fillMaxSize().background(Color.Black)) {
                val screenHeightPx = with(density) { maxHeight.toPx() }
                val barHeightPx = with(density) { barHeight.toPx() }
                val barColor = heroBarColor(effectiveArtwork, scrollPx, barHeightPx, screenHeightPx)
                HeroGradient(
                    colors = effectiveArtwork,
                    scrollPx = { scrollPx - imageHeightPx },
                    modifier = Modifier.fillMaxSize(),
                )
                LazyVerticalGrid(
                    state = listState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 16.dp + navBarBottom),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .onSizeChanged { imageHeightPx = it.height.toFloat() }
                                .padding(bottom = 12.dp),
                        ) {
                            CoverArt(
                                ref = art,
                                name = artist?.name.orEmpty(),
                                thumbnailRef = artThumbnail,
                                showPlaceholder = art == null,
                                modifier = Modifier.fillMaxWidth().height(ART_HEIGHT),
                            )
                            if (nameScrimAlpha > 0f) {
                                Box(
                                    modifier =
                                        Modifier
                                            .align(Alignment.BottomStart)
                                            .fillMaxWidth()
                                            .height(ART_HEIGHT * 0.4f)
                                            .graphicsLayer { alpha = nameScrimAlpha }
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                                ),
                                            ).testTag(ARTIST_NAME_SCRIM_TAG),
                                )
                            }
                            Text(
                                text = artist?.name.orEmpty(),
                                style = imageNameStyle,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.align(Alignment.BottomStart).padding(TITLE_INSET),
                            )
                        }
                    }
                    itemsIndexed(albums, key = { _, album -> album.id }) { index, album ->
                        PrefetchArtworkSeeds(coverArt(album.coverArt, true))
                        Column(
                            modifier =
                                Modifier
                                    .padding(
                                        start = if (index % 2 == 0) 16.dp else 0.dp,
                                        end = if (index % 2 == 1) 16.dp else 0.dp,
                                    ).combinedClickable(
                                        onClick = { onAlbumClick(album) },
                                        onLongClick = { onAlbumLongClick(MenuTarget.Album(album, coverArt(album.coverArt, true))) },
                                    ).onSizeChanged { albumHeightPx = it.height.toFloat() },
                        ) {
                            Box {
                                CoverArt(
                                    ref = coverArt(album.coverArt, false),
                                    name = album.name,
                                    thumbnailRef = coverArt(album.coverArt, true),
                                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(2.dp)),
                                )
                                StarredBadge(
                                    starred = album.starred != null,
                                    size = 16.dp,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                                )
                            }
                            Text(
                                text = album.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                            Text(
                                text = album.year?.takeIf { it > 0 }?.toString() ?: "\u00A0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Spacer(Modifier.height(fill))
                    }
                }

                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .height(barHeight + 24.dp)
                            .alpha(1f - barFraction)
                            .background(
                                Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)),
                            ),
                )

                TopAppBar(
                    title = {
                        Text(
                            text = artist?.name.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.graphicsLayer { alpha = barFraction },
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.graphicsLayer { alpha = barFraction }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onMore, modifier = Modifier.graphicsLayer { alpha = barFraction }) {
                            Icon(Icons.Rounded.MoreHoriz, contentDescription = "More options")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .background(barColor.copy(alpha = barFraction))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {},
                )
            }
        }
    }
}
