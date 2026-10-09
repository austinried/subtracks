package com.subtracks.data.sync

import com.subtracks.R
import com.subtracks.UiMessage
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.source.ServerActionSink
import com.subtracks.data.source.subsonic.SubsonicException
import com.subtracks.log.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface SyncStatus {
    data object Idle : SyncStatus

    data object Running : SyncStatus

    data object Success : SyncStatus

    data class Failed(
        val message: UiMessage,
    ) : SyncStatus
}

class SyncManager(
    private val db: SubtracksDatabase,
    private val sourceRepository: SourceRepository,
    private val queueRepository: QueueRepository,
    private val serverActions: ServerActionSink,
    private val showMessage: (UiMessage) -> Unit = {},
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val requests = Channel<Unit>(Channel.CONFLATED)
    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status

    init {
        scope.launch {
            for (ignored in requests) runSync()
        }
    }

    fun requestSync() {
        requests.trySend(Unit)
    }

    private suspend fun runSync() {
        if (sourceRepository.offline.value) {
            val offline = UiMessage(R.string.sync_offline)
            showMessage(offline)
            _status.value = SyncStatus.Failed(offline)
            return
        }
        _status.value = SyncStatus.Running
        val startedAt = System.nanoTime()
        val result =
            try {
                val source = sourceRepository.activeMusicSource() ?: throw NoServerException()
                Log.i("sync", "started (source ${source.id})")
                serverActions.flush()
                val summary = SyncService(db, source).sync()
                Log.i(
                    "sync",
                    "finished in ${elapsedMs(startedAt)}ms: " +
                        "${summary.artists} artists, ${summary.albums} albums (server reported ${summary.reportedAlbums}), " +
                        "${summary.songs} songs (server reported ${summary.reportedSongs}), " +
                        "${summary.playlists} playlists, ${summary.playlistSongs} playlist tracks, ${summary.pruned} pruned",
                )
                SyncStatus.Success
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                Log.w("sync", "failed after ${elapsedMs(startedAt)}ms", failure)
                val message =
                    when (failure) {
                        is NoServerException -> {
                            UiMessage(R.string.error_no_server)
                        }

                        else -> {
                            (failure as? SubsonicException)?.uiMessage
                                ?: UiMessage(R.string.sync_failed_detail, listOf(failure.message ?: ""))
                        }
                    }
                showMessage(message)
                SyncStatus.Failed(message)
            }
        queueRepository.invalidateLibraryCache()
        _status.value = result
    }

    private fun elapsedMs(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1_000_000
}

private class NoServerException : IllegalStateException()
