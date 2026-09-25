package com.subtracks.data.sync

import android.util.Log
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    private val sourceRepository: SourceRepository,
    private val queueRepository: QueueRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status

    fun requestSync() {
        scope.launch { runSync() }
    }

    private suspend fun runSync() {
        _status.value = SyncStatus.Running
        val result = sourceRepository.sync()
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
