import { useState, useCallback } from 'react'
import { useActiveServerRefresh } from './settings'

export const useFetchList = <T>(fetchList: () => Promise<T[]>) => {
  const [list, setList] = useState<T[]>([])
  const [refreshing, setRefreshing] = useState(false)

  const refresh = useCallback(() => {
    setRefreshing(true)

    fetchList().then(items => {
      setList(items)
      setRefreshing(false)
    })
  }, [fetchList])

  const reset = useCallback(() => {
    setList([])
    refresh()
  }, [refresh])

  useActiveServerRefresh(
    useCallback(() => {
      reset()
    }, [reset]),
  )

  return { list, refreshing, refresh, reset }
}

export const useFetchList2 = (fetchList: () => Promise<void>) => {
  const [refreshing, setRefreshing] = useState(false)

  const refresh = useCallback(async () => {
    setRefreshing(true)
    await fetchList()
    setRefreshing(false)
  }, [fetchList])

  useActiveServerRefresh(
    useCallback(async () => {
      await refresh()
    }, [refresh]),
  )

  return { refreshing, refresh }
}

export const useFetchPaginatedList = <T>(
  fetchList: (size: number, offset: number) => Promise<T[]>,
  pageSize: number,
) => {
  const [list, setList] = useState<T[]>([])
  const [refreshing, setRefreshing] = useState(false)
  const [offset, setOffset] = useState(0)

  const refresh = useCallback(() => {
    setOffset(0)
    setRefreshing(true)

    fetchList(pageSize, 0).then(firstPage => {
      setList(firstPage)
      setRefreshing(false)
    })
  }, [fetchList, pageSize])

  const reset = useCallback(() => {
    setList([])
    refresh()
  }, [refresh])

  useActiveServerRefresh(
    useCallback(() => {
      refresh()
    }, [refresh]),
  )

  const fetchNextPage = useCallback(() => {
    const newOffset = offset + pageSize
    setRefreshing(true)

    fetchList(pageSize, newOffset).then(nextPage => {
      setRefreshing(false)

      if (nextPage.length === 0) {
        return
      }

      setList([...list, ...nextPage])
      setOffset(newOffset)
    })
  }, [offset, pageSize, fetchList, list])

  return { list, refreshing, refresh, reset, fetchNextPage }
}
