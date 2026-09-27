package com.subtracks.data.download

class FakeDownloadEngine : DownloadEngine {
    private val downloads = mutableMapOf<Long, EngineDownload>()
    private var nextId = 1L

    val requests = mutableListOf<Pair<Long, EngineRequest>>()
    val cancelled = mutableListOf<Long>()

    override fun enqueue(request: EngineRequest): Long {
        val id = nextId++
        requests += id to request
        downloads[id] = EngineDownload(EngineStatus.Pending)
        return id
    }

    override fun download(id: Long): EngineDownload? = downloads[id]

    override fun cancel(id: Long) {
        cancelled += id
    }

    fun running(
        id: Long,
        bytes: Long,
        total: Long,
    ) {
        downloads[id] = EngineDownload(EngineStatus.Running, bytes, total)
    }

    fun complete(
        id: Long,
        total: Long,
    ) {
        downloads[id] = EngineDownload(EngineStatus.Completed, total, total)
    }

    fun fail(id: Long) {
        downloads[id] = EngineDownload(EngineStatus.Failed, error = "Download failed")
    }

    fun forget(id: Long) {
        downloads.remove(id)
    }
}
