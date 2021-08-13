import { Song } from '@app/models/music'
import PromiseQueue from '@app/util/PromiseQueue'
import { SetState, GetState } from 'zustand'
import { Store } from './store'
import produce from 'immer'
import RNFS from 'react-native-fs'

const imageDownloadQueue = new PromiseQueue(10)

export type DownloadFile = {
  path: string
  date: number
  progress: number
  promise?: Promise<void>
}

export type CacheSlice = {
  coverArtDir?: string
  artistArtDir?: string
  songsDir?: string

  cachedCoverArt: { [coverArt: string]: DownloadFile }
  downloadedCoverArt: { [coverArt: string]: DownloadFile }

  cacheCoverArt: (coverArt: string) => Promise<void>
  getCoverArtPath: (coverArt: string) => Promise<string>

  cachedArtistArt: { [artistId: string]: DownloadFile }
  downloadedArtistArt: { [artistId: string]: DownloadFile }

  cacheArtistArt: (artistId: string, url?: string) => Promise<void>

  cachedSongs: { [id: string]: DownloadFile }
  downloadedSongs: { [id: string]: DownloadFile }

  albumCoverArt: { [id: string]: string | undefined }
  albumCoverArtRequests: { [id: string]: Promise<void> }
  fetchAlbumCoverArt: (id: string) => Promise<void>
  getAlbumCoverArt: (id: string | undefined) => Promise<string | undefined>
  mapSongCoverArtFromAlbum: (songs: Song[]) => Promise<Song[]>
}

export const selectCache = {
  cacheCoverArt: (store: CacheSlice) => store.cacheCoverArt,
  getCoverArtPath: (store: CacheSlice) => store.getCoverArtPath,
  cacheArtistArt: (store: CacheSlice) => store.cacheArtistArt,

  fetchAlbumCoverArt: (store: CacheSlice) => store.fetchAlbumCoverArt,
}

export const createCacheSlice = (set: SetState<Store>, get: GetState<Store>): CacheSlice => ({
  cachedCoverArt: {},
  downloadedCoverArt: {},

  cacheCoverArt: async coverArt => {
    const client = get().client
    if (!client) {
      return
    }

    const path = `${get().coverArtDir}/${coverArt}`

    const existing = get().cachedCoverArt[coverArt]
    if (existing) {
      if (existing.promise !== undefined) {
        return await existing.promise
      } else {
        return
      }
    }

    const promise = imageDownloadQueue
      .enqueue(
        () =>
          RNFS.downloadFile({
            fromUrl: client.getCoverArtUri({ id: coverArt }),
            toFile: path,
          }).promise,
      )
      .then(() => {
        set(
          produce<CacheSlice>(state => {
            state.cachedCoverArt[coverArt].progress = 1
            delete state.cachedCoverArt[coverArt].promise
          }),
        )
      })
    set(
      produce<CacheSlice>(state => {
        state.cachedCoverArt[coverArt] = {
          path,
          date: Date.now(),
          progress: 0,
          promise,
        }
      }),
    )
    return await promise
  },

  getCoverArtPath: async coverArt => {
    const existing = get().cachedCoverArt[coverArt]
    if (existing) {
      if (existing.promise) {
        await existing.promise
      }
      return existing.path
    }

    await get().cacheCoverArt(coverArt)
    return get().cachedCoverArt[coverArt].path
  },

  cachedArtistArt: {},
  downloadedArtistArt: {},

  cacheArtistArt: async (artistId, url) => {
    if (!url) {
      return
    }

    const client = get().client
    if (!client) {
      return
    }

    const path = `${get().artistArtDir}/${artistId}`

    const existing = get().cachedArtistArt[artistId]
    if (existing) {
      if (existing.promise !== undefined) {
        return await existing.promise
      } else {
        return
      }
    }

    const promise = imageDownloadQueue
      .enqueue(
        () =>
          RNFS.downloadFile({
            fromUrl: url,
            toFile: path,
          }).promise,
      )
      .then(() => {
        set(
          produce<CacheSlice>(state => {
            state.cachedArtistArt[artistId].progress = 1
            delete state.cachedArtistArt[artistId].promise
          }),
        )
      })
    set(
      produce<CacheSlice>(state => {
        state.cachedArtistArt[artistId] = {
          path,
          date: Date.now(),
          progress: 0,
          promise,
        }
      }),
    )
  },

  cachedSongs: {},
  downloadedSongs: {},

  albumCoverArt: {},
  albumCoverArtRequests: {},

  fetchAlbumCoverArt: async id => {
    const client = get().client
    if (!client) {
      return
    }

    const inProgress = get().albumCoverArtRequests[id]
    if (inProgress !== undefined) {
      return await inProgress
    }

    const promise = new Promise<void>(async resolve => {
      try {
        const response = await client.getAlbum({ id })
        set(
          produce<CacheSlice>(state => {
            state.albumCoverArt[id] = response.data.album.coverArt
          }),
        )
      } finally {
        resolve()
      }
    }).then(() => {
      set(
        produce<CacheSlice>(state => {
          delete state.albumCoverArtRequests[id]
        }),
      )
    })
    set(
      produce<CacheSlice>(state => {
        state.albumCoverArtRequests[id] = promise
      }),
    )

    return await promise
  },

  getAlbumCoverArt: async id => {
    if (!id) {
      return
    }

    const existing = get().albumCoverArt[id]
    if (existing) {
      return existing
    }

    await get().fetchAlbumCoverArt(id)
    return get().albumCoverArt[id]
  },

  mapSongCoverArtFromAlbum: async songs => {
    const mapped: Song[] = []
    for (const s of songs) {
      mapped.push({
        ...s,
        coverArt: await get().getAlbumCoverArt(s.albumId),
      })
    }
    return mapped
  },
})
