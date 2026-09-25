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
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
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
        _status.value = SyncStatus.Running
        _status.value =
            try {
                val source = sourceRepository.activeMusicSource() ?: error("No server configured")
                SyncService(db, source).sync()
                SyncStatus.Success
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                Log.w(TAG, "Sync failed", failure)
                SyncStatus.Failed(failure.message ?: "Sync failed")
            }
        queueRepository.invalidateLibraryCache()
    }

    private companion object {
        const val TAG = "SubtracksSync"
    }
}
