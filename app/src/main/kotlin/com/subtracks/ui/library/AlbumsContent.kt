package com.subtracks.ui.library

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.subtracks.data.model.Album
import com.subtracks.data.model.CoverArtRef
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.components.EmptyState
import com.subtracks.ui.components.FilteredEmptyState
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.components.ResetScrollOnChange
import com.subtracks.ui.components.rememberViewportFill
import com.subtracks.ui.theme.PrefetchArtworkSeeds

const val ALBUM_COVER_TAG = "album-cover"

@Composable
fun AlbumsContent(
    items: LazyPagingItems<Album>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    bottomInset: Dp,
    onAlbumClick: (Album) -> Unit,
    onLongClick: (MenuTarget) -> Unit = {},
    filtered: Boolean = false,
    onClearFilters: () -> Unit = {},
    resetKey: Any? = null,
    topInset: Dp = 0.dp,
    onSync: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when {
        items.itemCount == 0 && items.loadState.refresh is LoadState.Loading -> {
            LoadingState(modifier)
        }

        items.itemCount == 0 -> {
            if (filtered) {
                FilteredEmptyState(onClearFilters, modifier)
            } else {
                EmptyState(
                    text = "No albums yet.\nSync with your server to fill your library.",
                    modifier = modifier,
                    actionLabel = "Sync",
                    onAction = onSync,
                )
            }
        }

        else -> {
            val gridState = rememberLazyGridState()
            ResetScrollOnChange(resetKey, { items.loadState.refresh }) { gridState.scrollToItem(0) }
            val fill = rememberViewportFill(gridState)
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 8.dp, top = topInset + 8.dp, end = 8.dp, bottom = bottomInset + 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = modifier.fillMaxSize(),
            ) {
                items(count = items.itemCount, key = items.itemKey { it.id }) { index ->
                    val album = items[index]
                    if (album != null) {
                        val art = coverArt(album.coverArt, true)
                        PrefetchArtworkSeeds(art)
                        CoverArt(
                            ref = art,
                            name = album.name,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(2.dp))
                                    .combinedClickable(
                                        onClick = { onAlbumClick(album) },
                                        onLongClick = { onLongClick(MenuTarget.Album(album, art)) },
                                    ).testTag(ALBUM_COVER_TAG),
                        )
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(Modifier.height(fill))
                }
            }
        }
    }
}
