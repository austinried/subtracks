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
import com.subtracks.data.prefs.UserPreferences
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.sync.SyncManager
import com.subtracks.data.sync.SyncStatus
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    userPreferences: UserPreferences,
) : ViewModel() {
    val albums: Flow<PagingData<Album>> =
        libraryRepository.activeSourceId
            .filterNotNull()
            .flatMapLatest { sourceId ->
                userPreferences.albumSort.flatMapLatest { sort -> libraryRepository.albums(sourceId, sort) }
            }.cachedIn(viewModelScope)

    val artists: Flow<PagingData<Artist>> =
        libraryRepository.activeSourceId
            .filterNotNull()
            .flatMapLatest { libraryRepository.artists(it) }
            .cachedIn(viewModelScope)

    val songs: Flow<PagingData<SongListItem>> =
        libraryRepository.activeSourceId
            .filterNotNull()
            .flatMapLatest { libraryRepository.songs(it) }
            .cachedIn(viewModelScope)

    val playlists: Flow<PagingData<Playlist>> =
        libraryRepository.activeSourceId
            .filterNotNull()
            .flatMapLatest { libraryRepository.playlists(it) }
            .cachedIn(viewModelScope)

    val syncing: StateFlow<Boolean> =
        syncManager.status
            .map { it == SyncStatus.Running }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val playingSongId: StateFlow<String?> =
        playbackController.state
            .map { state -> state.item?.id?.takeIf { state.context?.kind == QueueKind.Songs } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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
