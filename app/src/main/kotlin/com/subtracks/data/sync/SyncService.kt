package com.subtracks.data.sync

import androidx.room3.PooledConnection
import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import androidx.sqlite.SQLiteStatement
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Disc
import com.subtracks.data.model.DiscKey
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongGenre
import com.subtracks.data.source.MusicSource
import java.util.Arrays

class SyncService(
    private val db: SubtracksDatabase,
    private val source: MusicSource,
) {
    private var artistsSynced = 0
    private var albumsSynced = 0
    private var songsSynced = 0
    private var playlistsSynced = 0
    private var playlistSongsSynced = 0
    private var pruned = 0
    private var reportedSongs = 0L

    suspend fun sync(): SyncSummary {
        syncArtists()
        syncAlbums()
        syncSongs()
        aggregatePlayData()
        syncPlaylists()
        syncPlaylistSongs()
        return SyncSummary(
            artists = artistsSynced,
            albums = albumsSynced,
            songs = songsSynced,
            playlists = playlistsSynced,
            playlistSongs = playlistSongsSynced,
            pruned = pruned,
            reportedSongs = reportedSongs,
        )
    }

    private suspend fun syncArtists() {
        val library = db.libraryDao()
        val seen = HashedIds()
        source.artists().collect { batch ->
            if (batch.isEmpty()) return@collect
            write("artists", artistColumns, primaryKey, batch) { it.values() }
            batch.forEach { seen.add(idHash(it.id)) }
            artistsSynced += batch.size
        }
        pruned += pruneStaleIds(seen, { library.artistIdsAfter(source.id, it, PRUNE_PAGE) }, { library.deleteArtists(source.id, it) })
    }

    private suspend fun syncAlbums() {
        val library = db.libraryDao()
        val seenAlbums = HashedIds()
        val seenDiscs = HashedIds()
        source.albums().collect { batch ->
            if (batch.isEmpty()) return@collect
            val discs =
                batch.flatMap { album ->
                    album.discTitles.map { (disc, title) -> Disc(album.sourceId, album.id, disc, title) }
                }
            writeAlbumsAndDiscs(batch, discs)
            batch.forEach { seenAlbums.add(idHash(it.id)) }
            discs.forEach { seenDiscs.add(discHash(it.albumId, it.disc)) }
            albumsSynced += batch.size
            reportedSongs += batch.sumOf { it.songCount }
        }
        pruned += pruneStaleIds(seenAlbums, { library.albumIdsAfter(source.id, it, PRUNE_PAGE) }, { library.deleteAlbums(source.id, it) })
        pruned +=
            pruneStaleDiscs(seenDiscs, { albumId, disc -> library.discKeysAfter(source.id, albumId, disc, PRUNE_PAGE) }) {
                deleteStaleDiscs(it)
            }
    }

    private suspend fun syncSongs() {
        val library = db.libraryDao()
        val seen = HashedIds()
        source.songs().collect { batch ->
            if (batch.isEmpty()) return@collect
            writeSongs(batch)
            batch.forEach { seen.add(idHash(it.id)) }
            songsSynced += batch.size
        }
        pruned += pruneStaleIds(seen, { library.songIdsAfter(source.id, it, PRUNE_PAGE) }, { library.deleteSongs(source.id, it) })
    }

    private suspend fun writeSongs(batch: List<Song>) {
        val names = batch.associate { it.id to it.genreNames() }
        val genres =
            batch.flatMap { song ->
                names.getValue(song.id).mapIndexed { index, genre -> SongGenre(song.sourceId, song.id, index.toLong(), genre) }
            }
        db.useWriterConnection { connection ->
            connection.immediateTransaction {
                connection.upsertChanged("songs", songColumns, primaryKey, batch) { it.values() }
                connection.usePrepared("DELETE FROM song_genres WHERE sourceId = ? AND songId = ? AND position >= ?") { statement ->
                    batch.forEach { song ->
                        statement.bind(1, song.sourceId)
                        statement.bind(2, song.id)
                        statement.bind(3, names.getValue(song.id).size.toLong())
                        statement.step()
                        statement.reset()
                        statement.clearBindings()
                    }
                }
                connection.upsertChanged("song_genres", songGenreColumns, songGenreKey, genres) { it.values() }
            }
        }
    }

    private suspend fun aggregatePlayData() {
        db.libraryDao().recomputePlayData(source.id)
    }

    private suspend fun syncPlaylists() {
        val library = db.libraryDao()
        val seen = HashedIds()
        source.playlists().collect { batch ->
            if (batch.isEmpty()) return@collect
            write("playlists", playlistColumns, primaryKey, batch) { it.values() }
            batch.forEach { seen.add(idHash(it.id)) }
            playlistsSynced += batch.size
        }
        pruned += pruneStaleIds(seen, { library.playlistIdsAfter(source.id, it, PRUNE_PAGE) }, { library.deletePlaylists(source.id, it) })
    }

    private suspend fun syncPlaylistSongs() {
        val library = db.libraryDao()
        val playlistIds = library.playlistIds(source.id)
        val counts = HashMap<String, Long>()
        source.playlistSongs(playlistIds).collect { batch ->
            if (batch.isEmpty()) return@collect
            write("playlist_songs", playlistSongColumns, playlistSongKey, batch) { it.values() }
            batch.forEach { counts[it.playlistId] = (counts[it.playlistId] ?: 0L) + 1L }
            playlistSongsSynced += batch.size
        }
        playlistIds.forEach { library.deletePlaylistSongsFrom(source.id, it, counts[it] ?: 0L) }
        library.deleteOrphanPlaylistSongs(source.id)
    }

    // One commit per batch, so Room notifies paged observers once per batch rather than per row.
    private suspend fun <T> write(
        table: String,
        columns: List<String>,
        key: List<String>,
        rows: List<T>,
        values: (T) -> List<Any?>,
    ) {
        db.useWriterConnection { connection ->
            connection.immediateTransaction {
                connection.upsertChanged(table, columns, key, rows, values)
            }
        }
    }

    private suspend fun writeAlbumsAndDiscs(
        albums: List<Album>,
        discs: List<Disc>,
    ) {
        db.useWriterConnection { connection ->
            connection.immediateTransaction {
                connection.upsertChanged("albums", albumColumns, primaryKey, albums) { it.values() }
                connection.upsertChanged("discs", discColumns, discKey, discs) { it.values() }
            }
        }
    }

    private suspend fun deleteStaleDiscs(stale: Collection<DiscKey>) {
        stale.chunked(DELETE_CHUNK).forEach { chunk ->
            db.useWriterConnection { connection ->
                connection.immediateTransaction {
                    connection.usePrepared("DELETE FROM discs WHERE sourceId = ? AND albumId = ? AND disc = ?") { statement ->
                        chunk.forEach { key ->
                            statement.bind(1, source.id)
                            statement.bind(2, key.albumId)
                            statement.bind(3, key.disc)
                            statement.step()
                            statement.reset()
                            statement.clearBindings()
                        }
                    }
                }
            }
        }
    }

    private suspend fun pruneStaleIds(
        seen: HashedIds,
        pageOf: suspend (afterId: String) -> List<String>,
        delete: suspend (Collection<String>) -> Unit,
    ): Int {
        seen.freeze()
        var afterId = ""
        var pruned = 0
        while (true) {
            val ids = pageOf(afterId)
            if (ids.isEmpty()) return pruned
            val stale = ids.filterNot { seen.contains(idHash(it)) }
            stale.chunked(DELETE_CHUNK).forEach { delete(it) }
            pruned += stale.size
            if (ids.size < PRUNE_PAGE) return pruned
            afterId = ids.last()
        }
    }

    private suspend fun pruneStaleDiscs(
        seen: HashedIds,
        pageOf: suspend (afterAlbumId: String, afterDisc: Long) -> List<DiscKey>,
        delete: suspend (Collection<DiscKey>) -> Unit,
    ): Int {
        seen.freeze()
        var afterAlbumId = ""
        var afterDisc = 0L
        var pruned = 0
        while (true) {
            val keys = pageOf(afterAlbumId, afterDisc)
            if (keys.isEmpty()) return pruned
            val stale = keys.filterNot { seen.contains(discHash(it.albumId, it.disc)) }
            stale.chunked(DELETE_CHUNK).forEach { delete(it) }
            pruned += stale.size
            if (keys.size < PRUNE_PAGE) return pruned
            afterAlbumId = keys.last().albumId
            afterDisc = keys.last().disc
        }
    }
}

data class SyncSummary(
    val artists: Int,
    val albums: Int,
    val songs: Int,
    val playlists: Int,
    val playlistSongs: Int,
    val pruned: Int,
    val reportedSongs: Long,
)

private const val DELETE_CHUNK = 500
private const val PRUNE_PAGE = 500
private const val INITIAL_HASH_CAPACITY = 256
private const val FNV_OFFSET = -3750763034362895579L
private const val FNV_PRIME = 1099511628211L

private class HashedIds {
    private var values = LongArray(0)
    private var count = 0

    fun add(value: Long) {
        if (count == values.size) {
            values = values.copyOf(if (values.isEmpty()) INITIAL_HASH_CAPACITY else values.size * 2)
        }
        values[count++] = value
    }

    fun freeze() {
        Arrays.sort(values, 0, count)
    }

    operator fun contains(value: Long): Boolean = Arrays.binarySearch(values, 0, count, value) >= 0
}

private fun idHash(id: String): Long {
    var hash = FNV_OFFSET
    id.forEach { hash = (hash xor it.code.toLong()) * FNV_PRIME }
    return hash
}

private fun discHash(
    albumId: String,
    disc: Long,
): Long = idHash(albumId) * 31 + disc

private val primaryKey = listOf("sourceId", "id")

private val artistColumns = listOf("sourceId", "id", "name", "albumCount", "starred", "coverArt")

private val albumColumns =
    listOf(
        "sourceId",
        "id",
        "artistId",
        "name",
        "albumArtist",
        "created",
        "coverArt",
        "genre",
        "year",
        "starred",
        "songCount",
    )

private val discKey = listOf("sourceId", "albumId", "disc")

private val discColumns = listOf("sourceId", "albumId", "disc", "title")

private val songColumns =
    listOf(
        "sourceId",
        "id",
        "albumId",
        "artistId",
        "title",
        "album",
        "artist",
        "duration",
        "track",
        "disc",
        "starred",
        "created",
        "playCount",
        "played",
    )

private val playlistColumns = listOf("sourceId", "id", "name", "comment", "coverArt", "songCount", "created", "changed", "duration")

private fun Song.genreNames(): List<String> =
    genres
        .flatMap { it.genreParts() }
        .ifEmpty { genre?.genreParts().orEmpty() }
        .map { part -> id3v1Genre(part) ?: part }
        .distinct()

// Navidrome's default genre tag split (resources/mappings.yaml): semicolon, slash, comma.
private fun String.genreParts(): List<String> = split(';', '/', ',').map { it.trim() }.filter { it.isNotEmpty() }

private val songGenreColumns = listOf("sourceId", "songId", "position", "genre")

private val songGenreKey = listOf("sourceId", "songId", "position")

private val playlistSongColumns = listOf("sourceId", "playlistId", "songId", "position")

private val playlistSongKey = listOf("sourceId", "playlistId", "position")

private fun Artist.values() = listOf<Any?>(sourceId, id, name, albumCount, starred, coverArt)

private fun Album.values() = listOf<Any?>(sourceId, id, artistId, name, albumArtist, created, coverArt, genre, year, starred, songCount)

private fun Disc.values() = listOf<Any?>(sourceId, albumId, disc, title)

private fun Song.values() =
    listOf<Any?>(
        sourceId,
        id,
        albumId,
        artistId,
        title,
        album,
        artist,
        duration,
        track,
        disc,
        starred,
        created,
        playCount,
        played,
    )

private fun SongGenre.values() = listOf<Any?>(sourceId, songId, position, genre)

private fun Playlist.values() = listOf<Any?>(sourceId, id, name, comment, coverArt, songCount, created, changed, duration)

private fun PlaylistSong.values() = listOf<Any?>(sourceId, playlistId, songId, position)

private suspend fun <T> PooledConnection.upsertChanged(
    table: String,
    columns: List<String>,
    key: List<String>,
    rows: List<T>,
    values: (T) -> List<Any?>,
) {
    if (rows.isEmpty()) return
    val updates = columns.filterNot { it in key }
    val sql =
        "INSERT INTO $table (${columns.joinToString()}) VALUES (${columns.joinToString { "?" }}) " +
            "ON CONFLICT(${key.joinToString()}) DO UPDATE SET " +
            updates.joinToString { "$it = excluded.$it" } +
            " WHERE " +
            updates.joinToString(" OR ") { "$table.$it COLLATE BINARY IS NOT excluded.$it" }
    usePrepared(sql) { statement ->
        rows.forEach { row ->
            values(row).forEachIndexed { index, value -> statement.bind(index + 1, value) }
            statement.step()
            statement.reset()
            statement.clearBindings()
        }
    }
}

private fun SQLiteStatement.bind(
    index: Int,
    value: Any?,
) {
    when (value) {
        null -> bindNull(index)
        is Long -> bindLong(index, value)
        is Int -> bindLong(index, value.toLong())
        is Boolean -> bindLong(index, if (value) 1L else 0L)
        is Double -> bindDouble(index, value)
        is String -> bindText(index, value)
        else -> error("Cannot bind ${value::class}")
    }
}
