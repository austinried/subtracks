package com.subtracks.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.ui.components.CoverArt
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val ART_HEIGHT = 420.dp
private val TITLE_INSET = 16.dp
private val FADE_LEAD = 24.dp

@Composable
fun ArtistDetailRoute(
    artistId: String,
    onBack: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    viewModel: ArtistDetailViewModel = koinViewModel(key = artistId) { parametersOf(artistId) },
) {
    val artist by viewModel.artist.collectAsStateWithLifecycle(initialValue = null)
    val albums by viewModel.albums.collectAsStateWithLifecycle(initialValue = emptyList())
    val art by viewModel.art.collectAsStateWithLifecycle()
    ArtistDetailScreen(
        artist = artist,
        albums = albums,
        art = art,
        coverArt = viewModel::coverArt,
        onBack = onBack,
        onAlbumClick = onAlbumClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artist: Artist?,
    albums: List<Album>,
    art: CoverArtRef?,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onBack: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyGridState()
    val density = LocalDensity.current
    val lineHeight =
        with(density) {
            MaterialTheme.typography.headlineLarge.lineHeight
                .toDp()
        }
    val statusBarTop = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
    val barHeight = statusBarTop + TopAppBarDefaults.TopAppBarExpandedHeight
    val imageTitleTop = ART_HEIGHT - TITLE_INSET - lineHeight
    val fadeStartPx = with(density) { (imageTitleTop - barHeight - FADE_LEAD).toPx() }
    val fadeEndPx = with(density) { (imageTitleTop + lineHeight / 2 - barHeight).toPx() }
    val barFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                ((listState.firstVisibleItemScrollOffset - fadeStartPx) / (fadeEndPx - fadeStartPx)).coerceIn(0f, 1f)
            }
        }
    }
    val imageNameStyle =
        MaterialTheme.typography.headlineLarge.copy(
            shadow =
                Shadow(
                    color = Color.Black.copy(alpha = 0.8f),
                    offset = Offset(0f, 2f),
                    blurRadius = 10f,
                ),
        )
    val barNameStyle =
        MaterialTheme.typography.headlineLarge.copy(
            shadow =
                Shadow(
                    color = Color.Black.copy(alpha = 0.8f * barFraction),
                    offset = Offset(0f, 2f),
                    blurRadius = 10f,
                ),
        )

    Box(modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = listState,
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    CoverArt(
                        ref = art,
                        name = artist?.name.orEmpty(),
                        modifier = Modifier.fillMaxWidth().height(ART_HEIGHT),
                    )
                    Text(
                        text = artist?.name.orEmpty(),
                        style = imageNameStyle,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.BottomStart).padding(TITLE_INSET),
                    )
                }
            }
            itemsIndexed(albums, key = { _, album -> album.id }) { index, album ->
                Column(
                    modifier =
                        Modifier
                            .padding(
                                start = if (index % 2 == 0) 16.dp else 0.dp,
                                end = if (index % 2 == 1) 16.dp else 0.dp,
                            ).clickable { onAlbumClick(album) },
                ) {
                    CoverArt(
                        ref = coverArt(album.coverArt, false),
                        name = album.name,
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
        }

        TopAppBar(
            title = {
                Text(
                    text = artist?.name.orEmpty(),
                    style = barNameStyle,
                    color = Color.White.copy(alpha = barFraction),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Box {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = Color.Black.copy(alpha = 0.8f * barFraction),
                            modifier =
                                Modifier
                                    .offset(x = 0.dp, y = 2.dp)
                                    .blur(6.dp, BlurredEdgeTreatment.Unbounded),
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White.copy(alpha = barFraction),
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            modifier = Modifier.align(Alignment.TopStart).background(Color.Black.copy(alpha = barFraction)),
        )
    }
}
