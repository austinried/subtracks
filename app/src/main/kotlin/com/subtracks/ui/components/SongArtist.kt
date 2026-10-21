package com.subtracks.ui.components

import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.Song

private const val ARTIST_SEPARATOR = " \u00B7 "

internal fun Song.artistSubtitle(): String? = artistSubtitle(artist, artists)

internal fun Song.artistNames(): List<String> = artistNames(artist, artists)

internal fun artistNames(
    artist: String?,
    artists: List<ArtistRef>,
): List<String> {
    val names = artists.map { it.name }.filter { it.isNotBlank() }.distinct()
    return names.ifEmpty { listOfNotNull(artist?.takeIf { it.isNotBlank() }) }
}

internal fun artistSubtitle(
    artist: String?,
    artists: List<ArtistRef>,
): String? {
    val names = artistNames(artist, artists)
    if (names.isEmpty()) return null
    val primary = artist?.takeIf { it in names }
    return buildList {
        primary?.let(::add)
        addAll(names.filterNot { it == primary })
    }.joinToString(ARTIST_SEPARATOR)
}
