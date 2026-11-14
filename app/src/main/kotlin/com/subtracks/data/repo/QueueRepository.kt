package com.subtracks.data.repo

import androidx.room3.deferredTransaction
import androidx.room3.immediateTransaction
import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.PlaybackCursor
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.QueueOrigin
import com.subtracks.data.model.SongItem
import com.subtracks.data.model.UpNextEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

const val QUEUE_CHUNK = 60

private const val LENGTH_CACHE_LIMIT = 512

private data class EntryLengthKey(
    val version: Long,
    val sourceId: Long,
    val kind: QueueKind,
    val refId: String,
    val offset: Long,
    val count: Long?,
)

private data class LoadedSnapshot(
    val entries: List<ResolvedQueueEntry>,
    val upNext: List<ResolvedQueueEntry>,
    val shuffle: ShuffleState?,
    val anchor: Long,
)

private data class OrderKey(
    val id: String,
    val disc: Long? = null,
    val track: Long? = null,
    val position: Long? = null,
)

private data class KeyCursor(
    val ordinal: Long,
    val key: OrderKey,
)

data class ResolvedQueueEntry(
    val entry: QueueEntry,
    val length: Long,
)

class ShuffleState(
    val seed: Long,
    val domain: Long,
) {
    fun flatFor(play: Long): Long = Shuffle.toFlat(seed, domain, play)

    fun playFor(canonical: Long): Long = Shuffle.toSequence(seed, domain, canonical)
}

data class QueueSnapshot(
    val entries: List<ResolvedQueueEntry>,
    val upNext: List<ResolvedQueueEntry> = emptyList(),
    val shuffle: ShuffleState? = null,
    val upNextAnchor: Long = 0,
    val version: Long = 0,
    val resetVersion: Long = 0,
) {
    val contextSize: Long = entries.sumOf { it.length }

    val upNextSize: Long = upNext.sumOf { it.length }

    val size: Long = contextSize + upNextSize

    val shuffled: Boolean get() = shuffle != null

    val shuffleSeed: Long? get() = shuffle?.seed

    val anchorPlay: Long =
        (
            when {
                shuffle == null -> upNextAnchor
                else -> playContext(upNextAnchor) ?: -1L
            }
        ).coerceAtMost(contextSize - 1L)

    fun isUpNext(position: Long): Boolean = upNextSize > 0L && position > anchorPlay && position <= anchorPlay + upNextSize

    fun upNextIndex(position: Long): Long = position - anchorPlay - 1L

    fun contextPlay(position: Long): Long = if (position <= anchorPlay) position else position - upNextSize

    fun anchorContextPlay(position: Long): Long = if (isUpNext(position)) anchorPlay else contextPlay(position)

    fun combined(contextPlay: Long): Long = if (contextPlay <= anchorPlay) contextPlay else contextPlay + upNextSize

    fun flatContext(contextPlay: Long): Long? {
        if (contextPlay !in 0 until contextSize) return null
        return shuffle?.flatFor(contextPlay) ?: contextPlay
    }

    fun playContext(flat: Long): Long? {
        if (flat !in 0 until contextSize) return null
        return shuffle?.playFor(flat) ?: flat
    }

    fun locate(position: Long): Pair<QueueEntry, Long>? {
        if (isUpNext(position)) return locateIn(upNext, upNextIndex(position))
        return locateContext(contextPlay(position))
    }

    fun locateContext(contextPlay: Long): Pair<QueueEntry, Long>? = locateIn(entries, flatContext(contextPlay) ?: return null)

    fun isAlbumContext(position: Long): Boolean = locate(position)?.first?.kind == QueueKind.Album
}

private fun entryIndexAfter(
    entries: List<ResolvedQueueEntry>,
    index: Long,
): Int {
    var start = 0L
    entries.forEachIndexed { position, resolved ->
        if (index < start + resolved.length) return position + 1
        start += resolved.length
    }
    return entries.size
}

private fun locateIn(
    entries: List<ResolvedQueueEntry>,
    index: Long,
): Pair<QueueEntry, Long>? {
    var remaining = index
    for (resolved in entries) {
        if (remaining < resolved.length) return resolved.entry to remaining
        remaining -= resolved.length
    }
    return null
}

data class QueueModes(
    val shuffle: Boolean,
    val repeat: Int,
)

data class QueueWindowItem(
    val position: Long,
    val item: SongItem,
    val playlistPosition: Long? = null,
    val upNext: Boolean = false,
    val resetItem: QueueResetItem? = null,
)

data class Reanchored(
    val position: Long,
    val changed: Boolean,
    val stale: Boolean = false,
)

data class QueueResetItem(
    val origin: QueueOrigin,
    val songId: String,
    val position: Long,
    val upNext: Boolean,
    val entryOccurrence: Int = 0,
    val occurrence: Long = 0,
    val originEntryId: Long? = null,
)

data class QueueResetPoint(
    val current: QueueResetItem?,
    val anchor: QueueResetItem?,
    val upNextAnchor: Long? = null,
)

data class QueueReset(
    val point: QueueResetPoint,
    val snapshot: QueueSnapshot,
)

private interface QueueKindResolver {
    fun supportsKeyset(key: OrderKey): Boolean = false

    suspend fun length(
        dao: com.subtracks.data.db.QueueDao,
        entry: QueueEntry,
    ): Long

    suspend fun rows(
        dao: com.subtracks.data.db.QueueDao,
        entry: QueueEntry,
        offset: Long,
        limit: Int,
    ): List<SongItem>

    suspend fun rowsFrom(
        dao: com.subtracks.data.db.QueueDao,
        entry: QueueEntry,
        key: OrderKey,
        skip: Long,
        limit: Int,
    ): List<SongItem>? = null

    suspend fun rowsBefore(
        dao: com.subtracks.data.db.QueueDao,
        entry: QueueEntry,
        key: OrderKey,
        skip: Long,
        limit: Int,
    ): List<SongItem>? = null

    suspend fun rank(
        dao: com.subtracks.data.db.QueueDao,
        entry: QueueEntry,
        songId: String,
    ): Long? = null
}

private val queueKindResolvers =
    mapOf<QueueKind, QueueKindResolver>(
        QueueKind.Song to
            object : QueueKindResolver {
                override suspend fun length(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                ) = dao.songLength(entry.sourceId, entry.refId)

                override suspend fun rows(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    offset: Long,
                    limit: Int,
                ) = dao.song(entry.sourceId, entry.refId, offset, limit)

                override suspend fun rank(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    songId: String,
                ) = if (entry.refId == songId && length(dao, entry) > 0) 0L else null
            },
        QueueKind.Playlist to
            object : QueueKindResolver {
                override fun supportsKeyset(key: OrderKey) = key.position != null

                override suspend fun length(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                ) = dao.playlistLength(entry.sourceId, entry.refId)

                override suspend fun rows(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    offset: Long,
                    limit: Int,
                ) = dao.playlistSongs(entry.sourceId, entry.refId, offset, limit)

                override suspend fun rowsFrom(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    key: OrderKey,
                    skip: Long,
                    limit: Int,
                ) = dao.playlistSongsFrom(entry.sourceId, entry.refId, key.position!!, skip, limit)

                override suspend fun rowsBefore(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    key: OrderKey,
                    skip: Long,
                    limit: Int,
                ) = dao.playlistSongsBefore(entry.sourceId, entry.refId, key.position!!, skip, limit)

                override suspend fun rank(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    songId: String,
                ) = dao.playlistRank(entry.sourceId, entry.refId, songId)
            },
        QueueKind.Album to
            object : QueueKindResolver {
                override fun supportsKeyset(key: OrderKey) = key.disc != null && key.track != null

                override suspend fun length(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                ) = dao.albumLength(entry.sourceId, entry.refId)

                override suspend fun rows(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    offset: Long,
                    limit: Int,
                ) = dao.albumSongs(entry.sourceId, entry.refId, offset, limit)

                override suspend fun rowsFrom(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    key: OrderKey,
                    skip: Long,
                    limit: Int,
                ) = dao.albumSongsFrom(entry.sourceId, entry.refId, key.disc!!, key.track!!, key.id, skip, limit)

                override suspend fun rowsBefore(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    key: OrderKey,
                    skip: Long,
                    limit: Int,
                ) = dao.albumSongsBefore(entry.sourceId, entry.refId, key.disc!!, key.track!!, key.id, skip, limit)

                override suspend fun rank(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    songId: String,
                ) = dao.albumRank(entry.sourceId, entry.refId, songId)
            },
        QueueKind.Artist to
            object : QueueKindResolver {
                override suspend fun length(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                ) = dao.artistLength(entry.sourceId, entry.refId)

                override suspend fun rows(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    offset: Long,
                    limit: Int,
                ) = dao.artistSongs(entry.sourceId, entry.refId, offset, limit)
            },
        QueueKind.Genre to
            object : QueueKindResolver {
                override suspend fun length(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                ) = dao.genreLength(entry.sourceId, entry.refId)

                override suspend fun rows(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    offset: Long,
                    limit: Int,
                ) = dao.genreSongs(entry.sourceId, entry.refId, offset, limit)
            },
        QueueKind.Downloaded to
            object : QueueKindResolver {
                override suspend fun length(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                ) = dao.downloadedLength(entry.sourceId)

                override suspend fun rows(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    offset: Long,
                    limit: Int,
                ) = dao.downloadedSongs(entry.sourceId, offset, limit)
            },
        QueueKind.Starred to
            object : QueueKindResolver {
                override suspend fun length(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                ) = dao.starredLength(entry.sourceId)

                override suspend fun rows(
                    dao: com.subtracks.data.db.QueueDao,
                    entry: QueueEntry,
                    offset: Long,
                    limit: Int,
                ) = dao.starredSongs(entry.sourceId, offset, limit)
            },
    )

private fun resolverFor(kind: QueueKind): QueueKindResolver = queueKindResolvers.getValue(kind)

class QueueRepository(
    private val db: SubtracksDatabase,
) {
    private val dao get() = db.queueDao()

    private val queueVersion = AtomicLong()
    private val resetVersion = AtomicLong()
    private val libraryVersion = AtomicLong()
    private val queueMutex = Mutex()
    private var resetListener: (suspend QueueMutationScope.(QueueReset) -> Unit)? = null

    fun setResetListener(listener: suspend QueueMutationScope.(QueueReset) -> Unit) {
        resetListener = listener
    }

    suspend fun <T> withMutationLock(block: suspend QueueMutationScope.() -> T): T =
        queueMutex.withLock {
            QueueMutationScope().block()
        }

    private val lengthCache = ConcurrentHashMap<EntryLengthKey, Long>()

    private val keyCache = ConcurrentHashMap<QueueOrigin, KeyCursor>()

    internal var keysetSeeks = 0L
        private set

    internal var offsetSeeks = 0L
        private set

    internal var resolvedRowCount = 0L
        private set

    internal val cursorMutex = Mutex()

    fun playlistEntry(
        sourceId: Long,
        playlistId: String,
    ) = QueueEntry(position = 0, sourceId = sourceId, kind = QueueKind.Playlist, refId = playlistId)

    fun albumEntry(
        sourceId: Long,
        albumId: String,
    ) = QueueEntry(position = 0, sourceId = sourceId, kind = QueueKind.Album, refId = albumId)

    fun artistEntry(
        sourceId: Long,
        artistId: String,
    ) = QueueEntry(position = 0, sourceId = sourceId, kind = QueueKind.Artist, refId = artistId)

    fun songEntry(
        sourceId: Long,
        songId: String,
    ) = QueueEntry(position = 0, sourceId = sourceId, kind = QueueKind.Song, refId = songId)

    fun genreEntry(
        sourceId: Long,
        genre: String,
    ) = QueueEntry(position = 0, sourceId = sourceId, kind = QueueKind.Genre, refId = genre)

    fun downloadedEntry(sourceId: Long) = QueueEntry(position = 0, sourceId = sourceId, kind = QueueKind.Downloaded, refId = "")

    fun starredEntry(sourceId: Long) = QueueEntry(position = 0, sourceId = sourceId, kind = QueueKind.Starred, refId = "")

    suspend fun sourceName(
        kind: QueueKind,
        sourceId: Long,
        refId: String,
    ): String? {
        val library = db.libraryDao()
        return when (kind) {
            QueueKind.Album -> {
                library
                    .album(sourceId, refId)
                    .first()
                    ?.name
                    ?.takeIf { it.isNotBlank() }
            }

            QueueKind.Playlist -> {
                library
                    .playlist(sourceId, refId)
                    .first()
                    ?.name
                    ?.takeIf { it.isNotBlank() }
            }

            QueueKind.Artist -> {
                library
                    .artist(sourceId, refId)
                    .first()
                    ?.name
                    ?.takeIf { it.isNotBlank() }
            }

            // A song entry can be a single or a track reordered out of a playlist, artist or
            // genre, so label it by its album rather than losing the context name.
            QueueKind.Song -> {
                library
                    .song(sourceId, refId)
                    .first()
                    ?.album
                    ?.takeIf { it.isNotBlank() }
            }

            QueueKind.Genre -> {
                refId.takeIf { it.isNotBlank() }
            }

            QueueKind.Downloaded -> {
                null
            }

            QueueKind.Starred -> {
                null
            }
        }
    }

    private suspend fun replaceLocked(
        entries: List<QueueEntry>,
        shuffleSeed: Long?,
        upNext: List<QueueEntry>,
        upNextAnchor: Long,
    ) {
        invalidateLibraryCache()
        writeLocked(withOriginEntryIds(entries, assignFragmentIds = false))
        writeUpNextLocked(withOriginEntryIds(upNext, assignFragmentIds = false))
        setShuffleLocked(shuffleSeed)
        cursorMutex.withLock {
            dao.setCursor(cursorRow().copy(queuePosition = 0, positionMs = 0, upNextAnchor = upNextAnchor))
        }
    }

    suspend fun snapshot(): QueueSnapshot = withMutationLock { snapshot() }

    private suspend fun snapshotLocked(): QueueSnapshot {
        val libraryRevision = libraryVersion.get()
        val loaded =
            db.useReaderConnection { transactor ->
                transactor.deferredTransaction {
                    val entries =
                        withOriginEntryIds(dao.entries(), assignFragmentIds = false).map {
                            ResolvedQueueEntry(it, lengthOf(it, libraryRevision))
                        }
                    val upNext =
                        withOriginEntryIds(dao.upNextEntries().map { it.toQueueEntry() }, assignFragmentIds = false).map { entry ->
                            ResolvedQueueEntry(entry, lengthOf(entry, libraryRevision))
                        }
                    val row = dao.cursor()
                    val size = entries.sumOf { it.length }
                    val shuffle =
                        if (row != null && row.shuffleEnabled && row.shuffleSize == size && size > 0L) {
                            ShuffleState(row.shuffleSeed, row.shuffleSize)
                        } else {
                            null
                        }
                    LoadedSnapshot(entries, upNext, shuffle, row?.upNextAnchor ?: 0L)
                }
            }
        return QueueSnapshot(loaded.entries, loaded.upNext, loaded.shuffle, loaded.anchor, queueVersion.get(), resetVersion.get())
    }

    suspend fun modes(): QueueModes {
        val row = dao.cursor() ?: return QueueModes(shuffle = false, repeat = 0)
        return QueueModes(row.shuffleEnabled, row.repeatMode)
    }

    private suspend fun setShuffleLocked(seed: Long?) {
        val size = if (seed != null) contextSize() else 0L
        cursorMutex.withLock {
            dao.setCursor(
                cursorRow().copy(
                    shuffleEnabled = seed != null,
                    shuffleSeed = seed ?: 0L,
                    shuffleSize = size,
                ),
            )
        }
        queueVersion.incrementAndGet()
    }

    fun contentVersion(): Long = queueVersion.get()

    fun invalidateLibraryCache() {
        lengthCache.clear()
        keyCache.clear()
        libraryVersion.incrementAndGet()
    }

    suspend fun captureResetPoint(): QueueResetPoint {
        val snapshot = snapshot()
        val row = cursorRow()
        val current =
            resetItemAt(snapshot, row.queuePosition)
                ?: snapshot.size.takeIf { it > 0L }?.let { resetItemAt(snapshot, 0L) }
        val anchor = if (snapshot.upNextSize == 0L) null else resetItemAt(snapshot, snapshot.anchorPlay)
        return QueueResetPoint(current, anchor, snapshot.upNextAnchor.takeIf { snapshot.upNextSize > 0L })
    }

    suspend fun resetChangedOrigins(
        changed: Set<QueueOrigin>,
        point: QueueResetPoint,
    ) {
        if (changed.isEmpty()) return
        withMutationLock {
            val before = snapshot()
            val entries = canonicalEntries(before.entries.map { it.entry }, changed)
            val upNext = canonicalEntries(before.upNext.map { it.entry }, changed)
            val matchingOrigin =
                before.entries.any { it.entry.origin in changed } || before.upNext.any { it.entry.origin in changed }
            if (!matchingOrigin) return@withMutationLock
            val updated = writeResetLocked(before, entries, upNext, point)
            resetListener?.invoke(this, QueueReset(point, updated))
        }
    }

    private suspend fun writeResetLocked(
        before: QueueSnapshot,
        entries: List<QueueEntry>,
        upNext: List<QueueEntry>,
        point: QueueResetPoint,
    ): QueueSnapshot {
        invalidateLibraryCache()
        val resolvedEntries = entries.map { ResolvedQueueEntry(it, it.resolvedLength()) }
        val resolvedUpNext = upNext.map { ResolvedQueueEntry(it, it.resolvedLength()) }
        val shuffle = before.shuffleSeed?.let { ShuffleState(it, resolvedEntries.sumOf { it.length }) }
        var updated =
            QueueSnapshot(
                entries = resolvedEntries,
                upNext = resolvedUpNext,
                shuffle = shuffle,
                upNextAnchor = before.upNextAnchor,
                version = queueVersion.get(),
                resetVersion = resetVersion.get(),
            )
        val anchorChanged = before.upNextSize > 0L && before.upNextAnchor != point.upNextAnchor
        val latestAnchor = if (anchorChanged) resetItemAt(before, before.anchorPlay) else null
        val anchor =
            if (anchorChanged) {
                remapAnchor(updated, latestAnchor) ?: fallbackAnchor(updated, before.anchorPlay)
            } else {
                remapAnchor(updated, point.anchor)
                    ?: fallbackAnchor(updated, point.anchor?.position ?: before.upNextAnchor)
            }
        updated = updated.copy(upNextAnchor = anchor)
        cursorMutex.withLock {
            val row = cursorRow()
            val current = point.current ?: resetItemAt(before, row.queuePosition)
            val remappedCurrent = current?.let { remapPosition(updated, it) }
            val cursor =
                when {
                    updated.size == 0L -> 0L
                    remappedCurrent != null -> remappedCurrent.coerceIn(0, updated.size - 1)
                    current != null -> current.position.coerceIn(0, updated.size - 1)
                    else -> row.queuePosition.coerceIn(0, updated.size - 1)
                }
            val previousSongId = current?.songId
            val updatedSongId = itemAt(updated, cursor)?.song?.id
            val positionMs = if (remappedCurrent != null && previousSongId == updatedSongId) row.positionMs else 0L
            db.useWriterConnection { connection ->
                connection.immediateTransaction {
                    dao.clear()
                    dao.insert(entries.mapIndexed { index, entry -> entry.copy(id = 0, position = index.toLong()) })
                    dao.clearUpNext()
                    dao.insertUpNext(
                        upNext.mapIndexed { index, entry -> UpNextEntry.from(entry).copy(id = 0, position = index.toLong()) },
                    )
                    dao.setCursor(
                        row.copy(
                            queuePosition = cursor,
                            positionMs = positionMs,
                            upNextAnchor = anchor,
                            shuffleEnabled = shuffle != null,
                            shuffleSeed = shuffle?.seed ?: 0L,
                            shuffleSize = shuffle?.domain ?: 0L,
                        ),
                    )
                }
            }
        }
        queueVersion.incrementAndGet()
        resetVersion.incrementAndGet()
        return updated.copy(version = queueVersion.get(), resetVersion = resetVersion.get())
    }

    private suspend fun remapAnchor(
        snapshot: QueueSnapshot,
        item: QueueResetItem?,
    ): Long? {
        val target = item?.let { remapPosition(snapshot, it) } ?: return null
        return snapshot.flatContext(snapshot.contextPlay(target))
    }

    private fun fallbackAnchor(
        snapshot: QueueSnapshot,
        position: Long,
    ): Long {
        val play = position.coerceIn(-1L, snapshot.contextSize - 1L)
        return if (play < 0L) play else snapshot.flatContext(play) ?: play
    }

    suspend fun resetItemAt(
        snapshot: QueueSnapshot,
        position: Long,
        songId: String? = null,
        item: SongItem? = null,
    ): QueueResetItem? {
        if (position !in 0 until snapshot.size) return null
        val entry = snapshot.locate(position)?.first ?: return null
        val upNext = snapshot.isUpNext(position)
        val entries = if (upNext) snapshot.upNext else snapshot.entries
        val blockPosition =
            if (upNext) {
                snapshot.upNextIndex(position)
            } else {
                snapshot.flatContext(snapshot.contextPlay(position))
                    ?: return null
            }
        var start = 0L
        var entryIndex = -1
        for (index in entries.indices) {
            val end = start + entries[index].length
            if (blockPosition >= start && blockPosition < end) {
                entryIndex = index
                break
            }
            start = end
        }
        if (entryIndex < 0) return null
        val origin = entry.origin
        val entryOccurrence = resetEntryOccurrence(entries, entryIndex)
        val resolvedItem = item ?: itemAt(snapshot, position)
        val resolvedSongId = songId ?: resolvedItem?.song?.id ?: return null
        val occurrence =
            entry.originOccurrence
                ?: if (entry.kind == QueueKind.Playlist && resolvedItem?.playlistPosition != null) {
                    dao.playlistOccurrenceRank(entry.sourceId, entry.refId, resolvedSongId, resolvedItem.playlistPosition!!)
                } else {
                    0L
                }
        return QueueResetItem(origin, resolvedSongId, position, upNext, entryOccurrence, occurrence, entry.originEntryId)
    }

    private data class OriginEntryKey(
        val origin: QueueOrigin,
        val entryId: Long?,
    )

    private fun QueueEntry.originEntryKey() = OriginEntryKey(origin, originEntryId)

    private fun resetEntryOccurrence(
        entries: List<ResolvedQueueEntry>,
        targetIndex: Int,
    ): Int {
        val targetKey = entries[targetIndex].entry.originEntryKey()
        val groups = mutableListOf<OriginEntryKey>()
        for (index in 0..targetIndex) {
            val entry = entries[index].entry
            if (entry.origin != targetKey.origin) continue
            val key = entry.originEntryKey()
            if (key !in groups) groups += key
        }
        return groups.indexOf(targetKey)
    }

    private fun QueueEntry.isWholeOrigin(): Boolean {
        val origin = origin
        return kind == origin.kind && refId == origin.refId && rangeStart == null && rangeEnd == null
    }

    private fun QueueEntry.shouldAssignOriginEntryId(assignFragmentIds: Boolean) =
        originEntryId == null && (assignFragmentIds || isWholeOrigin())

    private fun withOriginEntryIds(
        entries: List<QueueEntry>,
        existingEntries: List<QueueEntry> = emptyList(),
        assignFragmentIds: Boolean = true,
    ): List<QueueEntry> {
        if (entries.none { it.shouldAssignOriginEntryId(assignFragmentIds) }) return entries
        val nextIds = mutableMapOf<QueueOrigin, Long>()
        for (entry in existingEntries + entries) {
            val id = entry.originEntryId ?: continue
            nextIds[entry.origin] = maxOf(nextIds[entry.origin] ?: 0L, id + 1)
        }
        val fragmentIds = mutableMapOf<QueueOrigin, Long>()
        return entries.map { entry ->
            if (!entry.shouldAssignOriginEntryId(assignFragmentIds)) return@map entry
            val origin = entry.origin
            val whole = entry.isWholeOrigin()
            val existingId = if (whole) null else fragmentIds[origin]
            val id = existingId ?: (nextIds[origin] ?: 0L)
            if (existingId == null) {
                nextIds[origin] = id + 1
                if (!whole) fragmentIds[origin] = id
            }
            entry.copy(originEntryId = id)
        }
    }

    private fun canonicalEntries(
        entries: List<QueueEntry>,
        changed: Set<QueueOrigin>,
    ): List<QueueEntry> {
        val result = mutableListOf<QueueEntry>()
        val reset = mutableSetOf<OriginEntryKey>()
        for (entry in entries) {
            val origin = entry.origin
            if (origin !in changed || entry.originEntryId == null) {
                result += entry
            } else if (reset.add(entry.originEntryKey())) {
                result +=
                    QueueEntry(
                        position = 0,
                        sourceId = origin.sourceId,
                        kind = origin.kind,
                        refId = origin.refId,
                        originEntryId = entry.originEntryId,
                    )
            }
        }
        return result
    }

    private data class OriginEntryGroup(
        val key: OriginEntryKey,
        val starts: List<Long>,
        val entries: List<ResolvedQueueEntry>,
    )

    private fun originEntryGroups(
        entries: List<ResolvedQueueEntry>,
        origin: QueueOrigin,
    ): List<OriginEntryGroup> {
        val groups = mutableListOf<OriginEntryGroup>()
        val groupIndices = mutableMapOf<OriginEntryKey, Int>()
        var start = 0L
        for (resolved in entries) {
            if (resolved.entry.origin == origin) {
                val key = resolved.entry.originEntryKey()
                val index = groupIndices[key]
                if (index == null) {
                    groupIndices[key] = groups.size
                    groups += OriginEntryGroup(key, listOf(start), listOf(resolved))
                } else {
                    groups[index] =
                        groups[index].copy(
                            starts = groups[index].starts + start,
                            entries = groups[index].entries + resolved,
                        )
                }
            }
            start += resolved.length
        }
        return groups
    }

    suspend fun remapPosition(
        snapshot: QueueSnapshot,
        item: QueueResetItem,
    ): Long? {
        val entries = if (item.upNext) snapshot.upNext else snapshot.entries
        val groups = originEntryGroups(entries, item.origin)
        val group =
            if (item.originEntryId == null) {
                groups.getOrNull(item.entryOccurrence)
            } else {
                groups.firstOrNull { it.key == OriginEntryKey(item.origin, item.originEntryId) }
            } ?: return null
        for ((index, resolved) in group.entries.withIndex()) {
            val entry = resolved.entry
            if (entry.originOccurrence != null && entry.originOccurrence != item.occurrence) continue
            val rank =
                if (entry.kind == QueueKind.Playlist) {
                    val position = dao.playlistPositionForOccurrence(entry.sourceId, entry.refId, item.songId, item.occurrence)
                    position?.let { dao.playlistRankAtPosition(entry.sourceId, entry.refId, it) }
                } else {
                    resolverFor(entry.kind).rank(dao, entry, item.songId) ?: fallbackRank(entry, resolved.length, item.songId)
                }
            if (rank != null) {
                val insideEntry = rank >= entry.offset && rank < entry.offset + resolved.length
                val flat =
                    when {
                        insideEntry -> group.starts[index] + rank - entry.offset
                        entry.originEntryId == null -> rank
                        else -> continue
                    }
                if (item.upNext) return snapshot.anchorPlay + 1 + flat
                val play = snapshot.playContext(flat) ?: return null
                return snapshot.combined(play)
            }
        }
        return null
    }

    suspend fun setRepeat(mode: Int) = cursorMutex.withLock { dao.setCursor(cursorRow().copy(repeatMode = mode)) }

    suspend fun itemAt(
        snapshot: QueueSnapshot,
        position: Long,
    ): SongItem? {
        val (entry, offset) = snapshot.locate(position) ?: return null
        return rows(entry, entry.offset + offset, 1).firstOrNull()
    }

    suspend fun combinedIndexOf(
        snapshot: QueueSnapshot,
        songId: String,
    ): Long? {
        val upIndex = flatIndexOf(snapshot.upNext, songId)
        if (upIndex != null) return snapshot.anchorPlay + 1 + upIndex
        return combinedContextIndexOf(snapshot, songId)
    }

    suspend fun combinedContextIndexOf(
        snapshot: QueueSnapshot,
        songId: String,
    ): Long? {
        val flat = flatIndexOf(snapshot.entries, songId) ?: return null
        return snapshot.playContext(flat)?.let { snapshot.combined(it) }
    }

    suspend fun range(
        snapshot: QueueSnapshot,
        first: Long,
        last: Long,
    ): List<QueueWindowItem> {
        val from = first.coerceAtLeast(0)
        val to = last.coerceAtMost(snapshot.size - 1)
        if (from > to) return emptyList()
        val anchor = snapshot.anchorPlay
        val block = snapshot.upNextSize
        val items = mutableListOf<QueueWindowItem>()
        val beforeEnd = minOf(to, anchor)
        if (from <= beforeEnd) items += contextPlayRange(snapshot, from, beforeEnd)
        val blockFirst = maxOf(from, anchor + 1)
        val blockLast = minOf(to, anchor + block)
        if (blockFirst <= blockLast) items += blockPlayRange(snapshot, blockFirst - anchor - 1, blockLast - anchor - 1)
        val afterFirst = maxOf(from, anchor + block + 1)
        if (afterFirst <= to) items += contextPlayRange(snapshot, afterFirst - block, to - block)
        return items
    }

    suspend fun window(
        snapshot: QueueSnapshot,
        center: Long,
        radius: Long,
    ): List<QueueWindowItem> {
        if (snapshot.size == 0L) return emptyList()
        return range(snapshot, (center - radius).coerceAtLeast(0), (center + radius).coerceAtMost(snapshot.size - 1))
    }

    private suspend fun addUpNextLocked(
        currentPosition: Long,
        entry: QueueEntry,
        playNext: Boolean,
    ): Long {
        invalidateLibraryCache()
        val current = snapshotLocked()
        val list = current.upNext.map { it.entry }.toMutableList()
        val insertIndex =
            when {
                playNext && current.isUpNext(currentPosition) -> {
                    entryIndexAfter(current.upNext, current.upNextIndex(currentPosition))
                }

                playNext -> {
                    0
                }

                else -> {
                    list.size
                }
            }.coerceIn(0, list.size)
        val newEntry = withOriginEntryIds(listOf(entry.copy(id = 0, position = 0)), list).single()
        list.add(insertIndex, newEntry)
        val anchor =
            if (current.upNextSize == 0L) {
                val play = current.anchorContextPlay(currentPosition)
                current.flatContext(play) ?: 0L
            } else {
                current.upNextAnchor
            }
        writeUpNextLocked(list)
        cursorMutex.withLock { dao.setCursor(cursorRow().copy(upNextAnchor = anchor)) }
        if (current.upNextSize == 0L) return currentPosition
        val before = current.upNext.take(insertIndex).sumOf { it.length }
        return if (currentPosition >= current.anchorPlay + 1 + before) currentPosition + 1 else currentPosition
    }

    private suspend fun reanchorForLocked(
        snapshot: QueueSnapshot,
        target: Long,
    ): Reanchored {
        if (snapshot.version != queueVersion.get()) return Reanchored(target, false, stale = true)
        if (snapshot.upNextSize == 0L || snapshot.isUpNext(target)) return Reanchored(target, false)
        val play = snapshot.contextPlay(target)
        val flat = snapshot.flatContext(play) ?: return Reanchored(target, false)
        if (flat == snapshot.upNextAnchor) return Reanchored(target, false)
        cursorMutex.withLock { dao.setCursor(cursorRow().copy(upNextAnchor = flat)) }
        queueVersion.incrementAndGet()
        return Reanchored(play, true)
    }

    private suspend fun removeUpNextAtLocked(
        snapshot: QueueSnapshot,
        index: Long,
    ): Boolean {
        if (index !in 0 until snapshot.upNextSize) return false
        writeUpNextLocked(compact(removeEntry(snapshot.upNext, index)).map { it.entry })
        return true
    }

    private suspend fun removeAtLocked(
        snapshot: QueueSnapshot,
        position: Long,
    ): Boolean {
        if (snapshot.version != queueVersion.get()) return false
        if (snapshot.isUpNext(position)) return removeUpNextAtLocked(snapshot, snapshot.upNextIndex(position))
        if (snapshot.shuffled) return false
        val play = snapshot.contextPlay(position)
        val removed = compact(removeEntry(snapshot.entries, play))
        writeLocked(removed.map { it.entry })
        adjustAnchorOnRemove(play)
        return true
    }

    private suspend fun adjustAnchorOnRemove(removedFlat: Long) =
        cursorMutex.withLock {
            val row = cursorRow()
            val anchor = row.upNextAnchor
            // Removing the track the block sits behind leaves the block next in line, so move the
            // anchor back to the preceding track (or in front of the whole context, -1, when the
            // anchor was the first track) rather than letting the following track play first.
            val adjusted = if (anchor >= removedFlat) anchor - 1 else anchor
            if (adjusted != anchor) dao.setCursor(row.copy(upNextAnchor = adjusted))
        }

    private suspend fun adjustAnchorOnMove(
        flatFrom: Long,
        flatTo: Long,
    ) = cursorMutex.withLock {
        val row = cursorRow()
        val anchor = row.upNextAnchor
        val moved =
            when {
                flatFrom == anchor -> flatTo
                flatFrom < anchor && flatTo >= anchor -> anchor - 1
                flatFrom > anchor && flatTo <= anchor -> anchor + 1
                else -> anchor
            }
        if (moved != anchor) dao.setCursor(row.copy(upNextAnchor = moved))
    }

    private suspend fun moveLocked(
        snapshot: QueueSnapshot,
        from: Long,
        to: Long,
    ): Boolean {
        if (snapshot.version != queueVersion.get()) return false
        val fromUpNext = snapshot.isUpNext(from)
        val toUpNext = snapshot.isUpNext(to)
        if (fromUpNext && toUpNext) {
            val song = itemAt(snapshot, from) ?: return false
            val located = snapshot.locate(from) ?: return false
            val resetItem = resetItemAt(snapshot, from, item = song)
            val removed = removeEntry(snapshot.upNext, snapshot.upNextIndex(from))
            val moved = ResolvedQueueEntry(movedEntry(located.first, song.song.id, located.second, resetItem), 1L)
            writeUpNextLocked(compact(insertEntry(removed, snapshot.upNextIndex(to), moved)).map { it.entry })
            return true
        }
        if (fromUpNext != toUpNext) return false
        // Reordering the context while shuffled would have to make the derived order match the
        // drop, which needs materialising the order again. The queue view disables context drags
        // while shuffled; manual ordering goes through the up-next block instead.
        if (snapshot.shuffled) return false
        val playFrom = snapshot.contextPlay(from)
        val playTo = snapshot.contextPlay(to)
        val located = snapshot.locateContext(playFrom) ?: return false
        val song = itemAt(snapshot, from) ?: return false
        val resetItem = resetItemAt(snapshot, from, item = song)
        val removed = removeEntry(snapshot.entries, playFrom)
        val moved = ResolvedQueueEntry(movedEntry(located.first, song.song.id, located.second, resetItem), 1L)
        val insertAt = playTo.coerceIn(0, removed.sumOf { it.length })
        writeLocked(compact(insertEntry(removed, insertAt, moved)).map { it.entry })
        adjustAnchorOnMove(playFrom, insertAt)
        return true
    }

    suspend fun cursor(): Long = dao.cursor()?.queuePosition ?: 0

    suspend fun cursorPositionMs(): Long = dao.cursor()?.positionMs ?: 0

    suspend fun setPosition(positionMs: Long) =
        cursorMutex.withLock {
            dao.setCursor(cursorRow().copy(positionMs = positionMs.coerceAtLeast(0)))
        }

    suspend fun setCursor(position: Long) =
        cursorMutex.withLock {
            val row = cursorRow()
            dao.setCursor(row.copy(queuePosition = position, positionMs = if (row.queuePosition == position) row.positionMs else 0))
        }

    private suspend fun cursorRow(): PlaybackCursor = dao.cursor() ?: PlaybackCursor(queuePosition = 0)

    private suspend fun contextSize(): Long = dao.entries().sumOf { it.resolvedLength() }

    private suspend fun writeLocked(entries: List<QueueEntry>) {
        db.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                dao.clear()
                dao.insert(entries.mapIndexed { index, entry -> entry.copy(id = 0, position = index.toLong()) })
            }
        }
        queueVersion.incrementAndGet()
    }

    private suspend fun writeUpNextLocked(entries: List<QueueEntry>) {
        db.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                dao.clearUpNext()
                dao.insertUpNext(
                    entries.mapIndexed { index, entry -> UpNextEntry.from(entry).copy(id = 0, position = index.toLong()) },
                )
            }
        }
        queueVersion.incrementAndGet()
    }

    inner class QueueMutationScope internal constructor() {
        suspend fun snapshot(): QueueSnapshot = snapshotLocked()

        suspend fun replace(
            entries: List<QueueEntry>,
            shuffleSeed: Long? = null,
            upNext: List<QueueEntry> = emptyList(),
            upNextAnchor: Long = 0,
        ) = replaceLocked(entries, shuffleSeed, upNext, upNextAnchor)

        suspend fun setShuffle(seed: Long?) = setShuffleLocked(seed)

        suspend fun addUpNext(
            currentPosition: Long,
            entry: QueueEntry,
            playNext: Boolean,
        ): Long = addUpNextLocked(currentPosition, entry, playNext)

        suspend fun reanchorFor(
            snapshot: QueueSnapshot,
            target: Long,
        ): Reanchored = reanchorForLocked(snapshot, target)

        suspend fun clearUpNext() = writeUpNextLocked(emptyList())

        suspend fun removeUpNextAt(
            snapshot: QueueSnapshot,
            index: Long,
        ): Boolean {
            if (snapshot.version != queueVersion.get()) return false
            return removeUpNextAtLocked(snapshot, index)
        }

        suspend fun removeAt(
            snapshot: QueueSnapshot,
            position: Long,
        ): Boolean = removeAtLocked(snapshot, position)

        suspend fun move(
            snapshot: QueueSnapshot,
            from: Long,
            to: Long,
        ): Boolean = moveLocked(snapshot, from, to)
    }

    private suspend fun flatIndexOf(
        entries: List<ResolvedQueueEntry>,
        songId: String,
    ): Long? {
        var start = 0L
        for (resolved in entries) {
            val entry = resolved.entry
            val rank = resolverFor(entry.kind).rank(dao, entry, songId)
            if (rank != null && rank >= entry.offset && rank < entry.offset + resolved.length) {
                return start + rank - entry.offset
            }
            val fallback = fallbackRank(entry, resolved.length, songId)
            if (fallback != null) return start + fallback - entry.offset
            start += resolved.length
        }
        return null
    }

    private suspend fun fallbackRank(
        entry: QueueEntry,
        length: Long,
        songId: String,
    ): Long? {
        var offset = entry.offset
        val end = entry.offset + length
        while (offset < end) {
            val limit = minOf(QUEUE_CHUNK.toLong(), end - offset).toInt()
            val batch = rows(entry, offset, limit)
            val index = batch.indexOfFirst { it.song.id == songId }
            if (index >= 0) return offset + index
            if (batch.size < limit) return null
            offset += batch.size
        }
        return null
    }

    private suspend fun contextPlayRange(
        snapshot: QueueSnapshot,
        first: Long,
        last: Long,
    ): List<QueueWindowItem> {
        if (first > last) return emptyList()
        val items = mutableListOf<QueueWindowItem>()
        if (snapshot.shuffled) {
            for (play in first..last) {
                val located = snapshot.locateContext(play) ?: continue
                val item = rows(located.first, located.first.offset + located.second, 1).firstOrNull() ?: continue
                val position = snapshot.combined(play)
                items +=
                    QueueWindowItem(
                        position,
                        item,
                        playlistPosition = item.playlistPosition,
                        resetItem = resetItemAt(snapshot, position, item = item),
                    )
            }
            return items
        }
        var start = 0L
        for (resolved in snapshot.entries) {
            val end = start + resolved.length - 1
            val entryStart = start
            start += resolved.length
            if (resolved.length == 0L || end < first || entryStart > last) continue
            val from = maxOf(first, entryStart)
            val to = minOf(last, end)
            val offset = resolved.entry.offset + (from - entryStart)
            rows(resolved.entry, offset, (to - from + 1).toInt()).forEachIndexed { index, item ->
                val position = snapshot.combined(from + index)
                items +=
                    QueueWindowItem(
                        position,
                        item,
                        playlistPosition = item.playlistPosition,
                        resetItem = resetItemAt(snapshot, position, item = item),
                    )
            }
        }
        return items
    }

    private suspend fun blockPlayRange(
        snapshot: QueueSnapshot,
        first: Long,
        last: Long,
    ): List<QueueWindowItem> {
        if (first > last) return emptyList()
        val items = mutableListOf<QueueWindowItem>()
        var start = 0L
        for (resolved in snapshot.upNext) {
            val end = start + resolved.length - 1
            val entryStart = start
            start += resolved.length
            if (resolved.length == 0L || end < first || entryStart > last) continue
            val from = maxOf(first, entryStart)
            val to = minOf(last, end)
            val offset = resolved.entry.offset + (from - entryStart)
            rows(resolved.entry, offset, (to - from + 1).toInt()).forEachIndexed { index, item ->
                val position = snapshot.anchorPlay + 1 + from + index
                items +=
                    QueueWindowItem(
                        position,
                        item,
                        playlistPosition = item.playlistPosition,
                        upNext = true,
                        resetItem = resetItemAt(snapshot, position, item = item),
                    )
            }
        }
        return items
    }

    private suspend fun rows(
        entry: QueueEntry,
        offset: Long,
        limit: Int,
    ): List<SongItem> {
        if (entry.kind == QueueKind.Song) {
            offsetSeeks++
            return resolverFor(entry.kind).rows(dao, entry, offset, limit).also { resolvedRowCount += it.size }
        }
        val ref = QueueOrigin(entry.kind, entry.sourceId, entry.refId)
        val anchored = keyCache[ref]?.let { keyedRows(entry, offset, limit, it) }
        if (anchored != null) {
            keysetSeeks++
            if (anchored.isNotEmpty()) remember(ref, offset, anchored)
            resolvedRowCount += anchored.size
            return anchored
        }
        offsetSeeks++
        val fresh = resolverFor(entry.kind).rows(dao, entry, offset, limit)
        if (fresh.isNotEmpty()) remember(ref, offset, fresh)
        resolvedRowCount += fresh.size
        return fresh
    }

    private fun remember(
        ref: QueueOrigin,
        offset: Long,
        resolved: List<SongItem>,
    ) {
        if (keyCache.size >= LENGTH_CACHE_LIMIT) keyCache.clear()
        keyCache[ref] = KeyCursor(offset + resolved.size - 1, orderKey(ref.kind, resolved.last()))
    }

    private fun orderKey(
        kind: QueueKind,
        row: SongItem,
    ): OrderKey =
        when (kind) {
            QueueKind.Album -> OrderKey(row.song.id, disc = row.song.disc, track = row.song.track)
            QueueKind.Playlist -> OrderKey(row.song.id, position = row.playlistPosition)
            QueueKind.Artist -> OrderKey(row.song.id)
            QueueKind.Genre -> OrderKey(row.song.id)
            QueueKind.Downloaded -> OrderKey(row.song.id)
            QueueKind.Starred -> OrderKey(row.song.id)
            QueueKind.Song -> OrderKey(row.song.id)
        }

    private suspend fun keyedRows(
        entry: QueueEntry,
        offset: Long,
        limit: Int,
        cursor: KeyCursor,
    ): List<SongItem>? {
        if (!resolverFor(entry.kind).supportsKeyset(cursor.key)) return null
        val from = cursor.ordinal
        // A backward seek from a far cursor is not cheaper than the offset query: the album
        // predicate is not sargable (it has to include NULL disc/track), so it scans the partition
        // back to `offset` instead of using a range. Fall back when the anchor is more than twice
        // the offset away, matching the documented far-jump behaviour.
        if (from > offset && from - offset >= offset) return null
        return if (offset >= from) {
            rowsFrom(entry, cursor.key, offset - from, limit)
        } else {
            val end = offset + limit - 1
            if (end < from) {
                rowsBefore(entry, cursor.key, from - end - 1, limit).asReversed()
            } else {
                val head = rowsBefore(entry, cursor.key, 0, (from - offset).toInt()).asReversed()
                val tail = rowsFrom(entry, cursor.key, 0, (end - from + 1).toInt())
                head + tail
            }
        }
    }

    private suspend fun rowsFrom(
        entry: QueueEntry,
        key: OrderKey,
        skip: Long,
        limit: Int,
    ): List<SongItem> = resolverFor(entry.kind).rowsFrom(dao, entry, key, skip, limit) ?: emptyList()

    private suspend fun rowsBefore(
        entry: QueueEntry,
        key: OrderKey,
        skip: Long,
        limit: Int,
    ): List<SongItem> = resolverFor(entry.kind).rowsBefore(dao, entry, key, skip, limit) ?: emptyList()

    private suspend fun lengthOf(
        entry: QueueEntry,
        version: Long,
    ): Long {
        val key = entry.lengthKey(version)
        lengthCache[key]?.let { return it }
        val length = entry.resolvedLength()
        if (lengthCache.size >= LENGTH_CACHE_LIMIT) lengthCache.clear()
        lengthCache[key] = length
        return length
    }

    private fun QueueEntry.lengthKey(version: Long) =
        EntryLengthKey(
            version = version,
            sourceId = sourceId,
            kind = kind,
            refId = refId,
            offset = offset,
            count = count,
        )

    private suspend fun QueueEntry.resolvedLength(): Long {
        val total = resolverFor(kind).length(dao, this)
        val available = (total - offset).coerceAtLeast(0)
        return count?.coerceAtMost(available) ?: available
    }

    private fun movedEntry(
        source: QueueEntry,
        songId: String,
        offsetInEntry: Long,
        resetItem: QueueResetItem?,
    ): QueueEntry =
        if (source.kind == QueueKind.Album) {
            val offset = source.offset + offsetInEntry
            source.range(offset, offset)
        } else {
            songEntry(source.sourceId, songId).copy(
                originKind = source.origin.kind,
                originRefId = source.origin.refId,
                originOccurrence = resetItem?.occurrence ?: source.originOccurrence,
                originEntryId = source.originEntryId,
            )
        }

    private fun QueueEntry.range(
        start: Long,
        end: Long,
    ) = copy(id = 0, position = 0, rangeStart = start, rangeEnd = end)

    private fun removeEntry(
        entries: List<ResolvedQueueEntry>,
        index: Long,
    ): List<ResolvedQueueEntry> {
        val result = mutableListOf<ResolvedQueueEntry>()
        var start = 0L
        for (resolved in entries) {
            val end = start + resolved.length - 1
            if (index in start..end) {
                val offset = resolved.entry.offset + (index - start)
                val first = resolved.entry.offset
                val last = first + resolved.length - 1
                if (offset > first) {
                    result += ResolvedQueueEntry(resolved.entry.range(first, offset - 1), offset - first)
                }
                if (offset < last) {
                    result += ResolvedQueueEntry(resolved.entry.range(offset + 1, last), last - offset)
                }
            } else {
                result += resolved
            }
            start += resolved.length
        }
        return result
    }

    private fun insertEntry(
        entries: List<ResolvedQueueEntry>,
        index: Long,
        newEntry: ResolvedQueueEntry,
    ): List<ResolvedQueueEntry> {
        val result = mutableListOf<ResolvedQueueEntry>()
        var start = 0L
        var placed = false
        for (resolved in entries) {
            val end = start + resolved.length - 1
            when {
                !placed && index <= start -> {
                    result += newEntry
                    result += resolved
                    placed = true
                }

                !placed && index in start..end -> {
                    val cut = index - start
                    val offset = resolved.entry.offset
                    if (cut > 0) {
                        result += ResolvedQueueEntry(resolved.entry.range(offset, offset + cut - 1), cut)
                    }
                    result += newEntry
                    if (cut < resolved.length) {
                        result +=
                            ResolvedQueueEntry(
                                resolved.entry.range(offset + cut, offset + resolved.length - 1),
                                resolved.length - cut,
                            )
                    }
                    placed = true
                }

                else -> {
                    result += resolved
                }
            }
            start += resolved.length
        }
        if (!placed) result += newEntry
        return result
    }

    private fun compact(entries: List<ResolvedQueueEntry>): List<ResolvedQueueEntry> {
        val merged = mutableListOf<ResolvedQueueEntry>()
        for (resolved in entries) {
            val previous = merged.lastOrNull()
            if (previous != null && previous.contiguousWith(resolved)) {
                merged[merged.size - 1] =
                    ResolvedQueueEntry(
                        previous.entry.range(previous.entry.offset, resolved.entry.offset + resolved.length - 1),
                        previous.length + resolved.length,
                    )
            } else {
                merged += resolved
            }
        }
        return merged
    }

    private fun ResolvedQueueEntry.contiguousWith(next: ResolvedQueueEntry): Boolean =
        entry.kind == next.entry.kind &&
            entry.sourceId == next.entry.sourceId &&
            entry.refId == next.entry.refId &&
            entry.origin == next.entry.origin &&
            entry.originOccurrence == next.entry.originOccurrence &&
            entry.originEntryId == next.entry.originEntryId &&
            entry.offset + length == next.entry.offset
}
