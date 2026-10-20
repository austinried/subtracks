package com.subtracks.ui.components

import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.Song

private const val ARTIST_SEPARATOR = " \u00B7 "

internal fun Song.artistSubtitle(): String? = artistSubtitle(artist, artists)

internal fun artistSubtitle(
    artist: String?,
    artists: List<ArtistRef>,
): String? {
    val names = artists.map { it.name }.filter { it.isNotBlank() }.distinct()
    if (names.isEmpty()) return artist?.takeIf { it.isNotBlank() }
    val primary = artist?.takeIf { it in names }
    return buildList {
        primary?.let(::add)
        addAll(names.filterNot { it == primary })
    }.joinToString(ARTIST_SEPARATOR)
}
