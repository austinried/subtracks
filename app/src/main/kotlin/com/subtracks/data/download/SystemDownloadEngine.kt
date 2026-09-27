package com.subtracks.data.download

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

class SystemDownloadEngine(
    private val context: Context,
) : DownloadEngine {
    override fun enqueue(request: EngineRequest): Long {
        val download =
            DownloadManager
                .Request(Uri.parse(request.uri))
                .setTitle(request.title)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, request.path)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setAllowedOverMetered(request.allowMetered)
        return manager().enqueue(download)
    }

    override fun download(id: Long): EngineDownload? {
        val query = DownloadManager.Query().setFilterById(id)
        manager().query(query).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            val bytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            val total = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
            val reason =
                cursor
                    .getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                    .takeIf { status == DownloadManager.STATUS_FAILED }
            return EngineDownload(
                status = engineStatus(status),
                bytes = bytes.coerceAtLeast(0),
                total = total.coerceAtLeast(0),
                error = reason?.let(::failureMessage),
            )
        }
    }

    override fun cancel(id: Long) {
        manager().remove(id)
    }

    private fun manager(): DownloadManager = context.getSystemService(DownloadManager::class.java)
}

internal fun engineStatus(status: Int): EngineStatus =
    when (status) {
        DownloadManager.STATUS_SUCCESSFUL -> EngineStatus.Completed
        DownloadManager.STATUS_FAILED -> EngineStatus.Failed
        DownloadManager.STATUS_RUNNING -> EngineStatus.Running
        else -> EngineStatus.Pending
    }

internal fun failureMessage(reason: Int): String =
    when (reason) {
        DownloadManager.ERROR_INSUFFICIENT_SPACE -> "Not enough space to download"
        DownloadManager.ERROR_DEVICE_NOT_FOUND -> "Storage is unavailable"
        DownloadManager.ERROR_FILE_ERROR, DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "Could not write the download"
        DownloadManager.ERROR_HTTP_DATA_ERROR, DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "The server refused to send this track"
        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "The server redirected too many times"
        DownloadManager.ERROR_CANNOT_RESUME -> "The download could not be resumed"
        else -> "Download failed"
    }
