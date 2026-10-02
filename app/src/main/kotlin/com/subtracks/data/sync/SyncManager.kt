package com.subtracks.data.sync

import android.util.Log
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
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
        val message: String,
    ) : SyncStatus
}

class SyncManager(
    private val db: SubtracksDatabase,
    private val sourceRepository: SourceRepository,
    private val queueRepository: QueueRepository,
    private val showMessage: (String) -> Unit = {},
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
            showMessage("Offline mode is on")
            _status.value = SyncStatus.Failed("Offline mode is on")
            return
        }
        _status.value = SyncStatus.Running
        val result =
            try {
                val source = sourceRepository.activeMusicSource() ?: error("No server configured")
                SyncService(db, source).sync()
                SyncStatus.Success
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                Log.w(TAG, "Sync failed", failure)
                val message = failure.message ?: "unknown error"
                showMessage("Sync failed: $message")
                SyncStatus.Failed(message)
            }
        queueRepository.invalidateLibraryCache()
        _status.value = result
    }

    private companion object {
        const val TAG = "SubtracksSync"
    }
}
