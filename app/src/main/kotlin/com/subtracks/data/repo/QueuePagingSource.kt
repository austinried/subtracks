package com.subtracks.data.repo

import androidx.paging.PagingSource
import androidx.paging.PagingState

const val QUEUE_PAGE_SIZE = 60

class QueuePagingSource(
    private val repository: QueueRepository,
    private val initialPosition: Long,
) : PagingSource<Long, QueueWindowItem>() {
    override fun getRefreshKey(state: PagingState<Long, QueueWindowItem>): Long? {
        val anchor = state.anchorPosition ?: return null
        val first =
            state.pages
                .firstOrNull()
                ?.data
                ?.firstOrNull()
                ?.position ?: return null
        return (first + anchor) / QUEUE_PAGE_SIZE
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, QueueWindowItem> {
        val snapshot = repository.snapshot()
        if (snapshot.size == 0L) return LoadResult.Page(emptyList(), null, null)
        val lastPage = (snapshot.size - 1) / QUEUE_PAGE_SIZE
        val page = (params.key ?: (initialPosition.coerceAtLeast(0) / QUEUE_PAGE_SIZE)).coerceIn(0, lastPage)
        val offset = page * QUEUE_PAGE_SIZE
        val end = (offset + params.loadSize).coerceAtMost(snapshot.size)
        return LoadResult.Page(
            data = repository.range(snapshot, offset, end - 1),
            prevKey = if (page > 0) page - 1 else null,
            nextKey = if (end < snapshot.size) end / QUEUE_PAGE_SIZE else null,
        )
    }
}
