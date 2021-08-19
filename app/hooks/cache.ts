import { CacheImageSize, CacheItemTypeKey } from '@app/models/cache'
import { selectCache } from '@app/state/cache'
import { selectSettings } from '@app/state/settings'
import { useStore, Store } from '@app/state/store'
import { useCallback, useEffect } from 'react'
import { useArtistInfo } from './music'

const useFileRequest = (key: CacheItemTypeKey, id: string) => {
  const file = useStore(
    useCallback(
      (store: Store) => {
        const activeServerId = store.settings.activeServer
        if (!activeServerId) {
          return
        }

        return store.cacheFiles[activeServerId][key][id]
      },
      [key, id],
    ),
  )
  const request = useStore(
    useCallback(
      (store: Store) => {
        const activeServerId = store.settings.activeServer
        if (!activeServerId) {
          return
        }

        return store.cacheRequests[activeServerId][key][id]
      },
      [key, id],
    ),
  )

  return { file, request }
}

export const useCoverArtFile = (coverArt = '-1', size: CacheImageSize = 'thumbnail') => {
  const type: CacheItemTypeKey = size === 'original' ? 'coverArt' : 'coverArtThumb'
  const { file, request } = useFileRequest(type, coverArt)
  const client = useStore(selectSettings.client)
  const cacheItem = useStore(selectCache.cacheItem)

  useEffect(() => {
    if (!file && client) {
      cacheItem(type, coverArt, () =>
        client.getCoverArtUri({
          id: coverArt,
          size: type === 'coverArtThumb' ? '256' : undefined,
        }),
      )
    }
  }, [cacheItem, client, coverArt, file, type])

  return { file, request }
}

export const useArtistArtFile = (artistId: string, size: CacheImageSize = 'thumbnail') => {
  const type: CacheItemTypeKey = size === 'original' ? 'artistArt' : 'artistArtThumb'
  const artistInfo = useArtistInfo(artistId)
  const { file, request } = useFileRequest(type, artistId)
  const cacheItem = useStore(selectCache.cacheItem)

  useEffect(() => {
    const url = type === 'artistArtThumb' ? artistInfo?.smallImageUrl : artistInfo?.largeImageUrl
    if (!file && artistInfo && url) {
      cacheItem(type, artistId, url)
    }
  }, [artistId, artistInfo, cacheItem, file, type])

  return { file, request }
}
