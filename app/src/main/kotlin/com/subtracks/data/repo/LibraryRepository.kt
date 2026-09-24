package com.subtracks.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongListItem
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.prefs.PlaylistSort
import com.subtracks.data.prefs.SongSort
import kotlinx.coroutines.flow.Flow

data class SearchHit(
    val type: String,
    val itemId: String,
    val title: String,
) {
    val isSong: Boolean get() = type == "song"
}

class LibraryRepository(
    private val db: SubtracksDatabase,
    private val sourceRepository: SourceRepository,
) {
    val activeSourceId: Flow<Long?> = sourceRepository.activeSourceId()

    fun albums(
        sourceId: Long,
        sort: AlbumSort,
        descending: Boolean,
        starredOnly: Boolean,
    ): Flow<PagingData<Album>> =
        pager(40) {
            val dao = db.libraryDao()
            when (sort) {
                AlbumSort.Name -> {
                    if (descending) dao.albumsByNameReversed(sourceId, starredOnly) else dao.albumsByName(sourceId, starredOnly)
                }

                AlbumSort.Artist -> {
                    if (descending) dao.albumsByArtistReversed(sourceId, starredOnly) else dao.albumsByArtist(sourceId, starredOnly)
                }

                AlbumSort.Year -> {
                    if (descending) dao.albumsByYearReversed(sourceId, starredOnly) else dao.albumsByYear(sourceId, starredOnly)
                }

                AlbumSort.RecentlyAdded -> {
                    if (descending) {
                        dao.albumsByRecentlyAddedReversed(sourceId, starredOnly)
                    } else {
                        dao.albumsByRecentlyAdded(sourceId, starredOnly)
                    }
                }
            }
        }

    fun artists(
        sourceId: Long,
        sort: ArtistSort,
        descending: Boolean,
        starredOnly: Boolean,
    ): Flow<PagingData<Artist>> =
        pager(60) {
            val dao = db.libraryDao()
            when (sort) {
                ArtistSort.Name -> {
                    if (descending) dao.artistsByNameReversed(sourceId, starredOnly) else dao.artistsByName(sourceId, starredOnly)
                }

                ArtistSort.AlbumCount -> {
                    if (descending) {
                        dao.artistsByAlbumCountReversed(sourceId, starredOnly)
                    } else {
                        dao.artistsByAlbumCount(sourceId, starredOnly)
                    }
                }
            }
        }

    fun playlists(
        sourceId: Long,
        sort: PlaylistSort,
        descending: Boolean,
    ): Flow<PagingData<Playlist>> =
        pager(40) {
            val dao = db.libraryDao()
            when (sort) {
                PlaylistSort.Name -> {
                    if (descending) dao.playlistsByNameReversed(sourceId) else dao.playlistsByName(sourceId)
                }

                PlaylistSort.RecentlyAdded -> {
                    if (descending) dao.playlistsByRecentlyAddedReversed(sourceId) else dao.playlistsByRecentlyAdded(sourceId)
                }
            }
        }

    fun songs(
        sourceId: Long,
        sort: SongSort,
        descending: Boolean,
        starredOnly: Boolean,
    ): Flow<PagingData<SongListItem>> =
        pager(60) {
            val dao = db.libraryDao()
            when (sort) {
                SongSort.Album -> {
                    dao.songs(sourceId, starredOnly)
                }

                SongSort.Title -> {
                    if (descending) dao.songsByTitleReversed(sourceId, starredOnly) else dao.songsByTitle(sourceId, starredOnly)
                }

                SongSort.Artist -> {
                    if (descending) dao.songsByArtistReversed(sourceId, starredOnly) else dao.songsByArtist(sourceId, starredOnly)
                }
            }
        }

    suspend fun search(
        sourceId: Long,
        query: String,
        limit: Int = 50,
    ): List<SearchHit> =
        db
            .searchDao()
            .search(sourceId.toString(), query, limit)
            .map { SearchHit(it.type, it.itemId, it.title) }

    fun album(
        sourceId: Long,
        albumId: String,
    ): Flow<Album?> = db.libraryDao().album(sourceId, albumId)

    fun artist(
        sourceId: Long,
        artistId: String,
    ): Flow<Artist?> = db.libraryDao().artist(sourceId, artistId)

    fun artistAlbums(
        sourceId: Long,
        artistId: String,
    ): Flow<List<Album>> = db.libraryDao().albumsForArtist(sourceId, artistId)

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

    private fun <T : Any> pager(
        pageSize: Int,
        source: () -> PagingSource<Int, T>,
    ): Flow<PagingData<T>> =
        Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = source,
        ).flow
}
