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
import com.subtracks.data.model.PlaylistSongItem
import com.subtracks.data.model.Song
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.prefs.PlaylistSort
import com.subtracks.data.prefs.StarredFilter
import com.subtracks.data.source.ServerActionSink
import com.subtracks.data.source.StarType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LibraryRepository(
    private val db: SubtracksDatabase,
    private val sourceRepository: SourceRepository,
    private val serverActions: ServerActionSink,
    private val showMessage: (String) -> Unit = {},
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) {
    val activeSourceId: Flow<Long?> = sourceRepository.activeSourceId()

    fun hasAlbumPlayCount(sourceId: Long): Flow<Boolean> = db.libraryDao().hasAlbumPlayCount(sourceId)

    fun hasAlbumPlayed(sourceId: Long): Flow<Boolean> = db.libraryDao().hasAlbumPlayed(sourceId)

    fun albums(
        sourceId: Long,
        sort: AlbumSort,
        descending: Boolean,
        starred: StarredFilter,
        search: String,
        downloaded: Boolean,
    ): Flow<PagingData<Album>> =
        pager(40) {
            val dao = db.libraryDao()
            val downloadedFilter = if (downloaded) 1 else 0
            when (sort) {
                AlbumSort.Name -> {
                    if (descending) {
                        dao.albumsByNameReversed(sourceId, starred.ordinal, search, downloadedFilter)
                    } else {
                        dao.albumsByName(sourceId, starred.ordinal, search, downloadedFilter)
                    }
                }

                AlbumSort.Artist -> {
                    if (descending) {
                        dao.albumsByArtistReversed(sourceId, starred.ordinal, search, downloadedFilter)
                    } else {
                        dao.albumsByArtist(sourceId, starred.ordinal, search, downloadedFilter)
                    }
                }

                AlbumSort.Year -> {
                    if (descending) {
                        dao.albumsByYearReversed(sourceId, starred.ordinal, search, downloadedFilter)
                    } else {
                        dao.albumsByYear(sourceId, starred.ordinal, search, downloadedFilter)
                    }
                }

                AlbumSort.Added -> {
                    if (descending) {
                        dao.albumsByRecentlyAddedReversed(sourceId, starred.ordinal, search, downloadedFilter)
                    } else {
                        dao.albumsByRecentlyAdded(sourceId, starred.ordinal, search, downloadedFilter)
                    }
                }

                AlbumSort.Starred -> {
                    if (descending) {
                        dao.albumsByStarredReversed(sourceId, starred.ordinal, search, downloadedFilter)
                    } else {
                        dao.albumsByStarred(sourceId, starred.ordinal, search, downloadedFilter)
                    }
                }

                AlbumSort.Frequent -> {
                    if (descending) {
                        dao.albumsByFrequentReversed(sourceId, starred.ordinal, search, downloadedFilter)
                    } else {
                        dao.albumsByFrequent(sourceId, starred.ordinal, search, downloadedFilter)
                    }
                }

                AlbumSort.Recent -> {
                    if (descending) {
                        dao.albumsByRecentReversed(sourceId, starred.ordinal, search, downloadedFilter)
                    } else {
                        dao.albumsByRecent(sourceId, starred.ordinal, search, downloadedFilter)
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
        downloaded: Boolean,
    ): Flow<PagingData<Artist>> =
        pager(60) {
            val dao = db.libraryDao()
            val downloadedFilter = if (downloaded) 1 else 0
            when (sort) {
                ArtistSort.Name -> {
                    if (descending) {
                        dao.artistsByNameReversed(sourceId, starred.ordinal, search, downloadedFilter)
                    } else {
                        dao.artistsByName(sourceId, starred.ordinal, search, downloadedFilter)
                    }
                }

                ArtistSort.AlbumCount -> {
                    if (descending) {
                        dao.artistsByAlbumCountReversed(sourceId, starred.ordinal, search, downloadedFilter)
                    } else {
                        dao.artistsByAlbumCount(sourceId, starred.ordinal, search, downloadedFilter)
                    }
                }

                ArtistSort.Starred -> {
                    if (descending) {
                        dao.artistsByStarredReversed(sourceId, starred.ordinal, search, downloadedFilter)
                    } else {
                        dao.artistsByStarred(sourceId, starred.ordinal, search, downloadedFilter)
                    }
                }

                ArtistSort.Frequent -> {
                    if (descending) {
                        dao.artistsByFrequentReversed(sourceId, starred.ordinal, search, downloadedFilter)
                    } else {
                        dao.artistsByFrequent(sourceId, starred.ordinal, search, downloadedFilter)
                    }
                }

                ArtistSort.Recent -> {
                    if (descending) {
                        dao.artistsByRecentReversed(sourceId, starred.ordinal, search, downloadedFilter)
                    } else {
                        dao.artistsByRecent(sourceId, starred.ordinal, search, downloadedFilter)
                    }
                }
            }
        }

    fun playlists(
        sourceId: Long,
        sort: PlaylistSort,
        descending: Boolean,
        search: String,
        downloaded: Boolean,
    ): Flow<PagingData<Playlist>> =
        pager(40) {
            val dao = db.libraryDao()
            val downloadedFilter = if (downloaded) 1 else 0
            when (sort) {
                PlaylistSort.Name -> {
                    if (descending) {
                        dao.playlistsByNameReversed(sourceId, search, downloadedFilter)
                    } else {
                        dao.playlistsByName(sourceId, search, downloadedFilter)
                    }
                }

                PlaylistSort.Added -> {
                    if (descending) {
                        dao.playlistsByAddedReversed(sourceId, search, downloadedFilter)
                    } else {
                        dao.playlistsByAdded(sourceId, search, downloadedFilter)
                    }
                }

                PlaylistSort.Updated -> {
                    if (descending) {
                        dao.playlistsByUpdatedReversed(sourceId, search, downloadedFilter)
                    } else {
                        dao.playlistsByUpdated(sourceId, search, downloadedFilter)
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

    suspend fun recordPlay(
        songId: String,
        at: Long,
    ) {
        val sourceId = sourceRepository.activeSourceIdOnce() ?: return
        db.libraryDao().recordPlay(sourceId, songId, at)
    }

    fun artistAlbums(
        sourceId: Long,
        artistId: String,
        downloaded: Boolean = false,
    ): Flow<List<Album>> =
        if (downloaded) {
            db.libraryDao().albumsForArtistDownloaded(sourceId, artistId)
        } else {
            db.libraryDao().albumsForArtist(sourceId, artistId)
        }

    fun albumDiscs(
        sourceId: Long,
        albumId: String,
    ): Flow<List<Disc>> = db.libraryDao().discs(sourceId, albumId)

    fun albumSongs(
        sourceId: Long,
        albumId: String,
        downloaded: Boolean = false,
    ): Flow<List<Song>> =
        if (downloaded) {
            db.libraryDao().songsByAlbumDownloaded(sourceId, albumId)
        } else {
            db.libraryDao().songsByAlbum(sourceId, albumId)
        }

    suspend fun albumSongOrdinal(
        sourceId: Long,
        albumId: String,
        songId: String,
    ): Long {
        val index = db.queueDao().albumSongIds(sourceId, albumId).indexOf(songId)
        return if (index < 0) 0L else index.toLong()
    }

    fun playlist(
        sourceId: Long,
        playlistId: String,
    ): Flow<Playlist?> = db.libraryDao().playlist(sourceId, playlistId)

    fun playlistSongs(
        sourceId: Long,
        playlistId: String,
        downloaded: Boolean = false,
    ): Flow<PagingData<PlaylistSongItem>> =
        pager(60) {
            if (downloaded) {
                db.libraryDao().playlistSongsDownloaded(sourceId, playlistId)
            } else {
                db.libraryDao().playlistSongs(sourceId, playlistId)
            }
        }

    private val starLock = Mutex()

    fun star(
        type: StarType,
        id: String,
        starred: Boolean,
    ) {
        scope.launch { setStar(type, id, starred) }
    }

    fun toggleStar(
        type: StarType,
        id: String,
    ) {
        scope.launch {
            starLock.withLock {
                val sourceId = sourceRepository.activeSourceIdOnce() ?: return@withLock
                setStarLocked(type, id, starredValue(sourceId, type, id) == null)
            }
        }
    }

    suspend fun setStar(
        type: StarType,
        id: String,
        starred: Boolean,
    ): Result<Unit> = starLock.withLock { setStarLocked(type, id, starred) }

    private suspend fun setStarLocked(
        type: StarType,
        id: String,
        starred: Boolean,
    ): Result<Unit> {
        val sourceId = sourceRepository.activeSourceIdOnce()
        if (sourceId == null) {
            return Result.failure(IllegalStateException("No active server"))
        }
        val previous = starredValue(sourceId, type, id)
        updateStarred(sourceId, type, id, if (starred) System.currentTimeMillis() else null)
        return try {
            serverActions.setStar(type, id, starred)
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
