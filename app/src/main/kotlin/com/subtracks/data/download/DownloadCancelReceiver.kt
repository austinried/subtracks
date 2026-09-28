package com.subtracks.data.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.subtracks.data.repo.DownloadRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DownloadCancelReceiver :
    BroadcastReceiver(),
    KoinComponent {
    private val repository: DownloadRepository by inject()

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                repository.cancelActive()
            } finally {
                pending.finish()
            }
        }
    }
}
