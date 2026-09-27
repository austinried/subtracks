package com.subtracks.data.repo

import androidx.room3.deferredTransaction
import androidx.room3.immediateTransaction
import androidx.room3.useReaderConnection
import androidx.room3.useWriterConnection
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.model.PlaybackCursor
import com.subtracks.data.model.QueueEntry
import com.subtracks.data.model.QueueKind
import com.subtracks.data.model.SongListItem
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

private data class QueueRef(
    val kind: QueueKind,
    val sourceId: Long,
    val refId: String,
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

/**
 * A shuffled order over a fixed domain of [domain] tracks, plus the domain positions removed since
 * the order was created. The permutation stays fixed and removals are subtracted (sparsely, in
 * memory) rather than re-derived, so editing a shuffled queue keeps the remaining tracks in place.
 * Only the seed and domain are persisted; on restart the removals are gone, so a queue with edits
 * falls back to unshuffled rather than guessing.
 */
class ShuffleState(
    val seed: Long,
    val domain: Long,
    private val removedFlats: LongArray = LongArray(0),
    private val removedSlots: LongArray = LongArray(0),
) {
    fun flatFor(play: Long): Long {
        val domainFlat = Shuffle.toFlat(seed, domain, liveSlot(play))
        return domainFlat - removedFlats.count { it < domainFlat }
    }

    fun playFor(canonical: Long): Long {
        val slot = Shuffle.toSequence(seed, domain, liveFlat(canonical))
        return slot - removedSlots.count { it < slot }
    }

    fun domainFor(canonical: Long): Long = liveFlat(canonical)

    val liveSize: Long get() = domain - removedFlats.size.toLong()

    fun withRemoved(domainFlat: Long): ShuffleState =
        ShuffleState(
            seed,
            domain,
            (removedFlats + domainFlat).sortedArray(),
            (removedSlots + Shuffle.toSequence(seed, domain, domainFlat)).sortedArray(),
        )

    private fun liveSlot(play: Long): Long {
        var slot = play
        while (true) {
            val next = play + removedSlots.count { it <= slot }
            if (next <= slot) return slot
            slot = next
        }
    }

    private fun liveFlat(canonical: Long): Long {
        var flat = canonical
        while (true) {
            val next = canonical + removedFlats.count { it <= flat }
            if (next <= flat) return flat
            flat = next
        }
    }
}

data class QueueSnapshot(
    val entries: List<ResolvedQueueEntry>,
    val upNext: List<ResolvedQueueEntry> = emptyList(),
    val shuffle: ShuffleState? = null,
    val upNextAnchor: Long = 0,
    val version: Long = 0,
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
    val item: SongListItem,
    val upNext: Boolean = false,
)

data class Reanchored(
    val position: Long,
    val changed: Boolean,
)

class QueueRepository(
    private val db: SubtracksDatabase,
) {
    private val dao get() = db.queueDao()

    private val queueVersion = AtomicLong()

    @Volatile
    private var cachedFlatIds: List<String>? = null

    @Volatile
    private var cachedFlatVersion = -1L

    @Volatile
    private var shuffleRemoved: ShuffleState? = null

    private val lengthCache = ConcurrentHashMap<EntryLengthKey, Long>()

    private val keyCache = ConcurrentHashMap<QueueRef, KeyCursor>()

    internal var keysetSeeks = 0L
        private set

    internal var offsetSeeks = 0L
        private set

    private val cursorMutex = Mutex()

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

            // An explicit song entry in the middle of a queue is a reordered context track, so
            // label it by its album rather than losing the context name.
            QueueKind.Song -> {
                library
                    .song(sourceId, refId)
                    .first()
                    ?.album
                    ?.takeIf { it.isNotBlank() }
            }
        }
    }

    suspend fun replace(
        entries: List<QueueEntry>,
        shuffleSeed: Long? = null,
        upNext: List<QueueEntry> = emptyList(),
        upNextAnchor: Long = 0,
    ) {
        write(entries)
        writeUpNext(upNext)
        setShuffle(shuffleSeed)
        cursorMutex.withLock {
            dao.setCursor(cursorRow().copy(queuePosition = 0, positionMs = 0, upNextAnchor = upNextAnchor))
        }
    }

    suspend fun snapshot(): QueueSnapshot {
        val version = queueVersion.get()
        val loaded =
            db.useReaderConnection { transactor ->
                transactor.deferredTransaction {
                    val entries = dao.entries().map { ResolvedQueueEntry(it, lengthOf(it, version)) }
                    val upNext =
                        dao.upNextEntries().map {
                            val entry = it.toQueueEntry()
                            ResolvedQueueEntry(entry, lengthOf(entry, version))
                        }
                    val row = dao.cursor()
                    val size = entries.sumOf { it.length }
                    val state = shuffleRemoved
                    val shuffle =
                        if (row != null && row.shuffleEnabled && size > 0L) {
                            when {
                                state != null && state.seed == row.shuffleSeed && state.domain == row.shuffleSize &&
                                    state.liveSize == size -> state

                                state == null && row.shuffleSize == size -> ShuffleState(row.shuffleSeed, row.shuffleSize)

                                else -> null
                            }
                        } else {
                            null
                        }
                    LoadedSnapshot(entries, upNext, shuffle, row?.upNextAnchor ?: 0L)
                }
            }
        return QueueSnapshot(loaded.entries, loaded.upNext, loaded.shuffle, loaded.anchor, version)
    }

    suspend fun modes(): QueueModes {
        val row = dao.cursor() ?: return QueueModes(shuffle = false, repeat = 0)
        return QueueModes(row.shuffleEnabled, row.repeatMode)
    }

    suspend fun setShuffle(seed: Long?) {
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
        shuffleRemoved = seed?.let { ShuffleState(it, size) }
    }

    fun invalidateLibraryCache() {
        cachedFlatIds = null
        lengthCache.clear()
        keyCache.clear()
        shuffleRemoved = null
        queueVersion.incrementAndGet()
    }

    suspend fun setRepeat(mode: Int) = cursorMutex.withLock { dao.setCursor(cursorRow().copy(repeatMode = mode)) }

    suspend fun itemAt(
        snapshot: QueueSnapshot,
        position: Long,
    ): SongListItem? {
        val (entry, offset) = snapshot.locate(position) ?: return null
        return rows(entry, entry.offset + offset, 1).firstOrNull()
    }

    suspend fun combinedIndexOf(
        snapshot: QueueSnapshot,
        songId: String,
    ): Long? {
        val upIndex = idsFor(snapshot.upNext).indexOf(songId)
        if (upIndex >= 0) return snapshot.anchorPlay + 1 + upIndex
        return combinedContextIndexOf(snapshot, songId)
    }

    suspend fun combinedContextIndexOf(
        snapshot: QueueSnapshot,
        songId: String,
    ): Long? {
        val flat = flatIds(snapshot).indexOf(songId)
        if (flat < 0) return null
        return snapshot.playContext(flat.toLong())?.let { snapshot.combined(it) }
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

    suspend fun addUpNext(
        snapshot: QueueSnapshot,
        currentPosition: Long,
        entry: QueueEntry,
        playNext: Boolean,
    ): Long {
        val list = snapshot.upNext.map { it.entry }.toMutableList()
        val insertIndex =
            when {
                playNext && snapshot.isUpNext(currentPosition) -> {
                    entryIndexAfter(snapshot.upNext, snapshot.upNextIndex(currentPosition))
                }

                playNext -> {
                    0
                }

                else -> {
                    list.size
                }
            }.coerceIn(0, list.size)
        list.add(insertIndex, entry.copy(id = 0, position = 0))
        val anchor =
            if (snapshot.upNextSize == 0L) {
                val play = snapshot.anchorContextPlay(currentPosition)
                snapshot.flatContext(play) ?: 0L
            } else {
                snapshot.upNextAnchor
            }
        writeUpNext(list)
        cursorMutex.withLock { dao.setCursor(cursorRow().copy(upNextAnchor = anchor)) }
        if (snapshot.upNextSize == 0L) return currentPosition
        val before = snapshot.upNext.take(insertIndex).sumOf { it.length }
        return if (currentPosition >= snapshot.anchorPlay + 1 + before) currentPosition + 1 else currentPosition
    }

    suspend fun reanchorFor(
        snapshot: QueueSnapshot,
        target: Long,
    ): Reanchored {
        if (snapshot.upNextSize == 0L || snapshot.isUpNext(target)) return Reanchored(target, false)
        val play = snapshot.contextPlay(target)
        val flat = snapshot.flatContext(play) ?: return Reanchored(target, false)
        if (flat == snapshot.upNextAnchor) return Reanchored(target, false)
        cursorMutex.withLock { dao.setCursor(cursorRow().copy(upNextAnchor = flat)) }
        return Reanchored(play, true)
    }

    suspend fun setUpNextAnchor(anchor: Long) = cursorMutex.withLock { dao.setCursor(cursorRow().copy(upNextAnchor = anchor)) }

    suspend fun clearUpNext() {
        writeUpNext(emptyList())
    }

    suspend fun removeUpNextAt(
        snapshot: QueueSnapshot,
        index: Long,
    ) {
        if (index !in 0 until snapshot.upNextSize) return
        writeUpNext(compact(removeEntry(snapshot.upNext, index)).map { it.entry })
    }

    suspend fun removeAt(
        snapshot: QueueSnapshot,
        position: Long,
    ) {
        if (snapshot.isUpNext(position)) {
            removeUpNextAt(snapshot, snapshot.upNextIndex(position))
            return
        }
        val play = snapshot.contextPlay(position)
        if (snapshot.shuffled) {
            // Subtract the track from the fixed shuffle order instead of re-deriving it, so the
            // tracks that remain keep their positions.
            val order = snapshot.shuffle ?: return
            val flat = snapshot.flatContext(play) ?: return
            val predecessor = if (play <= 0L) -1L else snapshot.flatContext(play - 1L) ?: -1L
            shuffleRemoved = order.withRemoved(order.domainFor(flat))
            write(compact(removeEntry(snapshot.entries, flat)).map { it.entry })
            if (cursorRow().upNextAnchor == flat) {
                // The removed track carried the block; move the anchor to the track before it in
                // play order so the block stays in place instead of jumping to the canonical
                // predecessor, which in a shuffled queue is an unrelated position.
                setUpNextAnchor(if (predecessor > flat) predecessor - 1 else predecessor)
            } else {
                adjustAnchorOnRemove(flat)
            }
        } else {
            val removed = compact(removeEntry(snapshot.entries, play))
            write(removed.map { it.entry })
            adjustAnchorOnRemove(play)
        }
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

    suspend fun move(
        snapshot: QueueSnapshot,
        from: Long,
        to: Long,
    ): Boolean {
        val fromUpNext = snapshot.isUpNext(from)
        val toUpNext = snapshot.isUpNext(to)
        if (fromUpNext && toUpNext) {
            val song = itemAt(snapshot, from) ?: return false
            val sourceId = snapshot.locate(from)?.first?.sourceId ?: return false
            val removed = removeEntry(snapshot.upNext, snapshot.upNextIndex(from))
            val moved = ResolvedQueueEntry(songEntry(sourceId, song.song.id), 1L)
            writeUpNext(compact(insertEntry(removed, snapshot.upNextIndex(to), moved)).map { it.entry })
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
        val removed = removeEntry(snapshot.entries, playFrom)
        val moved = ResolvedQueueEntry(songEntry(located.first.sourceId, song.song.id), 1L)
        val insertAt = playTo.coerceIn(0, removed.sumOf { it.length })
        write(compact(insertEntry(removed, insertAt, moved)).map { it.entry })
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

    private suspend fun write(entries: List<QueueEntry>) {
        db.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                dao.clear()
                dao.insert(entries.mapIndexed { index, entry -> entry.copy(id = 0, position = index.toLong()) })
            }
        }
        queueVersion.incrementAndGet()
        cachedFlatIds = null
    }

    private suspend fun writeUpNext(entries: List<QueueEntry>) {
        db.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                dao.clearUpNext()
                dao.insertUpNext(
                    entries.mapIndexed { index, entry ->
                        UpNextEntry.from(entry).copy(id = 0, position = index.toLong())
                    },
                )
            }
        }
        queueVersion.incrementAndGet()
    }

    private suspend fun flatIds(snapshot: QueueSnapshot): List<String> {
        val cached = cachedFlatIds
        if (cached != null && cachedFlatVersion == snapshot.version && cached.size.toLong() == snapshot.contextSize) return cached
        val ids = idsFor(snapshot.entries)
        cachedFlatIds = ids
        cachedFlatVersion = snapshot.version
        return ids
    }

    private suspend fun idsFor(resolved: List<ResolvedQueueEntry>): List<String> {
        val ids = ArrayList<String>()
        for (entry in resolved) {
            val ref = entry.entry
            val entryIds =
                when (ref.kind) {
                    QueueKind.Song -> listOf(ref.refId)
                    QueueKind.Album -> dao.albumSongIds(ref.sourceId, ref.refId)
                    QueueKind.Playlist -> dao.playlistSongIds(ref.sourceId, ref.refId)
                }
            val from = ref.offset.toInt().coerceIn(0, entryIds.size)
            val to = (ref.offset + entry.length).toInt().coerceIn(from, entryIds.size)
            ids.addAll(entryIds.subList(from, to))
        }
        return ids
    }

    private suspend fun contextPlayRange(
        snapshot: QueueSnapshot,
        first: Long,
        last: Long,
    ): List<QueueWindowItem> {
        if (first > last) return emptyList()
        val items = mutableListOf<QueueWindowItem>()
        if (snapshot.shuffled) {
            val sourceId =
                snapshot.entries
                    .firstOrNull()
                    ?.entry
                    ?.sourceId ?: return emptyList()
            val ids = flatIds(snapshot)
            val requested = mutableListOf<Pair<Long, String>>()
            for (play in first..last) {
                val flat = snapshot.flatContext(play) ?: continue
                val id = ids.getOrNull(flat.toInt()) ?: continue
                requested += play to id
            }
            if (requested.isEmpty()) return emptyList()
            val byId = dao.songsByIds(sourceId, requested.map { it.second }.distinct()).associateBy { it.song.id }
            requested.forEach { (play, id) -> byId[id]?.let { items += QueueWindowItem(snapshot.combined(play), it) } }
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
            rows(resolved.entry, offset, (to - from + 1).toInt())
                .forEachIndexed { index, item -> items += QueueWindowItem(snapshot.combined(from + index), item) }
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
                items += QueueWindowItem(snapshot.anchorPlay + 1 + from + index, item, upNext = true)
            }
        }
        return items
    }

    private suspend fun rows(
        entry: QueueEntry,
        offset: Long,
        limit: Int,
    ): List<SongListItem> {
        if (entry.kind == QueueKind.Song) {
            offsetSeeks++
            return dao.song(entry.sourceId, entry.refId, offset, limit)
        }
        val ref = QueueRef(entry.kind, entry.sourceId, entry.refId)
        val anchored = keyCache[ref]?.let { keyedRows(entry, offset, limit, it) }
        if (anchored != null) {
            keysetSeeks++
            if (anchored.isNotEmpty()) remember(ref, offset, anchored)
            return anchored
        }
        offsetSeeks++
        val fresh =
            when (entry.kind) {
                QueueKind.Playlist -> dao.playlistSongs(entry.sourceId, entry.refId, offset, limit)
                QueueKind.Album -> dao.albumSongs(entry.sourceId, entry.refId, offset, limit)
                QueueKind.Song -> emptyList()
            }
        if (fresh.isNotEmpty()) remember(ref, offset, fresh)
        return fresh
    }

    private fun remember(
        ref: QueueRef,
        offset: Long,
        resolved: List<SongListItem>,
    ) {
        if (keyCache.size >= LENGTH_CACHE_LIMIT) keyCache.clear()
        keyCache[ref] = KeyCursor(offset + resolved.size - 1, orderKey(ref.kind, resolved.last()))
    }

    private fun orderKey(
        kind: QueueKind,
        row: SongListItem,
    ): OrderKey =
        when (kind) {
            QueueKind.Album -> OrderKey(row.song.id, disc = row.song.disc, track = row.song.track)
            QueueKind.Playlist -> OrderKey(row.song.id, position = row.position)
            QueueKind.Song -> OrderKey(row.song.id)
        }

    private suspend fun keyedRows(
        entry: QueueEntry,
        offset: Long,
        limit: Int,
        cursor: KeyCursor,
    ): List<SongListItem>? {
        if (!cursor.key.seekable(entry.kind)) return null
        val from = cursor.ordinal
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
    ): List<SongListItem> =
        when (entry.kind) {
            QueueKind.Playlist -> dao.playlistSongsFrom(entry.sourceId, entry.refId, key.position!!, skip, limit)
            QueueKind.Album -> dao.albumSongsFrom(entry.sourceId, entry.refId, key.disc!!, key.track!!, key.id, skip, limit)
            QueueKind.Song -> emptyList()
        }

    private suspend fun rowsBefore(
        entry: QueueEntry,
        key: OrderKey,
        skip: Long,
        limit: Int,
    ): List<SongListItem> =
        when (entry.kind) {
            QueueKind.Playlist -> dao.playlistSongsBefore(entry.sourceId, entry.refId, key.position!!, skip, limit)
            QueueKind.Album -> dao.albumSongsBefore(entry.sourceId, entry.refId, key.disc!!, key.track!!, key.id, skip, limit)
            QueueKind.Song -> emptyList()
        }

    private fun OrderKey.seekable(kind: QueueKind): Boolean =
        when (kind) {
            QueueKind.Playlist -> position != null
            QueueKind.Album -> disc != null && track != null
            QueueKind.Song -> false
        }

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
        val total =
            when (kind) {
                QueueKind.Song -> {
                    dao.songLength(sourceId, refId)
                }

                QueueKind.Playlist -> {
                    dao.playlistLength(sourceId, refId)
                }

                QueueKind.Album -> {
                    dao.albumLength(sourceId, refId)
                }
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
            entry.offset + length == next.entry.offset
}
