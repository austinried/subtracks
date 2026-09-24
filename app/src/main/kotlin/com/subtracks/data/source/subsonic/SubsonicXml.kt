package com.subtracks.data.source.subsonic

import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import org.w3c.dom.Document
import org.w3c.dom.Element

object SubsonicXml {
    fun artists(
        sourceId: Long,
        document: Document,
    ): List<Artist> =
        document
            .elements("artist")
            .map { element ->
                Artist(
                    sourceId = sourceId,
                    id = element.attr("id"),
                    name = element.attr("name"),
                    albumCount = element.longAttr("albumCount") ?: 0L,
                    starred = element.dateAttr("starred"),
                    coverArt = element.attr("coverArt").ifEmpty { null },
                )
            }.filter { it.id.isNotEmpty() }

    fun albums(
        sourceId: Long,
        document: Document,
    ): List<Album> = document.elements("album").map { album(sourceId, it) }.filter { it.id.isNotEmpty() }

    fun album(
        sourceId: Long,
        element: Element,
    ): Album =
        Album(
            sourceId = sourceId,
            id = element.attr("id"),
            artistId = element.attr("artistId").ifEmpty { null },
            name = element.attr("name"),
            albumArtist = element.attr("artist").ifEmpty { null },
            created = element.dateAttr("created") ?: 0L,
            coverArt = element.attr("coverArt").ifEmpty { null },
            genre = element.attr("genre").ifEmpty { null },
            year = element.longAttr("year"),
            starred = element.dateAttr("starred"),
            songCount = element.longAttr("songCount") ?: 0L,
            frequentRank = null,
            recentRank = null,
        )

    fun songs(
        sourceId: Long,
        document: Document,
    ): List<Song> = document.elements("song").map { song(sourceId, it) }.filter { it.id.isNotEmpty() }

    fun song(
        sourceId: Long,
        element: Element,
    ): Song =
        Song(
            sourceId = sourceId,
            id = element.attr("id"),
            albumId = element.attr("albumId").ifEmpty { null },
            artistId = element.attr("artistId").ifEmpty { null },
            title = element.attr("title"),
            album = element.attr("album").ifEmpty { null },
            artist = element.attr("artist").ifEmpty { null },
            duration = element.longAttr("duration"),
            track = element.longAttr("track"),
            disc = element.longAttr("discNumber"),
            starred = element.dateAttr("starred"),
            genre = element.attr("genre").ifEmpty { null },
        )

    fun playlists(
        sourceId: Long,
        document: Document,
    ): List<Playlist> = document.elements("playlist").map { playlist(sourceId, it) }.filter { it.id.isNotEmpty() }

    fun playlist(
        sourceId: Long,
        element: Element,
    ): Playlist =
        Playlist(
            sourceId = sourceId,
            id = element.attr("id"),
            name = element.attr("name"),
            comment = element.attr("comment").ifEmpty { null },
            coverArt = element.attr("coverArt").ifEmpty { null },
            songCount = element.longAttr("songCount") ?: 0L,
            created = element.dateAttr("created") ?: 0L,
            changed = element.dateAttr("changed") ?: 0L,
            duration = element.longAttr("duration") ?: 0L,
        )

    fun playlistSongs(
        sourceId: Long,
        playlistId: String,
        document: Document,
    ): List<PlaylistSong> =
        document.elements("entry").mapIndexed { index, element ->
            PlaylistSong(
                sourceId = sourceId,
                playlistId = playlistId,
                songId = element.attr("id"),
                position = index.toLong(),
            )
        }
}

private fun Element.elements(tag: String): List<Element> {
    val nodes = getElementsByTagName(tag)
    return (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }
}

private fun Document.elements(tag: String): List<Element> {
    val nodes = getElementsByTagName(tag)
    return (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }
}

private fun Element.attr(name: String): String = getAttribute(name)

private fun Element.longAttr(name: String): Long? = getAttribute(name).takeIf { it.isNotEmpty() }?.toLongOrNull()

private fun Element.dateAttr(name: String): Long? = getAttribute(name).takeIf { it.isNotEmpty() }?.let(IsoDate::parse)

internal object IsoDate {
    private val pattern =
        Regex(
            "^(\\d{4})-(\\d{2})-(\\d{2})(?:[T ](\\d{2}):(\\d{2}):(\\d{2})(?:\\.\\d+)?\\s*(Z|[+-]\\d{2}:?\\d{2})?)?$",
        )

    fun parse(value: String): Long? {
        val match = pattern.matchEntire(value.trim()) ?: return null
        val year = match.groupValues[1].toIntOrNull() ?: return null
        val month = match.groupValues[2].toIntOrNull() ?: return null
        val day = match.groupValues[3].toIntOrNull() ?: return null
        val hour = match.groupValues[4].toIntOrNull() ?: 0
        val minute = match.groupValues[5].toIntOrNull() ?: 0
        val second = match.groupValues[6].toIntOrNull() ?: 0
        if (month !in 1..12 || day !in 1..31 || hour !in 0..23 || minute !in 0..59 || second !in 0..59) {
            return null
        }
        val days = daysFromCivil(year, month, day)
        return days * 86400L + hour * 3600L + minute * 60L + second - offsetSeconds(match.groupValues[7])
    }

    private fun offsetSeconds(zone: String): Long {
        if (zone.isEmpty() || zone.equals("Z", ignoreCase = true)) return 0
        val digits = zone.drop(1).replace(":", "")
        if (digits.length != 4) return 0
        val hours = digits.take(2).toLongOrNull() ?: return 0
        val minutes = digits.drop(2).toLongOrNull() ?: return 0
        val sign = if (zone[0] == '-') -1L else 1L
        return sign * (hours * 3600 + minutes * 60)
    }

    private fun daysFromCivil(
        year: Int,
        month: Int,
        day: Int,
    ): Long {
        val y = if (month <= 2) year - 1 else year
        val era = (if (y >= 0) y else y - 399) / 400
        val yearOfEra = y - era * 400
        val dayOfYear = (153 * (if (month > 2) month - 3 else month + 9) + 2) / 5 + day - 1
        val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
        return era * 146097L + dayOfEra - 719468L
    }
}
