package com.subtracks.data.sync

import androidx.room3.PooledConnection
import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import androidx.sqlite.SQLiteStatement
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.AlbumArtistRef
import com.subtracks.data.model.Artist
import com.subtracks.data.model.ArtistRef
import com.subtracks.data.model.ArtistRefsCodec
import com.subtracks.data.model.Disc
import com.subtracks.data.model.DiscKey
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.Song
import com.subtracks.data.model.SongArtistRef
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

    private var creditsChanged = false

    suspend fun sync(): SyncSummary {
        creditsChanged = false
        val artistsBefore = dbTotalChanges()
        syncArtists()
        val artistsChanged = dbTotalChanges() != artistsBefore
        syncAlbums()
        syncSongs()
        optimize()
        if (creditsChanged || artistsChanged) recomputeArtistSongCounts()
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
        }
        val prunedBefore = pruned
        pruned += pruneStaleIds(seenAlbums, { library.albumIdsAfter(source.id, it, PRUNE_PAGE) }, { library.deleteAlbums(source.id, it) })
        if (pruned > prunedBefore) creditsChanged = true
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
        val prunedBefore = pruned
        pruned += pruneStaleIds(seen, { library.songIdsAfter(source.id, it, PRUNE_PAGE) }, { library.deleteSongs(source.id, it) })
        if (pruned > prunedBefore) creditsChanged = true
    }

    private suspend fun writeSongs(batch: List<Song>) {
        val names = batch.associate { it.id to it.genreNames() }
        val genres =
            batch.flatMap { song ->
                names.getValue(song.id).mapIndexed { index, genre -> SongGenre(song.sourceId, song.id, index.toLong(), genre) }
            }
        val artists =
            batch.flatMap { song ->
                song.artistPositions().map { (position, ref) -> SongArtistRef(song.sourceId, song.id, position, ref.id, ref.name) }
            }
        db.useWriterConnection { connection ->
            connection.immediateTransaction {
                if (connection.albumIdsChanged(batch)) creditsChanged = true
                val resolved = connection.resolveSongSortNames(batch)
                connection.upsertChanged("songs", songColumns, primaryKey, resolved) { it.values() }
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
                val artistRowsBefore = connection.totalChanges()
                connection.usePrepared("DELETE FROM song_artists WHERE sourceId = ? AND songId = ? AND position >= ?") { statement ->
                    batch.forEach { song ->
                        statement.bind(1, song.sourceId)
                        statement.bind(2, song.id)
                        statement.bind(3, song.artistPositions().size.toLong())
                        statement.step()
                        statement.reset()
                        statement.clearBindings()
                    }
                }
                connection.upsertChanged("song_artists", songArtistColumns, songArtistKey, artists) { it.values() }
                if (connection.totalChanges() != artistRowsBefore) creditsChanged = true
            }
        }
    }

    private suspend fun aggregatePlayData() {
        db.libraryDao().recomputePlayData(source.id)
    }

    private suspend fun recomputeArtistSongCounts() {
        db.libraryDao().recomputeArtistSongCounts(source.id)
    }

    private suspend fun PooledConnection.totalChanges(): Long {
        var value = 0L
        usePrepared("SELECT total_changes()") { statement ->
            statement.step()
            value = statement.getLong(0)
        }
        return value
    }

    private suspend fun dbTotalChanges(): Long = db.useWriterConnection { it.totalChanges() }

    private suspend fun PooledConnection.albumIdsChanged(batch: List<Song>): Boolean {
        val expected = batch.associate { it.id to it.albumId }.toMutableMap()
        if (expected.isEmpty()) return false
        var changed = false
        batch
            .map { it.sourceId to it.id }
            .chunked(SQL_VARIABLES)
            .forEach { chunk ->
                val placeholders = chunk.joinToString(",") { "?" }
                usePrepared("SELECT id, albumId FROM songs WHERE sourceId = ? AND id IN ($placeholders)") { statement ->
                    statement.bindLong(1, chunk.first().first)
                    chunk.forEachIndexed { index, (_, id) -> statement.bindText(index + 2, id) }
                    while (statement.step()) {
                        val id = statement.getText(0)
                        val albumId = if (statement.isNull(1)) null else statement.getText(1)
                        if (expected.remove(id) != albumId) changed = true
                    }
                }
            }
        return changed || expected.isNotEmpty()
    }

    private suspend fun PooledConnection.resolveSongSortNames(batch: List<Song>): List<Song> {
        if (batch.isEmpty()) return batch
        val sortNames =
            artistSortNames(
                batch.first().sourceId,
                batch
                    .flatMap { listOfNotNull(it.artistId) + it.artistRefs.map { ref -> ref.id } }
                    .filter { it.isNotBlank() }
                    .toSet(),
            )
        return batch.map { song ->
            val primary = song.artistId ?: song.artists.firstOrNull()?.id
            val sortName =
                primary?.let(sortNames::get)?.takeIf { it.isNotBlank() }
                    ?: song.artists.firstNotNullOfOrNull { ref -> sortNames[ref.id]?.takeIf { it.isNotBlank() } }
                    ?: song.artistSort
            if (sortName == song.artistSort) song else song.copy(artistSort = sortName)
        }
    }

    private suspend fun PooledConnection.resolveAlbumSortNames(batch: List<Album>): List<Album> {
        if (batch.isEmpty()) return batch
        val sortNames =
            artistSortNames(
                batch.first().sourceId,
                batch
                    .flatMap { listOfNotNull(it.artistId) + it.artistRefs.map { ref -> ref.id } }
                    .filter { it.isNotBlank() }
                    .toSet(),
            )
        return batch.map { album ->
            val primary = album.artistId ?: album.artists.firstOrNull()?.id
            val sortName =
                primary?.let(sortNames::get)?.takeIf { it.isNotBlank() }
                    ?: album.artists.firstNotNullOfOrNull { ref -> sortNames[ref.id]?.takeIf { it.isNotBlank() } }
                    ?: album.albumArtistSort
            if (sortName == album.albumArtistSort) album else album.copy(albumArtistSort = sortName)
        }
    }

    private suspend fun PooledConnection.artistSortNames(
        sourceId: Long,
        ids: Collection<String>,
    ): Map<String, String> {
        if (ids.isEmpty()) return emptyMap()
        val sortNames = HashMap<String, String>(ids.size)
        ids.toList().chunked(SQL_VARIABLES).forEach { chunk ->
            val placeholders = chunk.joinToString(",") { "?" }
            usePrepared("SELECT id, sortName FROM artists WHERE sourceId = ? AND id IN ($placeholders)") { statement ->
                statement.bindLong(1, sourceId)
                chunk.forEachIndexed { index, id -> statement.bindText(index + 2, id) }
                while (statement.step()) sortNames[statement.getText(0)] = statement.getText(1)
            }
        }
        return sortNames
    }

    // Without sqlite_stat1 the artist and relation queries can fall back to whole-table scans.
    private suspend fun optimize() {
        db.useWriterConnection { connection ->
            connection.usePrepared("PRAGMA optimize") { it.step() }
        }
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
        val artists =
            albums.flatMap { album ->
                album.artistPositions().map { (position, ref) -> AlbumArtistRef(album.sourceId, album.id, position, ref.id, ref.name) }
            }
        db.useWriterConnection { connection ->
            connection.immediateTransaction {
                val resolved = connection.resolveAlbumSortNames(albums)
                connection.upsertChanged("albums", albumColumns, primaryKey, resolved) { it.values() }
                connection.upsertChanged("discs", discColumns, discKey, discs) { it.values() }
                val artistRowsBefore = connection.totalChanges()
                connection.usePrepared("DELETE FROM album_artists WHERE sourceId = ? AND albumId = ? AND position >= ?") { statement ->
                    albums.forEach { album ->
                        statement.bind(1, album.sourceId)
                        statement.bind(2, album.id)
                        statement.bind(3, album.artistPositions().size.toLong())
                        statement.step()
                        statement.reset()
                        statement.clearBindings()
                    }
                }
                connection.upsertChanged("album_artists", albumArtistColumns, albumArtistKey, artists) { it.values() }
                if (connection.totalChanges() != artistRowsBefore) creditsChanged = true
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
)

private const val DELETE_CHUNK = 500
private const val SQL_VARIABLES = 500
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

private val artistColumns = listOf("sourceId", "id", "name", "albumCount", "starred", "coverArt", "sortName")

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
        "artists",
        "sortName",
        "albumArtistSort",
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
        "trackGain",
        "albumGain",
        "trackPeak",
        "albumPeak",
        "artists",
        "sortName",
        "artistSort",
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

private val albumArtistColumns = listOf("sourceId", "albumId", "position", "artistId", "name")

private val albumArtistKey = listOf("sourceId", "albumId", "position")

private val songArtistColumns = listOf("sourceId", "songId", "position", "artistId", "name")

private val songArtistKey = listOf("sourceId", "songId", "position")

private val playlistSongColumns = listOf("sourceId", "playlistId", "songId", "position")

private val playlistSongKey = listOf("sourceId", "playlistId", "position")

private fun Artist.values() = listOf<Any?>(sourceId, id, name, albumCount, starred, coverArt, sortName)

private fun Album.values() =
    listOf<Any?>(
        sourceId,
        id,
        artistId,
        name,
        albumArtist,
        created,
        coverArt,
        genre,
        year,
        starred,
        songCount,
        ArtistRefsCodec.encode(artistRefs),
        sortName,
        albumArtistSort,
    )

private fun AlbumArtistRef.values() = listOf<Any?>(sourceId, albumId, position, artistId, name)

private fun SongArtistRef.values() = listOf<Any?>(sourceId, songId, position, artistId, name)

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
        trackGain,
        albumGain,
        trackPeak,
        albumPeak,
        ArtistRefsCodec.encode(artistRefs),
        sortName,
        artistSort,
    )

private fun List<ArtistRef>.positions(): List<Pair<Long, ArtistRef>> =
    filter { it.id.isNotBlank() }
        .distinctBy { it.id }
        .mapIndexed { index, ref -> index.toLong() to ref }

private fun Album.artistPositions(): List<Pair<Long, ArtistRef>> = artistRefs.positions()

private fun Song.artistPositions(): List<Pair<Long, ArtistRef>> = artistRefs.positions()

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
    check(values(rows.first()).size == columns.size) { "column/value mismatch for $table" }
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
        is Float -> bindDouble(index, value.toDouble())
        is String -> bindText(index, value)
        else -> error("Cannot bind ${value::class}")
    }
}
