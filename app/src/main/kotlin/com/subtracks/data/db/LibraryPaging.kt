package com.subtracks.data.db

import androidx.room3.RoomRawQuery
import androidx.sqlite.SQLiteStatement
import com.subtracks.data.prefs.AlbumSort
import com.subtracks.data.prefs.ArtistSort
import com.subtracks.data.prefs.PlaylistSort
import com.subtracks.data.prefs.SortNameMode

// The default filters never mention song_downloads, so the paging sources used without the
// Downloaded filter are not invalidated by download writes (including progress updates). The
// downloaded variants are the same statements the hand-written queries used.
internal const val ALBUMS_FILTER = """
FROM albums WHERE sourceId = :sourceId
AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL))
AND (:search = ''
    OR (length(:search) >= $SEARCH_MIN_LENGTH AND rowid IN (SELECT rowid FROM album_search
        WHERE album_search MATCH '"' || replace(:search, '"', '""') || '"'))
    OR (length(:search) < $SEARCH_MIN_LENGTH AND (instr(lower(name), lower(:search)) > 0
        OR instr(lower(albumArtist), lower(:search)) > 0
        OR instr(lower(sortName), lower(:search)) > 0
        OR instr(lower(albumArtistSort), lower(:search)) > 0)))
"""

internal const val ALBUMS_FILTER_DOWNLOADED = """
FROM albums WHERE sourceId = :sourceId
AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL))
AND (:downloadedFilter = 0 OR EXISTS (SELECT 1 FROM songs
    JOIN song_downloads sd ON sd.sourceId = songs.sourceId AND sd.songId = songs.id
    WHERE songs.sourceId = albums.sourceId AND songs.albumId = albums.id AND sd.status = 'Completed'))
AND (:search = ''
    OR (length(:search) >= $SEARCH_MIN_LENGTH AND rowid IN (SELECT rowid FROM album_search
        WHERE album_search MATCH '"' || replace(:search, '"', '""') || '"'))
    OR (length(:search) < $SEARCH_MIN_LENGTH AND (instr(lower(name), lower(:search)) > 0
        OR instr(lower(albumArtist), lower(:search)) > 0
        OR instr(lower(sortName), lower(:search)) > 0
        OR instr(lower(albumArtistSort), lower(:search)) > 0)))
"""

internal const val ARTISTS_FILTER = """
FROM artists WHERE sourceId = :sourceId
AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL))
AND $ARTIST_CREDITED
AND (:search = ''
    OR (length(:search) >= $SEARCH_MIN_LENGTH AND rowid IN (SELECT rowid FROM artist_search
        WHERE artist_search MATCH '"' || replace(:search, '"', '""') || '"'))
    OR (length(:search) < $SEARCH_MIN_LENGTH AND (instr(lower(name), lower(:search)) > 0
        OR instr(lower(sortName), lower(:search)) > 0)))
"""

internal const val ARTISTS_FILTER_DOWNLOADED = """
FROM artists WHERE sourceId = :sourceId
AND (:starredFilter = 0 OR (:starredFilter = 1 AND starred IS NOT NULL) OR (:starredFilter = 2 AND starred IS NULL))
AND $ARTIST_CREDITED
AND (:downloadedFilter = 0 OR EXISTS (SELECT 1 FROM ($ARTIST_DOWNLOAD_ROWS) dl
    WHERE dl.id = artists.id AND dl.status = 'Completed'))
AND (:search = ''
    OR (length(:search) >= $SEARCH_MIN_LENGTH AND rowid IN (SELECT rowid FROM artist_search
        WHERE artist_search MATCH '"' || replace(:search, '"', '""') || '"'))
    OR (length(:search) < $SEARCH_MIN_LENGTH AND (instr(lower(name), lower(:search)) > 0
        OR instr(lower(sortName), lower(:search)) > 0)))
"""

// With the Downloaded filter on, count the albums that actually have a download so the number under
// an artist matches the albums its detail screen lists.
internal const val ARTIST_COLUMNS_DOWNLOADED = """
artists.sourceId, artists.id, artists.name,
CASE WHEN :downloadedFilter = 1 THEN (SELECT COUNT(*) FROM ($DOWNLOADED_ARTIST_ALBUMS)) ELSE artists.albumCount END AS albumCount,
$ARTIST_COLUMNS_TAIL
"""

internal const val PLAYLISTS_FILTER = """
FROM playlists WHERE sourceId = :sourceId
AND (:search = ''
    OR (length(:search) >= $SEARCH_MIN_LENGTH AND rowid IN (SELECT rowid FROM playlist_search
        WHERE playlist_search MATCH '"' || replace(:search, '"', '""') || '"'))
    OR (length(:search) < $SEARCH_MIN_LENGTH AND instr(lower(name), lower(:search)) > 0))
"""

internal const val PLAYLISTS_FILTER_DOWNLOADED = """
FROM playlists WHERE sourceId = :sourceId
AND (:downloadedFilter = 0 OR EXISTS (SELECT 1 FROM playlist_songs ps
    JOIN song_downloads sd ON sd.sourceId = ps.sourceId AND sd.songId = ps.songId
    WHERE ps.sourceId = playlists.sourceId AND ps.playlistId = playlists.id AND sd.status = 'Completed'))
AND (:search = ''
    OR (length(:search) >= $SEARCH_MIN_LENGTH AND rowid IN (SELECT rowid FROM playlist_search
        WHERE playlist_search MATCH '"' || replace(:search, '"', '""') || '"'))
    OR (length(:search) < $SEARCH_MIN_LENGTH AND instr(lower(name), lower(:search)) > 0))
"""

// With the Downloaded filter on, count the songs that actually have a download so the number under
// a playlist matches the songs its detail screen lists.
internal const val PLAYLISTS_COLUMNS_DOWNLOADED = """
playlists.sourceId, playlists.id, playlists.name, playlists.comment, playlists.coverArt,
CASE WHEN :downloadedFilter = 1 THEN (SELECT COUNT(DISTINCT ps.songId)
    FROM playlist_songs ps
    JOIN song_downloads sd ON sd.sourceId = ps.sourceId AND sd.songId = ps.songId AND sd.status = 'Completed'
    WHERE ps.sourceId = playlists.sourceId AND ps.playlistId = playlists.id
) ELSE playlists.songCount END AS songCount,
playlists.created, playlists.changed, playlists.duration, playlists.owner, playlists.shared
"""

internal fun albumsPageQuery(
    sourceId: Long,
    starredFilter: Int,
    search: String,
    sort: AlbumSort,
    descending: Boolean,
    downloaded: Boolean,
    sortNameMode: SortNameMode = SortNameMode.SortName,
): RoomRawQuery =
    rawQuery(
        sql = "SELECT * ${if (downloaded) ALBUMS_FILTER_DOWNLOADED else ALBUMS_FILTER} ORDER BY ${albumOrder(sort, descending, sortNameMode)}",
        bindings =
            mapOf(
                "sourceId" to sourceId,
                "starredFilter" to starredFilter,
                "downloadedFilter" to if (downloaded) 1 else 0,
                "search" to search,
            ),
    )

internal fun artistsPageQuery(
    sourceId: Long,
    starredFilter: Int,
    search: String,
    sort: ArtistSort,
    descending: Boolean,
    downloaded: Boolean,
    sortNameMode: SortNameMode = SortNameMode.SortName,
): RoomRawQuery =
    rawQuery(
        sql =
            "SELECT ${if (downloaded) ARTIST_COLUMNS_DOWNLOADED else ARTIST_COLUMNS} " +
                "${if (downloaded) ARTISTS_FILTER_DOWNLOADED else ARTISTS_FILTER} " +
                "ORDER BY ${artistOrder(sort, descending, sortNameMode)}",
        bindings =
            mapOf(
                "sourceId" to sourceId,
                "starredFilter" to starredFilter,
                "downloadedFilter" to if (downloaded) 1 else 0,
                "search" to search,
            ),
    )

internal fun playlistsPageQuery(
    sourceId: Long,
    search: String,
    sort: PlaylistSort,
    descending: Boolean,
    downloaded: Boolean,
): RoomRawQuery =
    rawQuery(
        sql =
            "SELECT ${if (downloaded) PLAYLISTS_COLUMNS_DOWNLOADED else "*"} " +
                "${if (downloaded) PLAYLISTS_FILTER_DOWNLOADED else PLAYLISTS_FILTER} " +
                "ORDER BY ${playlistOrder(sort, descending)}",
        bindings =
            mapOf(
                "sourceId" to sourceId,
                "downloadedFilter" to if (downloaded) 1 else 0,
                "search" to search,
            ),
    )

internal fun rediscoverAlbumsQuery(
    sourceId: Long,
    starredFilter: Int,
    search: String,
    cutoff: Long,
): RoomRawQuery =
    rawQuery(
        sql =
            "SELECT * $ALBUMS_FILTER AND played IS NOT NULL AND played < :cutoff " +
                "ORDER BY played ASC, sortName COLLATE NOCASE, id",
        bindings =
            mapOf(
                "sourceId" to sourceId,
                "starredFilter" to starredFilter,
                "search" to search,
                "cutoff" to cutoff,
            ),
    )

// RoomRawQuery binds by position, so the named fragments are rewritten once here instead of
// every caller carrying an index for each repeated :sourceId.
internal fun rawQuery(
    sql: String,
    bindings: Map<String, Any?>,
): RoomRawQuery {
    val names = mutableListOf<String>()
    val positional =
        buildString(sql.length) {
            var index = 0
            var quote: Char? = null
            while (index < sql.length) {
                val char = sql[index]
                if (quote != null) {
                    append(char)
                    if (char == quote) quote = null
                    index++
                    continue
                }
                if (char == '\'' || char == '"') {
                    quote = char
                    append(char)
                    index++
                    continue
                }
                if (char == ':' && sql.getOrNull(index + 1).let { it != null && (it.isLetter() || it == '_') }) {
                    var end = index + 1
                    while (end < sql.length && (sql[end].isLetterOrDigit() || sql[end] == '_')) end++
                    names += sql.substring(index + 1, end)
                    append('?')
                    index = end
                    continue
                }
                append(char)
                index++
            }
        }
    return RoomRawQuery(positional) { statement ->
        names.forEachIndexed { index, name -> statement.bind(index + 1, bindings.getValue(name)) }
    }
}

private fun SQLiteStatement.bind(
    index: Int,
    value: Any?,
) {
    when (value) {
        null -> bindNull(index)
        is Int -> bindLong(index, value.toLong())
        is Long -> bindLong(index, value)
        is String -> bindText(index, value)
        else -> error("Unsupported SQL parameter: ${value::class.qualifiedName}")
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
