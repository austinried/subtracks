package com.subtracks.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
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
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.sync.SyncManager
import com.subtracks.data.sync.SyncStatus
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val sourceRepository: SourceRepository,
    private val syncManager: SyncManager,
    private val playbackController: PlaybackController,
    private val userPreferences: UserPreferences,
    private val downloadRepository: DownloadRepository,
) : ViewModel() {
    private val listQueries: Map<LibraryListTab, StateFlow<ListQuery>> =
        LibraryListTab.entries.associateWith { tab ->
            userPreferences
                .listQuery(tab)
                .stateIn(viewModelScope, SharingStarted.Eagerly, ListQuery(tab.defaultSort))
        }

    private val searches: Map<LibraryListTab, MutableStateFlow<String>> =
        LibraryListTab.entries.associateWith { MutableStateFlow("") }

    val albums: Flow<PagingData<Album>> =
        libraryRepository.activeSourceId
            .filterNotNull()
            .flatMapLatest { sourceId ->
                combine(listQueries.getValue(LibraryListTab.Albums), searches.getValue(LibraryListTab.Albums)) { query, search ->
                    query to search
                }.flatMapLatest { (query, search) ->
                    libraryRepository.albums(sourceId, query.albumSort(), query.descending, query.starred, search, query.downloaded)
                }
            }.cachedIn(viewModelScope)

    val artists: Flow<PagingData<Artist>> =
        libraryRepository.activeSourceId
            .filterNotNull()
            .flatMapLatest { sourceId ->
                combine(listQueries.getValue(LibraryListTab.Artists), searches.getValue(LibraryListTab.Artists)) { query, search ->
                    query to search
                }.flatMapLatest { (query, search) ->
                    libraryRepository.artists(sourceId, query.artistSort(), query.descending, query.starred, search, query.downloaded)
                }
            }.cachedIn(viewModelScope)

    val playlists: Flow<PagingData<Playlist>> =
        libraryRepository.activeSourceId
            .filterNotNull()
            .flatMapLatest { sourceId ->
                combine(listQueries.getValue(LibraryListTab.Playlists), searches.getValue(LibraryListTab.Playlists)) { query, search ->
                    query to search
                }.flatMapLatest { (query, search) ->
                    libraryRepository.playlists(sourceId, query.playlistSort(), query.descending, search, query.downloaded)
                }
            }.cachedIn(viewModelScope)

    val syncing: StateFlow<Boolean> =
        syncManager.status
            .map { it == SyncStatus.Running }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val activeSourceId = libraryRepository.activeSourceId.filterNotNull()

    private val albumDownloads: StateFlow<Map<String, ListDownloadStatus>> =
        activeSourceId
            .flatMapLatest { downloadRepository.statuses(it, DownloadList.Album) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val artistDownloads: StateFlow<Map<String, ListDownloadStatus>> =
        activeSourceId
            .flatMapLatest { downloadRepository.statuses(it, DownloadList.Artist) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val playlistDownloads: StateFlow<Map<String, ListDownloadStatus>> =
        activeSourceId
            .flatMapLatest { downloadRepository.statuses(it, DownloadList.Playlist) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun downloads(tab: LibraryListTab): StateFlow<Map<String, ListDownloadStatus>> =
        when (tab) {
            LibraryListTab.Albums -> albumDownloads
            LibraryListTab.Artists -> artistDownloads
            LibraryListTab.Playlists -> playlistDownloads
        }

    suspend fun downloadedBytes(
        list: DownloadList,
        refId: String,
    ): Long = downloadRepository.downloadedBytes(activeSourceId.first(), list, refId)

    fun onDownloadAction(
        list: DownloadList,
        refId: String,
        action: BulkDownloadAction,
    ) {
        viewModelScope.launch {
            downloadRepository.applyAction(activeSourceId.first(), list, refId, action)
            if (action == BulkDownloadAction.Delete) playbackController.refreshMediaItems()
        }
    }

    fun listQuery(tab: LibraryListTab): StateFlow<ListQuery> = listQueries.getValue(tab)

    fun setListQuery(
        tab: LibraryListTab,
        query: ListQuery,
    ) {
        viewModelScope.launch { userPreferences.setListQuery(tab, query) }
    }

    fun search(tab: LibraryListTab): StateFlow<String> = searches.getValue(tab)

    fun setSearch(
        tab: LibraryListTab,
        value: String,
    ) {
        searches.getValue(tab).value = value
    }

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean,
    ): CoverArtRef? = sourceRepository.coverArt(coverArt, thumbnail)

    fun sync() = syncManager.requestSync()
}

internal fun ListQuery.albumSort(): AlbumSort = AlbumSort.entries.firstOrNull { it.name == sort } ?: AlbumSort.Name

internal fun ListQuery.artistSort(): ArtistSort = ArtistSort.entries.firstOrNull { it.name == sort } ?: ArtistSort.Name

internal fun ListQuery.playlistSort(): PlaylistSort = PlaylistSort.entries.firstOrNull { it.name == sort } ?: PlaylistSort.Name
