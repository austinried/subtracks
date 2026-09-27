package com.subtracks.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.model.Album
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Disc
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongDownload
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumDetailViewModel(
    private val libraryRepository: LibraryRepository,
    private val sourceRepository: SourceRepository,
    private val playbackController: PlaybackController,
    private val downloadRepository: DownloadRepository,
    private val albumId: String,
) : ViewModel() {
    private val sourceId = libraryRepository.activeSourceId.filterNotNull()

    val album: Flow<Album?> = sourceId.flatMapLatest { libraryRepository.album(it, albumId) }

    val songs: Flow<List<Song>> = sourceId.flatMapLatest { libraryRepository.albumSongs(it, albumId) }

    val discs: Flow<List<Disc>> = sourceId.flatMapLatest { libraryRepository.albumDiscs(it, albumId) }

    val downloads: StateFlow<Map<String, SongDownload>> = downloadRepository.states()

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean,
    ): CoverArtRef? = sourceRepository.coverArt(coverArt, thumbnail)

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

    fun play(startIndex: Int) {
        viewModelScope.launch {
            playbackController.playAlbum(sourceId.first(), albumId, startIndex.toLong())
        }
    }

    fun playAll() {
        viewModelScope.launch {
            playbackController.playAlbumInOrder(sourceId.first(), albumId)
        }
    }

    fun shuffle() {
        viewModelScope.launch {
            playbackController.shuffleAlbum(sourceId.first(), albumId)
        }
    }
}
