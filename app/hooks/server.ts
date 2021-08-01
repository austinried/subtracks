import { albumListAtom, artistsAtom, homeListsAtom, playlistsAtom, searchResultsAtom } from '@app/state/music'
import { selectSettings } from '@app/state/settings'
import { useStore } from '@app/state/store'
import { useReset } from '@app/state/trackplayer'
import { useUpdateAtom } from 'jotai/utils'
import { useEffect } from 'react'

export const useSwitchActiveServer = () => {
  const activeServer = useStore(selectSettings.activeServer)
  const setActiveServer = useStore(selectSettings.setActiveServer)
  const setArtists = useUpdateAtom(artistsAtom)
  const setHomeLists = useUpdateAtom(homeListsAtom)
  const setSearchResults = useUpdateAtom(searchResultsAtom)
  const setPlaylists = useUpdateAtom(playlistsAtom)
  const setAlbumLists = useUpdateAtom(albumListAtom)
  const resetPlayer = useReset()

  return async (id: string) => {
    if (id === activeServer?.id) {
      return
    }

    await resetPlayer()

    setArtists([])
    setHomeLists({})
    setSearchResults({ artists: [], albums: [], songs: [] })
    setPlaylists([])
    setAlbumLists([])

    setActiveServer(id)
  }
}

export const useActiveListRefresh = (list: unknown[], update: () => void) => {
  const activeServer = useStore(selectSettings.activeServer)

  useEffect(() => {
    if (list.length === 0) {
      update()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeServer])
}

export const useActiveServerRefresh = (update: () => void) => {
  const activeServer = useStore(selectSettings.activeServer)

  useEffect(() => {
    if (activeServer) {
      update()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeServer])
}
