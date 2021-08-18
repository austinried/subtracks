import { useState, useCallback } from 'react'
import { useActiveServerRefresh } from './server'

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

  useActiveServerRefresh(
    useCallback(() => {
      setList([])
      refresh()
    }, [refresh]),
  )

  return { list, refreshing, refresh }
}

export const useFetchPaginatedList = <T>(
  fetchList: (size?: number, offset?: number) => Promise<T[]>,
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

  useActiveServerRefresh(
    useCallback(() => {
      setList([])
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

  return { list, refreshing, refresh, fetchNextPage }
}
