package com.subtracks.data.sync

import androidx.room3.PooledConnection
import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import androidx.sqlite.SQLiteStatement
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.Album
import com.subtracks.data.model.Artist
import com.subtracks.data.model.Playlist
import com.subtracks.data.model.PlaylistSong
import com.subtracks.data.model.SearchIndex
import com.subtracks.data.model.Song
import com.subtracks.data.source.MusicSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SyncService(
    private val db: SubtracksDatabase,
    private val source: MusicSource,
) {
    private val lock = Mutex()

    suspend fun sync() =
        lock.withLock {
            val artists = source.getArtists()
            val albums = source.getAlbums()
            val songs = source.getSongs()
            val playlists = source.getPlaylists()
            val playlistSongs = source.getPlaylistSongs(playlists)

            db.useWriterConnection { transactor ->
                transactor.immediateTransaction {
                    upsertChanged("artists", artistColumns, primaryKey, artists) { it.values() }
                    upsertChanged("albums", albumColumns, primaryKey, albums) { it.values() }
                    upsertChanged("songs", songColumns, primaryKey, songs) { it.values() }
                    upsertChanged("playlists", playlistColumns, primaryKey, playlists) { it.values() }
                    upsertChanged("playlist_songs", playlistSongColumns, playlistSongKey, playlistSongs) { it.values() }

                    val library = db.libraryDao()
                    val existingArtistIds = library.artistIds(source.id)
                    val existingAlbumIds = library.albumIds(source.id)
                    val existingSongIds = library.songIds(source.id)
                    val existingPlaylistIds = library.playlistIds(source.id)

                    prune(existingArtistIds, artists.map { it.id }) { library.deleteArtists(source.id, it) }
                    prune(existingAlbumIds, albums.map { it.id }) { library.deleteAlbums(source.id, it) }
                    prune(existingSongIds, songs.map { it.id }) { library.deleteSongs(source.id, it) }
                    prune(existingPlaylistIds, playlists.map { it.id }) { library.deletePlaylists(source.id, it) }

                    val playlistSongCounts = playlistSongs.groupingBy { it.playlistId }.eachCount()
                    playlists.forEach { playlist ->
                        library.deletePlaylistSongsFrom(source.id, playlist.id, (playlistSongCounts[playlist.id] ?: 0).toLong())
                    }
                    (existingPlaylistIds - playlists.map { it.id }).forEach { library.deletePlaylistSongsFrom(source.id, it, 0) }

                    val search = db.searchDao()
                    val sourceKey = source.id.toString()
                    search.clear(sourceKey)
                    search.insert(
                        buildList {
                            artists.forEach { add(SearchIndex(sourceId = sourceKey, type = "artist", itemId = it.id, title = it.name)) }
                            albums.forEach { add(SearchIndex(sourceId = sourceKey, type = "album", itemId = it.id, title = it.name)) }
                            songs.forEach { add(SearchIndex(sourceId = sourceKey, type = "song", itemId = it.id, title = it.title)) }
                            playlists.forEach { add(SearchIndex(sourceId = sourceKey, type = "playlist", itemId = it.id, title = it.name)) }
                        },
                    )
                }
            }
        }

    private suspend fun prune(
        existing: List<String>,
        current: List<String>,
        delete: suspend (Collection<String>) -> Unit,
    ) {
        val currentIds = current.toHashSet()
        existing
            .filterNot { it in currentIds }
            .chunked(500)
            .forEach { delete(it) }
    }
}

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
    )

private val playlistColumns = listOf("sourceId", "id", "name", "comment", "coverArt", "songCount", "created")

private val playlistSongColumns = listOf("sourceId", "playlistId", "songId", "position")

private val playlistSongKey = listOf("sourceId", "playlistId", "position")

private fun Artist.values() = listOf<Any?>(sourceId, id, name, albumCount, starred, coverArt)

private fun Album.values() =
    listOf<Any?>(sourceId, id, artistId, name, albumArtist, created, coverArt, genre, year, starred, songCount, frequentRank, recentRank)

private fun Song.values() = listOf<Any?>(sourceId, id, albumId, artistId, title, album, artist, duration, track, disc, starred, genre)

private fun Playlist.values() = listOf<Any?>(sourceId, id, name, comment, coverArt, songCount, created)

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
