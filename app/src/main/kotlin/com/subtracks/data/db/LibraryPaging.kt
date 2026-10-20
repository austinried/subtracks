package com.subtracks.data.db

import androidx.room3.RoomRawQuery
import androidx.sqlite.SQLiteStatement
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.prefs.PlaylistSort
import com.subtracks.data.prefs.SortNameMode

// These filters mirror the download-aware ones in LibraryDao but never mention song_downloads, so
// the resulting PagingSource is not invalidated by download writes (including progress updates).
internal const val ALBUMS_FILTER_BASE =
    "FROM albums WHERE sourceId = ? " +
        "AND (? = 0 OR (? = 1 AND starred IS NOT NULL) OR (? = 2 AND starred IS NULL)) " +
        "AND (? = '' " +
        "OR (length(?) >= 3 AND rowid IN (SELECT rowid FROM album_search " +
        "WHERE album_search MATCH '\"' || replace(?, '\"', '\"\"') || '\"')) " +
        "OR (length(?) < 3 AND (instr(lower(name), lower(?)) > 0 " +
        "OR instr(lower(albumArtist), lower(?)) > 0 " +
        "OR instr(lower(sortName), lower(?)) > 0 " +
        "OR instr(lower(albumArtistSort), lower(?)) > 0))) "

internal const val ARTISTS_FILTER_BASE =
    "FROM artists WHERE sourceId = ? " +
        "AND (? = 0 OR (? = 1 AND starred IS NOT NULL) OR (? = 2 AND starred IS NULL)) " +
        "AND $ARTIST_CREDITED " +
        "AND (? = '' " +
        "OR (length(?) >= 3 AND rowid IN (SELECT rowid FROM artist_search " +
        "WHERE artist_search MATCH '\"' || replace(?, '\"', '\"\"') || '\"')) " +
        "OR (length(?) < 3 AND (instr(lower(name), lower(?)) > 0 " +
        "OR instr(lower(sortName), lower(?)) > 0))) "

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
    sortNameMode: SortNameMode = SortNameMode.SortName,
): RoomRawQuery {
    val order = albumOrder(sort, descending, sortNameMode)
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
            "ORDER BY played ASC, sortName COLLATE NOCASE, id",
    ) { stmt ->
        stmt.bindAlbumFilters(sourceId, starredFilter, search)
        stmt.bindLong(13, cutoff)
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
    bindText(11, search)
    bindText(12, search)
}

internal fun artistsDefaultQuery(
    sourceId: Long,
    starredFilter: Int,
    search: String,
    sort: ArtistSort,
    descending: Boolean,
    sortNameMode: SortNameMode = SortNameMode.SortName,
): RoomRawQuery {
    val order = artistOrder(sort, descending, sortNameMode)
    return RoomRawQuery("SELECT $ARTIST_COLUMNS $ARTISTS_FILTER_BASE ORDER BY $order") { stmt ->
        stmt.bindLong(1, sourceId)
        stmt.bindInt(2, starredFilter)
        stmt.bindInt(3, starredFilter)
        stmt.bindInt(4, starredFilter)
        stmt.bindText(5, search)
        stmt.bindText(6, search)
        stmt.bindText(7, search)
        stmt.bindText(8, search)
        stmt.bindText(9, search)
        stmt.bindText(10, search)
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

internal fun albumNameSortKey(mode: SortNameMode): String =
    when (mode) {
        SortNameMode.SortName -> "sortName COLLATE NOCASE"
        SortNameMode.Name -> "name COLLATE NOCASE"
        SortNameMode.NonLatin -> "(CASE WHEN name GLOB '*[A-Za-z]*' THEN name ELSE sortName END) COLLATE NOCASE"
    }

internal fun albumArtistSortKey(mode: SortNameMode): String =
    when (mode) {
        SortNameMode.SortName -> {
            "albumArtistSort COLLATE NOCASE"
        }

        SortNameMode.Name -> {
            "albumArtist COLLATE NOCASE"
        }

        SortNameMode.NonLatin -> {
            "(CASE WHEN albumArtist GLOB '*[A-Za-z]*' THEN albumArtist ELSE albumArtistSort END) COLLATE NOCASE"
        }
    }

internal fun artistNameSortKey(mode: SortNameMode): String =
    when (mode) {
        SortNameMode.SortName -> "sortName COLLATE NOCASE"
        SortNameMode.Name -> "name COLLATE NOCASE"
        SortNameMode.NonLatin -> "(CASE WHEN name GLOB '*[A-Za-z]*' THEN name ELSE sortName END) COLLATE NOCASE"
    }

private fun albumOrder(
    sort: AlbumSort,
    descending: Boolean,
    mode: SortNameMode,
): String =
    when (sort) {
        AlbumSort.Name -> {
            val name = albumNameSortKey(mode)
            if (descending) "$name DESC, id DESC" else "$name, id"
        }

        AlbumSort.Artist -> {
            val artist = albumArtistSortKey(mode)
            val name = albumNameSortKey(mode)
            if (descending) "$artist DESC, year DESC, $name DESC, id DESC" else "$artist, year, $name, id"
        }

        AlbumSort.Year -> {
            if (descending) ALBUM_ORDER_BY_YEAR_REVERSED else ALBUM_ORDER_BY_YEAR
        }

        AlbumSort.Added -> {
            if (descending) ALBUM_ORDER_BY_ADDED_REVERSED else ALBUM_ORDER_BY_ADDED
        }

        AlbumSort.Starred -> {
            if (descending) ALBUM_ORDER_BY_STARRED_REVERSED else ALBUM_ORDER_BY_STARRED
        }

        AlbumSort.Frequent -> {
            if (descending) ALBUM_ORDER_BY_FREQUENT_REVERSED else ALBUM_ORDER_BY_FREQUENT
        }

        AlbumSort.Recent -> {
            if (descending) ALBUM_ORDER_BY_RECENT_REVERSED else ALBUM_ORDER_BY_RECENT
        }
    }

private fun artistOrder(
    sort: ArtistSort,
    descending: Boolean,
    mode: SortNameMode,
): String =
    when (sort) {
        ArtistSort.Name -> {
            val name = artistNameSortKey(mode)
            if (descending) "$name DESC, id DESC" else "$name, id"
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
