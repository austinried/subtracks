package com.subtracks.data.db

import androidx.room3.RoomRawQuery

internal val CREDITED_ALBUM_ROWS = albumCreditRows(AlbumCreditQuery())
internal val DOWNLOADED_ALBUM_ROWS = albumCreditRows(AlbumCreditQuery(downloaded = true))
internal val DOWNLOADED_ALBUM_DETAILS_FOR_ARTIST =
    albumCreditRows(AlbumCreditQuery(downloaded = true, artistScoped = true, selectAlbums = true))
internal val DOWNLOADED_COVER_ALBUM_ROWS = albumCreditRows(AlbumCreditQuery(downloaded = true, coverOnly = true))
internal val ARTIST_COVER_CREDITS = albumCreditRows(AlbumCreditQuery(coverOnly = true))
internal val ARTIST_COVER_CREDITS_FOR_ARTIST = albumCreditRows(AlbumCreditQuery(coverOnly = true, artistScoped = true))

private data class AlbumCreditQuery(
    val artistScoped: Boolean = false,
    val coverOnly: Boolean = false,
    val downloaded: Boolean = false,
    val selectAlbums: Boolean = false,
)

private fun albumCreditRows(query: AlbumCreditQuery): String =
    if (query.selectAlbums) {
        listOf(
            primaryAlbumCredits(query),
            albumRelationCredits(query),
            trackRelationCredits(query),
            trackCredits(query),
        )
    } else {
        listOf(
            primaryAlbumCredits(query),
            albumRelationCredits(query),
            trackCredits(query),
            trackRelationCredits(query),
        )
    }.joinToString(if (query.downloaded) " UNION " else " UNION ALL ")

private fun primaryAlbumCredits(query: AlbumCreditQuery): String =
    buildString {
        append(
            when {
                query.selectAlbums -> "SELECT albums.* FROM albums "
                query.downloaded -> "SELECT albums.id AS albumId, albums.artistId AS artistId FROM albums "
                else -> "SELECT albums.artistId AS artistId, albums.id AS albumId, 'album' AS creditType FROM albums "
            },
        )
        append("WHERE albums.sourceId = :sourceId AND ${query.directArtist("albums.artistId")} ")
        append(query.coverFilter())
        if (query.downloaded) append("AND $ALBUM_DOWNLOADED ")
    }.trim()

private fun albumRelationCredits(query: AlbumCreditQuery): String =
    buildString {
        append(
            when {
                query.selectAlbums -> "SELECT albums.* FROM album_artists "
                query.downloaded -> "SELECT albums.id, album_artists.artistId FROM album_artists "
                else -> "SELECT album_artists.artistId, albums.id, 'album' FROM album_artists "
            },
        )
        append("JOIN albums ON albums.sourceId = album_artists.sourceId AND albums.id = album_artists.albumId ")
        append("WHERE album_artists.sourceId = :sourceId ")
        if (query.artistScoped) append("AND album_artists.artistId = :artistId ")
        append(query.coverFilter())
        if (query.downloaded) append("AND $ALBUM_DOWNLOADED ")
    }.trim()

private fun trackCredits(query: AlbumCreditQuery): String =
    buildString {
        append(
            when {
                query.selectAlbums -> "SELECT albums.* FROM songs "
                query.downloaded -> "SELECT songs.albumId, songs.artistId FROM songs "
                else -> "SELECT songs.artistId, songs.albumId, 'track' FROM songs "
            },
        )
        append(SONG_ALBUM_JOIN)
        if (query.downloaded) append(TRACK_DOWNLOAD_JOIN)
        append("WHERE songs.sourceId = :sourceId AND ${query.directArtist("songs.artistId")} ")
        append(query.coverFilter())
        if (query.downloaded) append("AND sd.status = 'Completed' ")
    }.trim()

private fun trackRelationCredits(query: AlbumCreditQuery): String =
    buildString {
        append(
            when {
                query.selectAlbums -> "SELECT albums.* FROM song_artists "
                query.downloaded -> "SELECT songs.albumId, song_artists.artistId FROM song_artists "
                else -> "SELECT song_artists.artistId, songs.albumId, 'track' FROM song_artists "
            },
        )
        append("JOIN songs ON songs.sourceId = song_artists.sourceId AND songs.id = song_artists.songId ")
        append(SONG_ALBUM_JOIN)
        if (query.downloaded) append(TRACK_DOWNLOAD_JOIN)
        append("WHERE song_artists.sourceId = :sourceId ")
        if (query.artistScoped) append("AND song_artists.artistId = :artistId ")
        append(query.coverFilter())
        if (query.downloaded) append("AND sd.status = 'Completed' ")
    }.trim()

private fun AlbumCreditQuery.directArtist(column: String): String = if (artistScoped) "$column = :artistId" else "$column IS NOT NULL"

private fun AlbumCreditQuery.coverFilter(): String = if (coverOnly) "AND albums.coverArt IS NOT NULL " else ""

private const val SONG_ALBUM_JOIN = "JOIN albums ON albums.sourceId = songs.sourceId AND albums.id = songs.albumId "

private const val TRACK_DOWNLOAD_JOIN = "JOIN song_downloads sd ON sd.sourceId = songs.sourceId AND sd.songId = songs.id "

private const val ALBUM_DOWNLOADED =
    "EXISTS (SELECT 1 FROM songs " +
        "JOIN song_downloads sd ON sd.sourceId = songs.sourceId AND sd.songId = songs.id " +
        "WHERE songs.sourceId = albums.sourceId AND songs.albumId = albums.id AND sd.status = 'Completed')"

internal fun downloadedArtistArtQuery(
    sourceId: Long,
    artistIds: List<String>,
): RoomRawQuery {
    val bindings = mutableMapOf<String, Any?>("sourceId" to sourceId)
    val placeholders =
        artistIds
            .mapIndexed { index, artistId ->
                val name = "artistId$index"
                bindings[name] = artistId
                ":$name"
            }.ifEmpty { listOf("NULL") }
    return rawQuery(DOWNLOADED_ARTIST_ART_UPDATE_SQL.replace(":artistIds", placeholders.joinToString(",")), bindings)
}
