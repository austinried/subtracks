package com.subtracks.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.DownloadList
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSongItem
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistDetailViewModel(
    private val libraryRepository: LibraryRepository,
    private val sourceRepository: SourceRepository,
    private val playbackController: PlaybackController,
    private val downloadRepository: DownloadRepository,
    private val playlistId: String,
) : ViewModel() {
    private val sourceId = libraryRepository.activeSourceId.filterNotNull()

    val playlist: Flow<Playlist?> = sourceId.flatMapLatest { libraryRepository.playlist(it, playlistId) }

    val songs: Flow<PagingData<PlaylistSongItem>> =
        sourceId
            .flatMapLatest { libraryRepository.playlistSongs(it, playlistId) }
            .cachedIn(viewModelScope)

    val downloads: StateFlow<Map<String, SongDownload>> =
        downloadRepository.states()

    val downloadStatus: StateFlow<ListDownloadStatus> =
        sourceId
            .flatMapLatest { downloadRepository.status(it, DownloadList.Playlist, playlistId) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ListDownloadStatus())

    suspend fun downloadedBytes(): Long = downloadRepository.downloadedBytes(sourceId.first(), DownloadList.Playlist, playlistId)

    fun onDownloadAction(action: BulkDownloadAction) {
        viewModelScope.launch {
            downloadRepository.applyAction(sourceId.first(), DownloadList.Playlist, playlistId, action)
            if (action == BulkDownloadAction.Delete) playbackController.refreshMediaItems()
        }
    }

    fun download(song: Song) {
        viewModelScope.launch { downloadRepository.download(song.sourceId, song.id) }
    }

    fun cancelDownload(song: Song) {
        viewModelScope.launch { downloadRepository.remove(song.sourceId, song.id) }
    }

    fun deleteDownload(song: Song) {
        viewModelScope.launch {
            downloadRepository.remove(song.sourceId, song.id)
            playbackController.refreshMediaItems()
        }
    }

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean,
    ): CoverArtRef? = sourceRepository.coverArt(coverArt, thumbnail)

    fun play(startIndex: Int) {
        viewModelScope.launch {
            playbackController.playPlaylist(sourceId.first(), playlistId, startIndex.toLong())
        }
    }

    fun playAll() {
        viewModelScope.launch {
            playbackController.playPlaylistInOrder(sourceId.first(), playlistId)
        }
    }

    fun shuffle() {
        viewModelScope.launch {
            playbackController.shufflePlaylist(sourceId.first(), playlistId)
        }
    }
}
