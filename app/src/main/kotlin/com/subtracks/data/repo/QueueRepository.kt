package com.subtracks.data.repo

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

    suspend fun replace(entries: List<QueueEntry>) {
        db.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                dao.clear()
                dao.insert(entries)
                dao.setCursor(PlaybackCursor(queuePosition = 0))
            }
        }
    }

    suspend fun snapshot(): QueueSnapshot = QueueSnapshot(dao.entries().map { ResolvedQueueEntry(it, it.resolvedLength()) })

    suspend fun itemAt(
        snapshot: QueueSnapshot,
        position: Long,
    ): SongListItem? {
        val (entry, offset) = snapshot.locate(position) ?: return null
        return itemWithin(entry, offset)
    }

    suspend fun window(
        snapshot: QueueSnapshot,
        center: Long,
        radius: Long,
    ): List<QueueWindowItem> {
        if (snapshot.size == 0L) return emptyList()
        val first = (center - radius).coerceAtLeast(0)
        val last = (center + radius).coerceAtMost(snapshot.size - 1)
        return (first..last).mapNotNull { position ->
            itemAt(snapshot, position)?.let { QueueWindowItem(position, it) }
        }
    }

    suspend fun cursor(): Long = dao.cursor()?.queuePosition ?: 0

    suspend fun setCursor(position: Long) = dao.setCursor(PlaybackCursor(queuePosition = position))

    private suspend fun itemWithin(
        entry: QueueEntry,
        offset: Long,
    ): SongListItem? {
        val index = entry.offset + offset
        return when (entry.kind) {
            QueueKind.Playlist -> dao.playlistSongAt(entry.sourceId, entry.refId, index)
            QueueKind.Album -> dao.albumSongAt(entry.sourceId, entry.refId, index)
            QueueKind.Song -> if (offset == 0L) dao.song(entry.sourceId, entry.refId) else null
        }
    }

    private suspend fun QueueEntry.resolvedLength(): Long {
        val total =
            when (kind) {
                QueueKind.Song -> dao.songLength(sourceId, refId)
                QueueKind.Playlist -> dao.playlistLength(sourceId, refId)
                QueueKind.Album -> dao.albumLength(sourceId, refId)
            }
        val available = (total - offset).coerceAtLeast(0)
        return count?.coerceAtMost(available) ?: available
    }
}
