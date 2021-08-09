import { selectMusic } from '@app/state/music'
import { selectSettings } from '@app/state/settings'
import { Store, useStore } from '@app/state/store'
import { SubsonicApiClient } from '@app/subsonic/api'
import { GetCoverArtParams } from '@app/subsonic/params'
import { useCallback } from 'react'

export const useArtistInfo = (id: string) => {
  const artistInfo = useStore(useCallback((state: Store) => state.artistInfo[id], [id]))
  const fetchArtistInfo = useStore(selectMusic.fetchArtistInfo)

  if (!artistInfo) {
    fetchArtistInfo(id)
  }

  return artistInfo
}

export const useAlbumWithSongs = (id: string) => {
  const album = useStore(useCallback((state: Store) => state.albumsWithSongs[id], [id]))
  const fetchAlbum = useStore(selectMusic.fetchAlbumWithSongs)

  if (!album) {
    fetchAlbum(id)
  }

  return album
}

export const usePlaylistWithSongs = (id: string) => {
  const playlist = useStore(useCallback((state: Store) => state.playlistsWithSongs[id], [id]))
  const fetchPlaylist = useStore(selectMusic.fetchPlaylistWithSongs)

  if (!playlist) {
    fetchPlaylist(id)
  }

  return playlist
}

export const useStarred = (id: string, type: string) => {
  const starred = useStore(
    useCallback(
      (state: Store) => {
        if (!(type in state.starred)) {
          return false
        }
        return !!state.starred[type][id]
      },
      [type, id],
    ),
  )
  return starred
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
