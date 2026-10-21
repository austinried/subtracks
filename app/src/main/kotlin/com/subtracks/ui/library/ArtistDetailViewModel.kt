package com.subtracks.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.model.Artist
import com.subtracks.data.model.ArtistAlbums
import com.subtracks.data.model.ArtistBio
import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.DownloadList
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.components.ArtistArt
import com.subtracks.ui.components.artistArt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

    val artist: StateFlow<Artist?> =
        sourceId
            .flatMapLatest { libraryRepository.artist(it, artistId) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val artistName: StateFlow<String?> =
        sourceId
            .flatMapLatest { id ->
                combine(
                    libraryRepository.artist(id, artistId),
                    libraryRepository.artistName(id, artistId),
                ) { loaded, fromSongs -> loaded?.name?.takeIf { it.isNotBlank() } ?: fromSongs }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val offline: StateFlow<Boolean> = sourceRepository.offline

    val albums: StateFlow<ArtistAlbums?> =
        combine(sourceId, sourceRepository.offline) { id, offline -> id to offline }
            .flatMapLatest { (id, offline) -> libraryRepository.artistAlbums(id, artistId, offline) }
            .map<ArtistAlbums, ArtistAlbums?> { it }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val downloadStatus: StateFlow<ListDownloadStatus> =
        sourceId
            .flatMapLatest { downloadRepository.status(it, DownloadList.Artist, artistId) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListDownloadStatus())

    val albumDownloads: StateFlow<Map<String, ListDownloadStatus>> =
        sourceId
            .flatMapLatest { downloadRepository.statuses(it, DownloadList.Album) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val artistBio: StateFlow<ArtistBio?> =
        sourceId
            .flatMapLatest { libraryRepository.artistBio(it, artistId) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _bioLoading = MutableStateFlow(true)
    val bioLoading: StateFlow<Boolean> = _bioLoading

    private val _bioUnavailable = MutableStateFlow(false)
    val bioUnavailable: StateFlow<Boolean> = _bioUnavailable

    private val _bioExpanded = MutableStateFlow(false)
    val bioExpanded: StateFlow<Boolean> = _bioExpanded

    private var bioRefresh: Job? = null

    fun refreshBio() {
        if (bioRefresh?.isActive == true) return
        bioRefresh =
            viewModelScope.launch {
                _bioUnavailable.value = false
                if (sourceRepository.offline.value) {
                    _bioLoading.value = false
                    _bioUnavailable.value = true
                    return@launch
                }
                val id = sourceRepository.activeSourceIdOnce()
                if (id == null) {
                    _bioLoading.value = false
                    _bioUnavailable.value = true
                    return@launch
                }
                _bioLoading.value = true
                try {
                    _bioUnavailable.value = libraryRepository.refreshArtistBio(id, artistId).isFailure
                } finally {
                    _bioLoading.value = false
                }
            }
    }

    fun expandBio() {
        _bioExpanded.value = true
    }

    fun collapseBio() {
        _bioExpanded.value = false
    }

    suspend fun downloadedBytes(
        list: DownloadList,
        refId: String,
    ): Long = downloadRepository.downloadedBytes(sourceId.first(), list, refId)

    fun onDownloadAction(
        list: DownloadList,
        refId: String,
        action: BulkDownloadAction,
    ) {
        viewModelScope.launch {
            downloadRepository.applyAction(sourceId.first(), list, refId, action)
            if (action == BulkDownloadAction.Delete) playbackController.refreshMediaItems()
        }
    }

    private val _art = MutableStateFlow<CoverArtRef?>(null)
    val art: StateFlow<CoverArtRef?> = _art

    private val _artThumbnail = MutableStateFlow<CoverArtRef?>(null)
    val artThumbnail: StateFlow<CoverArtRef?> = _artThumbnail

    private val _heroArt = MutableStateFlow<ArtistArt?>(null)
    val heroArt: StateFlow<ArtistArt?> = _heroArt

    init {
        viewModelScope.launch {
            combine(
                artist.filterNotNull(),
                sourceRepository.offline,
                sourceRepository.activeSource,
            ) { loaded, _, activeSource -> loaded to activeSource }
                .collectLatest { (loaded, activeSource) ->
                    if (activeSource == null) return@collectLatest
                    val hero = sourceRepository.artistArt(loaded, forIcon = false)
                    val icon = sourceRepository.artistArt(loaded, forIcon = true)
                    _art.value = hero.image ?: hero.covers.firstOrNull()
                    _artThumbnail.value = icon.image ?: icon.covers.firstOrNull()
                    _heroArt.value = hero
                }
        }
    }

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean,
    ): CoverArtRef? = sourceRepository.coverArt(coverArt, thumbnail)

    fun shuffle() {
        viewModelScope.launch {
            playbackController.shuffleArtist(sourceId.first(), artistId)
        }
    }
}
