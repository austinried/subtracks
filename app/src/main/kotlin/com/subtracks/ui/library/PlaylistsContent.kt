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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.model.Playlist
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.components.EmptyState
import com.subtracks.ui.components.FilteredEmptyState
import com.subtracks.ui.components.ListDownloadIndicator
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.components.ResetScrollOnChange
import com.subtracks.ui.components.rememberViewportFill

@Composable
fun PlaylistsContent(
    items: LazyPagingItems<Playlist>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    bottomInset: Dp,
    onPlaylistClick: (Playlist) -> Unit,
    onLongClick: (MenuTarget) -> Unit = {},
    filtered: Boolean = false,
    onClearFilters: () -> Unit = {},
    resetKey: Any? = null,
    topInset: Dp = 0.dp,
    onSync: () -> Unit = {},
    downloadStatuses: Map<String, ListDownloadStatus> = emptyMap(),
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
                    stringResource(R.string.playlists_empty),
                    modifier,
                    actionLabel = stringResource(R.string.sync),
                    onAction = onSync,
                )
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
                    val playlist = items[index]
                    if (playlist != null) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = playlist.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            supportingContent = {
                                Text(
                                    text =
                                        playlist.comment?.takeIf { it.isNotBlank() }
                                            ?: pluralStringResource(
                                                R.plurals.resources_song_count,
                                                playlist.songCount.toInt(),
                                                playlist.songCount,
                                            ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            leadingContent = {
                                CoverArt(
                                    ref = coverArt(playlist.coverArt, true),
                                    name = playlist.name,
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                )
                            },
                            trailingContent = {
                                ListDownloadIndicator(downloadStatuses[playlist.id], Modifier.padding(end = 8.dp))
                            },
                            modifier =
                                Modifier.combinedClickable(
                                    onClick = { onPlaylistClick(playlist) },
                                    onLongClick = {
                                        onLongClick(
                                            MenuTarget.Playlist(playlist, coverArt(playlist.coverArt, true), downloadStatuses[playlist.id]),
                                        )
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
