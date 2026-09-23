package com.subtracks.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
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
            artist
                .filterNotNull()
                .distinctUntilChangedBy { it.id }
                .collectLatest { loaded -> _art.value = resolveArt(loaded) }
        }
    }

    private suspend fun resolveArt(artist: Artist): CoverArtRef? {
        repeat(3) { attempt ->
            val ref = sourceRepository.artistArt(artist.id) ?: sourceRepository.coverArt(artist.coverArt)
            if (ref != null) return ref
            if (attempt < 2) delay(300)
        }
        return null
    }

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean,
    ): CoverArtRef? = sourceRepository.coverArt(coverArt, thumbnail)
}
