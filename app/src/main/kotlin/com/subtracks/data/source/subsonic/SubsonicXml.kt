package com.subtracks.data.source.subsonic

import com.subtracks.R
import com.subtracks.UiMessage
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.InputStream
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

object SubsonicXml {
    internal fun artist(
        sourceId: Long,
        attrs: Attributes,
    ): Artist =
        Artist(
            sourceId = sourceId,
            id = attrs.attr("id"),
            name = attrs.attr("name"),
            albumCount = attrs.longAttr("albumCount") ?: 0L,
            starred = attrs.dateAttr("starred"),
            coverArt = attrs.attr("coverArt").ifEmpty { null },
            artistImageUrl = attrs.attr("artistImageUrl").ifEmpty { null },
            sortName = attrs.attr("sortName").takeIf { it.isNotBlank() } ?: attrs.attr("name"),
        )

    internal fun albumDraft(
        sourceId: Long,
        attrs: Attributes,
    ): AlbumDraft =
        AlbumDraft(
            sourceId = sourceId,
            id = attrs.attr("id"),
            artistId = attrs.attr("artistId").ifEmpty { null },
            name = attrs.attr("name"),
            albumArtist = attrs.attr("artist").ifEmpty { null },
            displayArtist = attrs.attr("displayArtist").ifEmpty { null },
            sortName = attrs.attr("sortName").ifEmpty { null },
            created = attrs.dateAttr("created") ?: 0L,
            coverArt = attrs.attr("coverArt").ifEmpty { null },
            genre = attrs.attr("genre").ifEmpty { null },
            year = attrs.longAttr("year"),
            starred = attrs.dateAttr("starred"),
            songCount = attrs.longAttr("songCount") ?: 0L,
        )

    internal fun song(
        sourceId: Long,
        attrs: Attributes,
    ): Song = songDraft(sourceId, attrs).toSong()

    internal fun songDraft(
        sourceId: Long,
        attrs: Attributes,
    ): SongDraft =
        SongDraft(
            sourceId = sourceId,
            id = attrs.attr("id"),
            albumId = attrs.attr("albumId").ifEmpty { null },
            artistId = attrs.attr("artistId").ifEmpty { null },
            title = attrs.attr("title"),
            album = attrs.attr("album").ifEmpty { null },
            artist = attrs.attr("artist").ifEmpty { null },
            displayArtist = attrs.attr("displayArtist").ifEmpty { null },
            sortName = attrs.attr("sortName").ifEmpty { null },
            duration = attrs.longAttr("duration"),
            track = attrs.longAttr("track"),
            disc = attrs.longAttr("discNumber"),
            starred = attrs.dateAttr("starred"),
            genre = attrs.attr("genre").ifEmpty { null },
            created = attrs.dateAttr("created") ?: 0L,
            playCount = attrs.longAttr("playCount") ?: 0L,
            played = attrs.dateAttr("played"),
        )

    internal fun songChild(
        entity: SongDraft,
        name: String,
        attrs: Attributes,
        level: Int,
    ) {
        when (name) {
            "genres" -> if (level == 1) entity.addGenre(attrs)
            "replayGain" -> if (level == 1) entity.addReplayGain(attrs)
            "artists" -> if (level == 1) entity.addArtists(attrs)
            "albumArtists" -> entity.clearNestedArtists()
            "artist" -> if (level == 2) entity.addNestedArtist(attrs)
            else -> if (level == 1) entity.clearNestedArtists()
        }
    }

    internal fun albumChild(
        entity: AlbumDraft,
        name: String,
        attrs: Attributes,
        level: Int,
    ) {
        when (name) {
            "discTitles" -> if (level == 1) entity.addDiscTitle(attrs)
            "artists" -> if (level == 1) entity.addArtists(attrs)
            "artist" -> if (level == 2) entity.addNestedArtist(attrs)
            else -> if (level == 1) entity.clearArtists()
        }
    }

    internal fun playlist(
        sourceId: Long,
        attrs: Attributes,
    ): Playlist =
        Playlist(
            sourceId = sourceId,
            id = attrs.attr("id"),
            name = attrs.attr("name"),
            comment = attrs.attr("comment").ifEmpty { null },
            coverArt = attrs.attr("coverArt").ifEmpty { null },
            songCount = attrs.longAttr("songCount") ?: 0L,
            created = attrs.dateAttr("created") ?: 0L,
            changed = attrs.dateAttr("changed") ?: 0L,
            duration = attrs.longAttr("duration") ?: 0L,
            owner = attrs.attr("owner").ifEmpty { null },
            shared = attrs.boolAttr("public"),
        )

    internal fun playlistSong(
        sourceId: Long,
        playlistId: String,
        position: Long,
        attrs: Attributes,
    ): PlaylistSong =
        PlaylistSong(
            sourceId = sourceId,
            playlistId = playlistId,
            songId = attrs.attr("id"),
            position = position,
        )

    internal fun readStatus(input: InputStream) {
        parse(input, SubsonicResponseHandler())
    }

    internal fun <T> readEntities(
        input: InputStream,
        entityTag: String,
        create: (Attributes) -> T,
        onChild: (T, String, Attributes, Int) -> Unit = { _, _, _, _ -> },
        onEnd: (T) -> Unit,
    ) {
        parse(input, EntityHandler(entityTag, create, onChild, onEnd, stopAfterFirst = false))
    }

    internal fun containsEntity(
        input: InputStream,
        entityTag: String,
    ): Boolean {
        val handler = EntityHandler(entityTag, { Unit }, { _, _, _, _ -> }, { }, stopAfterFirst = true)
        parse(input, handler)
        return handler.found
    }

    internal fun artistBiography(input: InputStream): String? {
        val handler = ArtistBiographyHandler()
        parse(input, handler)
        return handler.biography
    }
}

internal class AlbumDraft(
    val sourceId: Long,
    var id: String = "",
    var artistId: String? = null,
    var name: String = "",
    var albumArtist: String? = null,
    var displayArtist: String? = null,
    var sortName: String? = null,
    var created: Long = 0L,
    var coverArt: String? = null,
    var genre: String? = null,
    var year: Long? = null,
    var starred: Long? = null,
    var songCount: Long = 0L,
    val discTitles: MutableMap<Long, String> = mutableMapOf(),
) {
    private val artists = mutableListOf<ArtistRef>()
    private var artistContainer = false

    fun addDiscTitle(attrs: Attributes) {
        val disc = attrs.longAttr("disc") ?: return
        val title = attrs.attr("title").takeIf { it.isNotBlank() } ?: return
        discTitles[disc] = title
    }

    fun addArtists(attrs: Attributes) {
        val ref = attrs.artistRef()
        if (ref == null) {
            artistContainer = true
        } else {
            artists += ref
            artistContainer = false
        }
    }

    fun addNestedArtist(attrs: Attributes) {
        if (artistContainer) attrs.artistRef()?.let { artists += it }
    }

    fun clearArtists() {
        artistContainer = false
    }

    fun toAlbum(): Album {
        val refs = artistFallback(artists, artistId, albumArtist)
        return Album(
            sourceId = sourceId,
            id = id,
            artistId = artistId,
            name = name,
            albumArtist = displayName(albumArtist, displayArtist, refs),
            created = created,
            coverArt = coverArt,
            genre = genre,
            year = year,
            starred = starred,
            songCount = songCount,
            discTitles = discTitles.toMap(),
            artists = refs,
            sortName = sortName?.takeIf { it.isNotBlank() } ?: name,
        )
    }
}

internal class SongDraft(
    private val sourceId: Long,
    private val id: String = "",
    private val albumId: String? = null,
    private val artistId: String? = null,
    private val title: String = "",
    private val album: String? = null,
    private val artist: String? = null,
    private val displayArtist: String? = null,
    private val sortName: String? = null,
    private val duration: Long? = null,
    private val track: Long? = null,
    private val disc: Long? = null,
    private val starred: Long? = null,
    private val genre: String? = null,
    private val created: Long = 0L,
    private val playCount: Long = 0L,
    private val played: Long? = null,
    private var trackGain: Float? = null,
    private var albumGain: Float? = null,
    private var trackPeak: Float? = null,
    private var albumPeak: Float? = null,
    private val genres: MutableList<String> = mutableListOf(),
) {
    private val artists = mutableListOf<ArtistRef>()
    private var nestedArtists: MutableList<ArtistRef>? = null

    fun addGenre(attrs: Attributes) {
        attrs
            .attr("name")
            .trim()
            .takeIf { it.isNotEmpty() }
            ?.let { genres += it }
    }

    fun addReplayGain(attrs: Attributes) {
        attrs.floatAttr("trackGain")?.let { trackGain = it }
        attrs.floatAttr("albumGain")?.let { albumGain = it }
        attrs.floatAttr("trackPeak")?.let { trackPeak = it }
        attrs.floatAttr("albumPeak")?.let { albumPeak = it }
    }

    fun addArtists(attrs: Attributes) {
        val ref = attrs.artistRef()
        if (ref == null) {
            nestedArtists = artists
        } else {
            artists += ref
            nestedArtists = null
        }
    }

    fun clearNestedArtists() {
        nestedArtists = null
    }

    fun addNestedArtist(attrs: Attributes) {
        nestedArtists?.let { list -> attrs.artistRef()?.let { list += it } }
    }

    fun toSong(): Song {
        val refs = artistFallback(artists, artistId, artist)
        return Song(
            sourceId = sourceId,
            id = id,
            albumId = albumId,
            artistId = artistId,
            title = title,
            album = album,
            artist = displayName(artist, displayArtist, refs),
            duration = duration,
            track = track,
            disc = disc,
            starred = starred,
            genre = genre,
            created = created,
            playCount = playCount,
            played = played,
            trackGain = trackGain,
            albumGain = albumGain,
            trackPeak = trackPeak,
            albumPeak = albumPeak,
            genres = genres.toList(),
            artists = refs,
            sortName = sortName?.takeIf { it.isNotBlank() } ?: title,
        )
    }
}

private fun Attributes.artistRef(): ArtistRef? {
    val id = attr("id")
    val name = attr("name")
    if (id.isEmpty() && name.isEmpty()) return null
    return ArtistRef(id, name)
}

private fun artistFallback(
    parsed: List<ArtistRef>,
    artistId: String?,
    name: String?,
): List<ArtistRef> {
    val refs = parsed.filter { it.id.isNotBlank() }.distinctBy { it.id }
    if (refs.isNotEmpty()) return refs
    if (artistId.isNullOrBlank()) return emptyList()
    return listOf(ArtistRef(artistId, name.orEmpty()))
}

private const val ARTIST_JOINER = " • "

private fun displayName(
    legacy: String?,
    openSubsonic: String?,
    refs: List<ArtistRef>,
): String? =
    legacy?.takeIf { it.isNotBlank() }
        ?: openSubsonic?.takeIf { it.isNotBlank() }
        ?: refs
            .map { it.name }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(ARTIST_JOINER)
            .takeIf { it.isNotEmpty() }

internal fun Attributes.attr(name: String): String = getValue(name) ?: ""

internal fun Attributes.longAttr(name: String): Long? = attr(name).takeIf { it.isNotEmpty() }?.toLongOrNull()

internal fun Attributes.boolAttr(name: String): Boolean = attr(name).equals("true", ignoreCase = true)

internal fun Attributes.floatAttr(name: String): Float? = attr(name).takeIf { it.isNotEmpty() }?.toFloatOrNull()?.takeIf { it.isFinite() }

internal fun Attributes.dateAttr(name: String): Long? = attr(name).takeIf { it.isNotEmpty() }?.let(IsoDate::parse)

private const val ROOT_TAG = "subsonic-response"

private class XmlFailure(
    val code: Int,
    message: String,
    val uiMessage: UiMessage? = null,
) : RuntimeException(message)

private class StopReading : RuntimeException()

private open class SubsonicResponseHandler : DefaultHandler() {
    private var depth = 0
    private var statusFailed = false

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String,
        attributes: Attributes,
    ) {
        depth++
        if (depth == 1) {
            if (qName != ROOT_TAG) throw XmlFailure(-1, "Unexpected response from the server", UiMessage(R.string.error_server_unexpected))
            if (attributes.getValue("status") == "failed") statusFailed = true
        }
        if (qName == "error") {
            throw XmlFailure(
                attributes.getValue("code")?.toIntOrNull() ?: -1,
                attributes.getValue("message") ?: "Unknown error",
            )
        }
        onElementStart(qName, attributes, depth)
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String,
    ) {
        onElementEnd(qName, depth)
        depth--
    }

    override fun endDocument() {
        if (statusFailed) throw XmlFailure(-1, "Unknown error", UiMessage(R.string.error_server_unknown))
    }

    protected open fun onElementStart(
        name: String,
        attributes: Attributes,
        depth: Int,
    ) = Unit

    protected open fun onElementEnd(
        name: String,
        depth: Int,
    ) = Unit
}

private class ArtistBiographyHandler : SubsonicResponseHandler() {
    private var biographyDepth = -1
    private val text = StringBuilder()

    var biography: String? = null
        private set

    override fun characters(
        ch: CharArray,
        start: Int,
        length: Int,
    ) {
        if (biographyDepth >= 0) text.append(ch, start, length)
    }

    override fun onElementStart(
        name: String,
        attributes: Attributes,
        depth: Int,
    ) {
        if (name == "biography" && biographyDepth < 0) biographyDepth = depth
    }

    override fun onElementEnd(
        name: String,
        depth: Int,
    ) {
        if (name == "biography" && depth == biographyDepth) {
            biography = text.toString().trim().takeIf { it.isNotEmpty() }
            biographyDepth = -1
        }
    }
}

private class EntityHandler<T>(
    private val entityTag: String,
    private val create: (Attributes) -> T,
    private val onChild: (T, String, Attributes, Int) -> Unit,
    private val onEnd: (T) -> Unit,
    private val stopAfterFirst: Boolean,
) : SubsonicResponseHandler() {
    private var current: T? = null
    private var entityDepth = -1

    var found = false
        private set

    override fun onElementStart(
        name: String,
        attributes: Attributes,
        depth: Int,
    ) {
        val active = current
        if (active == null) {
            if (name == entityTag) {
                current = create(attributes)
                entityDepth = depth
            }
        } else if (depth > entityDepth) {
            onChild(active, name, attributes, depth - entityDepth)
        }
    }

    override fun onElementEnd(
        name: String,
        depth: Int,
    ) {
        val active = current ?: return
        if (name == entityTag && depth == entityDepth) {
            onEnd(active)
            found = true
            current = null
            entityDepth = -1
            if (stopAfterFirst) throw StopReading()
        }
    }
}

private fun parse(
    input: InputStream,
    handler: DefaultHandler,
) {
    try {
        secureParserFactory().newSAXParser().parse(input, handler)
    } catch (failure: XmlFailure) {
        throw SubsonicException(failure.code, failure.message ?: "Unknown error", failure.uiMessage)
    } catch (_: StopReading) {
    } catch (failure: SAXException) {
        throw SubsonicException(-1, failure.message ?: "Malformed response from the server", UiMessage(R.string.error_server_malformed))
    } catch (failure: IOException) {
        throw SubsonicException(-1, failure.message ?: "Truncated response from the server", UiMessage(R.string.error_server_truncated))
    }
}

private fun secureParserFactory(): SAXParserFactory =
    SAXParserFactory.newInstance().apply {
        isNamespaceAware = false
        isValidating = false
        setFeatureQuietly("http://apache.org/xml/features/disallow-doctype-decl", true)
        setFeatureQuietly("http://xml.org/sax/features/external-general-entities", false)
        setFeatureQuietly("http://xml.org/sax/features/external-parameter-entities", false)
        setFeatureQuietly("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    }

private fun SAXParserFactory.setFeatureQuietly(
    name: String,
    enabled: Boolean,
) {
    try {
        setFeature(name, enabled)
    } catch (_: ParserConfigurationException) {
    } catch (_: SAXException) {
    }
}

internal object IsoDate {
    private val timezone = Regex("(Z|[+-]\\d{2}:?\\d{2})$", RegexOption.IGNORE_CASE)
    private val longFraction = Regex("\\.(\\d{9})\\d+")

    fun parse(value: String): Long? {
        val text =
            value
                .trim()
                .let { if (it.endsWith("z")) it.dropLast(1) + "Z" else it }
                .replace(longFraction) { ".${it.groupValues[1]}" }
        return try {
            val zoned = if (timezone.containsMatchIn(text)) text else text + "Z"
            OffsetDateTime.parse(zoned).toEpochSecond()
        } catch (_: DateTimeParseException) {
            try {
                LocalDate.parse(text.removeSuffix("Z")).atStartOfDay(ZoneOffset.UTC).toEpochSecond()
            } catch (_: DateTimeParseException) {
                null
            }
        }
    }
}
