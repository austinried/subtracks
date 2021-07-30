import { useAtom } from 'jotai'
import { useAtomValue, useUpdateAtom } from 'jotai/utils'
import { useEffect } from 'react'
import { albumListAtom, artistsAtom, homeListsAtom, playlistsAtom, searchResultsAtom } from './music'
import { activeServerAtom } from './settings'
import { useReset } from './trackplayer'

export const useSetActiveServer = () => {
  const [activeServer, setActiveServer] = useAtom(activeServerAtom)
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
  const activeServer = useAtomValue(activeServerAtom)

  useEffect(() => {
    if (list.length === 0) {
      update()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeServer])
}

export const useActiveServerRefresh = (update: () => void) => {
  const activeServer = useAtomValue(activeServerAtom)

  useEffect(() => {
    if (activeServer) {
      update()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeServer])
}
