package com.subtracks.data.sync

import android.util.Log
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

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
    private val syncLock = Mutex()
    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status

    fun requestSync() {
        if (!syncLock.tryLock()) return
        scope.launch {
            try {
                runSync()
            } finally {
                syncLock.unlock()
            }
        }
    }

    private suspend fun runSync() {
        _status.value = SyncStatus.Running
        val result =
            try {
                val source = sourceRepository.activeMusicSource() ?: error("No server configured")
                SyncService(db, source).sync()
                Result.success(Unit)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                Result.failure(failure)
            }
        queueRepository.invalidateLibraryCache()
        _status.value =
            result.fold(
                onSuccess = { SyncStatus.Success },
                onFailure = { error ->
                    Log.w(TAG, "Sync failed", error)
                    SyncStatus.Failed(error.message ?: "Sync failed")
                },
            )
    }

    private companion object {
        const val TAG = "SubtracksSync"
    }
}
