package com.subtracks.data.source.subsonic

import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import javax.xml.datatype.DatatypeConfigurationException
import javax.xml.datatype.DatatypeFactory
import org.w3c.dom.Document
import org.w3c.dom.Element

object SubsonicXml {
    fun artists(sourceId: Long, document: Document): List<Artist> =
        document.elements("artist").map { element ->
            Artist(
                sourceId = sourceId,
                id = element.attr("id"),
                name = element.attr("name"),
                albumCount = element.longAttr("albumCount") ?: 0L,
                starred = element.dateAttr("starred"),
            )
        }.filter { it.id.isNotEmpty() }

    fun albums(sourceId: Long, document: Document): List<Album> =
        document.elements("album").map { album(sourceId, it) }.filter { it.id.isNotEmpty() }

    fun album(sourceId: Long, element: Element): Album =
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

    fun songs(sourceId: Long, document: Document): List<Song> =
        document.elements("song").map { song(sourceId, it) }.filter { it.id.isNotEmpty() }

    fun song(sourceId: Long, element: Element): Song =
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

    fun playlists(sourceId: Long, document: Document): List<Playlist> =
        document.elements("playlist").map { playlist(sourceId, it) }.filter { it.id.isNotEmpty() }

    fun playlist(sourceId: Long, element: Element): Playlist =
        Playlist(
            sourceId = sourceId,
            id = element.attr("id"),
            name = element.attr("name"),
            comment = element.attr("comment").ifEmpty { null },
            coverArt = element.attr("coverArt").ifEmpty { null },
            songCount = element.longAttr("songCount") ?: 0L,
            created = element.dateAttr("created") ?: 0L,
        )

    fun playlistSongs(sourceId: Long, playlistId: String, document: Document): List<PlaylistSong> =
        document.elements("entry").mapIndexed { index, element ->
            PlaylistSong(
                sourceId = sourceId,
                playlistId = playlistId,
                songId = element.attr("id"),
                position = index.toLong(),
            )
        }
}

private fun Document.elements(tag: String): List<Element> {
    val nodes = getElementsByTagName(tag)
    return (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }
}

private fun Element.attr(name: String): String = getAttribute(name)

private fun Element.longAttr(name: String): Long? =
    getAttribute(name).takeIf { it.isNotEmpty() }?.toLongOrNull()

private fun Element.dateAttr(name: String): Long? =
    getAttribute(name).takeIf { it.isNotEmpty() }?.let(IsoDate::parse)

internal object IsoDate {
    private val timezone = Regex("(Z|[+-]\\d{2}:?\\d{2})$", RegexOption.IGNORE_CASE)

    private val factory: DatatypeFactory? = try {
        DatatypeFactory.newInstance()
    } catch (_: DatatypeConfigurationException) {
        null
    }

    fun parse(value: String): Long? {
        val factory = factory ?: return null
        return try {
            val zoned = if (timezone.containsMatchIn(value)) value else value + "Z"
            factory.newXMLGregorianCalendar(zoned).toGregorianCalendar().timeInMillis / 1000
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
