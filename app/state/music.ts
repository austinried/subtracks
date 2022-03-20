import { Store } from '@app/state/store'
import produce from 'immer'
import { GetState, SetState } from 'zustand'

export type MusicSlice = {
  albumIdCoverArt: { [id: string]: string | undefined }
  albumIdCoverArtRequests: { [id: string]: Promise<void> }
  fetchAlbumCoverArt: (id: string) => Promise<void>
  getAlbumCoverArt: (id: string | undefined) => Promise<string | undefined>
}

export const createMusicSlice = (set: SetState<Store>, get: GetState<Store>): MusicSlice => ({
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
