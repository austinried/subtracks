package com.subtracks.data.model

data class CoverArtRef(
    val url: String,
    val cacheKey: String,
)

fun coverArtKey(
    sourceId: Long,
    coverArt: String,
    thumbnail: Boolean,
): String = "$sourceId:$coverArt:$thumbnail"
