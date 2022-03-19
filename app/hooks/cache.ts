import { CacheImageSize, CacheItemTypeKey } from '@app/models/cache'
import { selectCache } from '@app/state/cache'
import { selectSettings } from '@app/state/settings'
import { Store, useStore, useStoreDeep } from '@app/state/store'
import { useCallback, useEffect } from 'react'

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
    // intentionally leaving file out so it doesn't re-render if the request fails
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cacheItem, client, coverArt, type])

  return { file, request }
}

export const useArtistArtFile = (artistId: string, size: CacheImageSize = 'thumbnail') => {
  const type: CacheItemTypeKey = size === 'original' ? 'artistArt' : 'artistArtThumb'
  const fetchArtistInfo = useStore(store => store.fetchLibraryArtistInfo)
  const artistInfo = useStoreDeep(store => store.entities.artistInfo[artistId])
  const { file, request } = useFileRequest(type, artistId)
  const cacheItem = useStore(selectCache.cacheItem)

  useEffect(() => {
    if (!artistInfo) {
      fetchArtistInfo(artistId)
      return
    }

    if (!file) {
      cacheItem(type, artistId, async () => {
        return type === 'artistArtThumb' ? artistInfo?.smallImageUrl : artistInfo?.largeImageUrl
      })
    }
    // intentionally leaving file out so it doesn't re-render if the request fails
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [artistId, cacheItem, fetchArtistInfo, type, artistInfo])

  return { file, request }
}
