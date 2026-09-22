package com.subtracks.ui.library

import androidx.lifecycle.ViewModel
import com.subtracks.data.model.Album
import com.subtracks.data.model.CoverArtRef
import com.subtracks.data.model.Song
import com.subtracks.data.repo.LibraryRepository
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumDetailViewModel(
    private val libraryRepository: LibraryRepository,
    private val sourceRepository: SourceRepository,
    albumId: String,
) : ViewModel() {
    private val sourceId = libraryRepository.activeSourceId.filterNotNull()

    val album: Flow<Album?> = sourceId.flatMapLatest { libraryRepository.album(it, albumId) }

    val songs: Flow<List<Song>> = sourceId.flatMapLatest { libraryRepository.albumSongs(it, albumId) }

    fun coverArt(
        coverArt: String?,
        thumbnail: Boolean,
    ): CoverArtRef? = sourceRepository.coverArt(coverArt, thumbnail)
}
