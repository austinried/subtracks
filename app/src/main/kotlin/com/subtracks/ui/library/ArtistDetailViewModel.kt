package com.subtracks.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.source.StarType
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailViewModel(
    private val libraryRepository: LibraryRepository,
    private val sourceRepository: SourceRepository,
    private val playbackController: PlaybackController,
    private val artistId: String,
) : ViewModel() {
    private val sourceId = libraryRepository.activeSourceId.filterNotNull()

    val artist: Flow<Artist?> = sourceId.flatMapLatest { libraryRepository.artist(it, artistId) }

    val albums: Flow<List<Album>> = sourceId.flatMapLatest { libraryRepository.artistAlbums(it, artistId) }

    private val _art = MutableStateFlow<CoverArtRef?>(null)
    val art: StateFlow<CoverArtRef?> = _art

    private val _artThumbnail = MutableStateFlow<CoverArtRef?>(null)
    val artThumbnail: StateFlow<CoverArtRef?> = _artThumbnail

    init {
        viewModelScope.launch {
            artist
                .filterNotNull()
                .collectLatest { loaded ->
                    val (full, thumbnail) = resolveArt(loaded)
                    _art.value = full
                    _artThumbnail.value = thumbnail
                }
        }
    }

    private suspend fun resolveArt(artist: Artist): Pair<CoverArtRef?, CoverArtRef?> {
        if (artist.coverArt == null) return null to null
        repeat(3) { attempt ->
            val full = sourceRepository.coverArt(artist.coverArt)
            if (full != null) {
                return full to sourceRepository.coverArt(artist.coverArt, thumbnail = true)
            }
            if (attempt < 2) delay(300)
        }
        return null to null
    }

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean,
    ): CoverArtRef? = sourceRepository.coverArt(coverArt, thumbnail)

    fun setStar(
        type: StarType,
        id: String,
        starred: Boolean,
    ) {
        viewModelScope.launch { libraryRepository.setStar(type, id, starred) }
    }

    fun playAlbum(albumId: String) {
        viewModelScope.launch {
            playbackController.playAlbum(sourceId.first(), albumId, 0)
        }
    }

    fun shuffleAlbum(albumId: String) {
        viewModelScope.launch {
            playbackController.shuffleAlbum(sourceId.first(), albumId)
        }
    }
}
