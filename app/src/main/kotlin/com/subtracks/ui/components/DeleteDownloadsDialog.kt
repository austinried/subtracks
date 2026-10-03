package com.subtracks.ui.components

import android.content.Context
import android.text.format.Formatter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.subtracks.R
import com.subtracks.data.model.DownloadList

data class PendingDownloadDelete(
    val name: String,
    val list: DownloadList,
    val refId: String,
    val bytes: Long,
)

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
