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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.source.StarType
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.ContextMenuHost
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.components.ItemActions
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.components.rememberViewportFill
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.ArtworkTheme
import com.subtracks.ui.theme.HeroGradient
import com.subtracks.ui.theme.PrefetchArtworkSeeds
import com.subtracks.ui.theme.baseArtworkColors
import com.subtracks.ui.theme.heroBarColor
import com.subtracks.ui.theme.rememberArtworkColors
import com.subtracks.ui.theme.rememberOverlaidNameBusy
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val ART_HEIGHT = 420.dp
private val TITLE_INSET = 16.dp
private val ROW_GAP = 16.dp
private val SHUFFLE_WIDTH = 68.dp
private val SHUFFLE_HEIGHT = 48.dp
private val SHUFFLE_ICON = 30.dp
private val SHUFFLE_END = 22.dp
private val SHUFFLE_RESERVE = 86.dp
private val SHUFFLE_HANG = SHUFFLE_HEIGHT / 2

internal fun estimateScrollPx(
    index: Int,
    offset: Float,
    imageHeightPx: Float,
    albumHeightPx: Float,
    albumRowGapPx: Float,
): Float =
    if (index <= 0) {
        offset
    } else {
        imageHeightPx + albumRowGapPx + (albumHeightPx + albumRowGapPx) * ((index - 1) / 2) + offset
    }

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
    setStar: (StarType, String, Boolean) -> Unit,
    viewModel: ArtistDetailViewModel = koinViewModel(key = artistId) { parametersOf(artistId) },
    playbackController: PlaybackController = koinInject(),
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
            playNext = { playbackController.playNext(it.sourceId, it.kind, it.refId) },
            addToQueue = { playbackController.addToQueue(it.sourceId, it.kind, it.refId) },
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
        starred = artist?.starred != null,
        onToggleStar = artist?.let { a -> { setStar(StarType.Artist, a.id, a.starred == null) } },
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
    starred: Boolean = false,
    onToggleStar: (() -> Unit)? = null,
    onShuffle: () -> Unit = {},
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
    val albumRowGapPx = with(density) { ROW_GAP.toPx() }
    val scrollPx by remember {
        derivedStateOf {
            estimateScrollPx(
                index = listState.firstVisibleItemIndex,
                offset = listState.firstVisibleItemScrollOffset.toFloat(),
                imageHeightPx = imageHeightPx,
                albumHeightPx = albumHeightPx,
                albumRowGapPx = albumRowGapPx,
            )
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
                val gradientScrollPx = scrollPx - imageHeightPx
                val barColor = heroBarColor(effectiveArtwork, gradientScrollPx, barHeightPx, screenHeightPx)
                HeroGradient(
                    colors = effectiveArtwork,
                    scrollPx = { gradientScrollPx },
                    modifier = Modifier.fillMaxSize(),
                )
                LazyVerticalGrid(
                    state = listState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 16.dp + navBarBottom),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(ROW_GAP),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .onSizeChanged { imageHeightPx = it.height.toFloat() },
                        ) {
                            Box(Modifier.fillMaxWidth()) {
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
                                Box(Modifier.align(Alignment.BottomStart).fillMaxWidth()) {
                                    ArtistTitle(
                                        text = artist?.name.orEmpty(),
                                        style = imageNameStyle,
                                        color = Color.White,
                                        endReserve = SHUFFLE_RESERVE,
                                        modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(TITLE_INSET),
                                    )
                                    FilledIconButton(
                                        onClick = onShuffle,
                                        shape = RoundedCornerShape(24.dp),
                                        modifier =
                                            Modifier
                                                .align(Alignment.BottomEnd)
                                                .offset(y = SHUFFLE_HANG)
                                                .padding(end = SHUFFLE_END)
                                                .width(SHUFFLE_WIDTH)
                                                .height(SHUFFLE_HEIGHT),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Shuffle,
                                            contentDescription = "Shuffle artist",
                                            modifier = Modifier.size(SHUFFLE_ICON),
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 20.dp, bottom = 16.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = "${artist?.albumCount ?: 0} ${if (artist?.albumCount == 1L) "album" else "albums"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "Artist biography coming soon.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 8.dp),
                                    )
                                }
                                IconButton(
                                    onClick = { onToggleStar?.invoke() },
                                    enabled = onToggleStar != null,
                                    modifier = Modifier.padding(top = 8.dp),
                                ) {
                                    Icon(
                                        imageVector = if (starred) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                        contentDescription = if (starred) "Unstar" else "Star",
                                        tint =
                                            if (starred) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                    )
                                }
                                IconButton(onClick = onMore, modifier = Modifier.padding(top = 8.dp)) {
                                    Icon(Icons.Rounded.MoreHoriz, contentDescription = "More options")
                                }
                            }
                        }
                    }
                    itemsIndexed(albums, key = { _, album -> album.id }) { index, album ->
                        val art = coverArt(album.coverArt, false)
                        val thumbnail = coverArt(album.coverArt, true)
                        PrefetchArtworkSeeds(thumbnail)
                        Column(
                            modifier =
                                Modifier
                                    .padding(
                                        start = if (index % 2 == 0) 16.dp else 0.dp,
                                        end = if (index % 2 == 1) 16.dp else 0.dp,
                                    ).combinedClickable(
                                        onClick = { onAlbumClick(album) },
                                        onLongClick = { onAlbumLongClick(MenuTarget.Album(album, thumbnail)) },
                                    ).onSizeChanged { albumHeightPx = it.height.toFloat() },
                        ) {
                            CoverArt(
                                ref = art,
                                name = album.name,
                                thumbnailRef = thumbnail,
                                showPlaceholder = art == null,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(2.dp)),
                            )
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

@Composable
private fun ArtistTitle(
    text: String,
    style: TextStyle,
    color: Color,
    endReserve: Dp,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier) {
        val fullWidth = with(density) { maxWidth.roundToPx() }
        val reserve = with(density) { endReserve.roundToPx() }
        val lines = remember(text, style, fullWidth, reserve) { splitTitle(measurer, text, style, fullWidth, reserve) }
        Column {
            lines.forEachIndexed { index, line ->
                Text(
                    text = line,
                    style = style,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .then(if (index == lines.lastIndex) Modifier.padding(end = endReserve) else Modifier),
                )
            }
        }
    }
}

internal fun splitTitle(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    fullWidth: Int,
    reserve: Int,
): List<String> {
    if (text.isBlank()) return listOf(text)
    if (measurer.measure(AnnotatedString(text), style, maxLines = 1).size.width <= fullWidth - reserve) {
        return listOf(text)
    }
    val words = text.split(' ').filter { it.isNotEmpty() }
    if (words.size <= 1) return listOf(text)
    var count = 0
    var acc = ""
    for (i in words.indices) {
        val candidate = if (acc.isEmpty()) words[i] else "$acc ${words[i]}"
        if (measurer.measure(AnnotatedString(candidate), style, maxLines = 1).size.width <= fullWidth) {
            acc = candidate
            count = i + 1
        } else {
            break
        }
    }
    if (count >= words.size) count = words.size - 1
    if (count < 1) count = 1
    return listOf(words.take(count).joinToString(" "), words.drop(count).joinToString(" "))
}
