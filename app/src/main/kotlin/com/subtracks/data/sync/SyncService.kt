package com.subtracks.data.sync

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.SearchIndex
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
                    val library = db.libraryDao()
                    library.upsertArtists(artists)
                    library.upsertAlbums(albums)
                    library.upsertSongs(songs)
                    library.upsertPlaylists(playlists)

                    deleteMissing(library.artistIds(source.id), artists.map { it.id }) { library.deleteArtists(source.id, it) }
                    deleteMissing(library.albumIds(source.id), albums.map { it.id }) { library.deleteAlbums(source.id, it) }
                    deleteMissing(library.songIds(source.id), songs.map { it.id }) { library.deleteSongs(source.id, it) }
                    deleteMissing(library.playlistIds(source.id), playlists.map { it.id }) { library.deletePlaylists(source.id, it) }

                    library.deletePlaylistSongs(source.id)
                    library.upsertPlaylistSongs(playlistSongs)

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

    private suspend fun deleteMissing(
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
