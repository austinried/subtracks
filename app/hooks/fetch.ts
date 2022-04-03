import { Playlist, Song, Album, AlbumCoverArt } from '@app/models/library'
import { mapArtist, mapAlbum, mapPlaylist, mapSong, mapArtistInfo } from '@app/models/map'
import queryClient from '@app/queryClient'
import { useStore } from '@app/state/store'
import { GetAlbumList2TypeBase } from '@app/subsonic/params'
import { mapCollectionById } from '@app/util/state'

export const useClient = () => {
  const client = useStore(store => store.client)

  return () => {
    if (!client) {
      throw new Error('no client!')
    }

    return client
  }
}

export const useFetchArtists = () => {
  const client = useClient()
  return () =>
    client()
      .getArtists()
      .then(res => mapCollectionById(res.data.artists, mapArtist))
}

export const useFetchArtist = () => {
  const client = useClient()
  return (id: string) =>
    client()
      .getArtist({ id })
      .then(res => ({
        artist: mapArtist(res.data.artist),
        albums: res.data.albums.map(mapAlbum),
      }))
}

export const useFetchArtistInfo = () => {
  const client = useClient()
  return (id: string) =>
    client()
      .getArtistInfo2({ id })
      .then(res => mapArtistInfo(id, res.data.artistInfo))
}

export const useFetchArtistTopSongs = () => {
  const client = useClient()
  return (artistName: string) =>
    client()
      .getTopSongs({ artist: artistName })
      .then(res => res.data.songs.map(mapSong))
}

export const useFetchPlaylists = () => {
  const client = useClient()
  return () =>
    client()
      .getPlaylists()
      .then(res => mapCollectionById(res.data.playlists, mapPlaylist))
}

export const useFetchPlaylist = () => {
  const client = useClient()
  return (id: string): Promise<{ playlist: Playlist; songs?: Song[] }> =>
    client()
      .getPlaylist({ id })
      .then(res => ({
        playlist: mapPlaylist(res.data.playlist),
        songs: res.data.playlist.songs.map(mapSong),
      }))
}

export const useFetchAlbum = () => {
  const client = useClient()
  return (id: string): Promise<{ album: Album; songs?: Song[] }> =>
    client()
      .getAlbum({ id })
      .then(res => ({
        album: mapAlbum(res.data.album),
        songs: res.data.songs.map(mapSong),
      }))
      .then(res => {
        queryClient.setQueryData<AlbumCoverArt>(
          ['albumCoverArt', res.album.id],
          { albumId: res.album.id, coverArt: res.album.coverArt },
          { updatedAt: Date.now() },
        )
        return res
      })
}

export const useFetchAlbumList = () => {
  const client = useClient()
  return (size: number, offset: number, type: GetAlbumList2TypeBase) =>
    client()
      .getAlbumList2({ size, offset, type })
      .then(res => res.data.albums.map(mapAlbum))
      .then(res => {
        res.map(a =>
          queryClient.setQueryData<AlbumCoverArt>(
            ['albumCoverArt', a.id],
            { albumId: a.id, coverArt: a.coverArt },
            { updatedAt: Date.now() },
          ),
        )
        return res
      })
}
