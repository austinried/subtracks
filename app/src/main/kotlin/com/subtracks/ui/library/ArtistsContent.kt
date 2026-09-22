package com.subtracks.ui.library

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

@Composable
fun ArtistsContent(
    items: LazyPagingItems<Artist>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    bottomInset: Dp,
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
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomInset),
            ) {
                items(count = items.itemCount, key = items.itemKey { it.id }) { index ->
                    val artist = items[index]
                    if (artist != null) {
                        ListItem(
                            headlineContent = { Text(artist.name) },
                            supportingContent = { Text("${artist.albumCount} albums") },
                            leadingContent = {
                                CoverArt(
                                    ref = coverArt(artist.coverArt, true),
                                    name = artist.name,
                                    modifier = Modifier.size(48.dp).clip(CircleShape),
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }
            }
        }
    }
}
