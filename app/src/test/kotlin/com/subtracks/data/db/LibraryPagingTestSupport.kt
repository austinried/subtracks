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
        downloadedAlbumsPaging(
            albumsPageQuery(
                sourceId = sourceId,
                starredFilter = starredFilter,
                search = search,
                sort = sort,
                descending = descending,
                downloaded = true,
                sortNameMode = sortNameMode,
            ),
        )
    } else {
        albumsPaging(
            albumsPageQuery(
                sourceId = sourceId,
                starredFilter = starredFilter,
                search = search,
                sort = sort,
                descending = descending,
                downloaded = false,
                sortNameMode = sortNameMode,
            ),
        )
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
        downloadedArtistsPaging(
            artistsPageQuery(
                sourceId = sourceId,
                starredFilter = starredFilter,
                search = search,
                sort = sort,
                descending = descending,
                downloaded = true,
                sortNameMode = sortNameMode,
            ),
        )
    } else {
        artistsPaging(
            artistsPageQuery(
                sourceId = sourceId,
                starredFilter = starredFilter,
                search = search,
                sort = sort,
                descending = descending,
                downloaded = false,
                sortNameMode = sortNameMode,
            ),
        )
    }

internal fun LibraryDao.playlistPages(
    sourceId: Long,
    search: String = "",
    sort: PlaylistSort = PlaylistSort.Name,
    descending: Boolean = false,
    downloaded: Boolean = false,
): PagingSource<Int, Playlist> =
    if (downloaded) {
        downloadedPlaylistsPaging(
            playlistsPageQuery(
                sourceId = sourceId,
                search = search,
                sort = sort,
                descending = descending,
                downloaded = true,
            ),
        )
    } else {
        playlistsPaging(
            playlistsPageQuery(
                sourceId = sourceId,
                search = search,
                sort = sort,
                descending = descending,
                downloaded = false,
            ),
        )
    }
