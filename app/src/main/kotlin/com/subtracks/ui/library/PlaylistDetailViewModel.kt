package com.subtracks.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.SongListItem
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlaylistDetailViewModel(
    private val libraryRepository: LibraryRepository,
    private val sourceRepository: SourceRepository,
    private val playbackController: PlaybackController,
    private val playlistId: String,
) : ViewModel() {
    private val sourceId = libraryRepository.activeSourceId.filterNotNull()

    val playlist: Flow<Playlist?> = sourceId.flatMapLatest { libraryRepository.playlist(it, playlistId) }

    val songs: Flow<PagingData<SongListItem>> =
        sourceId
            .flatMapLatest { libraryRepository.playlistSongs(it, playlistId) }
            .cachedIn(viewModelScope)

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
