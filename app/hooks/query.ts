import { Album, Artist, Playlist, Song } from '@app/models/library'
import { mapAlbum, mapArtist, mapPlaylist, mapSong } from '@app/models/map'
import { ById } from '@app/models/state'
import queryClient from '@app/queryClient'
import { useStore } from '@app/state/store'
import { GetAlbumList2TypeBase } from '@app/subsonic/params'
import { mapId, reduceById } from '@app/util/state'
import { Query, useInfiniteQuery, useQueries, useQuery, UseQueryResult } from 'react-query'
import uniq from 'lodash.uniq'

interface CollectionById<T extends { id: string }> {
  byId: ById<T>
  allIds: string[]
}

interface AlbumCoverArt {
  albumId: string
  coverArt?: string
}

function mapCollectionById<T, U extends { id: string }>(collection: T[], map: (item: T) => U): CollectionById<U> {
  const mapped = collection.map(map)
  return {
    byId: reduceById(mapped),
    allIds: mapId(mapped),
  }
}

const useClient = () => {
  const client = useStore(store => store.client)

  return () => {
    if (!client) {
      throw new Error('no client!')
    }

    return client
  }
}

const useFetchArtists = () => {
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

const useFetchPlaylists = () => {
  const client = useClient()
  return () =>
    client()
      .getPlaylists()
      .then(res => mapCollectionById(res.data.playlists, mapPlaylist))
}

const useFetchPlaylist = () => {
  const client = useClient()
  return (id: string): Promise<{ playlist: Playlist; songs?: Song[] }> =>
    client()
      .getPlaylist({ id })
      .then(res => ({
        playlist: mapPlaylist(res.data.playlist),
        songs: res.data.playlist.songs.map(mapSong),
      }))
}

const useFetchAlbum = () => {
  const client = useClient()
  return (id: string): Promise<{ album: Album; songs?: Song[] }> =>
    client()
      .getAlbum({ id })
      .then(res => ({
        album: mapAlbum(res.data.album),
        songs: res.data.songs.map(mapSong),
      }))
      .then(res => {
        console.log('asdf', res.album.name)
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

export const useQueryArtists = () => useQuery('artists', useFetchArtists())

export const useQueryArtist: (id: string) => UseQueryResult<Artist> = id => {
  const fetchArtist = useFetchArtist()

  return useQuery(['artist', id], () => fetchArtist(id), {
    initialData: () => {
      const artist = queryClient.getQueryData<CollectionById<Artist>>('artists')?.byId[id]
      if (artist) {
        return { artist, albums: [] }
      }
    },
  })
}

export const useQueryPlaylists = () => useQuery('playlists', useFetchPlaylists())

export const useQueryPlaylist = (id: string, initialPlaylist?: Playlist) => {
  const fetchPlaylist = useFetchPlaylist()

  const query = useQuery(['playlist', id], () => fetchPlaylist(id), {
    initialData: () => {
      if (initialPlaylist) {
        return { playlist: initialPlaylist }
      }

      const playlist = queryClient.getQueryData<CollectionById<Playlist>>('playlists')?.byId[id]
      if (playlist) {
        return { playlist, songs: [] }
      }
    },
  })

  return useFixCoverArt(query)
}

// song cover art comes back from the api as a unique id per song even if it all points to the same
// album art, which prevents us from caching it once, so we need to use the album's cover art
const useFixCoverArt = <T extends { songs?: Song[] }>(query: UseQueryResult<T>) => {
  const fetchAlbum = useFetchAlbum()
  const albumIds = uniq((query.data?.songs || []).map(s => s.albumId).filter((id): id is string => id !== undefined))
  const coverArts = useQueries(
    albumIds.map(id => ({
      queryKey: ['albumCoverArt', id],
      queryFn: (): Promise<AlbumCoverArt> =>
        fetchAlbum(id).then(res => ({ albumId: res.album.id, coverArt: res.album.coverArt })),
      staleTime: Infinity,
      cacheTime: Infinity,
    })),
  )

  if (query.data && query.data.songs && coverArts.every(c => c.data)) {
    query.data.songs = query.data.songs.map(s => ({
      ...s,
      coverArt: coverArts.find(c => c.data?.albumId === s.albumId)?.data?.coverArt,
    }))
  }

  return query
}

export const useQueryAlbumList = (size: number, type: GetAlbumList2TypeBase) => {
  const fetchAlbumList = useFetchAlbumList()

  return useInfiniteQuery(
    ['albumList', size, type],
    async context => {
      return await fetchAlbumList(size, context.pageParam || 0, type)
    },
    {
      getNextPageParam: (lastPage, allPages) => {
        if (lastPage.length === 0) {
          return
        }
        return allPages.length * size
      },
      cacheTime: 0,
    },
  )
}

export const useQueryAlbum = (id: string, initialAlbum?: Album) => {
  const fetchAlbum = useFetchAlbum()

  const query = useQuery(['album', id], () => fetchAlbum(id), {
    initialData: (): { album: Album; songs?: Song[] } | undefined =>
      initialAlbum ? { album: initialAlbum } : undefined,
  })

  return useFixCoverArt(query)
}
