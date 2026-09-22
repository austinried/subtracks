package com.subtracks.data.repo

import androidx.paging.PagingSource
import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.PlaybackCursor
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.SongListItem

data class ResolvedQueueEntry(
    val entry: QueueEntry,
    val length: Long,
)

data class QueueSnapshot(
    val entries: List<ResolvedQueueEntry>,
) {
    val size: Long = entries.sumOf { it.length }

    fun locate(position: Long): Pair<QueueEntry, Long>? {
        var remaining = position
        for (resolved in entries) {
            if (remaining < resolved.length) return resolved.entry to remaining
            remaining -= resolved.length
        }
        return null
    }
}

data class QueueWindowItem(
    val position: Long,
    val item: SongListItem,
)

class QueueRepository(
    private val db: SubtracksDatabase,
) {
    private val dao get() = db.queueDao()

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

    suspend fun replace(entries: List<QueueEntry>) {
        write(entries)
        dao.setCursor(PlaybackCursor(queuePosition = 0))
    }

    suspend fun snapshot(): QueueSnapshot = QueueSnapshot(dao.entries().map { ResolvedQueueEntry(it, it.resolvedLength()) })

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
    ) = write(removeEntry(snapshot.entries, position))

    suspend fun move(
        from: Long,
        to: Long,
    ) {
        val before = snapshot()
        val located = before.locate(from) ?: return
        val song = itemAt(before, from) ?: return
        write(removeEntry(before.entries, from))
        val after = snapshot()
        write(insertEntry(after.entries, to.coerceIn(0, after.size), songEntry(located.first.sourceId, song.song.id)))
    }

    fun pagingSource(): PagingSource<Long, QueueWindowItem> = QueuePagingSource(this)

    suspend fun cursor(): Long = dao.cursor()?.queuePosition ?: 0

    suspend fun setCursor(position: Long) = dao.setCursor(PlaybackCursor(queuePosition = position))

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
    ): List<QueueEntry> {
        val result = mutableListOf<QueueEntry>()
        var start = 0L
        for (resolved in entries) {
            val end = start + resolved.length - 1
            if (index in start..end) {
                val offset = resolved.entry.offset + (index - start)
                val first = resolved.entry.offset
                val last = resolved.entry.offset + resolved.length - 1
                if (offset > first) result += resolved.entry.range(first, offset - 1)
                if (offset < last) result += resolved.entry.range(offset + 1, last)
            } else {
                result += resolved.entry
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
                    val offset = resolved.entry.offset + (index - start)
                    val first = resolved.entry.offset
                    val last = resolved.entry.offset + resolved.length - 1
                    if (offset > first) result += resolved.entry.range(first, offset - 1)
                    result += newEntry
                    if (offset <= last) result += resolved.entry.range(offset, last)
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
