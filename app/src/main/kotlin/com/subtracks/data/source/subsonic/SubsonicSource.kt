package com.subtracks.data.source.subsonic

import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.source.MusicSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubsonicSource(
    override val id: Long,
    private val client: SubsonicClient,
    private val maxBitrate: Int = 0,
    private val streamFormat: String? = null,
) : MusicSource {
    private var emptyQuerySearchSupported: Boolean? = null

    override suspend fun ping() {
        withContext(Dispatchers.IO) { client.get("ping") }
    }

    override suspend fun getArtists(): List<Artist> =
        withContext(Dispatchers.IO) {
            SubsonicXml.artists(id, client.get("getArtists"))
        }

    override suspend fun getAlbums(): List<Album> =
        withContext(Dispatchers.IO) {
            val (frequent, recent) = fetchRanks()
            val albums = mutableListOf<Album>()
            var offset = 0
            var pages = 0
            while (pages < MAX_PAGES) {
                val document = client.get("getAlbumList2", page("newest", offset))
                val batch = SubsonicXml.albums(id, document)
                albums +=
                    batch.map {
                        it.copy(frequentRank = frequent[it.id], recentRank = recent[it.id])
                    }
                if (batch.size < PAGE_SIZE) break
                offset += PAGE_SIZE
                pages++
            }
            albums
        }

    override suspend fun getSongs(): List<Song> =
        withContext(Dispatchers.IO) {
            if (supportsEmptyQuerySearch()) searchSongs() else albumSongs()
        }

    override suspend fun getPlaylists(): List<Playlist> =
        withContext(Dispatchers.IO) {
            SubsonicXml.playlists(id, client.get("getPlaylists"))
        }

    override suspend fun getPlaylistSongs(playlists: List<Playlist>): List<PlaylistSong> =
        withContext(Dispatchers.IO) {
            val result = mutableListOf<PlaylistSong>()
            for (playlist in playlists) {
                val document = client.get("getPlaylist", mapOf("id" to playlist.id))
                result += SubsonicXml.playlistSongs(id, playlist.id, document)
            }
            result
        }

    fun streamUri(songId: String) =
        client.uri(
            "stream",
            buildMap {
                put("id", songId)
                val transcodes = maxBitrate > 0 || !streamFormat.isNullOrEmpty()
                if (transcodes) put("estimateContentLength", "true")
                if (maxBitrate > 0) put("maxBitRate", maxBitrate.toString())
                streamFormat?.takeIf { it.isNotEmpty() }?.let { put("format", it) }
            },
        )

    fun downloadUri(songId: String) = client.uri("download", mapOf("id" to songId))

    fun coverArtUri(
        coverArt: String?,
        thumbnail: Boolean = false,
    ) = coverArt?.let {
        client.uri(
            "getCoverArt",
            buildMap {
                put("id", it)
                if (thumbnail) put("size", THUMBNAIL_SIZE.toString())
            },
        )
    }

    private fun fetchRanks(): Pair<Map<String, Long>, Map<String, Long>> = fetchRank("frequent") to fetchRank("recent")

    private fun fetchRank(type: String): Map<String, Long> {
        val ranks = mutableMapOf<String, Long>()
        var offset = 0
        var pages = 0
        while (pages < MAX_PAGES) {
            val batch = SubsonicXml.albums(id, client.get("getAlbumList2", page(type, offset)))
            batch.forEachIndexed { index, album -> ranks[album.id] = (offset + index).toLong() }
            if (batch.size < PAGE_SIZE) break
            offset += PAGE_SIZE
            pages++
        }
        return ranks
    }

    private fun supportsEmptyQuerySearch(): Boolean {
        emptyQuerySearchSupported?.let { return it }
        val supported =
            try {
                val document =
                    client.get(
                        "search3",
                        mapOf("query" to "\"\"", "songCount" to "1", "artistCount" to "0", "albumCount" to "0"),
                    )
                document.getElementsByTagName("song").length > 0
            } catch (_: SubsonicException) {
                false
            }
        emptyQuerySearchSupported = supported
        return supported
    }

    private fun searchSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        var offset = 0
        var pages = 0
        while (pages < MAX_PAGES) {
            val document =
                client.get(
                    "search3",
                    mapOf(
                        "query" to "\"\"",
                        "songCount" to PAGE_SIZE.toString(),
                        "songOffset" to offset.toString(),
                        "artistCount" to "0",
                        "albumCount" to "0",
                    ),
                )
            val batch = SubsonicXml.songs(id, document)
            songs += batch
            if (batch.size < PAGE_SIZE) break
            offset += PAGE_SIZE
            pages++
        }
        return songs
    }

    private fun albumSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        var offset = 0
        var pages = 0
        while (pages < MAX_PAGES) {
            val document = client.get("getAlbumList2", page("alphabeticalByName", offset))
            val albums = SubsonicXml.albums(id, document)
            for (album in albums) {
                val albumDocument = client.get("getAlbum", mapOf("id" to album.id))
                songs += SubsonicXml.songs(id, albumDocument)
            }
            if (albums.size < PAGE_SIZE) break
            offset += PAGE_SIZE
            pages++
        }
        return songs
    }

    private fun page(
        type: String,
        offset: Int,
    ): Map<String, String> =
        mapOf(
            "type" to type,
            "size" to PAGE_SIZE.toString(),
            "offset" to offset.toString(),
        )

    private companion object {
        const val PAGE_SIZE = 500
        const val MAX_PAGES = 1000
        const val THUMBNAIL_SIZE = 256
    }
}
