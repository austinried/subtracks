package com.subtracks.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailViewModel(
    libraryRepository: LibraryRepository,
    private val sourceRepository: SourceRepository,
    private val artistId: String,
) : ViewModel() {
    private val sourceId = libraryRepository.activeSourceId.filterNotNull()

    val artist: Flow<Artist?> = sourceId.flatMapLatest { libraryRepository.artist(it, artistId) }

    val albums: Flow<List<Album>> = sourceId.flatMapLatest { libraryRepository.artistAlbums(it, artistId) }

    private val _art = MutableStateFlow<CoverArtRef?>(null)
    val art: StateFlow<CoverArtRef?> = _art

    init {
        viewModelScope.launch {
            val loaded = artist.filterNotNull().first()
            _art.value = sourceRepository.artistArt(loaded.id) ?: sourceRepository.coverArt(loaded.coverArt)
        }
    }

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean,
    ): CoverArtRef? = sourceRepository.coverArt(coverArt, thumbnail)
}
