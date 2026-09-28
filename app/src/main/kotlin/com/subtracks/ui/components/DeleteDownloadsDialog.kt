package com.subtracks.ui.components

import android.content.Context
import android.text.format.Formatter
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
        title = { Text("Delete downloads") },
        text = { Text("$name\n\nDeleting will free $freed") },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
            ) { Text("Delete") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun formatBytes(
    context: Context,
    bytes: Long,
): String = Formatter.formatFileSize(context, bytes)
