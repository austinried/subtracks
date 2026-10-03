package com.subtracks.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.model.Album
import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.Song
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import com.subtracks.ui.home.playSongInContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class SearchResults(
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val songs: List<AlbumSongItem> = emptyList(),
) {
    val isEmpty: Boolean
        get() = albums.isEmpty() && artists.isEmpty() && playlists.isEmpty() && songs.isEmpty()
}

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val libraryRepository: LibraryRepository,
    private val sourceRepository: SourceRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<SearchResults> =
        libraryRepository.activeSourceId
            .flatMapLatest { sourceId ->
                _query.flatMapLatest { text ->
                    if (sourceId == null || text.isBlank()) {
                        flowOf(SearchResults())
                    } else {
                        combine(
                            libraryRepository.searchAlbums(sourceId, text),
                            libraryRepository.searchArtists(sourceId, text),
                            libraryRepository.searchPlaylists(sourceId, text),
                            libraryRepository.searchSongs(sourceId, text),
                        ) { albums, artists, playlists, songs ->
                            SearchResults(albums, artists, playlists, songs)
                        }
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchResults())

    fun setQuery(value: String) {
        _query.value = value
    }

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean,
    ): CoverArtRef? = sourceRepository.coverArt(coverArt, thumbnail)

    fun play(song: Song) {
        viewModelScope.playSongInContext(libraryRepository, playbackController, song)
    }
}
