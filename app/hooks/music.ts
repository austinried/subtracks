import {
  mapAlbumID3toAlbumListItem,
  mapArtistID3toArtist,
  mapChildToSong,
  mapPlaylistListItem,
} from '@app/models/music'
import {
  albumListAtom,
  albumListUpdatingAtom,
  artistsAtom,
  artistsUpdatingAtom,
  homeListsUpdatingAtom,
  homeListsWriteAtom,
  playlistsAtom,
  playlistsUpdatingAtom,
  searchResultsAtom,
  searchResultsUpdatingAtom,
} from '@app/state/music'
import { selectSettings } from '@app/state/settings'
import { Store, useStore } from '@app/state/store'
import { SubsonicApiClient } from '@app/subsonic/api'
import { GetAlbumList2Type, GetCoverArtParams } from '@app/subsonic/params'
import { useAtom } from 'jotai'
import { useUpdateAtom } from 'jotai/utils'
import { useCallback } from 'react'

const selectors = {
  fetchArtistInfo: (state: Store) => state.fetchArtistInfo,
  fetchAlbum: (state: Store) => state.fetchAlbum,
  fetchPlaylist: (state: Store) => state.fetchPlaylist,
}

export const useArtistInfo = (id: string) => {
  const server = useStore(selectSettings.activeServer)
  const artistInfo = useStore(useCallback((state: Store) => state.artistInfo[id], [id]))
  const fetchArtistInfo = useStore(selectors.fetchArtistInfo)

  if (server && !artistInfo) {
    fetchArtistInfo(server, id)
  }

  return artistInfo
}

export const useAlbumWithSongs = (id: string) => {
  const server = useStore(selectSettings.activeServer)
  const album = useStore(useCallback((state: Store) => state.albums[id], [id]))
  const fetchAlbum = useStore(selectors.fetchAlbum)

  if (server && !album) {
    fetchAlbum(server, id)
  }

  return album
}

export const usePlaylistWithSongs = (id: string) => {
  const server = useStore(selectSettings.activeServer)
  const playlist = useStore(useCallback((state: Store) => state.playlists[id], [id]))
  const fetchPlaylist = useStore(selectors.fetchPlaylist)

  if (server && !playlist) {
    fetchPlaylist(server, id)
  }

  return playlist
}

export const useUpdateArtists = () => {
  const server = useStore(selectSettings.activeServer)
  const [updating, setUpdating] = useAtom(artistsUpdatingAtom)
  const setArtists = useUpdateAtom(artistsAtom)

  if (!server) {
    return () => Promise.resolve()
  }

  return async () => {
    if (updating) {
      return
    }
    setUpdating(true)

    const client = new SubsonicApiClient(server)

    try {
      const response = await client.getArtists()
      setArtists(response.data.artists.map(mapArtistID3toArtist))
    } finally {
      setUpdating(false)
    }
  }
}

export const useUpdateHomeLists = () => {
  const server = useStore(selectSettings.activeServer)
  const types = useStore(selectSettings.homeLists)
  const updateHomeList = useUpdateAtom(homeListsWriteAtom)
  const [updating, setUpdating] = useAtom(homeListsUpdatingAtom)

  if (!server) {
    return async () => {}
  }

  return async () => {
    if (updating) {
      return
    }
    setUpdating(true)

    const client = new SubsonicApiClient(server)

    try {
      const promises: Promise<any>[] = []
      for (const type of types) {
        promises.push(
          client.getAlbumList2({ type: type as GetAlbumList2Type, size: 20 }).then(response => {
            updateHomeList({ type, albums: response.data.albums.map(mapAlbumID3toAlbumListItem) })
          }),
        )
      }
      await Promise.all(promises)
    } finally {
      setUpdating(false)
    }
  }
}

export const useUpdateSearchResults = () => {
  const server = useStore(selectSettings.activeServer)
  const updateList = useUpdateAtom(searchResultsAtom)
  const [updating, setUpdating] = useAtom(searchResultsUpdatingAtom)

  if (!server) {
    return async () => {}
  }

  return async (query: string) => {
    if (updating || query.length < 2) {
      return
    }
    setUpdating(true)

    const client = new SubsonicApiClient(server)

    try {
      const response = await client.search3({ query })
      updateList({
        artists: response.data.artists.map(mapArtistID3toArtist),
        albums: response.data.albums.map(mapAlbumID3toAlbumListItem),
        songs: response.data.songs.map(a => mapChildToSong(a, client)),
      })
    } finally {
      setUpdating(false)
    }
  }
}

export const useUpdatePlaylists = () => {
  const server = useStore(selectSettings.activeServer)
  const updateList = useUpdateAtom(playlistsAtom)
  const [updating, setUpdating] = useAtom(playlistsUpdatingAtom)

  if (!server) {
    return async () => {}
  }

  return async () => {
    if (updating) {
      return
    }
    setUpdating(true)

    const client = new SubsonicApiClient(server)

    try {
      const response = await client.getPlaylists()
      updateList(response.data.playlists.map(mapPlaylistListItem))
    } finally {
      setUpdating(false)
    }
  }
}

export const useUpdateAlbumList = () => {
  const server = useStore(selectSettings.activeServer)
  const updateList = useUpdateAtom(albumListAtom)
  const [updating, setUpdating] = useAtom(albumListUpdatingAtom)

  if (!server) {
    return async () => {}
  }

  return async () => {
    if (updating) {
      return
    }
    setUpdating(true)

    const client = new SubsonicApiClient(server)

    try {
      const response = await client.getAlbumList2({ type: 'alphabeticalByArtist', size: 500 })
      updateList(response.data.albums.map(mapAlbumID3toAlbumListItem))
    } finally {
      setUpdating(false)
    }
  }
}

export const useCoverArtUri = () => {
  const server = useStore(selectSettings.activeServer)

  if (!server) {
    return () => undefined
  }

  const client = new SubsonicApiClient(server)

  return (coverArt?: string, size: 'thumbnail' | 'original' = 'thumbnail') => {
    const params: GetCoverArtParams = { id: coverArt || '-1' }
    if (size === 'thumbnail') {
      params.size = '256'
    }

    return client.getCoverArtUri(params)
  }
}
