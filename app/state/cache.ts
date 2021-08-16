import { CacheFile, CacheItemTypeKey, CacheRequest } from '@app/models/music'
import PromiseQueue from '@app/util/PromiseQueue'
import produce from 'immer'
import RNFS from 'react-native-fs'
import { GetState, SetState } from 'zustand'
import { Store } from './store'

const imageDownloadQueue = new PromiseQueue(20)
const songDownloadQueue = new PromiseQueue(1)

export type CacheDownload = CacheFile & CacheRequest

export type CacheDirsByServer = Record<string, Record<CacheItemTypeKey, string>>
export type CacheFilesByServer = Record<string, Record<CacheItemTypeKey, Record<string, CacheFile>>>
export type CacheRequestsByServer = Record<string, Record<CacheItemTypeKey, Record<string, CacheRequest>>>

// export type DownloadedItemsByServer = Record<
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

  // cache: DownloadedItemsByServer
  cacheDirs: CacheDirsByServer
  cacheFiles: CacheFilesByServer
  cacheRequests: CacheRequestsByServer

  fetchCoverArtFilePath: (coverArt: string) => Promise<string | undefined>
}

export const selectCache = {
  cacheItem: (store: CacheSlice) => store.cacheItem,
  fetchCoverArtFilePath: (store: CacheSlice) => store.fetchCoverArtFilePath,

  songCacheDir: (store: Store) => {
    const activeServerId = store.settings.activeServer
    if (!activeServerId) {
      return
    }

    return store.cacheDirs[activeServerId].song
  },
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
    if (inProgress && inProgress.promise !== undefined) {
      return await inProgress.promise
    }

    const existing = get().cacheFiles[activeServerId][key][itemId]
    if (existing) {
      return
    }

    const path = `${get().cacheDirs[activeServerId][key]}/${itemId}`
    const urlResult = typeof url === 'string' ? url : url()
    const fromUrl = typeof urlResult === 'string' ? urlResult : await urlResult

    const queue = key === 'song' ? songDownloadQueue : imageDownloadQueue

    const promise = queue.enqueue(() =>
      RNFS.downloadFile({
        fromUrl,
        toFile: path,
        progressInterval: 100,
        progress: res => {
          set(
            produce<CacheSlice>(state => {
              state.cacheRequests[activeServerId][key][itemId].progress = Math.max(
                1,
                res.bytesWritten / (res.contentLength || 1),
              )
            }),
          )
        },
      })
        .promise.then(() => {
          set(
            produce<CacheSlice>(state => {
              state.cacheRequests[activeServerId][key][itemId].progress = 1
              delete state.cacheRequests[activeServerId][key][itemId].promise
            }),
          )
        })
        .catch(() => {
          set(
            produce<CacheSlice>(state => {
              delete state.cacheFiles[activeServerId][key][itemId]
              delete state.cacheRequests[activeServerId][key][itemId]
            }),
          )
        }),
    )
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

  fetchCoverArtFilePath: async coverArt => {
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
    return `file://${get().cacheFiles[activeServerId].coverArt[coverArt].path}`
  },
})
