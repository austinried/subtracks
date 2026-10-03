package com.subtracks.data.download

import com.subtracks.data.model.DownloadError

data class EngineRequest(
    val uri: String,
    val path: String,
    val title: String,
    val allowMetered: Boolean = false,
)

enum class EngineStatus { Pending, Running, Completed, Failed }

data class EngineDownload(
    val status: EngineStatus,
    val bytes: Long = 0,
    val total: Long = 0,
    val error: DownloadError? = null,
)

interface DownloadEngine {
    fun enqueue(request: EngineRequest): Long

    fun download(id: Long): EngineDownload?

    fun cancel(ids: List<Long>)
}
