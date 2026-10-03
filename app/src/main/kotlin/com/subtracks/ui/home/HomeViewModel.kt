package com.subtracks.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subtracks.data.model.Album
import com.subtracks.data.model.AlbumSongItem
import com.subtracks.data.model.Artist
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Song
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.repo.rediscoverCutoff
import com.subtracks.data.sync.SyncManager
import com.subtracks.data.sync.SyncStatus
import com.subtracks.playback.PlaybackController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val HOME_ROW_LIMIT = 10
const val HOME_STARRED_LIMIT = 5

data class HomeFeed(
    val recentlyPlayedAlbums: List<Album> = emptyList(),
    val recentlyPlayedArtists: List<Artist> = emptyList(),
    val mostPlayedAlbums: List<Album> = emptyList(),
    val mostPlayedArtists: List<Artist> = emptyList(),
    val genres: List<String> = emptyList(),
    val decades: List<Long> = emptyList(),
    val recentlyStarredSongs: List<AlbumSongItem> = emptyList(),
    val recentlyAddedAlbums: List<Album> = emptyList(),
    val rediscoverAlbums: List<Album> = emptyList(),
    val loading: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val libraryRepository: LibraryRepository,
    private val sourceRepository: SourceRepository,
    private val syncManager: SyncManager,
    private val playbackController: PlaybackController,
) : ViewModel() {
    val syncing: StateFlow<Boolean> =
        syncManager.status
            .map { it == SyncStatus.Running }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val feed: StateFlow<HomeFeed> =
        libraryRepository.activeSourceId
            .filterNotNull()
            .flatMapLatest { sourceId ->
                val plays =
                    combine(
                        libraryRepository.recentlyPlayedAlbums(sourceId, HOME_ROW_LIMIT),
                        libraryRepository.recentlyPlayedArtists(sourceId, HOME_ROW_LIMIT),
                        libraryRepository.mostPlayedAlbums(sourceId, HOME_ROW_LIMIT),
                        libraryRepository.mostPlayedArtists(sourceId, HOME_ROW_LIMIT),
                    ) { albums, artists, frequentAlbums, frequentArtists ->
                        PlayRows(albums, artists, frequentAlbums, frequentArtists)
                    }
                val discovery =
                    combine(
                        libraryRepository.genresByMostPlayed(sourceId),
                        libraryRepository.decades(sourceId),
                        libraryRepository.recentlyStarredSongs(sourceId, HOME_STARRED_LIMIT),
                        libraryRepository.recentlyAddedAlbums(sourceId, HOME_ROW_LIMIT),
                        libraryRepository.rediscoverAlbums(sourceId, rediscoverCutoff(), HOME_ROW_LIMIT),
                    ) { genres, decades, starred, added, rediscover ->
                        Discovery(genres, decades, starred, added, rediscover)
                    }
                val hasPlayData =
                    combine(
                        libraryRepository.hasAlbumPlayCount(sourceId),
                        libraryRepository.hasAlbumPlayed(sourceId),
                    ) { frequent, recent -> frequent || recent }
                combine(plays, discovery, hasPlayData) { rows, found, played ->
                    buildHomeFeed(
                        recentAlbums = rows.recentAlbums,
                        recentArtists = rows.recentArtists,
                        frequentAlbums = rows.frequentAlbums,
                        frequentArtists = rows.frequentArtists,
                        genres = found.genres,
                        decades = found.decades,
                        starredSongs = found.starredSongs,
                        addedAlbums = found.addedAlbums,
                        rediscover = found.rediscover,
                        hasPlayData = played,
                    )
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeFeed(loading = true))

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean,
    ): CoverArtRef? = sourceRepository.coverArt(coverArt, thumbnail)

    fun playStarred(song: Song) {
        viewModelScope.playStarredList(libraryRepository, playbackController, song)
    }

    fun sync() = syncManager.requestSync()

    private data class PlayRows(
        val recentAlbums: List<Album>,
        val recentArtists: List<Artist>,
        val frequentAlbums: List<Album>,
        val frequentArtists: List<Artist>,
    )

    private data class Discovery(
        val genres: List<String>,
        val decades: List<Long>,
        val starredSongs: List<AlbumSongItem>,
        val addedAlbums: List<Album>,
        val rediscover: List<Album>,
    )
}

internal fun buildHomeFeed(
    recentAlbums: List<Album>,
    recentArtists: List<Artist>,
    frequentAlbums: List<Album>,
    frequentArtists: List<Artist>,
    genres: List<String>,
    decades: List<Long>,
    starredSongs: List<AlbumSongItem>,
    addedAlbums: List<Album>,
    rediscover: List<Album>,
    hasPlayData: Boolean,
): HomeFeed =
    HomeFeed(
        recentlyPlayedAlbums = recentAlbums.takeIf { hasPlayData }.orEmpty(),
        recentlyPlayedArtists = recentArtists.takeIf { hasPlayData }.orEmpty(),
        mostPlayedAlbums = frequentAlbums.takeIf { hasPlayData }.orEmpty(),
        mostPlayedArtists = frequentArtists.takeIf { hasPlayData }.orEmpty(),
        genres = genres,
        decades = decades,
        recentlyStarredSongs = starredSongs,
        recentlyAddedAlbums = addedAlbums,
        rediscoverAlbums = rediscover,
    )

internal fun CoroutineScope.playSongInContext(
    libraryRepository: LibraryRepository,
    playbackController: PlaybackController,
    song: Song,
) {
    launch {
        val albumId = song.albumId
        if (albumId == null) {
            playbackController.playSong(song.sourceId, song.id)
        } else {
            val ordinal = libraryRepository.albumSongOrdinal(song.sourceId, albumId, song.id)
            playbackController.playAlbum(song.sourceId, albumId, ordinal)
        }
    }
}

internal fun CoroutineScope.playStarredList(
    libraryRepository: LibraryRepository,
    playbackController: PlaybackController,
    song: Song,
) {
    launch {
        val ids = libraryRepository.starredSongIds(song.sourceId)
        playbackController.playSongs(song.sourceId, ids, ids.indexOf(song.id).coerceAtLeast(0))
    }
}
