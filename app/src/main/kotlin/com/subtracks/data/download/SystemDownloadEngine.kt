package com.subtracks.data.download

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import com.subtracks.UserAgent
import com.subtracks.data.model.DownloadError

class SystemDownloadEngine(
    private val context: Context,
) : DownloadEngine {
    override fun enqueue(request: EngineRequest): Long {
        val download =
            DownloadManager
                .Request(request.uri.toUri())
                .setTitle(request.title)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, request.path)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                .addRequestHeader("User-Agent", UserAgent.value)
                // Wi-Fi only unless the preference allows a metered network, and roaming counts as
                // one of those; the platform otherwise permits it by default.
                .setAllowedOverMetered(request.allowMetered)
                .setAllowedOverRoaming(request.allowMetered)
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

    override fun cancel(ids: List<Long>) {
        // DownloadManager.remove throws on an empty id list, and queued rows waiting for a slot
        // have no engine id yet, so an offline cancel can legitimately select none.
        if (ids.isEmpty()) return
        manager().remove(*ids.toLongArray())
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

internal fun failureMessage(reason: Int): DownloadError =
    when (reason) {
        DownloadManager.ERROR_INSUFFICIENT_SPACE -> DownloadError.NoSpace
        DownloadManager.ERROR_DEVICE_NOT_FOUND -> DownloadError.StorageUnavailable
        DownloadManager.ERROR_FILE_ERROR, DownloadManager.ERROR_FILE_ALREADY_EXISTS -> DownloadError.WriteFailed
        DownloadManager.ERROR_HTTP_DATA_ERROR, DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> DownloadError.ServerRefused
        DownloadManager.ERROR_TOO_MANY_REDIRECTS -> DownloadError.TooManyRedirects
        DownloadManager.ERROR_CANNOT_RESUME -> DownloadError.ResumeFailed
        else -> DownloadError.Failed
    }
