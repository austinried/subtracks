package com.subtracks.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.components.EmptyState
import com.subtracks.ui.components.LoadingState
import com.subtracks.ui.components.stretchOverscroll

@Composable
fun ArtistsContent(
    items: LazyPagingItems<Artist>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    bottomInset: Dp,
    onArtistClick: (Artist) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        items.itemCount == 0 && items.loadState.refresh is LoadState.Loading -> {
            LoadingState(modifier)
        }

        items.itemCount == 0 -> {
            EmptyState("No artists yet.", modifier)
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize().stretchOverscroll(),
                contentPadding = PaddingValues(bottom = bottomInset),
            ) {
                items(count = items.itemCount, key = items.itemKey { it.id }) { index ->
                    val artist = items[index]
                    if (artist != null) {
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
                                    text = "${artist.albumCount} albums",
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
                            modifier = Modifier.clickable { onArtistClick(artist) },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }
            }
        }
    }
}
