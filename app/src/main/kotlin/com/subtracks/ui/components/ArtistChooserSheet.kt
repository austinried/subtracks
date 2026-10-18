package com.subtracks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.subtracks.R
import com.subtracks.data.model.ArtistRef
import kotlinx.coroutines.launch

const val ARTIST_CHOOSER_TAG = "artistChooser"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistChooserSheet(
    artists: List<ArtistRef>,
    onArtistClick: (ArtistRef) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.testTag(ARTIST_CHOOSER_TAG),
    ) {
        Text(
            text = stringResource(R.string.artist_chooser_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            artists.forEach { artist ->
                ListItem(
                    headlineContent = { Text(artist.name.ifBlank { stringResource(R.string.resources_filter_artist) }) },
                    leadingContent = { Icon(Icons.Rounded.Person, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier =
                        Modifier.clickable {
                            scope.launch {
                                sheetState.hide()
                                onArtistClick(artist)
                            }
                        },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}
