import { Store, useStore, useStoreDeep } from '@app/state/store'
import { useCallback, useEffect } from 'react'

export const useArtistInfo = (id: string) => {
  const artistInfo = useStoreDeep(useCallback(store => store.entities.artistInfo[id], [id]))
  const fetchArtistInfo = useStore(store => store.fetchLibraryArtistInfo)

  useEffect(() => {
    if (!artistInfo) {
      fetchArtistInfo(id)
    }
  }, [artistInfo, fetchArtistInfo, id])

  return artistInfo
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
