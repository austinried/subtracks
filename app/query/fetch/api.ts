import useClient from '@app/hooks/useClient'
import { Album, Playlist, Song } from '@app/models/library'
import { mapAlbum, mapArtist, mapArtistInfo, mapPlaylist, mapSong } from '@app/models/map'
import queryClient from '@app/query/queryClient'
import qk from '@app/query/queryKeys'
import { SubsonicApiClient } from '@app/subsonic/api'
import { GetAlbumList2TypeBase, Search3Params, StarParams } from '@app/subsonic/params'
import { mapCollectionById } from '@app/util/state'

function cacheStarredData<T extends { id: string; starred?: undefined | any }>(item: T) {
  queryClient.setQueryData<boolean>(qk.starredItems(item.id), !!item.starred)
}

function cacheAlbumCoverArtData<T extends { id: string; coverArt?: string }>(item: T) {
  queryClient.setQueryData<string | undefined>(qk.albumCoverArt(item.id), item.coverArt)
}

export const useFetchArtists = () => {
  const client = useClient()

  return async () => {
    const res = await client().getArtists()

    res.data.artists.forEach(cacheStarredData)

    return mapCollectionById(res.data.artists, mapArtist)
  }
}

export const useFetchArtist = () => {
  const client = useClient()

  return async (id: string) => {
    const res = await client().getArtist({ id })

    cacheStarredData(res.data.artist)
    res.data.albums.forEach(cacheStarredData)

    res.data.albums.forEach(cacheAlbumCoverArtData)

    return {
      artist: mapArtist(res.data.artist),
      albums: res.data.albums.map(mapAlbum),
    }
  }
}

export const useFetchArtistInfo = () => {
  const client = useClient()

  return async (id: string) => {
    const res = await client().getArtistInfo2({ id })
    return mapArtistInfo(id, res.data.artistInfo)
  }
}

export const useFetchArtistTopSongs = () => {
  const client = useClient()

  return async (artistName: string) => {
    const res = await client().getTopSongs({ artist: artistName })

    res.data.songs.forEach(cacheStarredData)

    return res.data.songs.map(mapSong)
  }
}

export const useFetchPlaylists = () => {
  const client = useClient()

  return async () => {
    const res = await client().getPlaylists()
    return mapCollectionById(res.data.playlists, mapPlaylist)
  }
}

export const useFetchPlaylist = () => {
  const client = useClient()

  return async (id: string): Promise<{ playlist: Playlist; songs?: Song[] }> => {
    const res = await client().getPlaylist({ id })

    res.data.playlist.songs.forEach(cacheStarredData)

    return {
      playlist: mapPlaylist(res.data.playlist),
      songs: res.data.playlist.songs.map(mapSong),
    }
  }
}

export async function fetchAlbum(id: string, client: SubsonicApiClient): Promise<{ album: Album; songs?: Song[] }> {
  const res = await client.getAlbum({ id })

  cacheStarredData(res.data.album)
  res.data.songs.forEach(cacheStarredData)

  cacheAlbumCoverArtData(res.data.album)

  return {
    album: mapAlbum(res.data.album),
    songs: res.data.songs.map(mapSong),
  }
}

export const useFetchAlbum = () => {
  const client = useClient()
  return async (id: string) => fetchAlbum(id, client())
}

export const useFetchAlbumList = () => {
  const client = useClient()

  return async (size: number, offset: number, type: GetAlbumList2TypeBase) => {
    const res = await client().getAlbumList2({ size, offset, type })

    res.data.albums.forEach(cacheStarredData)

    res.data.albums.forEach(cacheAlbumCoverArtData)

    return res.data.albums.map(mapAlbum)
  }
}

export const useFetchSong = () => {
  const client = useClient()

  return async (id: string) => {
    const res = await client().getSong({ id })

    cacheStarredData(res.data.song)

    return mapSong(res.data.song)
  }
}

export const useFetchSearchResults = () => {
  const client = useClient()

  return async (params: Search3Params) => {
    const res = await client().search3(params)

    res.data.artists.forEach(cacheStarredData)
    res.data.albums.forEach(cacheStarredData)
    res.data.songs.forEach(cacheStarredData)

    res.data.albums.forEach(cacheAlbumCoverArtData)

    return {
      artists: res.data.artists.map(mapArtist),
      albums: res.data.albums.map(mapAlbum),
      songs: res.data.songs.map(mapSong),
    }
  }
}

export const useFetchStar = () => {
  const client = useClient()

  return async (params: StarParams) => {
    await client().star(params)
    return
  }
}

export const useFetchUnstar = () => {
  const client = useClient()

  return async (params: StarParams) => {
    await client().unstar(params)
    return
  }
}
