package com.subtracks.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongListItem
import com.subtracks.data.prefs.AlbumSort
import kotlinx.coroutines.flow.Flow

class LibraryRepository(
    private val db: SubtracksDatabase,
    private val sourceRepository: SourceRepository,
) {
    val activeSourceId: Flow<Long?> = sourceRepository.activeSourceId()

    fun albums(
        sourceId: Long,
        sort: AlbumSort,
    ): Flow<PagingData<Album>> =
        Pager(PagingConfig(pageSize = 40, enablePlaceholders = false)) {
            when (sort) {
                AlbumSort.Name -> db.libraryDao().albumsByName(sourceId)
                AlbumSort.Artist -> db.libraryDao().albumsByArtist(sourceId)
                AlbumSort.Year -> db.libraryDao().albumsByYear(sourceId)
                AlbumSort.RecentlyAdded -> db.libraryDao().albumsByRecentlyAdded(sourceId)
            }
        }.flow

    fun artists(sourceId: Long): Flow<PagingData<Artist>> =
        Pager(PagingConfig(pageSize = 60, enablePlaceholders = false)) {
            db.libraryDao().artists(sourceId)
        }.flow

    fun songs(sourceId: Long): Flow<PagingData<SongListItem>> =
        Pager(PagingConfig(pageSize = 60, enablePlaceholders = false)) {
            db.libraryDao().songs(sourceId)
        }.flow

    fun playlists(sourceId: Long): Flow<PagingData<Playlist>> =
        Pager(PagingConfig(pageSize = 40, enablePlaceholders = false)) {
            db.libraryDao().playlists(sourceId)
        }.flow

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
    ): Flow<PagingData<SongListItem>> =
        Pager(PagingConfig(pageSize = 60, enablePlaceholders = false)) {
            db.libraryDao().playlistSongs(sourceId, playlistId)
        }.flow
}
