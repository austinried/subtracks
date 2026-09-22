package com.subtracks.data.repo

import androidx.paging.PagingSource
import androidx.paging.PagingState

const val QUEUE_PAGE_SIZE = 60

class QueuePagingSource(
    private val repository: QueueRepository,
) : PagingSource<Long, QueueWindowItem>() {
    override fun getRefreshKey(state: PagingState<Long, QueueWindowItem>): Long? =
        state.anchorPosition?.let { (it / QUEUE_PAGE_SIZE).toLong() }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, QueueWindowItem> {
        val snapshot = repository.snapshot()
        val page = params.key ?: 0L
        val offset = page * QUEUE_PAGE_SIZE
        val previousKey = if (page > 0) page - 1 else null
        if (offset >= snapshot.size) return LoadResult.Page(emptyList(), previousKey, null)
        val end = (offset + params.loadSize).coerceAtMost(snapshot.size)
        return LoadResult.Page(
            data = repository.range(snapshot, offset, end - 1),
            prevKey = previousKey,
            nextKey = if (end < snapshot.size) end / QUEUE_PAGE_SIZE else null,
        )
    }
}
