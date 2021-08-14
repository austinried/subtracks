import { CacheFile, CacheItemTypeKey, CacheRequest } from '@app/models/music'
import PromiseQueue from '@app/util/PromiseQueue'
import produce from 'immer'
import RNFS from 'react-native-fs'
import { GetState, SetState } from 'zustand'
import { Store } from './store'

const imageDownloadQueue = new PromiseQueue(10)

export type CacheDownload = CacheFile & CacheRequest

export type CacheDirsByServer = Record<string, Record<CacheItemTypeKey, string>>
export type CacheFilesByServer = Record<string, Record<CacheItemTypeKey, Record<string, CacheFile>>>
export type CacheRequestsByServer = Record<string, Record<CacheItemTypeKey, Record<string, CacheRequest>>>

// export type CacheItemsDb = Record<
//   string,
//   {
//     songs: { [songId: string]: DownloadedSong }
//     albums: { [albumId: string]: DownloadedAlbum }
//     artists: { [songId: string]: DownloadedArtist }
//     playlists: { [playlistId: string]: DownloadedPlaylist }
//   }
// >

export type CacheSlice = {
  cacheItem: (key: CacheItemTypeKey, itemId: string, url: string | (() => string | Promise<string>)) => Promise<void>

  // cache: CacheItemsDb
  cacheDirs: CacheDirsByServer
  cacheFiles: CacheFilesByServer
  cacheRequests: CacheRequestsByServer

  getCoverArtPath: (coverArt: string) => Promise<string | undefined>
}

export const selectCache = {
  cacheItem: (store: CacheSlice) => store.cacheItem,
  getCoverArtPath: (store: CacheSlice) => store.getCoverArtPath,
}

export const createCacheSlice = (set: SetState<Store>, get: GetState<Store>): CacheSlice => ({
  // cache: {},

  cacheDirs: {},
  cacheFiles: {},
  cacheRequests: {},

  cacheItem: async (key, itemId, url) => {
    const client = get().client
    if (!client) {
      return
    }

    const activeServerId = get().settings.activeServer
    if (!activeServerId) {
      return
    }

    const inProgress = get().cacheRequests[activeServerId][key][itemId]
    if (inProgress) {
      if (inProgress.promise !== undefined) {
        return await inProgress.promise
      } else {
        return
      }
    }

    const existing = get().cacheFiles[activeServerId][key][itemId]
    if (existing) {
      return
    }

    const path = `${get().cacheDirs[activeServerId][key]}/${itemId}`
    const urlResult = typeof url === 'string' ? url : url()
    const fromUrl = typeof urlResult === 'string' ? urlResult : await urlResult

    const promise = imageDownloadQueue
      .enqueue(
        () =>
          RNFS.downloadFile({
            fromUrl,
            toFile: path,
          }).promise,
      )
      .then(() => {
        set(
          produce<CacheSlice>(state => {
            state.cacheRequests[activeServerId][key][itemId].progress = 1
            delete state.cacheRequests[activeServerId][key][itemId].promise
          }),
        )
      })
    set(
      produce<Store>(state => {
        state.cacheFiles[activeServerId][key][itemId] = {
          path,
          date: Date.now(),
          permanent: false,
        }
        state.cacheRequests[activeServerId][key][itemId] = {
          progress: 0,
          promise,
        }
      }),
    )
    return await promise
  },

  getCoverArtPath: async coverArt => {
    const client = get().client
    if (!client) {
      return
    }

    const activeServerId = get().settings.activeServer
    if (!activeServerId) {
      return
    }

    const existing = get().cacheFiles[activeServerId].coverArt[coverArt]
    const inProgress = get().cacheRequests[activeServerId].coverArt[coverArt]
    if (existing && inProgress) {
      if (inProgress.promise) {
        await inProgress.promise
      }
      return existing.path
    }

    await get().cacheItem('coverArt', coverArt, () => client.getCoverArtUri({ id: coverArt }))
    return get().cacheFiles[activeServerId].coverArt[coverArt].path
  },
})
