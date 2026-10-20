package com.subtracks.data.db

import androidx.paging.PagingSource
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.prefs.PlaylistSort
import com.subtracks.data.prefs.SortNameMode

internal fun LibraryDao.albumPages(
    sourceId: Long,
    starredFilter: Int = 0,
    search: String = "",
    sort: AlbumSort = AlbumSort.Name,
    descending: Boolean = false,
    downloaded: Boolean = false,
    sortNameMode: SortNameMode = SortNameMode.SortName,
): PagingSource<Int, Album> =
    if (downloaded) {
        downloadedAlbumsPaging(albumsPageQuery(sourceId, starredFilter, search, sort, descending, true, sortNameMode))
    } else {
        albumsPaging(albumsPageQuery(sourceId, starredFilter, search, sort, descending, false, sortNameMode))
    }

internal fun LibraryDao.artistPages(
    sourceId: Long,
    starredFilter: Int = 0,
    search: String = "",
    sort: ArtistSort = ArtistSort.Name,
    descending: Boolean = false,
    downloaded: Boolean = false,
    sortNameMode: SortNameMode = SortNameMode.SortName,
): PagingSource<Int, Artist> =
    if (downloaded) {
        downloadedArtistsPaging(artistsPageQuery(sourceId, starredFilter, search, sort, descending, true, sortNameMode))
    } else {
        artistsPaging(artistsPageQuery(sourceId, starredFilter, search, sort, descending, false, sortNameMode))
    }

internal fun LibraryDao.playlistPages(
    sourceId: Long,
    search: String = "",
    sort: PlaylistSort = PlaylistSort.Name,
    descending: Boolean = false,
    downloaded: Boolean = false,
): PagingSource<Int, Playlist> =
    if (downloaded) {
        downloadedPlaylistsPaging(playlistsPageQuery(sourceId, search, sort, descending, true))
    } else {
        playlistsPaging(playlistsPageQuery(sourceId, search, sort, descending, false))
    }
