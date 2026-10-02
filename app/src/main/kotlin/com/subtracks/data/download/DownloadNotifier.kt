package com.subtracks.data.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.subtracks.R
import com.subtracks.data.repo.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Our own aggregated progress notification for the downloads the platform engine runs. The engine's
 * per-download notifications are hidden (VISIBILITY_HIDDEN), so this is the only sign of progress,
 * and it only exists while this process does.
 */
class DownloadNotifier(
    private val context: Context,
    private val repository: DownloadRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val manager = NotificationManagerCompat.from(context)

    fun start() {
        createChannel()
        scope.launch {
            repository.activeDownloads().collect { rows ->
                val content = downloadNotification(rows)
                if (content == null) {
                    manager.cancel(ID)
                } else if (manager.areNotificationsEnabled()) {
                    manager.notify(ID, build(content))
                }
            }
        }
    }

    private fun build(content: DownloadNotification): Notification =
        NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(content.title)
            .setContentText(content.text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(PROGRESS_MAX, ((content.progress ?: 0f) * PROGRESS_MAX).toInt(), content.progress == null)
            .addAction(0, context.getString(R.string.download_cancel), cancelIntent())
            .setContentIntent(launchIntent())
            .build()

    private fun cancelIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, DownloadCancelReceiver::class.java).setAction(ACTION_CANCEL),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun launchIntent(): PendingIntent? =
        context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.let { PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE) }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, context.getString(R.string.download_channel), NotificationManager.IMPORTANCE_LOW).apply {
                description = context.getString(R.string.download_channel_description)
                setShowBadge(false)
            },
        )
    }

    companion object {
        private const val ID = 1
        private const val CHANNEL_ID = "downloads"
        private const val ACTION_CANCEL = "com.subtracks.CANCEL_DOWNLOADS"
        private const val PROGRESS_MAX = 100
    }
}
