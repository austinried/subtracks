package com.subtracks.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.subtracks.data.model.Album
import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Song
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class HomeListRequest(
    val title: String,
    val section: HomeSection? = null,
    val genre: String? = null,
    val decade: Long? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeListViewModel(
    private val libraryRepository: LibraryRepository,
    private val sourceRepository: SourceRepository,
    private val playbackController: PlaybackController,
    val request: HomeListRequest,
) : ViewModel() {
    private val sourceId = libraryRepository.activeSourceId.filterNotNull()

    val albums: Flow<PagingData<Album>> =
        sourceId
            .flatMapLatest { id ->
                when {
                    request.decade != null -> {
                        libraryRepository.albumsByDecade(id, request.decade)
                    }

                    request.section == HomeSection.RecentlyPlayedAlbums -> {
                        libraryRepository.homeRecentlyPlayedAlbums(id)
                    }

                    request.section == HomeSection.MostPlayedAlbums -> {
                        libraryRepository.homeMostPlayedAlbums(id)
                    }

                    request.section == HomeSection.RecentlyAddedAlbums -> {
                        libraryRepository.homeRecentlyAddedAlbums(id)
                    }

                    request.section == HomeSection.Rediscover -> {
                        libraryRepository.rediscoverAlbumsPage(id)
                    }

                    else -> {
                        flowOf(PagingData.empty<Album>())
                    }
                }
            }.cachedIn(viewModelScope)

    val artists: Flow<PagingData<Artist>> =
        sourceId
            .flatMapLatest { id ->
                when (request.section) {
                    HomeSection.RecentlyPlayedArtists -> {
                        libraryRepository.homeRecentlyPlayedArtists(id)
                    }

                    HomeSection.MostPlayedArtists -> {
                        libraryRepository.homeMostPlayedArtists(id)
                    }

                    else -> {
                        flowOf(PagingData.empty<Artist>())
                    }
                }
            }.cachedIn(viewModelScope)

    val songs: Flow<PagingData<AlbumSongItem>> =
        sourceId
            .flatMapLatest { id ->
                when {
                    request.genre != null -> libraryRepository.songsByGenre(id, request.genre)
                    request.section == HomeSection.RecentlyStarredSongs -> libraryRepository.starredSongs(id)
                    else -> flowOf(PagingData.empty<AlbumSongItem>())
                }
            }.cachedIn(viewModelScope)

    val genres: StateFlow<List<String>> =
        sourceId
            .flatMapLatest { libraryRepository.genresByMostPlayed(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val decades: StateFlow<List<Long>> =
        sourceId
            .flatMapLatest { libraryRepository.decades(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean,
    ): CoverArtRef? = sourceRepository.coverArt(coverArt, thumbnail)

    fun play(song: Song) {
        if (request.section == HomeSection.RecentlyStarredSongs) {
            viewModelScope.playStarredList(libraryRepository, playbackController, song)
        } else {
            viewModelScope.playSongInContext(libraryRepository, playbackController, song)
        }
    }
}
