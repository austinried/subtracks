package com.subtracks.data.repo

import android.net.Uri
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.ArtworkFetcher
import com.subtracks.data.download.ArtworkStore
import com.subtracks.data.download.DownloadEngine
import com.subtracks.data.download.EngineDownload
import com.subtracks.data.download.EngineRequest
import com.subtracks.data.download.EngineStatus
import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.DownloadList
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.ListDownloadStatus
import com.subtracks.data.model.SongDownload
import com.subtracks.data.model.coverArtKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

class DownloadRepository(
    private val db: SubtracksDatabase,
    private val sourceRepository: SourceRepository,
    private val engine: DownloadEngine,
    private val downloadsDir: File,
    private val artworkStore: ArtworkStore,
    private val artworkFetcher: ArtworkFetcher,
    private val showMessage: (String) -> Unit = {},
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val mutex = Mutex()
    private val loaded = CompletableDeferred<Unit>()
    private var pollJob: Job? = null

    @Volatile
    private var started = false

    private val statesFlow = MutableStateFlow<Map<String, SongDownload>>(emptyMap())

    fun start() {
        if (started) return
        started = true
        downloadsDir.mkdirs()
        File(downloadsDir, NO_MEDIA).createNewFile()
        scope.launch {
            downloadStates().collect { rows ->
                statesFlow.value = rows
                loaded.complete(Unit)
            }
        }
        scope.launch {
            sourceRepository.activeSourceId().distinctUntilChanged().collect { reconcile() }
        }
    }

    fun close() {
        scope.cancel()
    }

    fun states(): StateFlow<Map<String, SongDownload>> = statesFlow

    suspend fun awaitLoaded() {
        if (started) loaded.await()
    }

    fun localUri(songId: String): String? =
        statesFlow.value[songId]
            ?.takeIf { it.status == DownloadStatus.Completed }
            ?.let { file(it.sourceId, it.songId) }
            ?.takeIf { it.exists() }
            ?.let { Uri.fromFile(it).toString() }

    suspend fun download(
        sourceId: Long,
        songId: String,
    ) = withContext(dispatcher) {
        if (sourceRepository.activeSourceIdOnce() != sourceId) {
            showMessage(NOT_ACTIVE)
            return@withContext
        }
        if (sourceRepository.downloadUri(sourceId, songId) == null) {
            showMessage(NO_ADDRESS)
            return@withContext
        }
        val promoted =
            mutex.withLock {
                if (!queueIfAbsentLocked(sourceId, songId)) return@withContext
                promote()
            }
        // The media is already with the platform, so this only has to happen while we are still
        // in the foreground: artwork is fetched by us, and it must not hold up the download.
        fetchArtworkSafely(sourceId, promoted.map { it.songId })
    }

    private suspend fun queueIfAbsentLocked(
        sourceId: Long,
        songId: String,
    ): Boolean {
        if (alreadyDownloadingOrDownloaded(sourceId, songId)) return false
        val existing = db.downloadDao().find(sourceId, songId)
        existing?.engineId?.let { engine.cancel(listOf(it)) }
        dir(sourceId).mkdirs()
        file(sourceId, songId).delete()
        db.downloadDao().upsert(SongDownload(sourceId = sourceId, songId = songId, status = DownloadStatus.Queued))
        ensurePolling()
        return true
    }

    /**
     * Hands the platform as many waiting songs as it can run, oldest first, so the queue it holds
     * mirrors the list on screen rather than the whole library. Everything else waits as a row
     * without an engine id, and the next tick promotes it.
     */
    private suspend fun promote(): List<SongDownload> {
        val rows = db.downloadDao().all()
        val free = IN_FLIGHT_LIMIT - rows.count { it.status.isActive && it.engineId != null }
        if (free <= 0) return emptyList()
        val promoted = ArrayList<SongDownload>()
        rows
            .filter { it.status == DownloadStatus.Queued && it.engineId == null }
            .take(free)
            .forEach { row ->
                val url = sourceRepository.downloadUri(row.sourceId, row.songId) ?: return@forEach
                val title = db.libraryDao().songOnce(row.sourceId, row.songId)?.title ?: row.songId
                val engineId =
                    try {
                        engine.enqueue(
                            EngineRequest(uri = url, path = enginePath(row.sourceId, row.songId), title = title),
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // Leave it failed rather than waiting, or the next tick would retry it forever.
                        markFailed(row, STOPPED)
                        return@forEach
                    }
                db.downloadDao().upsert(row.copy(engineId = engineId))
                promoted += row
            }
        return promoted
    }

    suspend fun remove(
        sourceId: Long,
        songId: String,
    ) = withContext(dispatcher) {
        mutex.withLock {
            if (!removeRow(sourceId, songId)) return@withLock
            sweep(db.downloadDao().all())
        }
    }

    fun status(
        sourceId: Long,
        list: DownloadList,
        refId: String,
    ): Flow<ListDownloadStatus> =
        when (list) {
            DownloadList.Album -> db.downloadDao().albumStatus(sourceId, refId)
            DownloadList.Playlist -> db.downloadDao().playlistStatus(sourceId, refId)
            DownloadList.Artist -> db.downloadDao().artistStatus(sourceId, refId)
        }

    suspend fun downloadAll(
        sourceId: Long,
        list: DownloadList,
        refId: String,
    ) = withContext(dispatcher) {
        if (sourceRepository.activeSourceIdOnce() != sourceId) {
            showMessage(NOT_ACTIVE)
            return@withContext
        }
        val songIds = songIds(sourceId, list, refId)
        if (songIds.isEmpty()) {
            showMessage(EMPTY_LIST)
            return@withContext
        }
        if (sourceRepository.downloadUri(sourceId, songIds.first()) == null) {
            showMessage(NO_ADDRESS)
            return@withContext
        }
        // Under one lock, so a cancel that lands mid-loop waits for the queue to be complete and
        // then removes all of it, rather than missing the songs enqueued after it looked.
        // ponytail: the lock is held for the whole insert pass, so cancel latency scales with the
        // list length; the promote below hands over at most IN_FLIGHT_LIMIT, which is what keeps
        // the platform's queue (and our poll writes) bounded.
        val promoted =
            mutex.withLock {
                songIds.forEach { songId ->
                    try {
                        queueIfAbsentLocked(sourceId, songId)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // One song failing to queue must not abandon the rest of the list.
                    }
                }
                promote()
            }
        fetchArtworkSafely(sourceId, promoted.map { it.songId })
    }

    suspend fun applyAction(
        sourceId: Long,
        list: DownloadList,
        refId: String,
        action: BulkDownloadAction,
    ) = when (action) {
        BulkDownloadAction.Download -> downloadAll(sourceId, list, refId)
        BulkDownloadAction.Cancel -> cancelAll(sourceId, list, refId)
        BulkDownloadAction.Delete -> deleteAll(sourceId, list, refId)
    }

    suspend fun downloadedBytes(
        sourceId: Long,
        list: DownloadList,
        refId: String,
    ): Long =
        withContext(dispatcher) {
            val songIds = songIds(sourceId, list, refId).toHashSet()
            db
                .downloadDao()
                .all()
                .filter { it.sourceId == sourceId && it.songId in songIds && it.status == DownloadStatus.Completed }
                .sumOf { file(it.sourceId, it.songId).length() }
        }

    suspend fun cancelAll(
        sourceId: Long,
        list: DownloadList,
        refId: String,
    ) = withContext(dispatcher) {
        mutex.withLock { removeListRows(sourceId, list, refId) { it.status.isActive } }
    }

    suspend fun deleteAll(
        sourceId: Long,
        list: DownloadList,
        refId: String,
    ) = withContext(dispatcher) {
        mutex.withLock { removeListRows(sourceId, list, refId) { it.status == DownloadStatus.Completed } }
    }

    /**
     * One binder call for the platform, one delete per chunk and one sweep, rather than a round of
     * each per row: a playlist of thousands took seconds to cancel and left the button stale.
     */
    private suspend fun removeListRows(
        sourceId: Long,
        list: DownloadList,
        refId: String,
        selecting: (SongDownload) -> Boolean,
    ) {
        val rows = rowsFor(sourceId, list, refId).filter(selecting)
        if (rows.isEmpty()) return
        engine.cancel(rows.mapNotNull { it.engineId })
        rows.forEach { file(sourceId, it.songId).delete() }
        rows.map { it.songId }.chunked(DELETE_CHUNK).forEach { db.downloadDao().deleteSongs(sourceId, it) }
        sweep(db.downloadDao().all())
    }

    suspend fun removeSource(sourceId: Long) =
        withContext(dispatcher) {
            mutex.withLock {
                db
                    .downloadDao()
                    .all()
                    .filter { it.sourceId == sourceId }
                    .mapNotNull { it.engineId }
                    .let(engine::cancel)
                db.downloadDao().deleteSource(sourceId)
                dir(sourceId).deleteRecursively()
            }
        }

    suspend fun reconcile() =
        withContext(dispatcher) {
            val promoted =
                mutex.withLock {
                    val rows = db.downloadDao().all()
                    reconcileRows(rows)
                    sweep(rows)
                    ensurePolling()
                    promote()
                }
            warmArtwork(promoted)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun downloadStates(): Flow<Map<String, SongDownload>> =
        sourceRepository
            .activeSourceId()
            .flatMapLatest { sourceId -> if (sourceId == null) flowOf(emptyList()) else db.downloadDao().downloads(sourceId) }
            .map { rows -> rows.associateBy { it.songId } }

    private suspend fun alreadyDownloadingOrDownloaded(
        sourceId: Long,
        songId: String,
    ): Boolean =
        db
            .downloadDao()
            .find(sourceId, songId)
            ?.status
            ?.let { it != DownloadStatus.Failed } == true

    private suspend fun fetchArtwork(
        sourceId: Long,
        songId: String,
    ) {
        val song = db.libraryDao().songOnce(sourceId, songId)
        val coverArt =
            listOfNotNull(
                song?.albumId?.let { db.libraryDao().albumOnce(sourceId, it)?.coverArt },
                song?.artistId?.let { db.libraryDao().artistOnce(sourceId, it)?.coverArt },
            ).distinct()
        coverArt.forEach { cover ->
            storeArtwork(sourceId, cover, thumbnail = false)
            storeArtwork(sourceId, cover, thumbnail = true)
        }
    }

    private suspend fun storeArtwork(
        sourceId: Long,
        coverArt: String,
        thumbnail: Boolean,
    ) {
        val cacheKey = coverArtKey(sourceId, coverArt, thumbnail)
        if (artworkStore.file(sourceId, cacheKey).exists()) return
        val url = sourceRepository.networkCoverArt(sourceId, coverArt, thumbnail) ?: return
        val bytes =
            try {
                artworkFetcher.fetch(url)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                return
            }
        if (bytes.isEmpty()) return
        artworkStore.write(sourceId, cacheKey, bytes)
    }

    /**
     * The artwork keys every download row still needs, per source. A null entry means the source's
     * rows could not all be resolved against the library, in which case nothing is swept for it:
     * pruning an album or artist would otherwise delete artwork a download still holds.
     */
    private suspend fun artworkToKeep(): Map<Long, Set<String>?> =
        db
            .downloadDao()
            .artwork()
            .groupBy { it.sourceId }
            .mapValues { (sourceId, rows) ->
                if (rows.any { it.hasMissingLibraryRow }) return@mapValues null
                rows
                    .flatMap { row -> listOfNotNull(row.albumCoverArt, row.artistCoverArt) }
                    .flatMap { cover -> listOf(false, true).map { thumbnail -> coverArtKey(sourceId, cover, thumbnail) } }
                    .toSet()
            }

    private suspend fun recover(row: SongDownload) {
        when (row.status) {
            DownloadStatus.Completed -> {
                if (!file(row.sourceId, row.songId).exists()) markFailed(row, MISSING_FILE)
            }

            // Waiting rows have no engine id by design; the next promote hands them over.
            DownloadStatus.Queued -> {
                if (row.engineId != null) db.downloadDao().upsert(row.copy(engineId = null))
            }

            DownloadStatus.Running -> {
                markFailed(row, STOPPED)
            }

            DownloadStatus.Failed -> {
                Unit
            }
        }
    }

    private suspend fun reconcileRows(rows: List<SongDownload>) {
        rows.forEach { row ->
            val state = row.engineId?.let(engine::download)
            if (state == null) recover(row) else apply(row, state)
        }
    }

    private suspend fun warmArtwork(promoted: List<SongDownload>) {
        promoted.groupBy { it.sourceId }.forEach { (sourceId, rows) ->
            fetchArtworkSafely(sourceId, rows.map { it.songId })
        }
    }

    private suspend fun removeRow(
        sourceId: Long,
        songId: String,
    ): Boolean {
        val row = db.downloadDao().find(sourceId, songId) ?: return false
        row.engineId?.let { engine.cancel(listOf(it)) }
        file(sourceId, songId).delete()
        db.downloadDao().delete(sourceId, songId)
        return true
    }

    private suspend fun rowsFor(
        sourceId: Long,
        list: DownloadList,
        refId: String,
    ): List<SongDownload> {
        val ids = songIds(sourceId, list, refId).toHashSet()
        if (ids.isEmpty()) return emptyList()
        return db.downloadDao().all().filter { it.sourceId == sourceId && it.songId in ids }
    }

    private suspend fun songIds(
        sourceId: Long,
        list: DownloadList,
        refId: String,
    ): List<String> =
        when (list) {
            DownloadList.Album -> db.queueDao().albumSongIds(sourceId, refId)
            DownloadList.Playlist -> db.queueDao().playlistSongIds(sourceId, refId)
            DownloadList.Artist -> db.queueDao().artistSongIds(sourceId, refId)
        }

    private suspend fun fetchArtworkSafely(
        sourceId: Long,
        songIds: List<String>,
    ) {
        try {
            withTimeoutOrNull(ART_TIMEOUT_MS) {
                songIds.forEach { songId ->
                    // A cancel between the queue and this pass takes the row away; nothing to warm.
                    if (db.downloadDao().find(sourceId, songId) != null) fetchArtwork(sourceId, songId)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Artwork is best effort and must never disturb a download that is already running.
        }
    }

    private suspend fun apply(
        row: SongDownload,
        state: EngineDownload,
    ) {
        val status =
            when (state.status) {
                EngineStatus.Pending -> DownloadStatus.Queued
                EngineStatus.Running -> DownloadStatus.Running
                EngineStatus.Completed -> if (file(row.sourceId, row.songId).exists()) DownloadStatus.Completed else DownloadStatus.Failed
                EngineStatus.Failed -> DownloadStatus.Failed
            }
        if (status == DownloadStatus.Failed) file(row.sourceId, row.songId).delete()
        db.downloadDao().upsert(
            row.copy(
                status = status,
                bytes = state.bytes,
                total = state.total,
                error = state.error ?: if (status == DownloadStatus.Failed) "Download failed" else null,
            ),
        )
    }

    private suspend fun markFailed(
        row: SongDownload,
        message: String,
    ) {
        row.engineId?.let { engine.cancel(listOf(it)) }
        file(row.sourceId, row.songId).delete()
        db.downloadDao().upsert(row.copy(status = DownloadStatus.Failed, engineId = null, error = message))
    }

    private fun ensurePolling() {
        if (pollJob?.isActive == true) return
        pollJob =
            scope.launch {
                while (true) {
                    delay(POLL_MS)
                    val promoted =
                        mutex.withLock {
                            val sourceId = sourceRepository.activeSourceIdOnce()
                            val active = db.downloadDao().all().filter { it.status.isActive && it.sourceId == sourceId }
                            if (active.isEmpty()) return@launch
                            reconcileRows(active)
                            promote()
                        }
                    // Fetching artwork here would hold up the next tick, and with it the progress
                    // the queue is mirrored from.
                    promoted.groupBy { it.sourceId }.forEach { (sourceId, rows) ->
                        scope.launch { fetchArtworkSafely(sourceId, rows.map { it.songId }) }
                    }
                }
            }
    }

    private suspend fun sweep(rows: List<SongDownload>) {
        val knownSongs = rows.map { it.sourceId to it.songId }.toHashSet()
        val sourcesWithRows = rows.mapTo(HashSet()) { it.sourceId }
        val knownArt = artworkToKeep()
        downloadsDir.listFiles().orEmpty().filter { it.isDirectory }.forEach { sourceDir ->
            val sourceId = sourceDir.name.toLongOrNull() ?: return@forEach
            when {
                sourceId !in sourcesWithRows -> artworkStore.sweep(sourceId, emptySet())
                else -> knownArt[sourceId]?.let { artworkStore.sweep(sourceId, it) }
            }
            sourceDir.listFiles().orEmpty().filter { it.isFile }.forEach { file ->
                if ((sourceId to file.name) !in knownSongs) file.delete()
            }
        }
    }

    private fun dir(sourceId: Long): File = downloadsDir.resolve(sourceId.toString())

    private fun file(
        sourceId: Long,
        songId: String,
    ): File = dir(sourceId).resolve(songId)

    private fun enginePath(
        sourceId: Long,
        songId: String,
    ): String = "$ENGINE_DIR/$sourceId/$songId"

    private val DownloadStatus.isActive: Boolean
        get() = this == DownloadStatus.Queued || this == DownloadStatus.Running

    private companion object {
        const val ENGINE_DIR = "downloads"
        const val NO_MEDIA = ".nomedia"
        const val POLL_MS = 1_000L
        const val IN_FLIGHT_LIMIT = 8
        const val DELETE_CHUNK = 900
        const val MISSING_FILE = "The downloaded file is missing"
        const val NO_ADDRESS = "Can't download: the server address is unavailable"
        const val EMPTY_LIST = "Nothing left to download from this list"
        const val STOPPED = "The download stopped unexpectedly"
        const val NOT_ACTIVE = "Can't download from a source that isn't active"
        const val ART_TIMEOUT_MS = 60_000L
    }
}
