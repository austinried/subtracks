package com.subtracks.data.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.subtracks.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SyncNotifier(
    private val context: Context,
    private val status: Flow<SyncStatus>,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val manager = NotificationManagerCompat.from(context)

    fun start() {
        createChannel()
        scope.launch {
            status.collect { syncStatus ->
                if (syncStatus is SyncStatus.Running && manager.areNotificationsEnabled()) {
                    manager.notify(ID, build())
                } else {
                    manager.cancel(ID)
                }
            }
        }
    }

    private fun build(): Notification =
        NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(context.getString(R.string.sync_notification_title))
            .setContentText(context.getString(R.string.sync_notification_text))
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setProgress(0, 0, true)
            .setContentIntent(launchIntent())
            .build()

    private fun launchIntent(): PendingIntent? =
        context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.let { PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE) }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.sync_notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            ).apply { setShowBadge(false) },
        )
    }

    companion object {
        private const val ID = 2
        private const val CHANNEL_ID = "sync"
    }
}
