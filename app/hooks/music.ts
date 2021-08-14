import { CacheItemTypeKey } from '@app/models/music'
import { selectCache } from '@app/state/cache'
import { selectMusic } from '@app/state/music'
import { selectSettings } from '@app/state/settings'
import { Store, useStore } from '@app/state/store'
import { useCallback, useEffect } from 'react'

export const useArtistInfo = (id: string) => {
  const artistInfo = useStore(useCallback((state: Store) => state.artistInfo[id], [id]))
  const fetchArtistInfo = useStore(selectMusic.fetchArtistInfo)

  useEffect(() => {
    if (!artistInfo) {
      fetchArtistInfo(id)
    }
  })

  return artistInfo
}

export const useAlbumWithSongs = (id: string) => {
  const album = useStore(useCallback((state: Store) => state.albumsWithSongs[id], [id]))
  const fetchAlbum = useStore(selectMusic.fetchAlbumWithSongs)

  useEffect(() => {
    if (!album) {
      fetchAlbum(id)
    }
  })

  return album
}

export const usePlaylistWithSongs = (id: string) => {
  const playlist = useStore(useCallback((state: Store) => state.playlistsWithSongs[id], [id]))
  const fetchPlaylist = useStore(selectMusic.fetchPlaylistWithSongs)

  useEffect(() => {
    if (!playlist) {
      fetchPlaylist(id)
    }
  })

  return playlist
}

export const useStarred = (id: string, type: string) => {
  return useStore(
    useCallback(
      (state: Store) => {
        switch (type) {
          case 'song':
            return state.starredSongs[id]
          case 'album':
            return state.starredAlbums[id]
          case 'artist':
            return state.starredArtists[id]
          default:
            return false
        }
      },
      [type, id],
    ),
  )
}

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

export const useCoverArtFile = (coverArt: string = '-1') => {
  const { file, request } = useFileRequest('coverArt', coverArt)
  const client = useStore(selectSettings.client)
  const cacheItem = useStore(selectCache.cacheItem)

  useEffect(() => {
    if (!file && client) {
      cacheItem('coverArt', coverArt, () => client.getCoverArtUri({ id: coverArt }))
    }
  }, [cacheItem, client, coverArt, file])

  // if (file && request && request.promise !== undefined) {
  //   return
  // }

  return { file, request }
}

export const useArtistArtFile = (artistId: string) => {
  const artistInfo = useArtistInfo(artistId)
  const { file, request } = useFileRequest('artistArt', artistId)
  const cacheItem = useStore(selectCache.cacheItem)

  useEffect(() => {
    if (!file && artistInfo && artistInfo.largeImageUrl) {
      console.log(artistInfo.largeImageUrl)
      cacheItem('artistArt', artistId, artistInfo.largeImageUrl)
    }
  }, [artistId, artistInfo, artistInfo?.largeImageUrl, cacheItem, file])

  // if (file && request && request.promise !== undefined) {
  //   return
  // }

  return { file, request }
}
