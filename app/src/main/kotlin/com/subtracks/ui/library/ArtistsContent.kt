package com.subtracks.ui.library

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.subtracks.R
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.components.EmptyState
import com.subtracks.ui.components.FilteredEmptyState
import com.subtracks.ui.components.ListDownloadIndicator
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.components.ResetScrollOnChange
import com.subtracks.ui.components.rememberViewportFill
import com.subtracks.ui.theme.PrefetchArtworkSeeds

@Composable
fun ArtistsContent(
    items: LazyPagingItems<Artist>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    bottomInset: Dp,
    onArtistClick: (Artist) -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (MenuTarget) -> Unit = {},
    filtered: Boolean = false,
    onClearFilters: () -> Unit = {},
    resetKey: Any? = null,
    topInset: Dp = 0.dp,
    onSync: () -> Unit = {},
    downloadStatuses: Map<String, ListDownloadStatus> = emptyMap(),
) {
    when {
        items.itemCount == 0 && items.loadState.refresh is LoadState.Loading -> {
            LoadingState(modifier)
        }

        items.itemCount == 0 -> {
            if (filtered) {
                FilteredEmptyState(onClearFilters, modifier)
            } else {
                EmptyState(stringResource(R.string.artists_empty), modifier, actionLabel = stringResource(R.string.sync), onAction = onSync)
            }
        }

        else -> {
            val listState = rememberLazyListState()
            ResetScrollOnChange(resetKey, { items.loadState.refresh }) { listState.scrollToItem(0) }
            val fill = rememberViewportFill(listState)
            LazyColumn(
                state = listState,
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = topInset, bottom = bottomInset),
            ) {
                items(count = items.itemCount, key = items.itemKey { it.id }) { index ->
                    val artist = items[index]
                    if (artist != null) {
                        PrefetchArtworkSeeds(coverArt(artist.coverArt, true))
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = artist.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            supportingContent = {
                                Text(
                                    text =
                                        pluralStringResource(
                                            R.plurals.resources_album_count,
                                            artist.albumCount.toInt(),
                                            artist.albumCount,
                                        ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            leadingContent = {
                                CoverArt(
                                    ref = coverArt(artist.coverArt, true),
                                    name = artist.name,
                                    modifier = Modifier.size(48.dp).clip(CircleShape),
                                )
                            },
                            trailingContent = {
                                ListDownloadIndicator(downloadStatuses[artist.id], Modifier.padding(end = 8.dp))
                            },
                            modifier =
                                Modifier.combinedClickable(
                                    onClick = { onArtistClick(artist) },
                                    onLongClick = {
                                        onLongClick(MenuTarget.Artist(artist, coverArt(artist.coverArt, true), downloadStatuses[artist.id]))
                                    },
                                ),
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }
                item { Spacer(Modifier.height(fill)) }
            }
        }
    }
}
