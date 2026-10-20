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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Mic
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.R
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.ArtistAlbums
import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.source.StarType
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.ArtistArt
import com.subtracks.ui.components.ArtistHeroArt
import com.subtracks.ui.components.CloseWhenDownloadsGone
import com.subtracks.ui.components.ContextMenuHost
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.components.DeleteDownloadsDialog
import com.subtracks.ui.components.DismissOnRequest
import com.subtracks.ui.components.ItemActions
import com.subtracks.ui.components.ListDownloadIndicator
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.components.PendingDownloadDelete
import com.subtracks.ui.components.bulkRef
import com.subtracks.ui.components.rememberViewportFill
import com.subtracks.ui.components.statusBarScrim
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.ArtworkTheme
import com.subtracks.ui.theme.HeroGradient
import com.subtracks.ui.theme.PrefetchArtworkSeeds
import com.subtracks.ui.theme.baseArtworkColors
import com.subtracks.ui.theme.heroBarColor
import com.subtracks.ui.theme.rememberArtworkColors
import com.subtracks.ui.theme.rememberOverlaidNameBusy
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private val ART_HEIGHT = 420.dp
private val TITLE_INSET = 16.dp
private val ROW_GAP = 16.dp
private val SHUFFLE_WIDTH = 68.dp
private val SHUFFLE_HEIGHT = 48.dp
private val SHUFFLE_ICON = 30.dp
private val SHUFFLE_END = 22.dp
private val SHUFFLE_RESERVE = 86.dp
private val SHUFFLE_HANG = SHUFFLE_HEIGHT / 2
private val BIO_ACTION_RESERVE = 104.dp
private val BIO_ACTION_HEIGHT = 48.dp
private val BIO_ACTION_OFFSET = 8.dp
private val BIO_TOP_GAP = 8.dp

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
private const val ARTIST_NAME_MAX_LINES = 6
private const val THEME_TRANSITION_MS = 100
private const val SCRIM_FADE_MS = 180
internal const val ARTIST_NAME_SCRIM_TAG = "artistNameScrim"
internal const val ARTIST_BIO_MORE_TAG = "artistBioMore"
internal const val ARTIST_BIO_LESS_TAG = "artistBioLess"

private data class BioSnapshot(
    val known: Boolean,
    val present: Boolean,
    val biography: String?,
)

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
    val artistName by viewModel.artistName.collectAsStateWithLifecycle(initialValue = null)
    val albums by viewModel.albums.collectAsStateWithLifecycle(initialValue = null)
    val bioSnapshots =
        remember(viewModel) {
            viewModel.artistBio.map { BioSnapshot(known = true, present = it != null, biography = it?.biography) }
        }
    val bioSnapshot by
        bioSnapshots.collectAsStateWithLifecycle(
            initialValue = BioSnapshot(known = false, present = false, biography = null),
        )
    val bioLoading by viewModel.bioLoading.collectAsStateWithLifecycle()
    val bioUnavailable by viewModel.bioUnavailable.collectAsStateWithLifecycle()
    val bioExpanded by viewModel.bioExpanded.collectAsStateWithLifecycle()
    LaunchedEffect(artistId) { viewModel.refreshBio() }
    val art by viewModel.art.collectAsStateWithLifecycle()
    val artThumbnail by viewModel.artThumbnail.collectAsStateWithLifecycle()
    val heroArt by viewModel.heroArt.collectAsStateWithLifecycle()
    val downloadStatus by viewModel.downloadStatus.collectAsStateWithLifecycle()
    val offline by viewModel.offline.collectAsStateWithLifecycle()
    CloseWhenDownloadsGone(downloadStatus.downloaded, offline, onBack)
    val albumDownloads by viewModel.albumDownloads.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<PendingDownloadDelete?>(null) }
    DismissOnRequest(contextMenuHost?.dismissals ?: emptyFlow()) { pendingDelete = null }
    val shortcutArt = remember(coverArtId) { coverArtId?.let { viewModel.coverArt(it, true) } }
    val actions =
        ItemActions(
            playNext = { playbackController.playNext(it.sourceId, it.kind, it.refId) },
            addToQueue = { playbackController.addToQueue(it.sourceId, it.kind, it.refId) },
            bulkDownload = { target, action ->
                target.bulkRef()?.let { (list, refId) ->
                    if (action == BulkDownloadAction.Delete) {
                        scope.launch {
                            pendingDelete =
                                PendingDownloadDelete(target.title, list, refId, viewModel.downloadedBytes(list, refId))
                        }
                    } else {
                        viewModel.onDownloadAction(list, refId, action)
                    }
                }
            },
            setStar = setStar,
            viewAlbum = onViewAlbum,
        )
    ArtistDetailScreen(
        artist = artist,
        artistName = artistName.orEmpty(),
        albums = albums ?: ArtistAlbums(),
        albumsLoaded = albums != null,
        bio = bioSnapshot.biography,
        bioLoading = bioLoading && !bioSnapshot.present,
        bioUnavailable = bioUnavailable && !bioSnapshot.present,
        bioPending = !bioSnapshot.known,
        bioExpanded = bioExpanded,
        art = art,
        artThumbnail = artThumbnail,
        heroArt = heroArt,
        artwork = rememberArtworkColors(shortcutArt ?: artThumbnail ?: art, THEME_TRANSITION_MS, fallbackName = artistName),
        coverArt = viewModel::coverArt,
        onBack = onBack,
        onAlbumClick = onAlbumClick,
        albumDownloads = albumDownloads,
        onAlbumLongClick = { contextMenuHost?.show(it, actions) },
        onMore = { artist?.let { contextMenuHost?.show(MenuTarget.Artist(it, artThumbnail ?: art, downloadStatus), actions) } },
        starred = artist?.starred != null,
        onToggleStar = artist?.let { a -> { setStar(StarType.Artist, a.id, a.starred == null) } },
        onExpandBio = viewModel::expandBio,
        onCollapseBio = viewModel::collapseBio,
        onShuffle = viewModel::shuffle,
    )

    pendingDelete?.let { pending ->
        DeleteDownloadsDialog(
            name = pending.name,
            bytes = pending.bytes,
            onConfirm = { viewModel.onDownloadAction(pending.list, pending.refId, BulkDownloadAction.Delete) },
            onDismiss = { pendingDelete = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artist: Artist?,
    modifier: Modifier = Modifier,
    artistName: String = "",
    albums: ArtistAlbums = ArtistAlbums(),
    albumsLoaded: Boolean = true,
    bio: String? = null,
    bioLoading: Boolean = false,
    bioUnavailable: Boolean = false,
    bioPending: Boolean = false,
    bioExpanded: Boolean = false,
    art: CoverArtRef?,
    artThumbnail: CoverArtRef? = null,
    artCovers: List<CoverArtRef> = emptyList(),
    heroArt: ArtistArt? = null,
    artwork: ArtworkColors? = null,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onBack: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    albumDownloads: Map<String, ListDownloadStatus> = emptyMap(),
    onAlbumLongClick: (MenuTarget) -> Unit = {},
    onMore: () -> Unit = {},
    starred: Boolean = false,
    onToggleStar: (() -> Unit)? = null,
    onExpandBio: () -> Unit = {},
    onCollapseBio: () -> Unit = {},
    onShuffle: () -> Unit = {},
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
    val albumCount = albums.count.toLong()
    val displayName = artistName.ifBlank { artist?.name.orEmpty() }
    val artistType =
        stringResource(
            if (albums.albumCredits.isNotEmpty()) R.string.artist_type_album_artist else R.string.artist_type_artist,
        )
    val albumCountText = pluralStringResource(R.plurals.resources_album_count, albumCount.toInt(), albumCount)
    val metaText = if (albumsLoaded) stringResource(R.string.artist_meta, artistType, albumCountText) else ""
    val hasBio = !bio.isNullOrBlank()
    val bioCollapsed = hasBio && !bioExpanded
    val bioText =
        when {
            bioPending -> ""
            hasBio -> bio.orEmpty()
            bioLoading -> stringResource(R.string.artist_bio_loading)
            bioUnavailable -> stringResource(R.string.artist_bio_unavailable)
            else -> stringResource(R.string.artist_bio_empty)
        }
    var titleHeightPx by remember { mutableFloatStateOf(0f) }
    val titleHeight = if (titleHeightPx > 0f) with(density) { titleHeightPx.toDp() } else lineHeight
    val titleMaxLines =
        ((ART_HEIGHT - TITLE_INSET - barHeight - FADE_LEAD) / lineHeight)
            .toInt()
            .coerceIn(1, ARTIST_NAME_MAX_LINES)
    val imageTitleTop = ART_HEIGHT - TITLE_INSET - titleHeight
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
    val effectiveArtwork = artwork ?: baseArtworkColors()

    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        ArtworkTheme(effectiveArtwork) {
            BoxWithConstraints(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                val screenHeightPx = with(density) { maxHeight.toPx() }
                val barHeightPx = with(density) { barHeight.toPx() }
                // The gradient is anchored to the hero image, not the whole header, whose bio can expand.
                val heroHeightPx = with(density) { ART_HEIGHT.toPx() }
                val gradientScrollPx = scrollPx - heroHeightPx
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
                    horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
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
                                ArtistHeroArt(
                                    art =
                                        heroArt ?: ArtistArt(
                                            image = art.takeIf { artCovers.isEmpty() },
                                            covers = artCovers,
                                        ),
                                    name = displayName,
                                    thumbnail = artThumbnail,
                                    modifier = Modifier.fillMaxWidth().height(ART_HEIGHT),
                                )
                                if (nameScrimAlpha > 0f) {
                                    Box(
                                        modifier =
                                            Modifier
                                                .align(Alignment.BottomStart)
                                                .fillMaxWidth()
                                                .height(maxOf(ART_HEIGHT * 0.4f, titleHeight + TITLE_INSET * 2))
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
                                        text = displayName,
                                        style = imageNameStyle,
                                        color = Color.White,
                                        endReserve = SHUFFLE_RESERVE,
                                        maxLines = titleMaxLines,
                                        modifier =
                                            Modifier
                                                .align(Alignment.BottomStart)
                                                .fillMaxWidth()
                                                .padding(TITLE_INSET)
                                                .onSizeChanged { titleHeightPx = it.height.toFloat() },
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
                                            contentDescription = stringResource(R.string.shuffle_artist),
                                            modifier = Modifier.size(SHUFFLE_ICON),
                                        )
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = TITLE_INSET),
                            ) {
                                Text(
                                    text = metaText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth().padding(end = SHUFFLE_RESERVE),
                                )
                                ArtistBio(
                                    text = bioText,
                                    color =
                                        if (hasBio) {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        },
                                    collapsed = bioCollapsed,
                                    expanded = hasBio && bioExpanded,
                                    artist = artist,
                                    starred = starred,
                                    onToggleStar = onToggleStar,
                                    onMore = onMore,
                                    onExpand = onExpandBio,
                                    onCollapse = onCollapseBio,
                                    modifier =
                                        Modifier.padding(
                                            bottom = if (hasBio && !bioCollapsed) 8.dp else 0.dp,
                                        ),
                                )
                            }
                        }
                    }
                    if (albums.albumCredits.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ArtistSectionHeader(stringResource(R.string.artist_section_albums), Icons.Rounded.Album)
                        }
                        itemsIndexed(albums.albumCredits, key = { _, album -> album.id }) { index, album ->
                            ArtistAlbumCard(
                                album = album,
                                index = index,
                                coverArt = coverArt,
                                downloadStatus = albumDownloads[album.id],
                                onClick = { onAlbumClick(album) },
                                onLongClick = { thumbnail ->
                                    onAlbumLongClick(MenuTarget.Album(album, thumbnail, albumDownloads[album.id]))
                                },
                                onSizeChanged = { albumHeightPx = it },
                            )
                        }
                    }
                    if (albums.appearances.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ArtistSectionHeader(stringResource(R.string.artist_section_appearances), Icons.Rounded.Mic, topGap = 16.dp)
                        }
                        itemsIndexed(albums.appearances, key = { _, album -> album.id }) { index, album ->
                            ArtistAlbumCard(
                                album = album,
                                index = index,
                                coverArt = coverArt,
                                downloadStatus = albumDownloads[album.id],
                                onClick = { onAlbumClick(album) },
                                onLongClick = { thumbnail ->
                                    onAlbumLongClick(MenuTarget.Album(album, thumbnail, albumDownloads[album.id]))
                                },
                                onSizeChanged = { albumHeightPx = it },
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
                            .alpha(1f - barFraction)
                            .statusBarScrim(),
                )

                TopAppBar(
                    title = {
                        Text(
                            text = displayName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.graphicsLayer { alpha = barFraction },
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.graphicsLayer { alpha = barFraction }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.navigation_back))
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

// The expanded layout narrows only the lines that overlap the action buttons; later lines use the
// full width.
@Composable
private fun ArtistBio(
    text: String,
    color: Color,
    collapsed: Boolean,
    expanded: Boolean,
    artist: Artist?,
    starred: Boolean,
    onToggleStar: (() -> Unit)?,
    onMore: () -> Unit,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val style = MaterialTheme.typography.bodyMedium
    val moreText = stringResource(R.string.artist_bio_more)
    val lessText = stringResource(R.string.artist_bio_less)
    BoxWithConstraints(modifier.fillMaxWidth().padding(top = BIO_TOP_GAP)) {
        val fullWidth = with(density) { maxWidth.roundToPx() }
        val reserve = with(density) { BIO_ACTION_RESERVE.roundToPx() }
        val lineHeightPx =
            remember(style) {
                measurer.measure(AnnotatedString("X"), style, maxLines = 1).size.height
            }.coerceAtLeast(1)
        val reserveLines =
            ceil(with(density) { (BIO_ACTION_HEIGHT - BIO_ACTION_OFFSET).toPx() } / lineHeightPx)
                .toInt()
                .coerceAtLeast(1)
        val bioLines =
            remember(text, fullWidth, reserve, reserveLines, collapsed, expanded, moreText, lessText) {
                when {
                    collapsed -> {
                        splitBioCollapsed(measurer, text, style, fullWidth, reserve, reserveLines, moreText, maxLines = 3)
                    }

                    expanded -> {
                        splitBioExpanded(measurer, text, style, fullWidth, reserve, reserveLines, lessText)
                    }

                    else -> {
                        BioLines(
                            splitBio(measurer, text, style, fullWidth, reserve, reserveLines).map {
                                AnnotatedString(it.ifEmpty { " " })
                            },
                            more = false,
                        )
                    }
                }
            }
        Box {
            Column(Modifier.fillMaxWidth()) {
                bioLines.lines.forEachIndexed { index, line ->
                    val isControl = index == bioLines.lines.lastIndex && (bioLines.more || bioLines.less)
                    Text(
                        text = line,
                        style = style,
                        color = color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier =
                            when {
                                isControl && bioLines.more -> Modifier.testTag(ARTIST_BIO_MORE_TAG).clickable(onClick = onExpand)
                                isControl && bioLines.less -> Modifier.testTag(ARTIST_BIO_LESS_TAG).clickable(onClick = onCollapse)
                                else -> Modifier
                            },
                    )
                }
            }
            Row(Modifier.align(Alignment.TopEnd).offset(y = -BIO_ACTION_OFFSET)) {
                ArtistActions(artist, starred, onToggleStar, onMore)
            }
        }
    }
}

private fun bioLineWidth(
    index: Int,
    fullWidth: Int,
    reserve: Int,
    reserveLines: Int,
): Int = if (index < reserveLines) fullWidth - reserve else fullWidth

/** Splits text at every source line break, then wraps it to the width left by the actions. */
internal fun splitBio(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    fullWidth: Int,
    reserve: Int,
    reserveLines: Int,
): List<String> {
    val lines = mutableListOf<String>()
    text.split('\n').forEach { sourceLine ->
        val words = sourceLine.trim().split(' ').filter { it.isNotEmpty() }
        if (words.isEmpty()) {
            lines += ""
            return@forEach
        }
        var current = ""
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            val available = bioLineWidth(lines.size, fullWidth, reserve, reserveLines)
            if (measurer.measure(AnnotatedString(candidate), style, maxLines = 1).size.width <= available) {
                current = candidate
            } else if (current.isEmpty()) {
                lines += candidate
            } else {
                lines += current
                current = word
            }
        }
        if (current.isNotEmpty()) lines += current
    }
    while (lines.isNotEmpty() && lines.last().isBlank()) lines.removeAt(lines.lastIndex)
    return lines.ifEmpty { listOf("") }
}

internal fun splitBioExpanded(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    fullWidth: Int,
    reserve: Int,
    reserveLines: Int,
    lessText: String,
): BioLines {
    val lines = splitBio(measurer, text, style, fullWidth, reserve, reserveLines).mapTo(mutableListOf()) { AnnotatedString(it) }
    appendBioControl(lines, lessText, measurer, style, fullWidth, reserve, reserveLines)
    return BioLines(lines, more = false, less = true)
}

private fun appendBioControl(
    lines: MutableList<AnnotatedString>,
    label: String,
    measurer: TextMeasurer,
    style: TextStyle,
    fullWidth: Int,
    reserve: Int,
    reserveLines: Int,
) {
    while (lines.isNotEmpty() && lines.last().text.isBlank()) lines.removeAt(lines.lastIndex)
    val control =
        buildAnnotatedString {
            append("  ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(label) }
        }
    if (lines.isEmpty()) {
        lines += control
        return
    }
    val lastIndex = lines.lastIndex
    val available = bioLineWidth(lastIndex, fullWidth, reserve, reserveLines)
    val combined =
        buildAnnotatedString {
            append(lines[lastIndex])
            append(control)
        }
    if (measurer.measure(combined, style, maxLines = 1).size.width <= available) {
        lines[lastIndex] = combined
        return
    }
    val words = lines[lastIndex].text.split(' ').filter { it.isNotEmpty() }
    if (words.size > 1) {
        val tailLine =
            buildAnnotatedString {
                append(words.last())
                append(control)
            }
        if (measurer.measure(tailLine, style, maxLines = 1).size.width <= available) {
            lines[lastIndex] = AnnotatedString(words.dropLast(1).joinToString(" "))
            lines += tailLine
            return
        }
    }
    lines += control
}

internal data class BioLines(
    val lines: List<AnnotatedString>,
    val more: Boolean,
    val less: Boolean = false,
)

/** Collapses text to [maxLines] lines, appending a bold More only when it does not fit. */
internal fun splitBioCollapsed(
    measurer: TextMeasurer,
    text: String,
    style: TextStyle,
    fullWidth: Int,
    reserve: Int,
    reserveLines: Int,
    moreText: String,
    maxLines: Int = 2,
): BioLines {
    val words =
        text
            .replace("\n\n", " ")
            .replace('\n', ' ')
            .split(' ')
            .filter { it.isNotEmpty() }
    if (words.isEmpty()) return BioLines(listOf(AnnotatedString("")), more = false)
    val lines = mutableListOf<AnnotatedString>()
    var index = 0
    while (index < words.size && lines.size < maxLines - 1) {
        var current = ""
        while (index < words.size) {
            val candidate = if (current.isEmpty()) words[index] else "$current ${words[index]}"
            val available = bioLineWidth(lines.size, fullWidth, reserve, reserveLines)
            if (measurer.measure(AnnotatedString(candidate), style, maxLines = 1).size.width <= available) {
                current = candidate
                index++
            } else if (current.isEmpty()) {
                current = words[index]
                index++
                break
            } else {
                break
            }
        }
        lines += AnnotatedString(current)
    }
    if (index >= words.size) {
        return BioLines(lines, more = false)
    }

    val remainingAvailable = bioLineWidth(lines.size, fullWidth, reserve, reserveLines)
    val remaining = words.drop(index).joinToString(" ")
    if (measurer.measure(AnnotatedString(remaining), style, maxLines = 1).size.width <= remainingAvailable) {
        lines += AnnotatedString(remaining)
        return BioLines(lines, more = false)
    }

    val more =
        buildAnnotatedString {
            append("… ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(moreText) }
        }
    val moreWidth =
        measurer.measure(more, style, maxLines = 1).size.width +
            measurer.measure(AnnotatedString(" "), style, maxLines = 1).size.width
    val lastAvailable = (bioLineWidth(lines.size, fullWidth, reserve, reserveLines) - moreWidth).coerceAtLeast(1)
    var truncated = ""
    while (index < words.size) {
        val candidate = if (truncated.isEmpty()) words[index] else "$truncated ${words[index]}"
        if (measurer.measure(AnnotatedString(candidate), style, maxLines = 1).size.width <= lastAvailable) {
            truncated = candidate
            index++
        } else {
            break
        }
    }
    val lastLine =
        buildAnnotatedString {
            if (truncated.isNotEmpty()) append("$truncated ")
            append("… ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(moreText) }
        }
    lines += lastLine
    return BioLines(lines, more = true)
}

@Composable
private fun ArtistSectionHeader(
    text: String,
    icon: ImageVector,
    topGap: Dp = 0.dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = topGap, bottom = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ArtistActions(
    artist: Artist?,
    starred: Boolean,
    onToggleStar: (() -> Unit)?,
    onMore: () -> Unit,
) {
    if (artist == null) return
    IconButton(
        onClick = { onToggleStar?.invoke() },
        enabled = onToggleStar != null,
    ) {
        Icon(
            imageVector = if (starred) Icons.Rounded.Star else Icons.Rounded.StarBorder,
            contentDescription =
                stringResource(
                    if (starred) R.string.actions_unstar else R.string.actions_star,
                ),
            tint =
                if (starred) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
    IconButton(onClick = onMore) {
        Icon(Icons.Rounded.MoreHoriz, contentDescription = stringResource(R.string.more_options))
    }
}

@Composable
private fun ArtistAlbumCard(
    album: Album,
    index: Int,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    downloadStatus: ListDownloadStatus?,
    onClick: () -> Unit,
    onLongClick: (CoverArtRef?) -> Unit,
    onSizeChanged: (Float) -> Unit,
) {
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
                    onClick = onClick,
                    onLongClick = { onLongClick(thumbnail) },
                ).onSizeChanged { onSizeChanged(it.height.toFloat()) },
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(2.dp)),
        ) {
            CoverArt(
                ref = art,
                name = album.name,
                thumbnailRef = thumbnail,
                showPlaceholder = art == null,
                modifier = Modifier.fillMaxSize(),
            )
            ListDownloadIndicator(
                status = downloadStatus,
                modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                scrim = true,
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

@Composable
private fun ArtistTitle(
    text: String,
    style: TextStyle,
    color: Color,
    endReserve: Dp,
    maxLines: Int,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier) {
        val fullWidth = with(density) { maxWidth.roundToPx() }
        val reserve = with(density) { endReserve.roundToPx() }
        val lines =
            remember(text, style, fullWidth, reserve, maxLines) {
                splitTitle(measurer, text, style, fullWidth, reserve, maxLines)
            }
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
    maxLines: Int = ARTIST_NAME_MAX_LINES,
): List<String> {
    if (text.isBlank()) return listOf(text)
    if (measurer.measure(AnnotatedString(text), style, maxLines = 1).size.width <= fullWidth - reserve) {
        return listOf(text)
    }
    val words = text.split(' ').filter { it.isNotEmpty() }
    if (words.size <= 1) return listOf(text)
    val lines = mutableListOf<String>()
    var index = 0
    while (index < words.size && lines.size < maxLines) {
        val remaining = words.subList(index, words.size).joinToString(" ")
        if (lines.size == maxLines - 1 ||
            measurer.measure(AnnotatedString(remaining), style, maxLines = 1).size.width <= fullWidth - reserve
        ) {
            lines.add(remaining)
            break
        }
        var end = index
        var acc = ""
        while (end < words.size) {
            val candidate = if (acc.isEmpty()) words[end] else "$acc ${words[end]}"
            if (measurer.measure(AnnotatedString(candidate), style, maxLines = 1).size.width <= fullWidth) {
                acc = candidate
                end++
            } else {
                break
            }
        }
        if (end == index) {
            acc = words[index]
            end = index + 1
        } else if (end >= words.size && end - index > 1) {
            end -= 1
            acc = words.subList(index, end).joinToString(" ")
        }
        lines.add(acc)
        index = end
    }
    return lines
}
