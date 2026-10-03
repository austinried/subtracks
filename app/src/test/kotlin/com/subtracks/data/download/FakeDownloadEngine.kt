package com.subtracks.data.download

import com.subtracks.data.model.DownloadError

class FakeDownloadEngine : DownloadEngine {
    private val downloads = mutableMapOf<Long, EngineDownload>()
    private var nextId = 1L

    val requests = mutableListOf<Pair<Long, EngineRequest>>()
    val cancelled = mutableListOf<Long>()
    var cancelCalls = 0
    var enqueueDelayMs = 0L
    var failEnqueueFor: String? = null
    var failCancelOnEmpty = false

    override fun enqueue(request: EngineRequest): Long {
        if (enqueueDelayMs > 0) Thread.sleep(enqueueDelayMs)
        val failing = failEnqueueFor
        if (failing != null && request.path.endsWith(failing)) throw IllegalStateException("enqueue failed")
        val id = nextId++
        requests += id to request
        downloads[id] = EngineDownload(EngineStatus.Pending)
        return id
    }

    override fun download(id: Long): EngineDownload? = downloads[id]

    override fun cancel(ids: List<Long>) {
        if (ids.isEmpty() && failCancelOnEmpty) throw IllegalArgumentException("no ids")
        cancelCalls++
        cancelled += ids
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
        downloads[id] = EngineDownload(EngineStatus.Failed, error = DownloadError.Failed)
    }

    fun forget(id: Long) {
        downloads.remove(id)
    }
}
