package com.subtracks.ui.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.ArtworkTheme
import com.subtracks.ui.theme.HeroGradient
import com.subtracks.ui.theme.baseArtworkColors
import com.subtracks.ui.theme.heroBarColor

private const val FADE_DISTANCE_DP = 64

/**
 * A themed detail screen: endless artwork gradient, a fading top bar, and a lazy list whose first
 * item is [header]. The header and its controls are measured so the bar fades in as they scroll off.
 *
 * [content] emits the rows after the header and assumes they share a uniform height, which is used
 * to estimate the scroll offset for the bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroDetailScaffold(
    artwork: ArtworkColors?,
    title: String,
    onBack: () -> Unit,
    header: @Composable (controlsModifier: Modifier, topInset: Dp) -> Unit,
    content: LazyListScope.(rowModifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val effectiveArtwork = artwork ?: baseArtworkColors
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        ArtworkTheme(effectiveArtwork) {
            BoxWithConstraints(modifier.fillMaxSize().background(Color.Black)) {
                val listState = rememberLazyListState()
                val fill = rememberViewportFill(listState)
                val density = LocalDensity.current
                val screenHeightPx = with(density) { maxHeight.toPx() }
                val statusBarTop = WindowInsets.statusBars.getTop(density)
                val statusBarDp = with(density) { statusBarTop.toDp() }
                val navBarBottom = WindowInsets.navigationBars.getBottom(density)
                val barHeightPx = statusBarTop + with(density) { TopAppBarDefaults.TopAppBarExpandedHeight.toPx() }
                val fadeDistancePx = with(density) { FADE_DISTANCE_DP.dp.toPx() }

                var headerHeightPx by remember { mutableFloatStateOf(0f) }
                var rowHeightPx by remember { mutableFloatStateOf(0f) }
                var controlsTopPx by remember { mutableFloatStateOf(0f) }
                val scrollPx by remember {
                    derivedStateOf {
                        val index = listState.firstVisibleItemIndex
                        val before = if (index <= 0) 0f else headerHeightPx + rowHeightPx * (index - 1)
                        (before + listState.firstVisibleItemScrollOffset).coerceAtLeast(0f)
                    }
                }
                val barFraction by remember {
                    derivedStateOf {
                        if (controlsTopPx <= 0f || fadeDistancePx <= 0f) {
                            0f
                        } else {
                            ((scrollPx - (controlsTopPx - barHeightPx)) / fadeDistancePx).coerceIn(0f, 1f)
                        }
                    }
                }
                val barColor by
                    remember(effectiveArtwork, barHeightPx, screenHeightPx) {
                        derivedStateOf {
                            heroBarColor(effectiveArtwork, scrollPx, barHeightPx, screenHeightPx)
                        }
                    }

                HeroGradient(
                    colors = effectiveArtwork,
                    scrollPx = { scrollPx },
                    modifier = Modifier.fillMaxSize(),
                )

                LazyColumn(
                    state = listState,
                    contentPadding =
                        PaddingValues(
                            bottom = 16.dp + with(density) { navBarBottom.toDp() },
                        ),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        Box(Modifier.onSizeChanged { headerHeightPx = it.height.toFloat() }) {
                            header(
                                Modifier.onGloballyPositioned { controlsTopPx = it.positionInParent().y },
                                statusBarDp,
                            )
                        }
                    }
                    content(Modifier.onSizeChanged { rowHeightPx = it.height.toFloat() })
                    item { Spacer(Modifier.height(fill)) }
                }

                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .graphicsLayer { alpha = 1f - barFraction }
                        .statusBarScrim(),
                )

                TopAppBar(
                    title = {
                        Text(
                            text = title,
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
                            .drawBehind { drawRect(barColor.copy(alpha = barFraction)) }
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
private fun HeroSubtitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

fun ListDownloadStatus.actions(): List<BulkDownloadAction> =
    when {
        complete -> listOf(BulkDownloadAction.Delete)
        downloading > 0 -> listOf(BulkDownloadAction.Cancel)
        downloaded > 0 -> listOf(BulkDownloadAction.Download, BulkDownloadAction.Delete)
        else -> listOf(BulkDownloadAction.Download)
    }

fun ListDownloadStatus.action(): BulkDownloadAction = actions().first()

private fun downloadActionLabel(action: BulkDownloadAction): String =
    when (action) {
        BulkDownloadAction.Download -> "Download"
        BulkDownloadAction.Cancel -> "Cancel download"
        BulkDownloadAction.Delete -> "Delete download"
    }

@Composable
fun HeroHeader(
    art: CoverArtRef?,
    name: String,
    subtitle: String,
    hasSongs: Boolean,
    onPlay: () -> Unit,
    onShuffle: () -> Unit,
    downloadStatus: ListDownloadStatus,
    onDownloadAction: (BulkDownloadAction) -> Unit,
    onMore: () -> Unit,
    topInset: Dp,
    controlsModifier: Modifier = Modifier,
    thumbnailRef: CoverArtRef? = null,
    comment: String? = null,
    onSubtitleClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = topInset + 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CoverArt(
            ref = art,
            name = name,
            thumbnailRef = thumbnailRef,
            square = false,
            elevation = 3.dp,
            modifier = Modifier.fillMaxWidth(0.86f),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val subtitleIsTarget = onSubtitleClick != null && subtitle.isNotEmpty() && comment.isNullOrBlank()
        if (subtitleIsTarget) {
            // The line plus the 4dp lead-in above and 8dp below: shorter than the 48dp
            // recommendation, deliberately, so it stays 12dp clear of the controls rather
            // than reaching down to them. Hugs the text instead of the row.
            Box(
                modifier =
                    Modifier
                        .heightIn(min = 32.dp)
                        .clickable(onClickLabel = "Open artist") { onSubtitleClick() },
                contentAlignment = Alignment.TopCenter,
            ) {
                HeroSubtitle(subtitle, Modifier.padding(top = 4.dp))
            }
        } else if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            HeroSubtitle(subtitle)
        }
        if (!comment.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            HeroSubtitle(comment)
        }
        if (!subtitleIsTarget) Spacer(Modifier.height(20.dp)) else Spacer(Modifier.height(12.dp))
        Row(
            modifier = controlsModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val action = downloadStatus.action()
            val canDelete = BulkDownloadAction.Delete in downloadStatus.actions()
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .combinedClickable(
                            enabled = hasSongs,
                            role = Role.Button,
                            onClick = { onDownloadAction(action) },
                            onClickLabel = downloadActionLabel(action),
                            onLongClick = if (canDelete) ({ onDownloadAction(BulkDownloadAction.Delete) }) else null,
                            onLongClickLabel = if (canDelete) "Delete download" else null,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                when (action) {
                    BulkDownloadAction.Delete -> {
                        Icon(
                            imageVector = Icons.Rounded.DownloadDone,
                            contentDescription = "Delete download",
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    BulkDownloadAction.Cancel -> {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { downloadStatus.downloaded.toFloat() / downloadStatus.total },
                                modifier = Modifier.size(26.dp),
                                strokeWidth = 2.dp,
                            )
                            Icon(Icons.Rounded.Close, contentDescription = "Cancel download", modifier = Modifier.size(16.dp))
                        }
                    }

                    BulkDownloadAction.Download -> {
                        Icon(Icons.Rounded.Download, contentDescription = "Download", modifier = Modifier.size(24.dp))
                    }
                }
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val dividerColor =
                    if (hasSongs) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                Box(contentAlignment = Alignment.Center) {
                    Row {
                        FilledIconButton(
                            onClick = onPlay,
                            enabled = hasSongs,
                            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                            modifier = Modifier.width(68.dp).height(48.dp),
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", modifier = Modifier.size(30.dp))
                        }
                        FilledIconButton(
                            onClick = onShuffle,
                            enabled = hasSongs,
                            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                            modifier = Modifier.width(68.dp).height(48.dp),
                        ) {
                            Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle play", modifier = Modifier.size(30.dp))
                        }
                    }
                    Box(
                        Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(dividerColor),
                    )
                }
            }
            IconButton(onClick = onMore, modifier = Modifier.padding(horizontal = 8.dp)) {
                Icon(Icons.Rounded.MoreHoriz, contentDescription = "More options")
            }
        }
    }
}
