import { Playlist, Song, Album, AlbumCoverArt } from '@app/models/library'
import { mapArtist, mapAlbum, mapPlaylist, mapSong, mapArtistInfo } from '@app/models/map'
import queryClient from '@app/queryClient'
import { useStore } from '@app/state/store'
import { GetAlbumList2TypeBase, Search3Params, StarParams } from '@app/subsonic/params'
import { mapCollectionById } from '@app/util/state'
import qk from './queryKeys'
import RNFS from 'react-native-fs'
import { CacheItemTypeKey } from '@app/models/cache'
import path from 'path'
import ReactNativeBlobUtil from 'react-native-blob-util'
import mime from 'mime-types'
import userAgent from '@app/util/userAgent'
import { cacheDir } from '@app/util/fs'

export const useClient = () => {
  const client = useStore(store => store.client)

  return () => {
    if (!client) {
      throw new Error('no client!')
    }

    return client
  }
}

function cacheStarredData<T extends { id: string; starred?: undefined | any }>(item: T) {
  queryClient.setQueryData<boolean>(qk.starredItems(item.id), !!item.starred)
}

function cacheAlbumCoverArtData<T extends { id: string; coverArt?: string }>(item: T) {
  queryClient.setQueryData<AlbumCoverArt>(qk.albumCoverArt(item.id), { albumId: item.id, coverArt: item.coverArt })
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

export const useFetchAlbum = () => {
  const client = useClient()

  return async (id: string): Promise<{ album: Album; songs?: Song[] }> => {
    const res = await client().getAlbum({ id })

    cacheStarredData(res.data.album)
    res.data.songs.forEach(cacheStarredData)

    cacheAlbumCoverArtData(res.data.album)

    return {
      album: mapAlbum(res.data.album),
      songs: res.data.songs.map(mapSong),
    }
  }
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

export type FetchExisingFileOptions = {
  serverId: string
  itemType: CacheItemTypeKey
  itemId: string
}

export const useFetchExistingFile: () => (options: FetchExisingFileOptions) => Promise<string | undefined> = () => {
  return async ({ serverId, itemType, itemId }) => {
    const fileDir = cacheDir(serverId, itemType, itemId)
    try {
      const dir = await RNFS.readDir(fileDir)
      console.log('existing file:', dir[0].path)
      return dir[0].path
    } catch {}
  }
}

function assertMimeType(expected?: string, actual?: string) {
  expected = expected?.toLowerCase()
  actual = actual?.toLowerCase()

  if (!expected || expected === actual) {
    return
  }

  if (!expected.includes(';')) {
    actual = actual?.split(';')[0]
  }

  if (!expected.includes('/')) {
    actual = actual?.split('/')[0]
  }

  if (expected !== actual) {
    throw new Error(`Request does not satisfy expected content type. Expected: ${expected} Actual: ${actual}`)
  }
}

export type FetchFileOptions = FetchExisingFileOptions & {
  fromUrl: string
  expectedContentType?: string
}

export const useFetchFile: () => (options: FetchFileOptions) => Promise<string> = () => {
  return async ({ serverId, itemType, itemId, fromUrl, expectedContentType }) => {
    const fileDir = cacheDir(serverId, itemType, itemId)
    const filePathNoExt = path.join(fileDir, useStore.getState().settings.cacheBuster)

    try {
      await RNFS.unlink(fileDir)
    } catch {}

    const headers = { 'User-Agent': userAgent }

    // we send a HEAD first for two reasons:
    // 1. to follow any redirects and get the actual URL (DownloadManager does not support redirects)
    // 2. to obtain the mime-type up front so we can use it for the file extension/validation
    const headRes = await fetch(fromUrl, { method: 'HEAD', headers })

    if (headRes.status > 399) {
      throw new Error(`HTTP status error ${headRes.status}. File: ${itemType} ID: ${itemId}`)
    }

    const contentType = headRes.headers.get('content-type') || undefined
    assertMimeType(expectedContentType, contentType)

    const extension = contentType ? mime.extension(contentType) : undefined

    const req = await ReactNativeBlobUtil.config({
      addAndroidDownloads: {
        useDownloadManager: true,
        notification: false,
        mime: contentType,
        description: 'subtracks',
        path: extension ? `${filePathNoExt}.${extension}` : filePathNoExt,
      },
    }).fetch('GET', headRes.url, headers)

    const downloadPath = req.path()
    queryClient.setQueryData<string>(qk.existingFiles(itemType, itemId), downloadPath)

    console.log('downloaded file:', downloadPath)
    return downloadPath
  }
}
