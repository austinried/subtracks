package com.subtracks.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.DownloadList
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDetailViewModel(
    private val libraryRepository: LibraryRepository,
    private val sourceRepository: SourceRepository,
    private val playbackController: PlaybackController,
    private val downloadRepository: DownloadRepository,
    private val artistId: String,
) : ViewModel() {
    private val sourceId = libraryRepository.activeSourceId.filterNotNull()

    val artist: Flow<Artist?> = sourceId.flatMapLatest { libraryRepository.artist(it, artistId) }

    val albums: Flow<List<Album>> = sourceId.flatMapLatest { libraryRepository.artistAlbums(it, artistId) }

    val downloadStatus: StateFlow<ListDownloadStatus> =
        sourceId
            .flatMapLatest { downloadRepository.status(it, DownloadList.Artist, artistId) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListDownloadStatus())

    fun downloadAll() {
        viewModelScope.launch { downloadRepository.downloadAll(sourceId.first(), DownloadList.Artist, artistId) }
    }

    fun cancelDownloads() {
        viewModelScope.launch { downloadRepository.cancelAll(sourceId.first(), DownloadList.Artist, artistId) }
    }

    fun deleteDownloads() {
        viewModelScope.launch {
            downloadRepository.deleteAll(sourceId.first(), DownloadList.Artist, artistId)
            playbackController.refreshMediaItems()
        }
    }

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
