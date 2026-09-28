package com.subtracks.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.data.model.Source
import com.subtracks.data.prefs.StreamQuality
import com.subtracks.ui.components.rememberViewportFill
import org.koin.compose.viewmodel.koinViewModel

private val bitrateOptions = listOf(0, 24, 32, 64, 96, 128, 192, 256, 320)
private val streamFormats = listOf(null, "mp3", "opus", "ogg", "webm", "aac", "flac")

private enum class SettingsDialog { WifiQuality, MobileQuality, DownloadQuality, SyncConcurrency }

@Composable
fun SettingsRoute(
    onAddServer: () -> Unit,
    onOpenDownloads: () -> Unit,
    onEditServer: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val sources by viewModel.sources.collectAsStateWithLifecycle()
    val activeSourceId by viewModel.activeSourceId.collectAsStateWithLifecycle()
    val wifiQuality by viewModel.wifiQuality.collectAsStateWithLifecycle()
    val mobileQuality by viewModel.mobileQuality.collectAsStateWithLifecycle()
    val syncConcurrency by viewModel.syncConcurrency.collectAsStateWithLifecycle()
    val downloadQuality by viewModel.downloadQuality.collectAsStateWithLifecycle()
    SettingsScreen(
        sources = sources,
        activeSourceId = activeSourceId,
        wifiQuality = wifiQuality,
        mobileQuality = mobileQuality,
        syncConcurrency = syncConcurrency,
        downloadQuality = downloadQuality,
        onSelectSource = viewModel::selectSource,
        onEditServer = onEditServer,
        onWifiQualityChange = viewModel::setWifiQuality,
        onMobileQualityChange = viewModel::setMobileQuality,
        onSyncConcurrencyChange = viewModel::setSyncConcurrency,
        onDownloadQualityChange = viewModel::setDownloadQuality,
        onAddServer = onAddServer,
        onOpenDownloads = onOpenDownloads,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    sources: List<Source>,
    activeSourceId: Long?,
    wifiQuality: StreamQuality,
    mobileQuality: StreamQuality,
    syncConcurrency: Int,
    downloadQuality: StreamQuality,
    onSelectSource: (Long) -> Unit,
    onEditServer: (Long) -> Unit,
    onWifiQualityChange: (StreamQuality) -> Unit,
    onMobileQualityChange: (StreamQuality) -> Unit,
    onSyncConcurrencyChange: (Int) -> Unit,
    onDownloadQualityChange: (StreamQuality) -> Unit,
    onAddServer: () -> Unit,
    onOpenDownloads: () -> Unit,
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
                title = { Text("Settings") },
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
                        IconButton(onClick = { onEditServer(source.id) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit ${source.name}")
                        }
                    },
                    modifier = Modifier.clickable(onClickLabel = "Use ${source.name}") { onSelectSource(source.id) },
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
                    headlineContent = { Text("Stream quality: Wi-Fi") },
                    supportingContent = { Text(qualityLabel(wifiQuality)) },
                    modifier = Modifier.clickable { dialog = SettingsDialog.WifiQuality },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Stream quality: Mobile") },
                    supportingContent = { Text(qualityLabel(mobileQuality)) },
                    modifier = Modifier.clickable { dialog = SettingsDialog.MobileQuality },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Sync concurrency") },
                    supportingContent = { Text(concurrencyLabel(syncConcurrency)) },
                    modifier = Modifier.clickable { dialog = SettingsDialog.SyncConcurrency },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item { SectionHeader("Downloads") }
            item {
                ListItem(
                    headlineContent = { Text("Download quality") },
                    supportingContent = { Text(qualityLabel(downloadQuality)) },
                    modifier = Modifier.clickable { dialog = SettingsDialog.DownloadQuality },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("Manage downloads") },
                    supportingContent = { Text("Space and deletion") },
                    modifier = Modifier.clickable(onClick = onOpenDownloads),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item { Spacer(Modifier.height(fill)) }
        }
    }

    when (dialog) {
        SettingsDialog.WifiQuality -> {
            QualityDialog(
                title = "Stream quality: Wi-Fi",
                quality = wifiQuality,
                onSelect = onWifiQualityChange,
                onDismiss = { dialog = null },
            )
        }

        SettingsDialog.MobileQuality -> {
            QualityDialog(
                title = "Stream quality: Mobile",
                quality = mobileQuality,
                onSelect = onMobileQualityChange,
                onDismiss = { dialog = null },
            )
        }

        SettingsDialog.DownloadQuality -> {
            QualityDialog(
                title = "Download quality",
                quality = downloadQuality,
                onSelect = onDownloadQualityChange,
                onDismiss = { dialog = null },
            )
        }

        SettingsDialog.SyncConcurrency -> {
            ConcurrencyDialog(
                selected = syncConcurrency,
                onSelect = onSyncConcurrencyChange,
                onDismiss = { dialog = null },
            )
        }

        null -> {
            Unit
        }
    }
}

private fun qualityLabel(quality: StreamQuality): String = "${bitrateLabel(quality.maxBitrate)} · ${quality.format ?: "Server default"}"

private fun bitrateLabel(kbps: Int): String = if (kbps == 0) "Unlimited" else "${kbps}kbps"

private val syncConcurrencyOptions = listOf(1, 2, 4, 8, 16)

private fun concurrencyLabel(value: Int): String = if (value <= 1) "1 (sequential)" else value.toString()

@Composable
private fun ConcurrencyDialog(
    selected: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(selected) { mutableStateOf(selected) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sync concurrency") },
        text = {
            ChoiceGroup(
                header = "Parallel server requests while syncing",
                options = syncConcurrencyOptions.map { it to concurrencyLabel(it) },
                selected = draft,
                onSelect = { draft = it },
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSelect(draft)
                    onDismiss()
                },
            ) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun QualityDialog(
    title: String,
    quality: StreamQuality,
    onSelect: (StreamQuality) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(quality) { mutableStateOf(quality) }
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Box {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    ChoiceGroup(
                        header = "Maximum bitrate",
                        options = bitrateOptions.map { it to bitrateLabel(it) },
                        selected = draft.maxBitrate,
                        onSelect = { draft = draft.copy(maxBitrate = it) },
                    )
                    Spacer(Modifier.height(16.dp))
                    ChoiceGroup(
                        header = "Preferred format",
                        options = streamFormats.map { it to (it ?: "Use server default") },
                        selected = draft.format,
                        onSelect = { draft = draft.copy(format = it) },
                    )
                }
                if (scrollState.canScrollForward) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(32.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, MaterialTheme.colorScheme.surfaceContainerHigh),
                                    ),
                                ),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSelect(draft)
                    onDismiss()
                },
            ) { Text("Done") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun <T> ChoiceGroup(
    header: String,
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column {
        Text(header, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
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
