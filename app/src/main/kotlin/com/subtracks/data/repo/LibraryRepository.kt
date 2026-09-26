package com.subtracks.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Disc
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongListItem
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.prefs.PlaylistSort
import com.subtracks.data.prefs.SongSort
import com.subtracks.data.prefs.StarredFilter
import com.subtracks.data.source.StarType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LibraryRepository(
    private val db: SubtracksDatabase,
    private val sourceRepository: SourceRepository,
    private val showMessage: (String) -> Unit = {},
) {
    val activeSourceId: Flow<Long?> = sourceRepository.activeSourceId()

    fun albums(
        sourceId: Long,
        sort: AlbumSort,
        descending: Boolean,
        starred: StarredFilter,
        search: String,
    ): Flow<PagingData<Album>> =
        pager(40) {
            val dao = db.libraryDao()
            when (sort) {
                AlbumSort.Name -> {
                    if (descending) {
                        dao.albumsByNameReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.albumsByName(sourceId, starred.ordinal, search)
                    }
                }

                AlbumSort.Artist -> {
                    if (descending) {
                        dao.albumsByArtistReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.albumsByArtist(sourceId, starred.ordinal, search)
                    }
                }

                AlbumSort.Year -> {
                    if (descending) {
                        dao.albumsByYearReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.albumsByYear(sourceId, starred.ordinal, search)
                    }
                }

                AlbumSort.Added -> {
                    if (descending) {
                        dao.albumsByRecentlyAddedReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.albumsByRecentlyAdded(sourceId, starred.ordinal, search)
                    }
                }

                AlbumSort.Starred -> {
                    if (descending) {
                        dao.albumsByStarredReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.albumsByStarred(sourceId, starred.ordinal, search)
                    }
                }
            }
        }

    fun artists(
        sourceId: Long,
        sort: ArtistSort,
        descending: Boolean,
        starred: StarredFilter,
        search: String,
    ): Flow<PagingData<Artist>> =
        pager(60) {
            val dao = db.libraryDao()
            when (sort) {
                ArtistSort.Name -> {
                    if (descending) {
                        dao.artistsByNameReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.artistsByName(sourceId, starred.ordinal, search)
                    }
                }

                ArtistSort.AlbumCount -> {
                    if (descending) {
                        dao.artistsByAlbumCountReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.artistsByAlbumCount(sourceId, starred.ordinal, search)
                    }
                }

                ArtistSort.Starred -> {
                    if (descending) {
                        dao.artistsByStarredReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.artistsByStarred(sourceId, starred.ordinal, search)
                    }
                }
            }
        }

    fun playlists(
        sourceId: Long,
        sort: PlaylistSort,
        descending: Boolean,
        search: String,
    ): Flow<PagingData<Playlist>> =
        pager(40) {
            val dao = db.libraryDao()
            when (sort) {
                PlaylistSort.Name -> {
                    if (descending) {
                        dao.playlistsByNameReversed(sourceId, search)
                    } else {
                        dao.playlistsByName(sourceId, search)
                    }
                }

                PlaylistSort.Added -> {
                    if (descending) {
                        dao.playlistsByAddedReversed(sourceId, search)
                    } else {
                        dao.playlistsByAdded(sourceId, search)
                    }
                }

                PlaylistSort.Updated -> {
                    if (descending) {
                        dao.playlistsByUpdatedReversed(sourceId, search)
                    } else {
                        dao.playlistsByUpdated(sourceId, search)
                    }
                }
            }
        }

    fun songs(
        sourceId: Long,
        sort: SongSort,
        descending: Boolean,
        starred: StarredFilter,
        search: String,
    ): Flow<PagingData<SongListItem>> =
        pager(60) {
            val dao = db.libraryDao()
            when (sort) {
                SongSort.Album -> {
                    if (descending) {
                        dao.songsByAlbumReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.songs(sourceId, starred.ordinal, search)
                    }
                }

                SongSort.Title -> {
                    if (descending) {
                        dao.songsByTitleReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.songsByTitle(sourceId, starred.ordinal, search)
                    }
                }

                SongSort.Artist -> {
                    if (descending) {
                        dao.songsByArtistReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.songsByArtist(sourceId, starred.ordinal, search)
                    }
                }

                SongSort.Starred -> {
                    if (descending) {
                        dao.songsByStarredReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.songsByStarred(sourceId, starred.ordinal, search)
                    }
                }

                SongSort.Added -> {
                    if (descending) {
                        dao.songsByAddedReversed(sourceId, starred.ordinal, search)
                    } else {
                        dao.songsByAdded(sourceId, starred.ordinal, search)
                    }
                }
            }
        }

    fun album(
        sourceId: Long,
        albumId: String,
    ): Flow<Album?> = db.libraryDao().album(sourceId, albumId)

    fun artist(
        sourceId: Long,
        artistId: String,
    ): Flow<Artist?> = db.libraryDao().artist(sourceId, artistId)

    fun song(
        sourceId: Long,
        songId: String,
    ): Flow<Song?> = db.libraryDao().song(sourceId, songId)

    fun artistAlbums(
        sourceId: Long,
        artistId: String,
    ): Flow<List<Album>> = db.libraryDao().albumsForArtist(sourceId, artistId)

    fun albumDiscs(
        sourceId: Long,
        albumId: String,
    ): Flow<List<Disc>> = db.libraryDao().discs(sourceId, albumId)

    fun albumSongs(
        sourceId: Long,
        albumId: String,
    ): Flow<List<Song>> = db.libraryDao().songsByAlbum(sourceId, albumId)

    fun playlist(
        sourceId: Long,
        playlistId: String,
    ): Flow<Playlist?> = db.libraryDao().playlist(sourceId, playlistId)

    fun playlistSongs(
        sourceId: Long,
        playlistId: String,
    ): Flow<PagingData<SongListItem>> = pager(60) { db.libraryDao().playlistSongs(sourceId, playlistId) }

    private val starLock = Mutex()

    suspend fun setStar(
        type: StarType,
        id: String,
        starred: Boolean,
    ): Result<Unit> =
        starLock.withLock {
            val sourceId = sourceRepository.activeSourceIdOnce()
            val source = sourceRepository.activeMusicSource()
            if (sourceId == null || source == null) {
                return@withLock Result.failure(IllegalStateException("No active server"))
            }
            val previous = starredValue(sourceId, type, id)
            updateStarred(sourceId, type, id, if (starred) System.currentTimeMillis() else null)
            try {
                source.setStar(type, id, starred)
                Result.success(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                withContext(NonCancellable) { updateStarred(sourceId, type, id, previous) }
                showMessage("Could not update star")
                Result.failure(failure)
            }
        }

    private suspend fun starredValue(
        sourceId: Long,
        type: StarType,
        id: String,
    ): Long? =
        when (type) {
            StarType.Song -> song(sourceId, id).first()?.starred
            StarType.Album -> album(sourceId, id).first()?.starred
            StarType.Artist -> artist(sourceId, id).first()?.starred
        }

    private suspend fun updateStarred(
        sourceId: Long,
        type: StarType,
        id: String,
        starred: Long?,
    ) {
        val dao = db.libraryDao()
        when (type) {
            StarType.Song -> dao.setSongStar(sourceId, id, starred)
            StarType.Album -> dao.setAlbumStar(sourceId, id, starred)
            StarType.Artist -> dao.setArtistStar(sourceId, id, starred)
        }
    }

    private fun <T : Any> pager(
        pageSize: Int,
        source: () -> PagingSource<Int, T>,
    ): Flow<PagingData<T>> =
        Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = source,
        ).flow
}
