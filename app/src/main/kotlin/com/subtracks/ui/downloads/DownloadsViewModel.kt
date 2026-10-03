package com.subtracks.ui.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.media.readAudioEncoding
import com.subtracks.data.model.AudioEncoding
import com.subtracks.data.model.DownloadList
import com.subtracks.data.model.DownloadedSong
import com.subtracks.data.repo.DownloadRepository
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DownloadTree(
    val artists: List<DownloadArtistNode> = emptyList(),
) {
    val songs: Int get() = artists.sumOf { it.albums.sumOf { album -> album.songs.size } }
    val bytes: Long get() = artists.sumOf { it.bytes }
}

data class DownloadArtistNode(
    val id: String,
    val name: String,
    val bytes: Long,
    val albums: List<DownloadAlbumNode>,
)

data class DownloadAlbumNode(
    val id: String,
    val name: String,
    val bytes: Long,
    val songs: List<DownloadedSong>,
)

@OptIn(ExperimentalCoroutinesApi::class)
class DownloadsViewModel(
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val playbackController: PlaybackController,
    private val unknownArtist: String,
    private val unknownAlbum: String,
) : ViewModel() {
    private val sourceId = libraryRepository.activeSourceId.filterNotNull()

    val tree: StateFlow<DownloadTree> =
        sourceId
            .flatMapLatest { downloadRepository.downloadedSongs(it) }
            .map { buildTree(it, unknownArtist, unknownAlbum) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DownloadTree())

    fun deleteAll() {
        viewModelScope.launch {
            downloadRepository.removeSource(sourceId.first())
            playbackController.refreshMediaItems()
        }
    }

    fun delete(
        list: DownloadList,
        refId: String,
    ) {
        viewModelScope.launch {
            downloadRepository.removeList(sourceId.first(), list, refId)
            playbackController.refreshMediaItems()
        }
    }

    fun deleteSongs(songIds: List<String>) {
        viewModelScope.launch {
            val source = sourceId.first()
            songIds.forEach { downloadRepository.remove(source, it) }
            playbackController.refreshMediaItems()
        }
    }

    fun cancelSongs(songIds: List<String>) {
        viewModelScope.launch {
            downloadRepository.cancel(sourceId.first(), songIds)
        }
    }

    suspend fun encoding(song: DownloadedSong): AudioEncoding? =
        downloadRepository.localFile(song.sourceId, song.songId)?.let { readAudioEncoding(it) }
}

internal fun buildTree(
    songs: List<DownloadedSong>,
    unknownArtist: String,
    unknownAlbum: String,
): DownloadTree =
    DownloadTree(
        artists =
            songs
                .groupBy { it.artistId.orEmpty() to it.artistName.orEmpty() }
                .map { (artistKey, artistSongs) ->
                    DownloadArtistNode(
                        id = artistKey.first,
                        name = artistKey.second.ifBlank { unknownArtist },
                        bytes = artistSongs.sumOf { it.size },
                        albums =
                            artistSongs
                                .groupBy { it.albumId.orEmpty() to it.albumName.orEmpty() }
                                .map { (albumKey, albumSongs) ->
                                    DownloadAlbumNode(
                                        id = albumKey.first,
                                        name = albumKey.second.ifBlank { unknownAlbum },
                                        bytes = albumSongs.sumOf { it.size },
                                        songs = albumSongs.sortedBy { it.title.lowercase() },
                                    )
                                }.sortedBy { it.name.lowercase() },
                    )
                }.sortedBy { it.name.lowercase() },
    )
