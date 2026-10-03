package com.subtracks.data.sync

import android.util.Log
import com.subtracks.R
import com.subtracks.UiException
import com.subtracks.UiMessage
import com.subtracks.data.db.SubtracksDatabase
import com.subtracks.data.repo.QueueRepository
import com.subtracks.data.repo.SourceRepository
import com.subtracks.data.source.subsonic.SubsonicException
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
        val result =
            try {
                val source = sourceRepository.activeMusicSource() ?: throw NoServerException()
                SyncService(db, source).sync()
                SyncStatus.Success
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                Log.w(TAG, "Sync failed", failure)
                val message =
                    when (failure) {
                        is NoServerException -> {
                            UiMessage(R.string.error_no_server)
                        }

                        is UiException -> {
                            failure.uiMessage
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

    private companion object {
        const val TAG = "SubtracksSync"
    }
}

private class NoServerException : IllegalStateException()
