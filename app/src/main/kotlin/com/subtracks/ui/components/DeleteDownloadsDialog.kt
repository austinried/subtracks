package com.subtracks.ui.components

import android.content.Context
import android.text.format.Formatter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.subtracks.R
import com.subtracks.data.model.BulkDownloadAction
import com.subtracks.data.model.DownloadList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

data class PendingDownloadDelete(
    val name: String,
    val list: DownloadList,
    val refId: String,
    val bytes: Long,
)

class PendingDownloadDeleteController internal constructor(
    private val scope: CoroutineScope,
    private val downloadedBytes: State<suspend (DownloadList, String) -> Long>,
) {
    var pending by mutableStateOf<PendingDownloadDelete?>(null)
        private set

    fun onAction(
        target: MenuTarget,
        action: BulkDownloadAction,
        onAction: (DownloadList, String, BulkDownloadAction) -> Unit,
    ) {
        val (list, refId) = target.bulkRef() ?: return
        onAction(target.title, list, refId, action, onAction)
    }

    fun onAction(
        name: String,
        list: DownloadList,
        refId: String,
        action: BulkDownloadAction,
        onAction: (DownloadList, String, BulkDownloadAction) -> Unit,
    ) {
        if (action == BulkDownloadAction.Delete) {
            scope.launch {
                pending = PendingDownloadDelete(name, list, refId, downloadedBytes.value(list, refId))
            }
        } else {
            onAction(list, refId, action)
        }
    }

    fun dismiss() {
        pending = null
    }
}

@Composable
fun rememberPendingDownloadDelete(
    dismissals: Flow<Unit>,
    downloadedBytes: suspend (DownloadList, String) -> Long,
): PendingDownloadDeleteController {
    val scope = rememberCoroutineScope()
    val currentDownloadedBytes = rememberUpdatedState(downloadedBytes)
    val controller = remember(scope) { PendingDownloadDeleteController(scope, currentDownloadedBytes) }
    DismissOnRequest(dismissals, controller::dismiss)
    return controller
}

@Composable
fun PendingDownloadDeleteConfirmation(
    controller: PendingDownloadDeleteController,
    onConfirm: (PendingDownloadDelete) -> Unit,
) {
    controller.pending?.let { pending ->
        DeleteDownloadsDialog(
            name = pending.name,
            bytes = pending.bytes,
            onConfirm = { onConfirm(pending) },
            onDismiss = controller::dismiss,
        )
    }
}

@Composable
fun DeleteDownloadsDialog(
    name: String,
    bytes: Long,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val freed = formatBytes(LocalContext.current, bytes)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.resources_song_list_delete_all_title)) },
        text = { Text(stringResource(R.string.resources_song_list_delete_all_content, name, freed)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
            ) { Text(stringResource(R.string.actions_delete)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.actions_cancel)) } },
    )
}

private fun formatBytes(
    context: Context,
    bytes: Long,
): String = Formatter.formatFileSize(context, bytes)
