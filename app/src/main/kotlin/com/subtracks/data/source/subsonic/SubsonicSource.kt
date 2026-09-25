package com.subtracks.data.source.subsonic

import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.source.MusicSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.xml.sax.Attributes

@OptIn(ExperimentalCoroutinesApi::class)
class SubsonicSource(
    override val id: Long,
    private val client: SubsonicClient,
    private val maxBitrate: Int = 0,
    private val streamFormat: String? = null,
) : MusicSource {
    private var emptyQuerySearchSupported: Boolean? = null

    override suspend fun ping() {
        withContext(Dispatchers.IO) { client.check("ping") }
    }

    override fun artists(): Flow<List<Artist>> =
        entityBatches(
            method = "getArtists",
            tag = "artist",
            create = { SubsonicXml.artist(id, it) },
            accept = { it.id.isNotEmpty() },
        )

    override fun albums(): Flow<List<Album>> =
        flow {
            val (frequent, recent) = fetchRanks()
            var offset = 0
            var pages = 0
            while (pages < MAX_PAGES) {
                var count = 0
                entityBatches(
                    method = "getAlbumList2",
                    params = page("newest", offset),
                    tag = "album",
                    create = { SubsonicXml.albumDraft(id, it) },
                    onChild = { draft, name, attrs -> if (name == "discTitles") draft.addDiscTitle(attrs) },
                    accept = { it.id.isNotEmpty() },
                ).collect { batch ->
                    count += batch.size
                    emit(
                        batch.map {
                            it.toAlbum().copy(
                                frequentRank = frequent[it.id],
                                recentRank = recent[it.id],
                            )
                        },
                    )
                }
                if (count < PAGE_SIZE) break
                offset += PAGE_SIZE
                pages++
            }
        }.flowOn(Dispatchers.IO)

    override fun songs(): Flow<List<Song>> =
        flow {
            if (supportsEmptyQuerySearch()) {
                emitAll(searchSongs())
            } else {
                emitAll(albumSongs())
            }
        }.flowOn(Dispatchers.IO)

    override fun playlists(): Flow<List<Playlist>> =
        entityBatches(
            method = "getPlaylists",
            tag = "playlist",
            create = { SubsonicXml.playlist(id, it) },
            accept = { it.id.isNotEmpty() },
        )

    override fun playlistSongs(playlistIds: List<String>): Flow<List<PlaylistSong>> =
        flow {
            for (playlistId in playlistIds) {
                var position = 0L
                entityBatches(
                    method = "getPlaylist",
                    params = mapOf("id" to playlistId),
                    tag = "entry",
                    create = { attrs -> SubsonicXml.playlistSong(id, playlistId, position++, attrs) },
                ).collect { emit(it) }
            }
        }.flowOn(Dispatchers.IO)

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

    private fun searchSongs(): Flow<List<Song>> =
        flow {
            var offset = 0
            var pages = 0
            while (pages < MAX_PAGES) {
                var count = 0
                entityBatches(
                    method = "search3",
                    params =
                        mapOf(
                            "query" to "\"\"",
                            "songCount" to PAGE_SIZE.toString(),
                            "songOffset" to offset.toString(),
                            "artistCount" to "0",
                            "albumCount" to "0",
                        ),
                    tag = "song",
                    create = { SubsonicXml.song(id, it) },
                    accept = { it.id.isNotEmpty() },
                ).collect { batch ->
                    count += batch.size
                    emit(batch)
                }
                if (count < PAGE_SIZE) break
                offset += PAGE_SIZE
                pages++
            }
        }

    private fun albumSongs(): Flow<List<Song>> =
        flow {
            var offset = 0
            var pages = 0
            while (pages < MAX_PAGES) {
                val albums = ArrayList<String>(PAGE_SIZE)
                entityBatches(
                    method = "getAlbumList2",
                    params = page("alphabeticalByName", offset),
                    tag = "album",
                    create = { it.attr("id") },
                    accept = { it.isNotEmpty() },
                ).collect { albums += it }
                for (albumId in albums) {
                    entityBatches(
                        method = "getAlbum",
                        params = mapOf("id" to albumId),
                        tag = "song",
                        create = { SubsonicXml.song(id, it) },
                        accept = { it.id.isNotEmpty() },
                    ).collect { emit(it) }
                }
                if (albums.size < PAGE_SIZE) break
                offset += PAGE_SIZE
                pages++
            }
        }

    private suspend fun fetchRanks(): Pair<Map<String, Long>, Map<String, Long>> = fetchRank("frequent") to fetchRank("recent")

    private suspend fun fetchRank(type: String): Map<String, Long> {
        val ranks = HashMap<String, Long>()
        var offset = 0
        var pages = 0
        while (pages < MAX_PAGES) {
            var index = 0
            entityBatches(
                method = "getAlbumList2",
                params = page(type, offset),
                tag = "album",
                create = { it.attr("id") },
                accept = { it.isNotEmpty() },
            ).collect { batch ->
                batch.forEach { albumId ->
                    ranks[albumId] = (offset + index).toLong()
                    index++
                }
            }
            if (index < PAGE_SIZE) break
            offset += PAGE_SIZE
            pages++
        }
        return ranks
    }

    private fun supportsEmptyQuerySearch(): Boolean {
        emptyQuerySearchSupported?.let { return it }
        val supported =
            try {
                client.stream(
                    "search3",
                    mapOf("query" to "\"\"", "songCount" to "1", "artistCount" to "0", "albumCount" to "0"),
                ) { input -> SubsonicXml.containsEntity(input, "song") }
            } catch (_: SubsonicException) {
                false
            }
        emptyQuerySearchSupported = supported
        return supported
    }

    private fun <T> entityBatches(
        method: String,
        params: Map<String, String> = emptyMap(),
        tag: String,
        create: (Attributes) -> T,
        onChild: (T, String, Attributes) -> Unit = { _, _, _ -> },
        accept: (T) -> Boolean = { true },
    ): Flow<List<T>> =
        channelFlow {
            val buffer = ArrayList<T>(PAGE_SIZE)

            fun flush() {
                if (buffer.isEmpty()) return
                if (trySendBlocking(buffer.toList()).isFailure) {
                    throw CancellationException("Library sync cancelled")
                }
                buffer.clear()
            }

            client.stream(method, params) { input ->
                SubsonicXml.readEntities(
                    input = input,
                    entityTag = tag,
                    create = create,
                    onChild = onChild,
                    onEnd = { entity ->
                        if (accept(entity)) {
                            buffer += entity
                            if (buffer.size >= PAGE_SIZE) flush()
                        }
                    },
                )
            }
            flush()
        }.flowOn(Dispatchers.IO).buffer(1)

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
