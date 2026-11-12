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
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.QueueOrigin
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
    private val committedOrigins = mutableSetOf<QueueOrigin>()
    private var transactionOrigins: MutableSet<QueueOrigin>? = null
    private val changedOrigins =
        object : AbstractMutableSet<QueueOrigin>() {
            override val size: Int get() = committedOrigins.size

            override fun iterator(): MutableIterator<QueueOrigin> = committedOrigins.iterator()

            override fun add(element: QueueOrigin): Boolean = (transactionOrigins ?: committedOrigins).add(element)
        }

    private suspend fun <T> commitOrigins(block: suspend () -> T): T {
        check(transactionOrigins == null)
        val pending = mutableSetOf<QueueOrigin>()
        transactionOrigins = pending
        return try {
            block().also { committedOrigins.addAll(pending) }
        } finally {
            transactionOrigins = null
        }
    }

    suspend fun sync(): SyncSummary {
        val sources = db.sourcesDao()
        val wasStale = sources.creditsStale(source.id) ?: true
        val artistsChanged = syncArtists()
        val albumsChanged = syncAlbums()
        val songsChanged = syncSongs()
        optimize()
        if (artistsChanged || albumsChanged || songsChanged || wasStale) {
            recomputeAggregates()
            sources.setCreditsStale(source.id, false)
        }
        syncPlaylists()
        syncPlaylistSongs()
        return SyncSummary(
            artists = artistsSynced,
            albums = albumsSynced,
            songs = songsSynced,
            playlists = playlistsSynced,
            playlistSongs = playlistSongsSynced,
            pruned = pruned,
            changedOrigins = changedOrigins.toSet(),
        )
    }

    private suspend fun syncArtists(): Boolean {
        val library = db.libraryDao()
        val seen = HashedIds()
        var changed = false
        source.artists().collect { batch ->
            if (batch.isEmpty()) return@collect
            changed =
                write(
                    "artists",
                    primaryKey,
                    batch,
                    preserved = setOf("albumCount"),
                    markStale = true,
                    onChanged = { changedOrigins += origin(QueueKind.Artist, it.id) },
                ) { it.columns() } ||
                changed
            batch.forEach { seen.add(idHash(it.id)) }
            artistsSynced += batch.size
        }
        val prunedBefore = pruned
        pruned +=
            pruneStaleIds(seen, { library.artistIdsAfter(source.id, it, PRUNE_PAGE) }, {
                it.forEach { id -> changedOrigins += origin(QueueKind.Artist, id) }
                deleteSyncedIds("artists", it)
            })
        return changed || pruned > prunedBefore
    }

    private suspend fun syncAlbums(): Boolean {
        val library = db.libraryDao()
        val seenAlbums = HashedIds()
        val seenDiscs = HashedIds()
        var changed = false
        source.albums().collect { batch ->
            if (batch.isEmpty()) return@collect
            val discs =
                batch.flatMap { album ->
                    album.discTitles.map { (disc, title) -> Disc(album.sourceId, album.id, disc, title) }
                }
            changed = writeAlbumsAndDiscs(batch, discs) || changed
            batch.forEach { seenAlbums.add(idHash(it.id)) }
            discs.forEach { seenDiscs.add(discHash(it.albumId, it.disc)) }
            albumsSynced += batch.size
        }
        val prunedBefore = pruned
        pruned +=
            pruneStaleIds(
                seenAlbums,
                { library.albumIdsAfter(source.id, it, PRUNE_PAGE) },
                {
                    it.forEach { id -> changedOrigins += origin(QueueKind.Album, id) }
                    db.useWriterConnection { connection ->
                        connection.immediateTransaction {
                            val artistIds = connection.albumArtistIds(it.toSet()).values.flatten()
                            val trackArtistIds = connection.songArtistIds(it.toSet()).values.flatten()
                            changedOrigins += (artistIds + trackArtistIds).map { id -> origin(QueueKind.Artist, id) }
                            connection.deleteIds("albums", it)
                            connection.persistPendingOrigins()
                        }
                    }
                },
                markStale = { db.sourcesDao().setCreditsStale(source.id, true) },
            )
        pruned +=
            pruneStaleDiscs(seenDiscs, { albumId, disc -> library.discKeysAfter(source.id, albumId, disc, PRUNE_PAGE) }) {
                deleteStaleDiscs(it)
            }
        return changed || pruned > prunedBefore
    }

    private suspend fun syncSongs(): Boolean {
        val library = db.libraryDao()
        val seen = HashedIds()
        var changed = false
        source.songs().collect { batch ->
            if (batch.isEmpty()) return@collect
            changed = writeSongs(batch) || changed
            batch.forEach { seen.add(idHash(it.id)) }
            songsSynced += batch.size
        }
        val prunedBefore = pruned
        pruned +=
            pruneStaleIds(
                seen,
                { library.songIdsAfter(source.id, it, PRUNE_PAGE) },
                {
                    deleteSongsAndRecordOrigins(it)
                },
                markStale = { db.sourcesDao().setCreditsStale(source.id, true) },
            )
        return changed || pruned > prunedBefore
    }

    private suspend fun writeSongs(batch: List<Song>): Boolean {
        val names = batch.associate { it.id to it.genreNames() }
        val genres =
            batch.flatMap { song ->
                names.getValue(song.id).mapIndexed { index, genre -> SongGenre(song.sourceId, song.id, index.toLong(), genre) }
            }
        val artists =
            batch.flatMap { song ->
                song.artistPositions().map { (position, ref) -> SongArtistRef(song.sourceId, song.id, position, ref.id, ref.name) }
            }
        return commitOrigins {
            db.useWriterConnection { connection ->
                connection.immediateTransaction {
                    val before = connection.totalChanges()
                    val previousStates = connection.storedSongSyncStates(batch.map { it.id })
                    val albumArtists = connection.albumArtistIds(batch.mapNotNull { it.albumId }.toSet())
                    val resolved = connection.resolveSongSortNames(batch)
                    val nextStates = resolved.associate { it.id to it.syncState(albumArtists) }
                    val downloadedIds = connection.completedDownloadIds(batch.map { it.id })
                    val changedSongIds = mutableSetOf<String>()
                    connection.upsertChanged("songs", primaryKey, resolved, onChanged = { changedSongIds += it.id }) { it.columns() }
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
                    connection.upsertChanged("song_genres", songGenreKey, genres) { it.columns() }
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
                    connection.upsertChanged("song_artists", songArtistKey, artists) { it.columns() }
                    batch.forEach { song ->
                        val previous = previousStates[song.id]
                        val next = nextStates.getValue(song.id)
                        if (song.id in changedSongIds || previous != next) {
                            recordSongOrigins(previous, next)
                        }
                        if (song.id in downloadedIds && (previous == null || previous.sortName != next.sortName)) {
                            changedOrigins += origin(QueueKind.Downloaded, "")
                        }
                    }
                    val changed = connection.totalChanges() != before
                    if (changed) connection.markCreditsStale(batch.first().sourceId)
                    connection.persistPendingOrigins()
                    changed
                }
            }
        }
    }

    private suspend fun deleteSongsAndRecordOrigins(ids: Collection<String>) {
        ids.forEach { changedOrigins += origin(QueueKind.Song, it) }
        db.useWriterConnection { connection ->
            connection.immediateTransaction {
                connection.storedSongSyncStates(ids.toList()).values.forEach { recordSongOrigins(it, null) }
                if (connection.completedDownloadIds(ids.toList()).isNotEmpty()) changedOrigins += origin(QueueKind.Downloaded, "")
                connection.deleteIds("songs", ids)
                connection.persistPendingOrigins()
            }
        }
    }

    private suspend fun PooledConnection.recordSongOrigins(
        previous: SongSyncState?,
        next: SongSyncState?,
    ) {
        if ((previous == null) != (next == null)) {
            val songId = (previous ?: next)!!.id
            usePrepared("SELECT playlistId FROM playlist_songs WHERE sourceId = ? AND songId = ?") { statement ->
                statement.bindLong(1, source.id)
                statement.bindText(2, songId)
                while (statement.step()) changedOrigins += origin(QueueKind.Playlist, statement.getText(0))
            }
        }
        val states = listOfNotNull(previous, next)
        states.mapTo(changedOrigins) { origin(QueueKind.Song, it.id) }
        states.mapNotNullTo(changedOrigins) { it.albumId?.let { id -> origin(QueueKind.Album, id) } }
        states.flatMapTo(changedOrigins) { state -> state.artistIds.map { origin(QueueKind.Artist, it) } }
        states.flatMapTo(changedOrigins) { state -> state.genres.map { origin(QueueKind.Genre, it) } }
        if (states.any { it.starred }) changedOrigins += origin(QueueKind.Starred, "")
    }

    private suspend fun PooledConnection.storedSongSyncStates(ids: List<String>): Map<String, SongSyncState> {
        val states = mutableMapOf<String, MutableSongSyncState>()
        readSourceIds(
            ids,
            { "SELECT id, albumId, artistId, sortName, starred FROM songs WHERE sourceId = ? AND id IN ($it)" },
        ) { statement ->
            val state = MutableSongSyncState()
            state.albumId = if (statement.isNull(1)) null else statement.getText(1)
            if (!statement.isNull(2)) state.artistIds += statement.getText(2)
            state.sortName = statement.getText(3)
            state.starred = !statement.isNull(4)
            states[statement.getText(0)] = state
        }
        readSourceIds(ids, { "SELECT songId, artistId FROM song_artists WHERE sourceId = ? AND songId IN ($it)" }) { statement ->
            states[statement.getText(0)]?.artistIds?.plusAssign(statement.getText(1))
        }
        readSourceIds(ids, { "SELECT songId, genre FROM song_genres WHERE sourceId = ? AND songId IN ($it)" }) { statement ->
            states[statement.getText(0)]?.genres?.plusAssign(statement.getText(1))
        }
        val albumArtists = albumArtistIds(states.values.mapNotNull { it.albumId }.toSet())
        return states.mapValues { (id, state) ->
            SongSyncState(
                id = id,
                albumId = state.albumId,
                artistIds = state.artistIds + state.albumId?.let { albumArtists[it].orEmpty() }.orEmpty(),
                genres = state.genres,
                starred = state.starred,
                sortName = state.sortName,
            )
        }
    }

    private suspend fun PooledConnection.completedDownloadIds(ids: List<String>): Set<String> {
        val result = mutableSetOf<String>()
        readSourceIds(
            ids,
            { "SELECT songId FROM song_downloads WHERE sourceId = ? AND status = 'Completed' AND songId IN ($it)" },
        ) { statement ->
            result += statement.getText(0)
        }
        return result
    }

    private suspend fun PooledConnection.albumArtistIds(albumIds: Set<String>): Map<String, Set<String>> {
        val artists = mutableMapOf<String, MutableSet<String>>()
        readSourceIds(albumIds, { "SELECT id, artistId FROM albums WHERE sourceId = ? AND artistId IS NOT NULL AND id IN ($it)" }) {
            artists.getOrPut(it.getText(0)) { mutableSetOf() } += it.getText(1)
        }
        readSourceIds(albumIds, { "SELECT albumId, artistId FROM album_artists WHERE sourceId = ? AND albumId IN ($it)" }) {
            artists.getOrPut(it.getText(0)) { mutableSetOf() } += it.getText(1)
        }
        return artists
    }

    private suspend fun PooledConnection.songArtistIds(albumIds: Set<String>): Map<String, Set<String>> {
        val result = mutableMapOf<String, MutableSet<String>>()
        readSourceIds(albumIds, {
            "SELECT albumId, artistId FROM songs WHERE sourceId = ? AND artistId IS NOT NULL AND albumId IN ($it)"
        }) { statement ->
            result.getOrPut(statement.getText(0)) { mutableSetOf() } += statement.getText(1)
        }
        albumIds.chunked(SQL_VARIABLES).forEach { chunk ->
            val placeholders = chunk.joinToString(",") { "?" }
            usePrepared(
                "SELECT songs.albumId, song_artists.artistId FROM song_artists " +
                    "JOIN songs ON songs.sourceId = song_artists.sourceId AND songs.id = song_artists.songId " +
                    "WHERE song_artists.sourceId = ? AND songs.albumId IN ($placeholders)",
            ) { statement ->
                statement.bindLong(1, source.id)
                chunk.forEachIndexed { index, id -> statement.bindText(index + 2, id) }
                while (statement.step()) result.getOrPut(statement.getText(0)) { mutableSetOf() } += statement.getText(1)
            }
        }
        return result
    }

    private suspend fun PooledConnection.readSourceIds(
        ids: Collection<String>,
        sql: (placeholders: String) -> String,
        row: (SQLiteStatement) -> Unit,
    ) {
        ids.chunked(SQL_VARIABLES).forEach { chunk ->
            usePrepared(sql(chunk.joinToString(",") { "?" })) { statement ->
                statement.bindLong(1, source.id)
                chunk.forEachIndexed { index, id -> statement.bindText(index + 2, id) }
                while (statement.step()) row(statement)
            }
        }
    }

    private fun origin(
        kind: QueueKind,
        refId: String,
    ) = QueueOrigin(kind, source.id, refId)

    private suspend fun PooledConnection.persistPendingOrigins() {
        val origins = transactionOrigins.orEmpty()
        origins.chunked(SQL_VARIABLES).forEach { chunk ->
            usePrepared("INSERT OR IGNORE INTO pending_sync_origins (sourceId, kind, refId) VALUES (?, ?, ?)") { statement ->
                chunk.forEach { origin ->
                    statement.bindLong(1, origin.sourceId)
                    statement.bindText(2, origin.kind.name)
                    statement.bindText(3, origin.refId)
                    statement.step()
                    statement.reset()
                    statement.clearBindings()
                }
            }
        }
    }

    private suspend fun PooledConnection.deleteIds(
        table: String,
        ids: Collection<String>,
    ) {
        ids.chunked(SQL_VARIABLES).forEach { chunk ->
            val placeholders = chunk.joinToString(",") { "?" }
            usePrepared("DELETE FROM $table WHERE sourceId = ? AND id IN ($placeholders)") { statement ->
                statement.bindLong(1, source.id)
                chunk.forEachIndexed { index, id -> statement.bindText(index + 2, id) }
                statement.step()
            }
        }
    }

    private suspend fun deleteSyncedIds(
        table: String,
        ids: Collection<String>,
    ) {
        db.useWriterConnection { connection ->
            connection.immediateTransaction {
                connection.deleteIds(table, ids)
                connection.persistPendingOrigins()
            }
        }
    }

    private suspend fun recomputeAggregates() {
        db.libraryDao().recomputeAggregates(source.id)
    }

    private suspend fun PooledConnection.totalChanges(): Long {
        var value = 0L
        usePrepared("SELECT total_changes()") { statement ->
            statement.step()
            value = statement.getLong(0)
        }
        return value
    }

    private suspend fun PooledConnection.changes(): Long {
        var value = 0L
        usePrepared("SELECT changes()") { statement ->
            statement.step()
            value = statement.getLong(0)
        }
        return value
    }

    private suspend fun PooledConnection.markCreditsStale(sourceId: Long) {
        usePrepared("UPDATE sources SET creditsStale = 1 WHERE id = ? AND creditsStale = 0") { statement ->
            statement.bindLong(1, sourceId)
            statement.step()
        }
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
            write("playlists", primaryKey, batch, onChanged = { changedOrigins += origin(QueueKind.Playlist, it.id) }) { it.columns() }
            batch.forEach { seen.add(idHash(it.id)) }
            playlistsSynced += batch.size
        }
        pruned +=
            pruneStaleIds(seen, { library.playlistIdsAfter(source.id, it, PRUNE_PAGE) }, {
                it.forEach { id -> changedOrigins += origin(QueueKind.Playlist, id) }
                deleteSyncedIds("playlists", it)
            })
    }

    private suspend fun syncPlaylistSongs() {
        val library = db.libraryDao()
        val playlistIds = library.playlistIds(source.id)
        val counts = HashMap<String, Long>()
        source.playlistSongs(playlistIds).collect { batch ->
            if (batch.isEmpty()) return@collect
            write("playlist_songs", playlistSongKey, batch, onChanged = { changedOrigins += origin(QueueKind.Playlist, it.playlistId) }) {
                it.columns()
            }
            batch.forEach { counts[it.playlistId] = (counts[it.playlistId] ?: 0L) + 1L }
            playlistSongsSynced += batch.size
        }
        playlistIds.forEach { playlistId ->
            deletePlaylistSongsFrom(playlistId, counts[playlistId] ?: 0L)
        }
        library.deleteOrphanPlaylistSongs(source.id)
    }

    private suspend fun deletePlaylistSongsFrom(
        playlistId: String,
        position: Long,
    ): Boolean =
        commitOrigins {
            db.useWriterConnection { connection ->
                connection.immediateTransaction {
                    connection.usePrepared(
                        "DELETE FROM playlist_songs WHERE sourceId = ? AND playlistId = ? AND position >= ?",
                    ) { statement ->
                        statement.bindLong(1, source.id)
                        statement.bindText(2, playlistId)
                        statement.bindLong(3, position)
                        statement.step()
                    }
                    val changed = connection.changes() > 0
                    if (changed) {
                        changedOrigins += origin(QueueKind.Playlist, playlistId)
                        connection.persistPendingOrigins()
                    }
                    changed
                }
            }
        }

    // One commit per batch, so Room notifies paged observers once per batch rather than per row.
    private suspend fun <T> write(
        table: String,
        key: List<String>,
        rows: List<T>,
        preserved: Set<String> = emptySet(),
        markStale: Boolean = false,
        onChanged: (T) -> Unit = {},
        columns: (T) -> List<Pair<String, Any?>>,
    ): Boolean =
        commitOrigins {
            db.useWriterConnection { connection ->
                connection.immediateTransaction {
                    val before = connection.totalChanges()
                    connection.upsertChanged(table, key, rows, preserved, onChanged, columns)
                    val changed = connection.totalChanges() != before
                    if (changed && markStale) connection.markCreditsStale(source.id)
                    connection.persistPendingOrigins()
                    changed
                }
            }
        }

    private suspend fun writeAlbumsAndDiscs(
        albums: List<Album>,
        discs: List<Disc>,
    ): Boolean {
        val artists =
            albums.flatMap { album ->
                album.artistPositions().map { (position, ref) -> AlbumArtistRef(album.sourceId, album.id, position, ref.id, ref.name) }
            }
        return commitOrigins {
            db.useWriterConnection { connection ->
                connection.immediateTransaction {
                    val before = connection.totalChanges()
                    val previousArtists = connection.albumArtistIds(albums.map { it.id }.toSet())
                    val previousTrackArtists = connection.songArtistIds(albums.map { it.id }.toSet())
                    val changedAlbums = mutableSetOf<String>()
                    val resolved = connection.resolveAlbumSortNames(albums)
                    connection.upsertChanged(
                        "albums",
                        primaryKey,
                        resolved,
                        onChanged = {
                            changedAlbums += it.id
                            changedOrigins += origin(QueueKind.Album, it.id)
                        },
                    ) { it.columns() }
                    connection.upsertChanged(
                        "discs",
                        discKey,
                        discs,
                        onChanged = { changedOrigins += origin(QueueKind.Album, it.albumId) },
                    ) { it.columns() }
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
                    connection.upsertChanged("album_artists", albumArtistKey, artists) { it.columns() }
                    albums.forEach { album ->
                        val previous = previousArtists[album.id].orEmpty()
                        val current = (album.artistPositions().map { it.second.id } + album.artistId).filterNotNull().toSet()
                        if (album.id in changedAlbums || previous != current) {
                            changedOrigins += (previous + current).map { origin(QueueKind.Artist, it) }
                        }
                    }
                    changedAlbums.forEach { albumId ->
                        changedOrigins += previousTrackArtists[albumId].orEmpty().map { origin(QueueKind.Artist, it) }
                    }
                    val changed = connection.totalChanges() != before
                    if (changed) connection.markCreditsStale(albums.first().sourceId)
                    connection.persistPendingOrigins()
                    changed
                }
            }
        }
    }

    private suspend fun deleteStaleDiscs(stale: Collection<DiscKey>) {
        stale.chunked(DELETE_CHUNK).forEach { chunk ->
            commitOrigins {
                chunk.forEach { changedOrigins += origin(QueueKind.Album, it.albumId) }
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
                        connection.persistPendingOrigins()
                    }
                }
            }
        }
    }

    private suspend fun pruneStaleIds(
        seen: HashedIds,
        pageOf: suspend (afterId: String) -> List<String>,
        delete: suspend (Collection<String>) -> Unit,
        markStale: (suspend () -> Unit)? = null,
    ): Int {
        seen.freeze()
        var afterId = ""
        var pruned = 0
        while (true) {
            val ids = pageOf(afterId)
            if (ids.isEmpty()) return pruned
            val stale = ids.filterNot { seen.contains(idHash(it)) }
            if (stale.isNotEmpty()) markStale?.invoke()
            stale.chunked(DELETE_CHUNK).forEach { commitOrigins { delete(it) } }
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
    val changedOrigins: Set<QueueOrigin> = emptySet(),
)

private data class SongSyncState(
    val id: String,
    val albumId: String?,
    val artistIds: Set<String>,
    val genres: Set<String>,
    val starred: Boolean,
    val sortName: String,
)

private class MutableSongSyncState {
    var albumId: String? = null
    var sortName: String = ""
    val artistIds = mutableSetOf<String>()
    val genres = mutableSetOf<String>()
    var starred = false
}

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

private val discKey = listOf("sourceId", "albumId", "disc")

private fun Song.genreNames(): List<String> =
    genres
        .flatMap { it.genreParts() }
        .ifEmpty { genre?.genreParts().orEmpty() }
        .map { part -> id3v1Genre(part) ?: part }
        .distinct()

private fun Song.syncState(albumArtists: Map<String, Set<String>>): SongSyncState {
    val artistIds = artistPositions().mapTo(mutableSetOf()) { it.second.id }
    this.artistId?.let(artistIds::add)
    albumId?.let { artistIds += albumArtists[it].orEmpty() }
    return SongSyncState(
        id = id,
        albumId = albumId,
        artistIds = artistIds,
        genres = genreNames().toSet(),
        starred = starred != null,
        sortName = sortName,
    )
}

// Navidrome's default genre tag split (resources/mappings.yaml): semicolon, slash, comma.
private fun String.genreParts(): List<String> = split(';', '/', ',').map { it.trim() }.filter { it.isNotEmpty() }

private val songGenreKey = listOf("sourceId", "songId", "position")

private val albumArtistKey = listOf("sourceId", "albumId", "position")

private val songArtistKey = listOf("sourceId", "songId", "position")

private val playlistSongKey = listOf("sourceId", "playlistId", "position")

private fun Artist.columns(): List<Pair<String, Any?>> =
    listOf(
        "sourceId" to sourceId,
        "id" to id,
        "name" to name,
        "albumCount" to albumCount,
        "starred" to starred,
        "coverArt" to coverArt,
        "artistImageUrl" to artistImageUrl,
        "sortName" to sortName,
    )

private fun Album.columns(): List<Pair<String, Any?>> =
    listOf(
        "sourceId" to sourceId,
        "id" to id,
        "artistId" to artistId,
        "name" to name,
        "albumArtist" to albumArtist,
        "created" to created,
        "coverArt" to coverArt,
        "genre" to genre,
        "year" to year,
        "starred" to starred,
        "songCount" to songCount,
        "artists" to ArtistRefsCodec.encode(artistRefs),
        "sortName" to sortName,
        "albumArtistSort" to albumArtistSort,
    )

private fun AlbumArtistRef.columns(): List<Pair<String, Any?>> =
    listOf(
        "sourceId" to sourceId,
        "albumId" to albumId,
        "position" to position,
        "artistId" to artistId,
        "name" to name,
    )

private fun SongArtistRef.columns(): List<Pair<String, Any?>> =
    listOf(
        "sourceId" to sourceId,
        "songId" to songId,
        "position" to position,
        "artistId" to artistId,
        "name" to name,
    )

private fun Disc.columns(): List<Pair<String, Any?>> =
    listOf(
        "sourceId" to sourceId,
        "albumId" to albumId,
        "disc" to disc,
        "title" to title,
    )

private fun Song.columns(): List<Pair<String, Any?>> =
    listOf(
        "sourceId" to sourceId,
        "id" to id,
        "albumId" to albumId,
        "artistId" to artistId,
        "title" to title,
        "album" to album,
        "artist" to artist,
        "duration" to duration,
        "track" to track,
        "disc" to disc,
        "starred" to starred,
        "created" to created,
        "playCount" to playCount,
        "played" to played,
        "trackGain" to trackGain,
        "albumGain" to albumGain,
        "trackPeak" to trackPeak,
        "albumPeak" to albumPeak,
        "artists" to ArtistRefsCodec.encode(artistRefs),
        "sortName" to sortName,
        "artistSort" to artistSort,
    )

private fun List<ArtistRef>.positions(): List<Pair<Long, ArtistRef>> =
    filter { it.id.isNotBlank() }
        .distinctBy { it.id }
        .mapIndexed { index, ref -> index.toLong() to ref }

private fun Album.artistPositions(): List<Pair<Long, ArtistRef>> = artistRefs.positions()

private fun Song.artistPositions(): List<Pair<Long, ArtistRef>> = artistRefs.positions()

private fun SongGenre.columns(): List<Pair<String, Any?>> =
    listOf(
        "sourceId" to sourceId,
        "songId" to songId,
        "position" to position,
        "genre" to genre,
    )

private fun Playlist.columns(): List<Pair<String, Any?>> =
    listOf(
        "sourceId" to sourceId,
        "id" to id,
        "name" to name,
        "comment" to comment,
        "coverArt" to coverArt,
        "songCount" to songCount,
        "created" to created,
        "changed" to changed,
        "duration" to duration,
        "owner" to owner,
        "shared" to shared,
    )

private fun PlaylistSong.columns(): List<Pair<String, Any?>> =
    listOf(
        "sourceId" to sourceId,
        "playlistId" to playlistId,
        "songId" to songId,
        "position" to position,
    )

private suspend fun <T> PooledConnection.upsertChanged(
    table: String,
    key: List<String>,
    rows: List<T>,
    preserved: Set<String> = emptySet(),
    onChanged: (T) -> Unit = {},
    columns: (T) -> List<Pair<String, Any?>>,
) {
    if (rows.isEmpty()) return
    val columnNames = columns(rows.first()).map { it.first }
    val updates = columnNames.filterNot { it in key || it in preserved }
    val sql =
        "INSERT INTO $table (${columnNames.joinToString()}) VALUES (${columnNames.joinToString { "?" }}) " +
            "ON CONFLICT(${key.joinToString()}) DO UPDATE SET " +
            updates.joinToString { "$it = excluded.$it" } +
            " WHERE " +
            updates.joinToString(" OR ") { "$table.$it COLLATE BINARY IS NOT excluded.$it" } +
            " RETURNING 1"
    usePrepared(sql) { statement ->
        rows.forEach { row ->
            columns(row).forEachIndexed { index, (_, value) -> statement.bind(index + 1, value) }
            if (statement.step()) onChanged(row)
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
