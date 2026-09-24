package com.subtracks.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.data.model.Source
import com.subtracks.ui.components.rememberViewportFill
import org.koin.compose.viewmodel.koinViewModel

private val bitrateOptions = listOf(0, 24, 32, 64, 96, 128, 192, 256, 320)
private val streamFormats = listOf(null, "mp3", "opus", "ogg", "webm", "aac", "flac")

private enum class SettingsDialog { Bitrate, Format }

@Composable
fun SettingsRoute(
    onAddServer: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val sources by viewModel.sources.collectAsStateWithLifecycle()
    val activeSourceId by viewModel.activeSourceId.collectAsStateWithLifecycle()
    val maxBitrate by viewModel.maxBitrate.collectAsStateWithLifecycle()
    val streamFormat by viewModel.streamFormat.collectAsStateWithLifecycle()
    SettingsScreen(
        sources = sources,
        activeSourceId = activeSourceId,
        maxBitrate = maxBitrate,
        streamFormat = streamFormat,
        onSelectSource = viewModel::selectSource,
        onDeleteSource = viewModel::deleteSource,
        onMaxBitrateChange = viewModel::setMaxBitrate,
        onStreamFormatChange = viewModel::setStreamFormat,
        onAddServer = onAddServer,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    sources: List<Source>,
    activeSourceId: Long?,
    maxBitrate: Int,
    streamFormat: String?,
    onSelectSource: (Long) -> Unit,
    onDeleteSource: (Long) -> Unit,
    onMaxBitrateChange: (Int) -> Unit,
    onStreamFormatChange: (String?) -> Unit,
    onAddServer: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    val listState = rememberLazyListState()
    val fill = rememberViewportFill(listState)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(padding).fillMaxSize(),
        ) {
            item { SectionHeader("Servers") }
            items(sources.size, key = { sources[it].id }) { index ->
                val source = sources[index]
                ListItem(
                    headlineContent = { Text(source.name) },
                    supportingContent = {
                        Text(source.address, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    leadingContent = {
                        RadioButton(
                            selected = source.id == activeSourceId,
                            onClick = { onSelectSource(source.id) },
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { onDeleteSource(source.id) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Remove ${source.name}")
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Button(onClick = onAddServer) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add server")
                    }
                }
            }
            item { SectionHeader("Network") }
            item {
                ListItem(
                    headlineContent = { Text("Maximum bitrate") },
                    supportingContent = { Text(bitrateLabel(maxBitrate)) },
                    modifier = Modifier.clickable { dialog = SettingsDialog.Bitrate },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Preferred stream format") },
                    supportingContent = { Text(streamFormat ?: "Use server default") },
                    modifier = Modifier.clickable { dialog = SettingsDialog.Format },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item { Spacer(Modifier.height(fill)) }
        }
    }

    when (dialog) {
        SettingsDialog.Bitrate -> {
            ChoiceDialog(
                title = "Maximum bitrate",
                options = bitrateOptions.map { it to bitrateLabel(it) },
                selected = maxBitrate,
                onSelect = { value ->
                    onMaxBitrateChange(value)
                    dialog = null
                },
                onDismiss = { dialog = null },
            )
        }

        SettingsDialog.Format -> {
            ChoiceDialog(
                title = "Preferred stream format",
                options = streamFormats.map { it to (it ?: "Use server default") },
                selected = streamFormat,
                onSelect = { value ->
                    onStreamFormatChange(value)
                    dialog = null
                },
                onDismiss = { dialog = null },
            )
        }

        null -> {
            Unit
        }
    }
}

private fun bitrateLabel(kbps: Int): String = if (kbps == 0) "Unlimited" else "${kbps}kbps"

@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(value) },
                    ) {
                        RadioButton(selected = value == selected, onClick = { onSelect(value) })
                        Spacer(Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}
