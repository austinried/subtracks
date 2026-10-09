package com.subtracks.ui.settings

import android.content.res.Resources
import android.widget.Toast
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
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.subtracks.BuildConfig
import com.subtracks.R
import com.subtracks.data.model.Source
import com.subtracks.data.prefs.StreamQuality
import com.subtracks.log.Log
import com.subtracks.ui.components.DismissOnRequest
import com.subtracks.ui.components.rememberViewportFill
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

private val bitrateOptions = listOf(0, 24, 32, 64, 96, 128, 192, 256, 320)
private val streamFormats = listOf(null, "mp3", "opus", "ogg", "webm", "aac", "flac")

private const val PROJECT_HOMEPAGE = "https://github.com/austinried/subtracks"
private const val SUPPORT_URL = "https://ko-fi.com/austinried"

private enum class SettingsDialog { WifiQuality, MobileQuality, DownloadQuality, SyncConcurrency }

@Composable
fun SettingsRoute(
    onAddServer: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenLicenses: () -> Unit,
    onEditServer: (Long) -> Unit,
    onBack: () -> Unit,
    dismissals: Flow<Unit> = emptyFlow(),
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val sources by viewModel.sources.collectAsStateWithLifecycle()
    val activeSourceId by viewModel.activeSourceId.collectAsStateWithLifecycle()
    val wifiQuality by viewModel.wifiQuality.collectAsStateWithLifecycle()
    val mobileQuality by viewModel.mobileQuality.collectAsStateWithLifecycle()
    val syncConcurrency by viewModel.syncConcurrency.collectAsStateWithLifecycle()
    val downloadQuality by viewModel.downloadQuality.collectAsStateWithLifecycle()
    val downloadOverMetered by viewModel.downloadOverMetered.collectAsStateWithLifecycle()
    val scrobbling by viewModel.scrobbling.collectAsStateWithLifecycle()
    val offline by viewModel.offline.collectAsStateWithLifecycle()
    val verboseLogging by viewModel.verboseLogging.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    SettingsScreen(
        sources = sources,
        activeSourceId = activeSourceId,
        wifiQuality = wifiQuality,
        mobileQuality = mobileQuality,
        syncConcurrency = syncConcurrency,
        downloadQuality = downloadQuality,
        downloadOverMetered = downloadOverMetered,
        scrobbling = scrobbling,
        offline = offline,
        verboseLogging = verboseLogging,
        onSelectSource = viewModel::selectSource,
        onEditServer = onEditServer,
        onWifiQualityChange = viewModel::setWifiQuality,
        onMobileQualityChange = viewModel::setMobileQuality,
        onSyncConcurrencyChange = viewModel::setSyncConcurrency,
        onDownloadQualityChange = viewModel::setDownloadQuality,
        onDownloadOverMeteredChange = viewModel::setDownloadOverMetered,
        onScrobblingChange = viewModel::setScrobbling,
        onOfflineChange = viewModel::setOfflineMode,
        onVerboseLoggingChange = viewModel::setVerboseLogging,
        onAddServer = onAddServer,
        onOpenDownloads = onOpenDownloads,
        onOpenLicenses = onOpenLicenses,
        onShareLogs = {
            scope.launch {
                val result =
                    runCatching {
                        val zip = withContext(Dispatchers.IO) { Log.exportZip() }
                        if (zip != null) Log.share(context, zip)
                        zip
                    }
                val message =
                    when {
                        result.isFailure -> R.string.settings_about_share_logs_failed
                        result.getOrNull() == null -> R.string.settings_about_share_logs_empty
                        else -> null
                    }
                message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
            }
        },
        onBack = onBack,
        dismissals = dismissals,
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
    downloadOverMetered: Boolean,
    scrobbling: Boolean,
    modifier: Modifier = Modifier,
    offline: Boolean = false,
    verboseLogging: Boolean = false,
    onSelectSource: (Long) -> Unit,
    onEditServer: (Long) -> Unit,
    onWifiQualityChange: (StreamQuality) -> Unit,
    onMobileQualityChange: (StreamQuality) -> Unit,
    onSyncConcurrencyChange: (Int) -> Unit,
    onDownloadQualityChange: (StreamQuality) -> Unit,
    onDownloadOverMeteredChange: (Boolean) -> Unit,
    onScrobblingChange: (Boolean) -> Unit,
    onOfflineChange: (Boolean) -> Unit = {},
    onVerboseLoggingChange: (Boolean) -> Unit = {},
    onAddServer: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenLicenses: () -> Unit,
    onShareLogs: () -> Unit,
    onBack: () -> Unit,
    dismissals: Flow<Unit> = emptyFlow(),
) {
    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }
    DismissOnRequest(dismissals) { dialog = null }
    val listState = rememberLazyListState()
    val fill = rememberViewportFill(listState)
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.navigation_tabs_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.navigation_back))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(padding).fillMaxSize(),
        ) {
            item { SectionHeader(stringResource(R.string.settings_servers_name)) }
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
                            Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit_source_description, source.name))
                        }
                    },
                    modifier =
                        Modifier.clickable(
                            onClickLabel = stringResource(R.string.use_source_description, source.name),
                        ) { onSelectSource(source.id) },
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
                        Text(stringResource(R.string.settings_servers_actions_add))
                    }
                }
            }
            item { SectionHeader(stringResource(R.string.settings_network_name)) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_network_options_offline_mode)) },
                    supportingContent = {
                        Text(
                            if (offline) {
                                stringResource(R.string.settings_network_options_offline_mode_on)
                            } else {
                                stringResource(R.string.settings_network_options_offline_mode_off)
                            },
                        )
                    },
                    trailingContent = {
                        Switch(checked = offline, onCheckedChange = onOfflineChange)
                    },
                    modifier = Modifier.clickable { onOfflineChange(!offline) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_network_options_max_bitrate_wifi_title)) },
                    supportingContent = { Text(qualityLabel(wifiQuality)) },
                    modifier = Modifier.clickable { dialog = SettingsDialog.WifiQuality },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_network_options_max_bitrate_mobile_title)) },
                    supportingContent = { Text(qualityLabel(mobileQuality)) },
                    modifier = Modifier.clickable { dialog = SettingsDialog.MobileQuality },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.sync_concurrency)) },
                    supportingContent = { Text(concurrencyLabel(LocalResources.current, syncConcurrency)) },
                    modifier = Modifier.clickable { dialog = SettingsDialog.SyncConcurrency },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item { SectionHeader(stringResource(R.string.settings_music_name)) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_music_options_scrobble_title)) },
                    supportingContent = { Text(stringResource(R.string.settings_music_options_scrobble_description_on)) },
                    trailingContent = {
                        Switch(
                            checked = scrobbling,
                            onCheckedChange = onScrobblingChange,
                            colors =
                                SwitchDefaults.colors(
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                                ),
                        )
                    },
                    modifier = Modifier.clickable { onScrobblingChange(!scrobbling) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item { SectionHeader(stringResource(R.string.downloads_title)) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.download_quality)) },
                    supportingContent = { Text(qualityLabel(downloadQuality)) },
                    modifier = Modifier.clickable { dialog = SettingsDialog.DownloadQuality },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.download_over_mobile)) },
                    supportingContent = {
                        Text(
                            if (downloadOverMetered) {
                                stringResource(R.string.download_network_wifi_and_mobile)
                            } else {
                                stringResource(R.string.download_network_wifi_only)
                            },
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = downloadOverMetered,
                            onCheckedChange = onDownloadOverMeteredChange,
                            colors =
                                SwitchDefaults.colors(
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                                ),
                        )
                    },
                    modifier = Modifier.clickable { onDownloadOverMeteredChange(!downloadOverMetered) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.manage_downloads)) },
                    leadingContent = { Icon(Icons.Rounded.Download, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onOpenDownloads),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item { SectionHeader(stringResource(R.string.settings_about_name)) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.app_name)) },
                    supportingContent = {
                        Text(stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME))
                    },
                    leadingContent = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_about_actions_licenses)) },
                    leadingContent = { Icon(Icons.Rounded.Description, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onOpenLicenses),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_about_actions_project_homepage)) },
                    supportingContent = { Text(PROJECT_HOMEPAGE) },
                    leadingContent = { Icon(Icons.Rounded.Language, contentDescription = null) },
                    modifier = Modifier.clickable { runCatching { uriHandler.openUri(PROJECT_HOMEPAGE) } },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_about_actions_support)) },
                    supportingContent = { Text(SUPPORT_URL) },
                    leadingContent = {
                        Icon(
                            Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    modifier = Modifier.clickable { runCatching { uriHandler.openUri(SUPPORT_URL) } },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_about_actions_share_logs)) },
                    leadingContent = { Icon(Icons.Rounded.Share, contentDescription = null) },
                    modifier = Modifier.clickable(onClick = onShareLogs),
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_about_verbose_logging)) },
                    supportingContent = { Text(stringResource(R.string.settings_about_verbose_logging_description)) },
                    trailingContent = {
                        Switch(
                            checked = verboseLogging,
                            onCheckedChange = onVerboseLoggingChange,
                            colors =
                                SwitchDefaults.colors(
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                                ),
                        )
                    },
                    modifier = Modifier.clickable { onVerboseLoggingChange(!verboseLogging) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
            item { Spacer(Modifier.height(fill)) }
        }
    }

    when (dialog) {
        SettingsDialog.WifiQuality -> {
            QualityDialog(
                title = stringResource(R.string.settings_network_options_max_bitrate_wifi_title),
                quality = wifiQuality,
                onSelect = onWifiQualityChange,
                onDismiss = { dialog = null },
            )
        }

        SettingsDialog.MobileQuality -> {
            QualityDialog(
                title = stringResource(R.string.settings_network_options_max_bitrate_mobile_title),
                quality = mobileQuality,
                onSelect = onMobileQualityChange,
                onDismiss = { dialog = null },
            )
        }

        SettingsDialog.DownloadQuality -> {
            QualityDialog(
                title = stringResource(R.string.download_quality),
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

@Composable
private fun qualityLabel(quality: StreamQuality): String {
    val resources = LocalResources.current
    val format = quality.format ?: stringResource(R.string.stream_quality_server_default)
    return stringResource(R.string.download_status_summary, format, bitrateLabel(resources, quality.maxBitrate))
}

private fun bitrateLabel(
    resources: Resources,
    kbps: Int,
): String =
    if (kbps == 0) {
        resources.getString(R.string.settings_network_values_unlimited_kbps)
    } else {
        resources.getString(R.string.settings_network_values_kbps, kbps.toString())
    }

private val syncConcurrencyOptions = listOf(1, 2, 4, 8, 16)

private fun concurrencyLabel(
    resources: Resources,
    value: Int,
): String = if (value <= 1) resources.getString(R.string.sync_concurrency_sequential) else value.toString()

@Composable
private fun ConcurrencyDialog(
    selected: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val resources = LocalResources.current
    var draft by remember(selected) { mutableIntStateOf(selected) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sync_concurrency)) },
        text = {
            ChoiceGroup(
                header = stringResource(R.string.sync_concurrency_parallel),
                options = syncConcurrencyOptions.map { it to concurrencyLabel(resources, it) },
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
            ) { Text(stringResource(R.string.action_done)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.actions_cancel)) }
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
    val resources = LocalResources.current
    var draft by remember(quality) { mutableStateOf(quality) }
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Box {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    ChoiceGroup(
                        header = stringResource(R.string.maximum_bitrate),
                        options = bitrateOptions.map { it to bitrateLabel(resources, it) },
                        selected = draft.maxBitrate,
                        onSelect = { draft = draft.copy(maxBitrate = it) },
                    )
                    Spacer(Modifier.height(16.dp))
                    ChoiceGroup(
                        header = stringResource(R.string.settings_network_options_stream_format),
                        options =
                            streamFormats.map {
                                it to
                                    (it ?: resources.getString(R.string.settings_network_options_stream_format_server_default))
                            },
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
            ) { Text(stringResource(R.string.action_done)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.actions_cancel)) }
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
