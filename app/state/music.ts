import { StarrableItemType } from '@app/models/music'
import { Store } from '@app/state/store'
import { StarParams } from '@app/subsonic/params'
import produce from 'immer'
import { GetState, SetState } from 'zustand'

export type MusicSlice = {
  //
  // actions, etc.
  //
  starredSongs: { [id: string]: boolean }
  starredAlbums: { [id: string]: boolean }
  starredArtists: { [id: string]: boolean }
  starItem: (id: string, type: StarrableItemType, unstar?: boolean) => Promise<void>

  albumIdCoverArt: { [id: string]: string | undefined }
  albumIdCoverArtRequests: { [id: string]: Promise<void> }
  fetchAlbumCoverArt: (id: string) => Promise<void>
  getAlbumCoverArt: (id: string | undefined) => Promise<string | undefined>
}

export const selectMusic = {
  starItem: (store: MusicSlice) => store.starItem,
}

function reduceStarred(
  starredType: { [id: string]: boolean },
  items: { id: string; starred?: Date | boolean }[],
): { [id: string]: boolean } {
  return {
    ...starredType,
    ...items.reduce((acc, val) => {
      acc[val.id] = !!val.starred
      return acc
    }, {} as { [id: string]: boolean }),
  }
}

export const createMusicSlice = (set: SetState<Store>, get: GetState<Store>): MusicSlice => ({
  starredSongs: {},
  starredAlbums: {},
  starredArtists: {},

  starItem: async (id, type, unstar = false) => {
    const client = get().client
    if (!client) {
      return
    }

    let params: StarParams
    let setStarred: (starred: boolean) => void

    switch (type) {
      case 'song':
        params = { id }
        setStarred = starred => {
          set(
            produce<MusicSlice>(state => {
              state.starredSongs = reduceStarred(state.starredSongs, [{ id, starred }])
            }),
          )
        }
        break
      case 'album':
        params = { albumId: id }
        setStarred = starred => {
          set(
            produce<MusicSlice>(state => {
              state.starredAlbums = reduceStarred(state.starredAlbums, [{ id, starred }])
            }),
          )
        }
        break
      case 'artist':
        params = { artistId: id }
        setStarred = starred => {
          set(
            produce<MusicSlice>(state => {
              state.starredArtists = reduceStarred(state.starredArtists, [{ id, starred }])
            }),
          )
        }
        break
      default:
        return
    }

    try {
      setStarred(!unstar)
      if (unstar) {
        await client.unstar(params)
      } else {
        await client.star(params)
      }
    } catch {
      setStarred(unstar)
    }
  },

  albumIdCoverArt: {},
  albumIdCoverArtRequests: {},

  fetchAlbumCoverArt: async id => {
    const client = get().client
    if (!client) {
      return
    }

    const inProgress = get().albumIdCoverArtRequests[id]
    if (inProgress !== undefined) {
      return await inProgress
    }

    const promise = new Promise<void>(async resolve => {
      try {
        const response = await client.getAlbum({ id })
        set(
          produce<MusicSlice>(state => {
            state.albumIdCoverArt[id] = response.data.album.coverArt
          }),
        )
      } finally {
        resolve()
      }
    }).then(() => {
      set(
        produce<MusicSlice>(state => {
          delete state.albumIdCoverArtRequests[id]
        }),
      )
    })
    set(
      produce<MusicSlice>(state => {
        state.albumIdCoverArtRequests[id] = promise
      }),
    )

    return await promise
  },

  getAlbumCoverArt: async id => {
    if (!id) {
      return
    }

    const existing = get().albumIdCoverArt[id]
    if (existing) {
      return existing
    }

    await get().fetchAlbumCoverArt(id)
    return get().albumIdCoverArt[id]
  },
})
