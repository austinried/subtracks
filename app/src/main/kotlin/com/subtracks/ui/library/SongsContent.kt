package com.subtracks.ui.library

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongListItem
import com.subtracks.ui.components.CoverArt
import com.subtracks.ui.components.EmptyState
import com.subtracks.ui.components.LoadingState

@Composable
fun SongsContent(
    items: LazyPagingItems<SongListItem>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    bottomInset: Dp,
    modifier: Modifier = Modifier,
) {
    when {
        items.itemCount == 0 && items.loadState.refresh is LoadState.Loading -> {
            LoadingState(modifier)
        }

        items.itemCount == 0 -> {
            EmptyState("No songs yet.", modifier)
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomInset),
            ) {
                items(count = items.itemCount, key = items.itemKey { it.song.id }) { index ->
                    val item = items[index]
                    if (item != null) {
                        SongRow(song = item.song, coverArtId = item.coverArt, coverArt = coverArt)
                    }
                }
            }
        }
    }
}

@Composable
fun SongRow(
    song: Song,
    coverArtId: String? = null,
    coverArt: ((String?, Boolean) -> CoverArtRef?)? = null,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        leadingContent =
            if (coverArt != null) {
                {
                    CoverArt(
                        ref = coverArt(coverArtId, true),
                        name = song.album ?: song.title,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)),
                    )
                }
            } else {
                null
            },
        headlineContent = { Text(song.title) },
        supportingContent = {
            val subtitle = listOfNotNull(song.artist, song.album).joinToString(" • ")
            if (subtitle.isNotEmpty()) Text(subtitle)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
