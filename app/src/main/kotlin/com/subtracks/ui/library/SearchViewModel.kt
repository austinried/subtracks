package com.subtracks.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SearchHit
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModel(
    private val libraryRepository: LibraryRepository,
    private val sourceRepository: SourceRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val results: StateFlow<List<SearchHit>> =
        query
            .debounce(SEARCH_DEBOUNCE_MS)
            .flatMapLatest { text ->
                sourceRepository
                    .activeSourceId()
                    .filterNotNull()
                    .flatMapLatest { sourceId -> flow { emit(libraryRepository.search(sourceId, text)) } }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun playSong(songId: String) {
        viewModelScope.launch {
            val sourceId = sourceRepository.activeSourceIdOnce() ?: return@launch
            playbackController.playSong(sourceId, songId)
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 200L
    }
}
