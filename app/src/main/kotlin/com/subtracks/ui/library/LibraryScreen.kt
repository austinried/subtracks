package com.subtracks.ui.library

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.SongListItem
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.prefs.LibraryListTab
import com.subtracks.data.prefs.ListQuery
import com.subtracks.data.prefs.PlaylistSort
import com.subtracks.data.prefs.SongSort
import com.subtracks.data.prefs.StarredFilter
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
    Songs("Songs", Icons.Rounded.MusicNote),
}

data class SortOption(
    val value: String,
    val label: String,
)

fun LibraryTab.listTab(): LibraryListTab = LibraryListTab.valueOf(name)

fun sortOptionsFor(tab: LibraryTab): List<SortOption> =
    when (tab) {
        LibraryTab.Albums -> {
            listOf(
                SortOption(AlbumSort.Name.name, "Name"),
                SortOption(AlbumSort.Artist.name, "Artist"),
                SortOption(AlbumSort.Year.name, "Year"),
                SortOption(AlbumSort.Added.name, "Added"),
                SortOption(AlbumSort.Starred.name, "Starred"),
            )
        }

        LibraryTab.Artists -> {
            listOf(
                SortOption(ArtistSort.Name.name, "Name"),
                SortOption(ArtistSort.AlbumCount.name, "Albums"),
                SortOption(ArtistSort.Starred.name, "Starred"),
            )
        }

        LibraryTab.Playlists -> {
            listOf(
                SortOption(PlaylistSort.Name.name, "Name"),
                SortOption(PlaylistSort.Added.name, "Added"),
                SortOption(PlaylistSort.Updated.name, "Updated"),
            )
        }

        LibraryTab.Songs -> {
            listOf(
                SortOption(SongSort.Album.name, "Album"),
                SortOption(SongSort.Title.name, "Title"),
                SortOption(SongSort.Artist.name, "Artist"),
                SortOption(SongSort.Starred.name, "Starred"),
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
    bottomInset: Dp,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    var selectedTab by rememberSaveable { mutableStateOf(LibraryTab.Albums) }
    val syncing by viewModel.syncing.collectAsStateWithLifecycle()
    val playingSongId by viewModel.playingSongId.collectAsStateWithLifecycle()
    val listTab = selectedTab.listTab()
    val listQuery by viewModel.listQuery(listTab).collectAsStateWithLifecycle()
    val search by viewModel.search(listTab).collectAsStateWithLifecycle()
    LibraryScreen(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        syncing = syncing,
        bottomInset = bottomInset,
        albums = viewModel.albums.collectAsLazyPagingItems(),
        artists = viewModel.artists.collectAsLazyPagingItems(),
        songs = viewModel.songs.collectAsLazyPagingItems(),
        playlists = viewModel.playlists.collectAsLazyPagingItems(),
        coverArt = viewModel::coverArt,
        onAlbumClick = onAlbumClick,
        onArtistClick = onArtistClick,
        onPlaylistClick = onPlaylistClick,
        onSongClick = viewModel::playSong,
        onSync = viewModel::sync,
        onOpenSettings = onOpenSettings,
        playingSongId = playingSongId,
        listQuery = listQuery,
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
            viewModel.setListQuery(listTab, listQuery.copy(starred = StarredFilter.Any))
            viewModel.setSearch(listTab, "")
        },
        search = search,
        onSearchChange = { viewModel.setSearch(listTab, it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    selectedTab: LibraryTab,
    onTabSelected: (LibraryTab) -> Unit,
    albums: LazyPagingItems<Album>,
    artists: LazyPagingItems<Artist>,
    songs: LazyPagingItems<SongListItem>,
    playlists: LazyPagingItems<Playlist>,
    coverArt: (String?, Boolean) -> CoverArtRef?,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onSongClick: (Int) -> Unit,
    onSync: () -> Unit,
    onOpenSettings: () -> Unit,
    syncing: Boolean = false,
    playingSongId: String? = null,
    bottomInset: Dp = 0.dp,
    listQuery: ListQuery = ListQuery(""),
    sortOptions: List<SortOption> = emptyList(),
    starredSupported: Boolean = false,
    onSortChange: (String) -> Unit = {},
    onToggleSortDirection: () -> Unit = {},
    onCycleStarred: () -> Unit = {},
    onClearFilters: () -> Unit = {},
    search: String = "",
    onSearchChange: (String) -> Unit = {},
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
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onTabSelected(LibraryTab.entries[page])
        }
    }

    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab.ordinal) {
            pagerState.animateScrollToPage(selectedTab.ordinal)
        }
    }

    val density = LocalDensity.current
    val titleLineHeight =
        with(density) {
            MaterialTheme.typography.headlineLarge.lineHeight
                .toDp()
        }
    val titleTop = ((TopAppBarDefaults.TopAppBarExpandedHeight - titleLineHeight) / 2).coerceAtLeast(0.dp)
    val titleHeight = titleTop + titleLineHeight + 4.dp
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    SideEffect { scrollBehavior.state.heightOffsetLimit = -with(density) { titleHeight.toPx() } }
    val titleFraction = scrollBehavior.state.collapsedFraction

    val statusBarTop = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
    val filtersActive = listQuery.starred != StarredFilter.Any || search.isNotEmpty()
    val listBottomInset = bottomInset + if (searchActive) SEARCH_BAR_CLEARANCE else FAB_CLEARANCE
    val resetKey = listOf(listQuery.sort, listQuery.descending, listQuery.starred, search)

    Box(modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = statusBarTop),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(titleHeight * (1f - titleFraction))
                            .clipToBounds(),
                ) {
                    Text(
                        text = selectedTab.label,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        modifier =
                            Modifier
                                .offset(y = -(titleHeight * titleFraction))
                                .wrapContentHeight(unbounded = true)
                                .padding(start = 12.dp, end = 16.dp, top = titleTop),
                    )
                }
                LibraryTabs(
                    pagerState = pagerState,
                    onTabSelected = onTabSelected,
                    syncing = syncing,
                    onSync = onSync,
                    onOpenSettings = onOpenSettings,
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) { page ->
                key(resetKey) {
                    when (LibraryTab.entries[page]) {
                        LibraryTab.Albums -> {
                            AlbumsContent(
                                albums,
                                coverArt,
                                listBottomInset,
                                onAlbumClick,
                                filtered = filtersActive,
                                onClearFilters = onClearFilters,
                                resetKey = resetKey,
                            )
                        }

                        LibraryTab.Artists -> {
                            ArtistsContent(
                                artists,
                                coverArt,
                                listBottomInset,
                                onArtistClick,
                                filtered = filtersActive,
                                onClearFilters = onClearFilters,
                                resetKey = resetKey,
                            )
                        }

                        LibraryTab.Songs -> {
                            SongsContent(
                                songs,
                                coverArt,
                                listBottomInset,
                                onSongClick,
                                playingSongId,
                                filtered = filtersActive,
                                onClearFilters = onClearFilters,
                                resetKey = resetKey,
                            )
                        }

                        LibraryTab.Playlists -> {
                            PlaylistsContent(
                                playlists,
                                coverArt,
                                listBottomInset,
                                onPlaylistClick,
                                filtered = filtersActive,
                                onClearFilters = onClearFilters,
                                resetKey = resetKey,
                            )
                        }
                    }
                }
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
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            ) {
                Box {
                    Icon(Icons.Filled.Sort, contentDescription = "List options")
                    if (listQuery.starred != StarredFilter.Any) {
                        Box(
                            modifier =
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
        }
    }

    if (showOptions) {
        ModalBottomSheet(onDismissRequest = { showOptions = false }) {
            ListOptionsSheet(
                listQuery = listQuery,
                sortOptions = sortOptions,
                starredSupported = starredSupported,
                onSortChange = onSortChange,
                onToggleSortDirection = onToggleSortDirection,
                onCycleStarred = onCycleStarred,
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
    onSortChange: (String) -> Unit,
    onToggleSortDirection: () -> Unit,
    onCycleStarred: () -> Unit,
    onClearFilters: () -> Unit,
    onSearch: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        ListItem(
            headlineContent = { Text("Search this list") },
            leadingContent = { Icon(Icons.Rounded.Search, contentDescription = null) },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.clickable(onClick = onSearch),
        )
        HorizontalDivider()

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
                    imageVector = if (listQuery.descending) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                    contentDescription = if (listQuery.descending) "Sort descending" else "Sort ascending",
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
                    onClick = { onSortChange(option.value) },
                    label = { Text(option.label) },
                )
            }
        }

        if (starredSupported) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
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
                if (listQuery.starred != StarredFilter.Any) {
                    TextButton(onClick = onClearFilters) { Text("Clear filters") }
                }
            }
        }
    }
}

private val FAB_CLEARANCE = 80.dp
private val SEARCH_BAR_CLEARANCE = 80.dp

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTabs(
    pagerState: PagerState,
    onTabSelected: (LibraryTab) -> Unit,
    syncing: Boolean,
    onSync: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val iconFadeThreshold = 0.65f
    val indicatorStretch = 18.dp
    val density = LocalDensity.current
    val bounds = remember { mutableStateMapOf<Int, Rect>() }
    val page = pagerState.currentPage
    val fraction = pagerState.currentPageOffsetFraction
    val position = page + fraction

    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 12.dp),
    ) {
        bounds[page]?.let { current ->
            val neighbour = bounds[if (fraction >= 0f) page + 1 else page - 1]
            val step = if (neighbour != null) abs(fraction) else 0f
            val left = current.left + ((neighbour?.left ?: current.left) - current.left) * step
            val right = current.right + ((neighbour?.right ?: current.right) - current.right) * step
            val stretch = with(density) { indicatorStretch.toPx() } * abs(fraction) / 2f
            Box(
                modifier =
                    Modifier
                        .offset { IntOffset((left - stretch).roundToInt(), current.top.roundToInt()) }
                        .width(with(density) { (right - left + stretch * 2f).toDp() })
                        .height(with(density) { current.height.toDp() })
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onBackground),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LibraryTab.entries.forEachIndexed { index, tab ->
                TabButton(
                    tab = tab,
                    progress =
                        ((iconFadeThreshold - abs(position - index)) / iconFadeThreshold)
                            .coerceIn(0f, 1f),
                    onClick = { onTabSelected(tab) },
                    modifier =
                        Modifier.onGloballyPositioned { coordinates ->
                            bounds[index] = coordinates.boundsInParent()
                        },
                )
            }
            Spacer(Modifier.weight(1f))
            if (syncing) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 4.dp).size(20.dp),
                    strokeWidth = 2.dp,
                )
            }
            IconButton(onClick = onSync) {
                Icon(
                    imageVector = Icons.Rounded.Sync,
                    contentDescription = "Sync",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabButton(
    tab: LibraryTab,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = lerp(MaterialTheme.colorScheme.onBackground, MaterialTheme.colorScheme.background, progress)
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = content,
            modifier = Modifier.size(24.dp),
        )
    }
}
