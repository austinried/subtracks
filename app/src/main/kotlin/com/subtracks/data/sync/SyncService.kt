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
import com.subtracks.data.source.MusicSource

class SyncService(
    private val db: SubtracksDatabase,
    private val source: MusicSource,
) {
    suspend fun sync() {
        syncArtists()
        syncAlbums()
        syncSongs()
        syncPlaylists()
        syncPlaylistSongs()
    }

    private suspend fun syncArtists() {
        val library = db.libraryDao()
        val stale = library.artistIds(source.id).toHashSet()
        source.artists().collect { batch ->
            if (batch.isEmpty()) return@collect
            write("artists", artistColumns, primaryKey, batch) { it.values() }
            stale -= batch.mapTo(HashSet()) { it.id }
        }
        deleteStale(stale) { library.deleteArtists(source.id, it) }
    }

    private suspend fun syncAlbums() {
        val library = db.libraryDao()
        val staleAlbums = library.albumIds(source.id).toHashSet()
        val staleDiscs = library.discKeys(source.id).toHashSet()
        source.albums().collect { batch ->
            if (batch.isEmpty()) return@collect
            val discs =
                batch.flatMap { album ->
                    album.discTitles.map { (disc, title) -> Disc(album.sourceId, album.id, disc, title) }
                }
            writeAlbumsAndDiscs(batch, discs)
            staleAlbums -= batch.mapTo(HashSet()) { it.id }
            staleDiscs -= discs.mapTo(HashSet()) { DiscKey(it.albumId, it.disc) }
        }
        deleteStale(staleAlbums) { library.deleteAlbums(source.id, it) }
        deleteStaleDiscs(staleDiscs)
    }

    private suspend fun syncSongs() {
        val library = db.libraryDao()
        val stale = library.songIds(source.id).toHashSet()
        source.songs().collect { batch ->
            if (batch.isEmpty()) return@collect
            write("songs", songColumns, primaryKey, batch) { it.values() }
            stale -= batch.mapTo(HashSet()) { it.id }
        }
        deleteStale(stale) { library.deleteSongs(source.id, it) }
    }

    private suspend fun syncPlaylists() {
        val library = db.libraryDao()
        val stale = library.playlistIds(source.id).toHashSet()
        source.playlists().collect { batch ->
            if (batch.isEmpty()) return@collect
            write("playlists", playlistColumns, primaryKey, batch) { it.values() }
            stale -= batch.mapTo(HashSet()) { it.id }
        }
        deleteStale(stale) { library.deletePlaylists(source.id, it) }
    }

    private suspend fun syncPlaylistSongs() {
        val library = db.libraryDao()
        val playlistIds = library.playlistIds(source.id)
        val counts = HashMap<String, Long>()
        source.playlistSongs(playlistIds).collect { batch ->
            if (batch.isEmpty()) return@collect
            write("playlist_songs", playlistSongColumns, playlistSongKey, batch) { it.values() }
            batch.forEach { counts[it.playlistId] = (counts[it.playlistId] ?: 0L) + 1L }
        }
        playlistIds.forEach { library.deletePlaylistSongsFrom(source.id, it, counts[it] ?: 0L) }
        library.deleteOrphanPlaylistSongs(source.id)
    }

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

    private suspend fun deleteStaleDiscs(stale: Set<DiscKey>) {
        if (stale.isEmpty()) return
        db.useWriterConnection { connection ->
            connection.immediateTransaction {
                connection.usePrepared("DELETE FROM discs WHERE sourceId = ? AND albumId = ? AND disc = ?") { statement ->
                    stale.forEach { key ->
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

    private suspend fun deleteStale(
        stale: Set<String>,
        delete: suspend (Collection<String>) -> Unit,
    ) {
        stale.chunked(DELETE_CHUNK).forEach { delete(it) }
    }
}

private const val DELETE_CHUNK = 500

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
        "frequentRank",
        "recentRank",
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
        "genre",
        "created",
    )

private val playlistColumns = listOf("sourceId", "id", "name", "comment", "coverArt", "songCount", "created", "changed", "duration")

private val playlistSongColumns = listOf("sourceId", "playlistId", "songId", "position")

private val playlistSongKey = listOf("sourceId", "playlistId", "position")

private fun Artist.values() = listOf<Any?>(sourceId, id, name, albumCount, starred, coverArt)

private fun Album.values() =
    listOf<Any?>(sourceId, id, artistId, name, albumArtist, created, coverArt, genre, year, starred, songCount, frequentRank, recentRank)

private fun Disc.values() = listOf<Any?>(sourceId, albumId, disc, title)

private fun Song.values() =
    listOf<Any?>(sourceId, id, albumId, artistId, title, album, artist, duration, track, disc, starred, genre, created)

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
            updates.joinToString(" OR ") { "$table.$it IS NOT excluded.$it" }
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
