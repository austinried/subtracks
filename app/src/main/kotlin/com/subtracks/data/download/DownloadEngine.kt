package com.subtracks.data.download

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
    val error: String? = null,
)

interface DownloadEngine {
    fun enqueue(request: EngineRequest): Long

    fun download(id: Long): EngineDownload?

    fun cancel(id: Long)
}
