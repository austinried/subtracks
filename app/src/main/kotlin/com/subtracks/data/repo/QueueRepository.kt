package com.subtracks.data.repo

import androidx.room3.deferredTransaction
import androidx.room3.immediateTransaction
import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.PlaybackCursor
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.ShuffleOrder
import com.subtracks.data.model.SongListItem

const val QUEUE_CHUNK = 60

data class ResolvedQueueEntry(
    val entry: QueueEntry,
    val length: Long,
)

data class QueueSnapshot(
    val entries: List<ResolvedQueueEntry>,
    val shuffleOrder: LongArray? = null,
) {
    val size: Long = entries.sumOf { it.length }

    val shuffled: Boolean get() = shuffleOrder != null

    fun locate(position: Long): Pair<QueueEntry, Long>? = locateFlat(flatPosition(position) ?: return null)

    fun flatPosition(sequence: Long): Long? = if (shuffleOrder == null) sequence else shuffleOrder.getOrNull(sequence.toInt())

    private fun locateFlat(position: Long): Pair<QueueEntry, Long>? {
        var remaining = position
        for (resolved in entries) {
            if (remaining < resolved.length) return resolved.entry to remaining
            remaining -= resolved.length
        }
        return null
    }
}

data class QueueModes(
    val shuffle: Boolean,
    val repeat: Int,
)

data class QueueWindowItem(
    val position: Long,
    val item: SongListItem,
)

class QueueRepository(
    private val db: SubtracksDatabase,
) {
    private val dao get() = db.queueDao()

    @Volatile
    private var cachedShuffleSeed = 0L

    @Volatile
    private var cachedShuffleOrder: LongArray? = null

    fun playlistEntry(
        sourceId: Long,
        playlistId: String,
    ) = QueueEntry(position = 0, sourceId = sourceId, kind = QueueKind.Playlist, refId = playlistId)

    fun albumEntry(
        sourceId: Long,
        albumId: String,
    ) = QueueEntry(position = 0, sourceId = sourceId, kind = QueueKind.Album, refId = albumId)

    fun songEntry(
        sourceId: Long,
        songId: String,
    ) = QueueEntry(position = 0, sourceId = sourceId, kind = QueueKind.Song, refId = songId)

    fun songsEntry(sourceId: Long) = QueueEntry(position = 0, sourceId = sourceId, kind = QueueKind.Songs, refId = "")

    suspend fun replace(
        entries: List<QueueEntry>,
        shuffleOrder: LongArray? = null,
    ) {
        write(entries)
        setShuffle(shuffleOrder != null, shuffleOrder)
        dao.setCursor(cursorRow().copy(queuePosition = 0))
    }

    suspend fun snapshot(): QueueSnapshot =
        db.useReaderConnection { transactor ->
            transactor.deferredTransaction {
                val entries = dao.entries().map { ResolvedQueueEntry(it, it.resolvedLength()) }
                val row = dao.cursor()
                val order =
                    if (row?.shuffleEnabled == true) {
                        loadShuffleOrder(row.shuffleSeed, entries.sumOf { it.length })
                    } else {
                        null
                    }
                QueueSnapshot(entries, order)
            }
        }

    suspend fun modes(): QueueModes {
        val row = dao.cursor() ?: return QueueModes(shuffle = false, repeat = 0)
        return QueueModes(row.shuffleEnabled, row.repeatMode)
    }

    suspend fun setShuffle(
        enabled: Boolean,
        order: LongArray?,
    ) {
        val seed = if (enabled && order != null) System.nanoTime() else 0L
        dao.clearShuffleOrder()
        if (enabled && order != null) {
            dao.insertShuffleOrder(order.mapIndexed { index, flat -> ShuffleOrder(index.toLong(), flat) })
        }
        dao.setCursor(cursorRow().copy(shuffleEnabled = enabled, shuffleSeed = seed))
        cachedShuffleSeed = 0
        cachedShuffleOrder = null
    }

    suspend fun setRepeat(mode: Int) = dao.setCursor(cursorRow().copy(repeatMode = mode))

    private suspend fun loadShuffleOrder(
        seed: Long,
        size: Long,
    ): LongArray? {
        val cached = cachedShuffleOrder
        if (seed == cachedShuffleSeed && cached != null && cached.size.toLong() == size) return cached
        val loaded = dao.shuffleOrder().map { it.flatPosition }.toLongArray()
        cachedShuffleSeed = seed
        cachedShuffleOrder = loaded.takeIf { it.size.toLong() == size }
        return cachedShuffleOrder
    }

    suspend fun itemAt(
        snapshot: QueueSnapshot,
        position: Long,
    ): SongListItem? {
        val (entry, offset) = snapshot.locate(position) ?: return null
        return rows(entry, entry.offset + offset, 1).firstOrNull()
    }

    suspend fun range(
        snapshot: QueueSnapshot,
        first: Long,
        last: Long,
    ): List<QueueWindowItem> {
        if (snapshot.shuffled) {
            val items = mutableListOf<QueueWindowItem>()
            for (position in first..last) {
                val (entry, offset) = snapshot.locate(position) ?: continue
                rows(entry, entry.offset + offset, 1).firstOrNull()?.let { items += QueueWindowItem(position, it) }
            }
            return items
        }
        val items = mutableListOf<QueueWindowItem>()
        var start = 0L
        for (resolved in snapshot.entries) {
            val end = start + resolved.length - 1
            val entryStart = start
            start += resolved.length
            if (resolved.length == 0L || end < first || entryStart > last) continue
            val from = maxOf(first, entryStart)
            val to = minOf(last, end)
            val offset = resolved.entry.offset + (from - entryStart)
            rows(resolved.entry, offset, (to - from + 1).toInt())
                .forEachIndexed { index, item -> items += QueueWindowItem(from + index, item) }
        }
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

    suspend fun removeAt(
        snapshot: QueueSnapshot,
        position: Long,
    ): QueueSnapshot {
        val order = snapshot.shuffleOrder
        if (order == null) {
            write(removeEntry(snapshot.entries, position).map { it.entry })
            return snapshot()
        }
        val flat = order.getOrNull(position.toInt()) ?: return snapshot
        write(removeEntry(snapshot.entries, flat).map { it.entry })
        val reordered = order.toMutableList().apply { removeAt(position.toInt()) }
        for (index in reordered.indices) if (reordered[index] > flat) reordered[index] -= 1
        setShuffle(true, reordered.toLongArray())
        return snapshot()
    }

    suspend fun move(
        snapshot: QueueSnapshot,
        from: Long,
        to: Long,
    ): Boolean {
        val order = snapshot.shuffleOrder
        if (order != null) {
            val reordered = order.toMutableList()
            val moved = reordered.removeAt(from.toInt())
            reordered.add(to.coerceIn(0L, reordered.size.toLong()).toInt(), moved)
            setShuffle(true, reordered.toLongArray())
            return true
        }
        val located = snapshot.locate(from) ?: return false
        val song = itemAt(snapshot, from) ?: return false
        val removed = removeEntry(snapshot.entries, from)
        val moved = songEntry(located.first.sourceId, song.song.id)
        write(insertEntry(removed, to.coerceIn(0, removed.sumOf { it.length }), moved))
        return true
    }

    suspend fun cursor(): Long = dao.cursor()?.queuePosition ?: 0

    suspend fun setCursor(position: Long) = dao.setCursor(cursorRow().copy(queuePosition = position))

    private suspend fun cursorRow(): PlaybackCursor = dao.cursor() ?: PlaybackCursor(queuePosition = 0)

    private suspend fun write(entries: List<QueueEntry>) {
        db.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                dao.clear()
                dao.insert(entries.mapIndexed { index, entry -> entry.copy(id = 0, position = index.toLong()) })
            }
        }
    }

    private suspend fun rows(
        entry: QueueEntry,
        offset: Long,
        limit: Int,
    ): List<SongListItem> =
        when (entry.kind) {
            QueueKind.Playlist -> dao.playlistSongs(entry.sourceId, entry.refId, offset, limit)
            QueueKind.Album -> dao.albumSongs(entry.sourceId, entry.refId, offset, limit)
            QueueKind.Songs -> dao.songs(entry.sourceId, offset, limit)
            QueueKind.Song -> dao.song(entry.sourceId, entry.refId, offset, limit)
        }

    private suspend fun QueueEntry.resolvedLength(): Long {
        val total =
            when (kind) {
                QueueKind.Song -> dao.songLength(sourceId, refId)
                QueueKind.Playlist -> dao.playlistLength(sourceId, refId)
                QueueKind.Album -> dao.albumLength(sourceId, refId)
                QueueKind.Songs -> dao.songsLength(sourceId)
            }
        val available = (total - offset).coerceAtLeast(0)
        return count?.coerceAtMost(available) ?: available
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
        newEntry: QueueEntry,
    ): List<QueueEntry> {
        val result = mutableListOf<QueueEntry>()
        var start = 0L
        var placed = false
        for (resolved in entries) {
            val end = start + resolved.length - 1
            when {
                !placed && index <= start -> {
                    result += newEntry
                    result += resolved.entry
                    placed = true
                }

                !placed && index in start..end -> {
                    val cut = index - start
                    val offset = resolved.entry.offset
                    if (cut > 0) result += resolved.entry.range(offset, offset + cut - 1)
                    result += newEntry
                    if (cut < resolved.length) result += resolved.entry.range(offset + cut, offset + resolved.length - 1)
                    placed = true
                }

                else -> {
                    result += resolved.entry
                }
            }
            start += resolved.length
        }
        if (!placed) result += newEntry
        return result
    }
}
