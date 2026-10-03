package com.subtracks.data.source.subsonic

import com.subtracks.R
import com.subtracks.UiMessage
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.source.DEFAULT_FETCH_CONCURRENCY
import com.subtracks.data.source.MusicSource
import com.subtracks.data.source.StarType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.xml.sax.Attributes
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SubsonicSource(
    override val id: Long,
    private val client: SubsonicClient,
    private val maxBitrate: Int = 0,
    private val streamFormat: String? = null,
    private val maxPages: Int = MAX_PAGES,
    private val maxConcurrentFetches: Int = DEFAULT_FETCH_CONCURRENCY,
) : MusicSource {
    private var emptyQuerySearchSupported: Boolean? = null

    init {
        require(maxConcurrentFetches > 0) { "maxConcurrentFetches must be positive" }
    }

    override suspend fun ping() {
        withContext(Dispatchers.IO) { client.check("ping") }
    }

    override suspend fun setStar(
        type: StarType,
        id: String,
        starred: Boolean,
    ) {
        val param =
            when (type) {
                StarType.Song -> "id"
                StarType.Album -> "albumId"
                StarType.Artist -> "artistId"
            }
        withContext(Dispatchers.IO) {
            client.check(if (starred) "star" else "unstar", mapOf(param to id))
        }
    }

    override suspend fun scrobble(
        songId: String,
        submission: Boolean,
        time: Long?,
    ) {
        withContext(Dispatchers.IO) {
            client.check(
                "scrobble",
                buildMap {
                    put("id", songId)
                    put("submission", submission.toString())
                    time?.let { put("time", it.toString()) }
                },
            )
        }
    }

    override fun artists(): Flow<List<Artist>> =
        entityBatches(
            method = "getArtists",
            tag = "artist",
            create = { SubsonicXml.artist(id, it) },
        ).map { batch -> batch.filter { it.id.isNotEmpty() } }

    override fun albums(): Flow<List<Album>> =
        flow {
            var offset = 0
            var pages = 0
            while (true) {
                var raw = 0
                entityBatches(
                    method = "getAlbumList2",
                    params = page("newest", offset),
                    tag = "album",
                    create = { SubsonicXml.albumDraft(id, it) },
                    onChild = { draft, name, attrs -> if (name == "discTitles") draft.addDiscTitle(attrs) },
                ).collect { batch ->
                    raw += batch.size
                    val accepted = batch.filter { it.id.isNotEmpty() }
                    if (accepted.isNotEmpty()) emit(accepted.map { it.toAlbum() })
                }
                if (raw < PAGE_SIZE) {
                    if (raw > 0 && pages >= maxPages) throw pageCapExceeded()
                    break
                }
                pages++
                if (pages > maxPages) throw pageCapExceeded()
                offset += PAGE_SIZE
            }
        }.flowOn(Dispatchers.IO).buffer(1)

    override fun songs(): Flow<List<Song>> =
        flow {
            if (supportsEmptyQuerySearch()) {
                emitAll(searchSongs())
            } else {
                emitAll(albumSongs())
            }
        }.flowOn(Dispatchers.IO).buffer(1)

    override fun playlists(): Flow<List<Playlist>> =
        entityBatches(
            method = "getPlaylists",
            tag = "playlist",
            create = { SubsonicXml.playlist(id, it) },
        ).map { batch -> batch.filter { it.id.isNotEmpty() } }

    override fun playlistSongs(playlistIds: List<String>): Flow<List<PlaylistSong>> =
        channelFlow {
            val permits = Semaphore(maxConcurrentFetches)
            playlistIds.forEach { playlistId ->
                launch {
                    permits.withPermit {
                        var position = 0L
                        entityBatches(
                            method = "getPlaylist",
                            params = mapOf("id" to playlistId),
                            tag = "entry",
                            create = { attrs ->
                                val entryId = attrs.attr("id")
                                SubsonicXml.playlistSong(id, playlistId, if (entryId.isEmpty()) -1L else position++, attrs)
                            },
                        ).collect { batch ->
                            val accepted = batch.filter { it.songId.isNotEmpty() }
                            if (accepted.isNotEmpty()) send(accepted)
                        }
                    }
                }
            }
        }.flowOn(Dispatchers.IO).buffer(1)

    fun streamUri(songId: String) = streamUri(songId, maxBitrate, streamFormat)

    fun streamUri(
        songId: String,
        maxBitrate: Int,
        format: String?,
    ) = client.uri(
        "stream",
        buildMap {
            put("id", songId)
            if (maxBitrate > 0) put("maxBitRate", maxBitrate.toString())
            format?.takeIf { it.isNotEmpty() }?.let { put("format", it) }
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
            while (true) {
                var raw = 0
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
                    create = { SubsonicXml.songDraft(id, it) },
                    onChild = { song, name, attrs -> if (name == "genres") song.addGenre(attrs) },
                ).collect { batch ->
                    raw += batch.size
                    val accepted = batch.map { it.toSong() }.filter { it.id.isNotEmpty() }
                    if (accepted.isNotEmpty()) emit(accepted)
                }
                if (raw < PAGE_SIZE) {
                    if (raw > 0 && pages >= maxPages) throw pageCapExceeded()
                    break
                }
                pages++
                if (pages > maxPages) throw pageCapExceeded()
                offset += PAGE_SIZE
            }
        }

    private fun albumSongs(): Flow<List<Song>> =
        channelFlow {
            val permits = Semaphore(maxConcurrentFetches)
            var offset = 0
            var pages = 0
            while (true) {
                var raw = 0
                val albums = ArrayList<String>(PAGE_SIZE)
                entityBatches(
                    method = "getAlbumList2",
                    params = page("alphabeticalByName", offset),
                    tag = "album",
                    create = { it.attr("id") },
                ).collect { batch ->
                    raw += batch.size
                    batch.forEach { if (it.isNotEmpty()) albums += it }
                }
                coroutineScope {
                    albums
                        .map { albumId ->
                            async {
                                permits.withPermit {
                                    entityBatches(
                                        method = "getAlbum",
                                        params = mapOf("id" to albumId),
                                        tag = "song",
                                        create = { SubsonicXml.songDraft(id, it) },
                                        onChild = { song, name, attrs -> if (name == "genres") song.addGenre(attrs) },
                                    ).collect { batch ->
                                        val accepted = batch.map { it.toSong() }.filter { it.id.isNotEmpty() }
                                        if (accepted.isNotEmpty()) send(accepted)
                                    }
                                }
                            }
                        }.awaitAll()
                }
                if (raw < PAGE_SIZE) {
                    if (raw > 0 && pages >= maxPages) throw pageCapExceeded()
                    break
                }
                pages++
                if (pages > maxPages) throw pageCapExceeded()
                offset += PAGE_SIZE
            }
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
            } catch (_: IOException) {
                false
            }
        emptyQuerySearchSupported = supported
        return supported
    }

    private fun pageCapExceeded(): PageCapExceeded {
        val rows = maxPages.toLong() * PAGE_SIZE
        return PageCapExceeded(
            "Library is too large to sync: it exceeds $rows rows",
            UiMessage(R.string.error_library_too_large, listOf(rows)),
        )
    }

    private fun <T> entityBatches(
        method: String,
        params: Map<String, String> = emptyMap(),
        tag: String,
        create: (Attributes) -> T,
        onChild: (T, String, Attributes) -> Unit = { _, _, _ -> },
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
                        buffer += entity
                        if (buffer.size >= PAGE_SIZE) flush()
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
        const val MAX_PAGES = 20_000
        const val THUMBNAIL_SIZE = 384
    }
}

private class PageCapExceeded(
    message: String,
    uiMessage: UiMessage,
) : SubsonicException(-1, message, uiMessage)
