import { Artist, Playlist } from '@app/models/library'
import { mapAlbum, mapArtist, mapPlaylist, mapSong } from '@app/models/map'
import { ById } from '@app/models/state'
import queryClient from '@app/queryClient'
import { useStore } from '@app/state/store'
import { GetAlbumList2TypeBase } from '@app/subsonic/params'
import { mapId, reduceById } from '@app/util/state'
import { QueryFunctionContext, useInfiniteQuery, useQuery, UseQueryResult } from 'react-query'

interface CollectionById<T extends { id: string }> {
  byId: ById<T>
  allIds: string[]
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
  return (context: QueryFunctionContext<['artist', string]>) =>
    client()
      .getArtist({ id: context.queryKey[1] })
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
  return (context: QueryFunctionContext<['playlist', string]>) =>
    client()
      .getPlaylist({ id: context.queryKey[1] })
      .then(res => ({
        playlist: mapPlaylist(res.data.playlist),
        songs: res.data.playlist.songs.map(mapSong),
      }))
}

export const useFetchAlbumList = () => {
  const client = useClient()
  return (size: number, offset: number, type: GetAlbumList2TypeBase) => {
    return client()
      .getAlbumList2({ size, offset, type })
      .then(res => res.data.albums.map(mapAlbum))
  }
}

export const useQueryArtists = () => useQuery('artists', useFetchArtists())

export const useQueryArtist: (id: string) => UseQueryResult<Artist> = id =>
  useQuery(['artist', id], useFetchArtist(), {
    initialData: () => {
      const artist = queryClient.getQueryData<CollectionById<Artist>>('artists')?.byId[id]
      if (artist) {
        return { artist, albums: [] }
      }
    },
  })

export const useQueryPlaylists = () => useQuery('playlists', useFetchPlaylists())

export const useQueryPlaylist = (id: string) =>
  useQuery(['playlist', id], useFetchPlaylist(), {
    initialData: () => {
      const playlist = queryClient.getQueryData<CollectionById<Playlist>>('playlists')?.byId[id]
      if (playlist) {
        return { playlist, songs: [] }
      }
    },
  })

export const useQueryAlbumList = (size: number, type: GetAlbumList2TypeBase) => {
  const fetchAlbumList = useFetchAlbumList()

  return useInfiniteQuery(
    ['albumList', { size, type }],
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
