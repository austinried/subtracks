package com.subtracks.data.db

import androidx.room3.RoomRawQuery
import androidx.sqlite.SQLiteStatement
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.prefs.PlaylistSort

// These filters mirror the download-aware ones in LibraryDao but never mention song_downloads, so
// the resulting PagingSource is not invalidated by download writes (including progress updates).
internal const val ALBUMS_FILTER_BASE =
    "FROM albums WHERE sourceId = ? " +
        "AND (? = 0 OR (? = 1 AND starred IS NOT NULL) OR (? = 2 AND starred IS NULL)) " +
        "AND (? = '' " +
        "OR (length(?) >= 3 AND rowid IN (SELECT rowid FROM album_search " +
        "WHERE album_search MATCH '\"' || replace(?, '\"', '\"\"') || '\"')) " +
        "OR (length(?) < 3 AND (instr(lower(name), lower(?)) > 0 " +
        "OR instr(lower(albumArtist), lower(?)) > 0))) "

internal const val ARTISTS_FILTER_BASE =
    "FROM artists WHERE sourceId = ? " +
        "AND (? = 0 OR (? = 1 AND starred IS NOT NULL) OR (? = 2 AND starred IS NULL)) " +
        "AND (? = '' " +
        "OR (length(?) >= 3 AND rowid IN (SELECT rowid FROM artist_search " +
        "WHERE artist_search MATCH '\"' || replace(?, '\"', '\"\"') || '\"')) " +
        "OR (length(?) < 3 AND instr(lower(name), lower(?)) > 0)) "

internal const val PLAYLISTS_FILTER_BASE =
    "FROM playlists WHERE sourceId = ? " +
        "AND (? = '' " +
        "OR (length(?) >= 3 AND rowid IN (SELECT rowid FROM playlist_search " +
        "WHERE playlist_search MATCH '\"' || replace(?, '\"', '\"\"') || '\"')) " +
        "OR (length(?) < 3 AND instr(lower(name), lower(?)) > 0)) "

internal fun albumsDefaultQuery(
    sourceId: Long,
    starredFilter: Int,
    search: String,
    sort: AlbumSort,
    descending: Boolean,
): RoomRawQuery {
    val order = albumOrder(sort, descending)
    return RoomRawQuery("SELECT * $ALBUMS_FILTER_BASE ORDER BY $order") { stmt ->
        stmt.bindAlbumFilters(sourceId, starredFilter, search)
    }
}

internal fun rediscoverAlbumsQuery(
    sourceId: Long,
    starredFilter: Int,
    search: String,
    cutoff: Long,
): RoomRawQuery =
    RoomRawQuery(
        "SELECT * $ALBUMS_FILTER_BASE AND played IS NOT NULL AND played < ? " +
            "ORDER BY played ASC, name COLLATE NOCASE, id",
    ) { stmt ->
        stmt.bindAlbumFilters(sourceId, starredFilter, search)
        stmt.bindLong(11, cutoff)
    }

private fun SQLiteStatement.bindAlbumFilters(
    sourceId: Long,
    starredFilter: Int,
    search: String,
) {
    bindLong(1, sourceId)
    bindInt(2, starredFilter)
    bindInt(3, starredFilter)
    bindInt(4, starredFilter)
    bindText(5, search)
    bindText(6, search)
    bindText(7, search)
    bindText(8, search)
    bindText(9, search)
    bindText(10, search)
}

internal fun artistsDefaultQuery(
    sourceId: Long,
    starredFilter: Int,
    search: String,
    sort: ArtistSort,
    descending: Boolean,
): RoomRawQuery {
    val order = artistOrder(sort, descending)
    return RoomRawQuery("SELECT * $ARTISTS_FILTER_BASE ORDER BY $order") { stmt ->
        stmt.bindLong(1, sourceId)
        stmt.bindInt(2, starredFilter)
        stmt.bindInt(3, starredFilter)
        stmt.bindInt(4, starredFilter)
        stmt.bindText(5, search)
        stmt.bindText(6, search)
        stmt.bindText(7, search)
        stmt.bindText(8, search)
        stmt.bindText(9, search)
    }
}

internal fun playlistsDefaultQuery(
    sourceId: Long,
    search: String,
    sort: PlaylistSort,
    descending: Boolean,
): RoomRawQuery {
    val order = playlistOrder(sort, descending)
    return RoomRawQuery("SELECT * $PLAYLISTS_FILTER_BASE ORDER BY $order") { stmt ->
        stmt.bindLong(1, sourceId)
        stmt.bindText(2, search)
        stmt.bindText(3, search)
        stmt.bindText(4, search)
        stmt.bindText(5, search)
        stmt.bindText(6, search)
    }
}

private fun albumOrder(
    sort: AlbumSort,
    descending: Boolean,
): String =
    when (sort) {
        AlbumSort.Name -> if (descending) ALBUM_ORDER_BY_NAME_REVERSED else ALBUM_ORDER_BY_NAME
        AlbumSort.Artist -> if (descending) ALBUM_ORDER_BY_ARTIST_REVERSED else ALBUM_ORDER_BY_ARTIST
        AlbumSort.Year -> if (descending) ALBUM_ORDER_BY_YEAR_REVERSED else ALBUM_ORDER_BY_YEAR
        AlbumSort.Added -> if (descending) ALBUM_ORDER_BY_ADDED_REVERSED else ALBUM_ORDER_BY_ADDED
        AlbumSort.Starred -> if (descending) ALBUM_ORDER_BY_STARRED_REVERSED else ALBUM_ORDER_BY_STARRED
        AlbumSort.Frequent -> if (descending) ALBUM_ORDER_BY_FREQUENT_REVERSED else ALBUM_ORDER_BY_FREQUENT
        AlbumSort.Recent -> if (descending) ALBUM_ORDER_BY_RECENT_REVERSED else ALBUM_ORDER_BY_RECENT
    }

private fun artistOrder(
    sort: ArtistSort,
    descending: Boolean,
): String =
    when (sort) {
        ArtistSort.Name -> {
            if (descending) ARTIST_ORDER_BY_NAME_REVERSED else ARTIST_ORDER_BY_NAME
        }

        ArtistSort.AlbumCount -> {
            if (descending) ARTIST_ORDER_BY_ALBUM_COUNT_REVERSED else ARTIST_ORDER_BY_ALBUM_COUNT
        }

        ArtistSort.Starred -> {
            if (descending) ARTIST_ORDER_BY_STARRED_REVERSED else ARTIST_ORDER_BY_STARRED
        }

        ArtistSort.Frequent -> {
            if (descending) ARTIST_ORDER_BY_FREQUENT_REVERSED else ARTIST_ORDER_BY_FREQUENT
        }

        ArtistSort.Recent -> {
            if (descending) ARTIST_ORDER_BY_RECENT_REVERSED else ARTIST_ORDER_BY_RECENT
        }
    }

private fun playlistOrder(
    sort: PlaylistSort,
    descending: Boolean,
): String =
    when (sort) {
        PlaylistSort.Name -> if (descending) PLAYLIST_ORDER_BY_NAME_REVERSED else PLAYLIST_ORDER_BY_NAME
        PlaylistSort.Added -> if (descending) PLAYLIST_ORDER_BY_ADDED_REVERSED else PLAYLIST_ORDER_BY_ADDED
        PlaylistSort.Updated -> if (descending) PLAYLIST_ORDER_BY_UPDATED_REVERSED else PLAYLIST_ORDER_BY_UPDATED
    }
