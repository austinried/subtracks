package com.subtracks.data.repo

import android.net.Uri
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.download.DownloadEngine
import com.subtracks.data.download.EngineDownload
import com.subtracks.data.download.EngineRequest
import com.subtracks.data.download.EngineStatus
import com.subtracks.data.model.DownloadStatus
import com.subtracks.data.model.SongDownload
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import java.io.File

class DownloadRepository(
    private val db: SubtracksDatabase,
    private val sourceRepository: SourceRepository,
    private val engine: DownloadEngine,
    private val downloadsDir: File,
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
        mutex.withLock {
            if (sourceRepository.activeSourceIdOnce() != sourceId) {
                showMessage("Can't download from a source that isn't active")
                return@withLock
            }
            val existing = db.downloadDao().find(sourceId, songId)
            if (existing != null && existing.status != DownloadStatus.Failed) return@withLock
            existing?.engineId?.let(engine::cancel)
            val url = sourceRepository.downloadUri(sourceId, songId)
            if (url == null) {
                showMessage(NO_ADDRESS)
                return@withLock
            }
            enqueue(sourceId, songId, url)
        }
    }

    suspend fun remove(
        sourceId: Long,
        songId: String,
    ) = withContext(dispatcher) {
        mutex.withLock {
            val row = db.downloadDao().find(sourceId, songId) ?: return@withLock
            row.engineId?.let(engine::cancel)
            file(sourceId, songId).delete()
            db.downloadDao().delete(sourceId, songId)
        }
    }

    suspend fun removeSource(sourceId: Long) =
        withContext(dispatcher) {
            mutex.withLock {
                db
                    .downloadDao()
                    .all()
                    .filter { it.sourceId == sourceId }
                    .forEach { row -> row.engineId?.let(engine::cancel) }
                db.downloadDao().deleteSource(sourceId)
                dir(sourceId).deleteRecursively()
            }
        }

    suspend fun reconcile() =
        withContext(dispatcher) {
            mutex.withLock {
                db.downloadDao().all().forEach { row ->
                    val state = row.engineId?.let(engine::download)
                    if (state == null) recover(row) else apply(row, state)
                }
                sweep(db.downloadDao().all())
                ensurePolling()
            }
        }

    private fun downloadStates(): Flow<Map<String, SongDownload>> =
        sourceRepository
            .activeSourceId()
            .flatMapLatest { sourceId -> if (sourceId == null) flowOf(emptyList()) else db.downloadDao().downloads(sourceId) }
            .map { rows -> rows.associateBy { it.songId } }

    private suspend fun enqueue(
        sourceId: Long,
        songId: String,
        url: String,
    ) {
        dir(sourceId).mkdirs()
        file(sourceId, songId).delete()
        val title = db.libraryDao().songOnce(sourceId, songId)?.title ?: songId
        val engineId =
            engine.enqueue(
                EngineRequest(uri = url, path = enginePath(sourceId, songId), title = title),
            )
        db.downloadDao().upsert(
            SongDownload(sourceId = sourceId, songId = songId, status = DownloadStatus.Queued, engineId = engineId),
        )
        ensurePolling()
    }

    private suspend fun recover(row: SongDownload) {
        when (row.status) {
            DownloadStatus.Completed -> {
                if (!file(row.sourceId, row.songId).exists()) markFailed(row, MISSING_FILE)
            }

            DownloadStatus.Queued -> {
                if (sourceRepository.activeSourceIdOnce() != row.sourceId) return
                val url = sourceRepository.downloadUri(row.sourceId, row.songId) ?: return
                enqueue(row.sourceId, row.songId, url)
            }

            DownloadStatus.Running -> {
                markFailed(row, STOPPED)
            }

            DownloadStatus.Failed -> {
                Unit
            }
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
        row.engineId?.let(engine::cancel)
        file(row.sourceId, row.songId).delete()
        db.downloadDao().upsert(row.copy(status = DownloadStatus.Failed, engineId = null, error = message))
    }

    private fun ensurePolling() {
        if (pollJob?.isActive == true) return
        pollJob =
            scope.launch {
                while (true) {
                    delay(POLL_MS)
                    mutex.withLock {
                        val sourceId = sourceRepository.activeSourceIdOnce()
                        val active = db.downloadDao().all().filter { it.status.isActive && it.sourceId == sourceId }
                        if (active.isEmpty()) return@launch
                        active.forEach { row ->
                            val state = row.engineId?.let(engine::download)
                            if (state == null) recover(row) else apply(row, state)
                        }
                    }
                }
            }
    }

    private fun sweep(rows: List<SongDownload>) {
        val known = rows.map { it.sourceId to it.songId }.toHashSet()
        // Artwork (a later block) lives in subdirectories and is swept separately.
        downloadsDir.listFiles().orEmpty().filter { it.isDirectory }.forEach { sourceDir ->
            val sourceId = sourceDir.name.toLongOrNull() ?: return@forEach
            sourceDir.listFiles().orEmpty().filter { it.isFile }.forEach { file ->
                if ((sourceId to file.name) !in known) file.delete()
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
        const val MISSING_FILE = "The downloaded file is missing"
        const val NO_ADDRESS = "Can't download: the server address is unavailable"
        const val STOPPED = "The download stopped unexpectedly"
    }
}
