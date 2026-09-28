package com.subtracks.ui.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.FilterAltOff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.DownloadList
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.model.Playlist
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.prefs.LibraryListTab
import com.subtracks.data.prefs.ListQuery
import com.subtracks.data.prefs.PlaylistSort
import com.subtracks.data.prefs.StarredFilter
import com.subtracks.data.source.StarType
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.ContextMenuHost
import com.subtracks.ui.components.DeleteDownloadsDialog
import com.subtracks.ui.components.ItemActions
import com.subtracks.ui.components.MenuTarget
import com.subtracks.ui.components.PendingDownloadDelete
import com.subtracks.ui.components.bulkRef
import com.subtracks.ui.components.statusBarScrim
import com.subtracks.ui.theme.ArtworkColors
import com.subtracks.ui.theme.librarySurfaceColor
import com.subtracks.ui.theme.playerSurfaceColor
import com.subtracks.ui.theme.rememberArtworkColors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

enum class LibraryTab(
    val label: String,
    val icon: ImageVector,
) {
    Albums("Albums", Icons.Rounded.Album),
    Artists("Artists", Icons.Rounded.Person),
    Playlists("Playlists", Icons.AutoMirrored.Rounded.PlaylistPlay),
}

data class SortOption(
    val value: String,
    val label: String,
    val descendingByDefault: Boolean = false,
)

fun LibraryTab.listTab(): LibraryListTab = LibraryListTab.valueOf(name)

fun sortOptionsFor(tab: LibraryTab): List<SortOption> =
    when (tab) {
        LibraryTab.Albums -> {
            listOf(
                SortOption(AlbumSort.Name.name, "Name"),
                SortOption(AlbumSort.Artist.name, "Artist"),
                SortOption(AlbumSort.Year.name, "Year", descendingByDefault = true),
                SortOption(AlbumSort.Added.name, "Added", descendingByDefault = true),
                SortOption(AlbumSort.Starred.name, "Starred", descendingByDefault = true),
            )
        }

        LibraryTab.Artists -> {
            listOf(
                SortOption(ArtistSort.Name.name, "Name"),
                SortOption(ArtistSort.AlbumCount.name, "Albums", descendingByDefault = true),
                SortOption(ArtistSort.Starred.name, "Starred", descendingByDefault = true),
            )
        }

        LibraryTab.Playlists -> {
            listOf(
                SortOption(PlaylistSort.Name.name, "Name"),
                SortOption(PlaylistSort.Added.name, "Added", descendingByDefault = true),
                SortOption(PlaylistSort.Updated.name, "Updated", descendingByDefault = true),
            )
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryRoute(
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onOpenSettings: () -> Unit,
    onViewAlbum: (String) -> Unit,
    onViewArtist: (String) -> Unit,
    contextMenuHost: ContextMenuHost? = null,
    setStar: (StarType, String, Boolean) -> Unit,
    bottomInset: Dp,
    viewModel: LibraryViewModel = koinViewModel(),
    playbackController: PlaybackController = koinInject(),
) {
    var selectedTabName by rememberSaveable { mutableStateOf(LibraryTab.Albums.name) }
    var previousTabName by rememberSaveable { mutableStateOf(LibraryTab.Albums.name) }
    val selectedTab = LibraryTab.entries.firstOrNull { it.name == selectedTabName } ?: LibraryTab.Albums
    val previousTab = LibraryTab.entries.firstOrNull { it.name == previousTabName } ?: LibraryTab.Albums
    val syncing by viewModel.syncing.collectAsStateWithLifecycle()
    val listTab = selectedTab.listTab()
    LaunchedEffect(selectedTab) {
        if (previousTab != selectedTab) {
            viewModel.setSearch(previousTab.listTab(), "")
            previousTabName = selectedTab.name
        }
    }
    val listQuery by viewModel.listQuery(listTab).collectAsStateWithLifecycle()
    val search by viewModel.search(listTab).collectAsStateWithLifecycle()
    val resetKeys =
        LibraryTab.entries.associateWith { tab ->
            val query by viewModel.listQuery(tab.listTab()).collectAsStateWithLifecycle()
            val term by viewModel.search(tab.listTab()).collectAsStateWithLifecycle()
            "${query.sort}|${query.descending}|${query.starred}|${query.downloaded}|$term"
        }
    val playback by playbackController.state.collectAsStateWithLifecycle()
    val artwork = rememberArtworkColors(playbackController.coverArt(playback.item, thumbnail = true))
    val downloads by viewModel.downloads(listTab).collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var pendingDelete by remember { mutableStateOf<PendingDownloadDelete?>(null) }
    val itemActions =
        ItemActions(
            playNext = { playbackController.playNext(it.sourceId, it.kind, it.refId) },
            addToQueue = { playbackController.addToQueue(it.sourceId, it.kind, it.refId) },
            bulkDownload = { target, action ->
                target.bulkRef()?.let { (list, refId) ->
                    if (action == BulkDownloadAction.Delete) {
                        scope.launch {
                            pendingDelete =
                                PendingDownloadDelete(target.title, list, refId, viewModel.downloadedBytes(list, refId))
                        }
                    } else {
                        viewModel.onDownloadAction(list, refId, action)
                    }
                }
            },
            setStar = setStar,
            viewAlbum = onViewAlbum,
            viewArtist = onViewArtist,
        )
    LibraryScreen(
        selectedTab = selectedTab,
        onTabSelected = { selectedTabName = it.name },
        syncing = syncing,
        bottomInset = bottomInset,
        artwork = artwork,
        albums = viewModel.albums,
        artists = viewModel.artists,
        playlists = viewModel.playlists,
        coverArt = viewModel::coverArt,
        onAlbumClick = onAlbumClick,
        onArtistClick = onArtistClick,
        onPlaylistClick = onPlaylistClick,
        onSync = viewModel::sync,
        onOpenSettings = onOpenSettings,
        onItemLongClick = { contextMenuHost?.show(it, itemActions) },
        listQuery = listQuery,
        resetKeys = resetKeys,
        sortOptions = sortOptionsFor(selectedTab),
        starredSupported = listTab.supportsStarred,
        onSortChange = { viewModel.setListQuery(listTab, listQuery.copy(sort = it)) },
        onToggleSortDirection = { viewModel.setListQuery(listTab, listQuery.copy(descending = !listQuery.descending)) },
        onCycleStarred = {
            val next =
                when (listQuery.starred) {
                    StarredFilter.Any -> StarredFilter.Starred
                    StarredFilter.Starred -> StarredFilter.NotStarred
                    StarredFilter.NotStarred -> StarredFilter.Any
                }
            viewModel.setListQuery(listTab, listQuery.copy(starred = next))
        },
        onClearFilters = {
            viewModel.setListQuery(listTab, listQuery.copy(starred = StarredFilter.Any, downloaded = false))
            viewModel.setSearch(listTab, "")
        },
        search = search,
        onSearchChange = { viewModel.setSearch(listTab, it) },
        onToggleDownloaded = { viewModel.setListQuery(listTab, listQuery.copy(downloaded = !listQuery.downloaded)) },
        albumDownloads = downloads.takeIf { listTab == LibraryListTab.Albums }.orEmpty(),
        artistDownloads = downloads.takeIf { listTab == LibraryListTab.Artists }.orEmpty(),
        playlistDownloads = downloads.takeIf { listTab == LibraryListTab.Playlists }.orEmpty(),
    )

    pendingDelete?.let { pending ->
        DeleteDownloadsDialog(
            name = pending.name,
            bytes = pending.bytes,
            onConfirm = { viewModel.onDownloadAction(pending.list, pending.refId, BulkDownloadAction.Delete) },
            onDismiss = { pendingDelete = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    albums: Flow<PagingData<Album>>,
    artists: Flow<PagingData<Artist>>,
    playlists: Flow<PagingData<Playlist>>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onSync: () -> Unit,
    onOpenSettings: () -> Unit,
    onItemLongClick: (MenuTarget) -> Unit = {},
    syncing: Boolean = false,
    bottomInset: Dp = 0.dp,
    listQuery: ListQuery = ListQuery(""),
    resetKeys: Map<LibraryTab, Any?> = emptyMap(),
    sortOptions: List<SortOption> = emptyList(),
    starredSupported: Boolean = false,
    onSortChange: (String) -> Unit = {},
    onToggleSortDirection: () -> Unit = {},
    onCycleStarred: () -> Unit = {},
    onToggleDownloaded: () -> Unit = {},
    onClearFilters: () -> Unit = {},
    search: String = "",
    onSearchChange: (String) -> Unit = {},
    artwork: ArtworkColors? = null,
    albumDownloads: Map<String, ListDownloadStatus> = emptyMap(),
    artistDownloads: Map<String, ListDownloadStatus> = emptyMap(),
    playlistDownloads: Map<String, ListDownloadStatus> = emptyMap(),
    modifier: Modifier = Modifier,
) {
    val pagerState =
        rememberPagerState(
            initialPage = selectedTab.ordinal,
            pageCount = { LibraryTab.entries.size },
        )
    var showOptions by rememberSaveable { mutableStateOf(false) }
    var searchActive by rememberSaveable { mutableStateOf(false) }
    val searchFocus = remember { FocusRequester() }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            onTabSelected(LibraryTab.entries[page])
        }
    }

    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab.ordinal) {
            pagerState.animateScrollToPage(selectedTab.ordinal)
        }
        if (search.isNotEmpty()) searchActive = true
    }

    fun dismissSearch() {
        if (searchActive) {
            searchActive = false
            onSearchChange("")
        }
    }

    val density = LocalDensity.current
    val statusBarTop = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
    val filtersActive = listQuery.starred != StarredFilter.Any || listQuery.downloaded || search.isNotEmpty()
    var tabBarHeightPx by remember { mutableFloatStateOf(with(density) { (TAB_BAR_CONTENT_HEIGHT + bottomInset).toPx() }) }
    val tabBarHeight = with(density) { tabBarHeightPx.toDp() }
    val listTopInset = statusBarTop
    val listBottomInset = tabBarHeight + BOTTOM_CLEARANCE
    val headerColor = artwork?.let(::playerSurfaceColor) ?: MaterialTheme.colorScheme.background

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(artwork?.let(::librarySurfaceColor) ?: MaterialTheme.colorScheme.background),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
        ) { page ->
            val pageTab = LibraryTab.entries[page]
            val resetKey = resetKeys[pageTab]
            val pullToRefreshState = rememberPullToRefreshState()
            PullToRefreshBox(
                isRefreshing = syncing,
                onRefresh = onSync,
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = syncing,
                        modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding(),
                        containerColor = headerColor,
                        color = artwork?.scheme?.primary ?: MaterialTheme.colorScheme.primary,
                    )
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (pageTab) {
                    LibraryTab.Albums -> {
                        AlbumsContent(
                            albums.collectAsLazyPagingItems(),
                            coverArt,
                            listBottomInset,
                            {
                                dismissSearch()
                                onAlbumClick(it)
                            },
                            onLongClick = onItemLongClick,
                            filtered = filtersActive,
                            onClearFilters = onClearFilters,
                            resetKey = resetKey,
                            topInset = listTopInset,
                            onSync = onSync,
                            downloadStatuses = albumDownloads,
                        )
                    }

                    LibraryTab.Artists -> {
                        ArtistsContent(
                            artists.collectAsLazyPagingItems(),
                            coverArt,
                            listBottomInset,
                            {
                                dismissSearch()
                                onArtistClick(it)
                            },
                            onLongClick = onItemLongClick,
                            filtered = filtersActive,
                            onClearFilters = onClearFilters,
                            resetKey = resetKey,
                            topInset = listTopInset,
                            onSync = onSync,
                            downloadStatuses = artistDownloads,
                        )
                    }

                    LibraryTab.Playlists -> {
                        PlaylistsContent(
                            playlists.collectAsLazyPagingItems(),
                            coverArt,
                            listBottomInset,
                            {
                                dismissSearch()
                                onPlaylistClick(it)
                            },
                            onLongClick = onItemLongClick,
                            filtered = filtersActive,
                            onClearFilters = onClearFilters,
                            resetKey = resetKey,
                            topInset = listTopInset,
                            onSync = onSync,
                            downloadStatuses = playlistDownloads,
                        )
                    }
                }
            }
        }

        Spacer(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .statusBarScrim(),
        )

        if (!searchActive) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(headerColor)
                        .onSizeChanged { tabBarHeightPx = it.height.toFloat() }
                        .padding(bottom = bottomInset),
            ) {
                LibraryTabs(
                    pagerState = pagerState,
                    onTabSelected = onTabSelected,
                    onOpenSettings = onOpenSettings,
                    artwork = artwork,
                )
            }
        }

        if (searchActive) {
            SearchField(
                value = search,
                onValueChange = onSearchChange,
                onClose = {
                    searchActive = false
                    onSearchChange("")
                },
                focusRequester = searchFocus,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }

        if (!searchActive) {
            FloatingActionButton(
                onClick = { showOptions = true },
                containerColor = artwork?.scheme?.primary ?: MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = artwork?.scheme?.onPrimary ?: MaterialTheme.colorScheme.onBackground,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 12.dp),
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = tabBarHeight + 16.dp),
            ) {
                Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                    ListOptionsGlyph(
                        modifier =
                            Modifier
                                .size(24.dp)
                                .semantics { contentDescription = "List options" },
                    )
                    if (filtersActive) {
                        Box(
                            modifier =
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(artwork?.scheme?.onPrimary ?: MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
        }
    }

    if (showOptions) {
        ModalBottomSheet(
            onDismissRequest = { showOptions = false },
            dragHandle = null,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            ListOptionsSheet(
                listQuery = listQuery,
                sortOptions = sortOptions,
                starredSupported = starredSupported,
                canClearFilters = filtersActive,
                onSortChange = onSortChange,
                onToggleSortDirection = onToggleSortDirection,
                onCycleStarred = onCycleStarred,
                onToggleDownloaded = onToggleDownloaded,
                onClearFilters = onClearFilters,
                onSearch = {
                    showOptions = false
                    searchActive = true
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListOptionsSheet(
    listQuery: ListQuery,
    sortOptions: List<SortOption>,
    starredSupported: Boolean,
    canClearFilters: Boolean,
    onSortChange: (String) -> Unit,
    onToggleSortDirection: () -> Unit,
    onCycleStarred: () -> Unit,
    onToggleDownloaded: () -> Unit,
    onClearFilters: () -> Unit,
    onSearch: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        val naturalDescending = sortOptions.firstOrNull { it.value == listQuery.sort }?.descendingByDefault == true
        val descending = if (naturalDescending) !listQuery.descending else listQuery.descending
        Surface(
            onClick = onSearch,
            shape = RoundedCornerShape(4.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Search this list", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp),
        ) {
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onToggleSortDirection) {
                Icon(
                    imageVector = if (descending) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                    contentDescription = if (descending) "Sort descending" else "Sort ascending",
                )
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            sortOptions.forEach { option ->
                FilterChip(
                    selected = listQuery.sort == option.value,
                    onClick = {
                        if (listQuery.sort == option.value) onToggleSortDirection() else onSortChange(option.value)
                    },
                    label = { Text(option.label) },
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 16.dp),
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onClearFilters,
                enabled = canClearFilters,
            ) {
                Icon(Icons.Rounded.FilterAltOff, contentDescription = "Clear filters")
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            FilterChip(
                selected = listQuery.downloaded,
                onClick = onToggleDownloaded,
                label = { Text("Downloaded") },
                leadingIcon =
                    if (listQuery.downloaded) {
                        {
                            Icon(
                                Icons.Rounded.DownloadDone,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    },
            )
            if (starredSupported) {
                FilterChip(
                    selected = listQuery.starred != StarredFilter.Any,
                    onClick = onCycleStarred,
                    label = { Text("Starred") },
                    leadingIcon =
                        when (listQuery.starred) {
                            StarredFilter.Any -> {
                                null
                            }

                            StarredFilter.Starred -> {
                                {
                                    Icon(
                                        Icons.Rounded.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                                    )
                                }
                            }

                            StarredFilter.NotStarred -> {
                                {
                                    Icon(
                                        Icons.Rounded.Remove,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                                    )
                                }
                            }
                        },
                )
            }
        }
    }
}

private val BOTTOM_CLEARANCE = 80.dp
private val TAB_BAR_CONTENT_HEIGHT = 52.dp
private val TAB_ICON_SIZE = 24.dp
private val TAB_VERTICAL_PADDING = 6.dp
private val TOUCH_TARGET = 48.dp
private const val LIST_OPTIONS_WEIGHT = 0.11f
private val LIST_OPTIONS_BAR_WIDTHS = listOf(1f, 0.62f, 0.34f)

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClose: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    TextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = { Text("Search") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Rounded.Close, contentDescription = "Close search")
            }
        },
        shape = RoundedCornerShape(4.dp),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        modifier = modifier.focusRequester(focusRequester),
    )
}

@Composable
private fun ListOptionsGlyph(modifier: Modifier = Modifier) {
    val color = LocalContentColor.current
    Canvas(modifier) {
        val thickness = size.height * LIST_OPTIONS_WEIGHT
        val gap = size.height / (LIST_OPTIONS_BAR_WIDTHS.size + 1)
        LIST_OPTIONS_BAR_WIDTHS.forEachIndexed { index, widthFraction ->
            val y = gap * (index + 1)
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(size.width * widthFraction, y),
                strokeWidth = thickness,
                cap = StrokeCap.Round,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryTabs(
    pagerState: PagerState,
    onTabSelected: (LibraryTab) -> Unit,
    onOpenSettings: () -> Unit,
    artwork: ArtworkColors?,
) {
    val indicatorStretch = 18.dp
    val density = LocalDensity.current
    val bounds = remember { mutableStateMapOf<Int, Rect>() }
    val page = pagerState.currentPage
    val fraction = pagerState.currentPageOffsetFraction
    val raw = abs(fraction).coerceIn(0f, 1f)
    val transition = raw * raw * (3f - 2f * raw)
    var travel by remember { mutableIntStateOf(1) }
    LaunchedEffect(Unit) {
        var previous = pagerState.currentPage + pagerState.currentPageOffsetFraction
        snapshotFlow { pagerState.currentPage + pagerState.currentPageOffsetFraction }.collect { current ->
            if (current != previous) {
                travel = if (current > previous) 1 else -1
                previous = current
            }
        }
    }

    val current = bounds[page]
    val neighbour = current?.let { bounds[if (fraction >= 0f) page + 1 else page - 1] }
    val step = if (neighbour != null) transition else 0f
    val stretch = with(density) { indicatorStretch.toPx() } * step
    val leading = stretch * 2f
    val trailing = stretch * 0.25f
    val leftExtra = if (travel > 0) trailing else leading
    val rightExtra = if (travel > 0) leading else trailing
    val indicatorLeft = current?.let { it.left + ((neighbour?.left ?: it.left) - it.left) * step - leftExtra }
    val indicatorRight = current?.let { it.right + ((neighbour?.right ?: it.right) - it.right) * step + rightExtra }

    val headerColor = artwork?.let(::playerSurfaceColor) ?: MaterialTheme.colorScheme.background
    val indicatorColor = artwork?.scheme?.primary ?: MaterialTheme.colorScheme.onBackground
    val tabContentHeightPx = with(density) { (TAB_ICON_SIZE + TAB_VERTICAL_PADDING * 2).toPx() }

    Box(Modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Box(
                modifier =
                    Modifier.drawBehind {
                        if (current != null && indicatorLeft != null && indicatorRight != null && indicatorRight > indicatorLeft) {
                            val height = tabContentHeightPx.coerceAtMost(current.height)
                            drawRoundRect(
                                color = indicatorColor,
                                topLeft = Offset(indicatorLeft, current.top + (current.height - height) / 2f),
                                size = Size(indicatorRight - indicatorLeft, height),
                                cornerRadius = CornerRadius(8.dp.toPx()),
                            )
                        }
                    },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    LibraryTab.entries.forEachIndexed { index, tab ->
                        TabButton(
                            tab = tab,
                            artwork = artwork,
                            progress =
                                when (index) {
                                    page -> 1f - transition
                                    page + (if (fraction >= 0f) 1 else -1) -> transition
                                    else -> 0f
                                },
                            indicatorLeft = indicatorLeft,
                            indicatorRight = indicatorRight,
                            onClick = { onTabSelected(tab) },
                            modifier =
                                Modifier.onGloballyPositioned { coordinates ->
                                    bounds[index] = coordinates.boundsInParent()
                                },
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabButton(
    tab: LibraryTab,
    artwork: ArtworkColors?,
    progress: Float,
    indicatorLeft: Float?,
    indicatorRight: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedContent = artwork?.scheme?.onPrimary ?: MaterialTheme.colorScheme.background
    val paddingStart = with(LocalDensity.current) { 8.dp.toPx() }
    var tabLeft by remember { mutableFloatStateOf(0f) }
    val clipStart = indicatorLeft?.minus(tabLeft + paddingStart)
    val clipEnd = indicatorRight?.minus(tabLeft + paddingStart)
    Box(
        modifier =
            modifier
                .onGloballyPositioned { tabLeft = it.boundsInParent().left }
                .clickable(onClick = onClick)
                .height(TOUCH_TARGET),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = TAB_VERTICAL_PADDING, bottom = TAB_VERTICAL_PADDING),
        ) {
            if (clipStart != null && clipEnd != null) {
                Box(
                    modifier =
                        Modifier.drawWithContent {
                            val hole =
                                Path().apply {
                                    addRect(Rect(clipStart, 0f, clipEnd, size.height))
                                }
                            clipPath(hole, clipOp = ClipOp.Difference) {
                                this@drawWithContent.drawContent()
                            }
                        },
                ) {
                    TabContent(tab, progress, MaterialTheme.colorScheme.onBackground)
                }
                Box(
                    modifier =
                        Modifier
                            .clearAndSetSemantics {}
                            .drawWithContent {
                                clipRect(clipStart, 0f, clipEnd, size.height) {
                                    this@drawWithContent.drawContent()
                                }
                            },
                ) {
                    TabContent(tab, progress, selectedContent)
                }
            } else {
                TabContent(tab, progress, MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
private fun TabContent(
    tab: LibraryTab,
    progress: Float,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = color,
            modifier =
                Modifier.size(TAB_ICON_SIZE).graphicsLayer {
                    if (tab == LibraryTab.Playlists) {
                        scaleX = 1.12f
                        scaleY = 1.12f
                        translationX = 1.5.dp.toPx()
                    }
                },
        )
        Box(
            modifier =
                Modifier
                    .clipToBounds()
                    .clearAndSetSemantics {}
                    .layout { measurable, _ ->
                        val placeable = measurable.measure(Constraints())
                        val width = (placeable.width * progress).roundToInt()
                        layout(width, placeable.height) {
                            placeable.place(0, 0)
                        }
                    },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = color,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}
