package com.subtracks.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.SongListItem
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.prefs.LibraryListTab
import com.subtracks.data.prefs.ListQuery
import com.subtracks.data.prefs.PlaylistSort
import com.subtracks.data.prefs.SongSort
import com.subtracks.data.prefs.UserPreferences
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
                    libraryRepository.albums(sourceId, query.albumSort(), query.descending, query.starred, search)
                }
            }.cachedIn(viewModelScope)

    val artists: Flow<PagingData<Artist>> =
        libraryRepository.activeSourceId
            .filterNotNull()
            .flatMapLatest { sourceId ->
                combine(listQueries.getValue(LibraryListTab.Artists), searches.getValue(LibraryListTab.Artists)) { query, search ->
                    query to search
                }.flatMapLatest { (query, search) ->
                    libraryRepository.artists(sourceId, query.artistSort(), query.descending, query.starred, search)
                }
            }.cachedIn(viewModelScope)

    val playlists: Flow<PagingData<Playlist>> =
        libraryRepository.activeSourceId
            .filterNotNull()
            .flatMapLatest { sourceId ->
                combine(listQueries.getValue(LibraryListTab.Playlists), searches.getValue(LibraryListTab.Playlists)) { query, search ->
                    query to search
                }.flatMapLatest { (query, search) ->
                    libraryRepository.playlists(sourceId, query.playlistSort(), query.descending, search)
                }
            }.cachedIn(viewModelScope)

    val songs: Flow<PagingData<SongListItem>> =
        libraryRepository.activeSourceId
            .filterNotNull()
            .flatMapLatest { sourceId ->
                combine(listQueries.getValue(LibraryListTab.Songs), searches.getValue(LibraryListTab.Songs)) { query, search ->
                    query to search
                }.flatMapLatest { (query, search) ->
                    libraryRepository.songs(sourceId, query.songSort(), query.descending, query.starred, search)
                }
            }.cachedIn(viewModelScope)

    val syncing: StateFlow<Boolean> =
        syncManager.status
            .map { it == SyncStatus.Running }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val playingSongId: StateFlow<String?> =
        playbackController.state
            .map { state -> state.item?.id?.takeIf { state.context?.kind == QueueKind.Songs } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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

    fun playSong(position: Int) {
        viewModelScope.launch {
            val sourceId = sourceRepository.activeSourceIdOnce() ?: return@launch
            playbackController.playSongs(sourceId, position.toLong())
        }
    }

    fun sync() = syncManager.requestSync()
}

private fun ListQuery.albumSort(): AlbumSort = AlbumSort.entries.firstOrNull { it.name == sort } ?: AlbumSort.Name

private fun ListQuery.artistSort(): ArtistSort = ArtistSort.entries.firstOrNull { it.name == sort } ?: ArtistSort.Name

private fun ListQuery.playlistSort(): PlaylistSort = PlaylistSort.entries.firstOrNull { it.name == sort } ?: PlaylistSort.Name

private fun ListQuery.songSort(): SongSort = SongSort.entries.firstOrNull { it.name == sort } ?: SongSort.Album
