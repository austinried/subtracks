import { Album, AlbumCoverArt, Artist, Playlist, Song } from '@app/models/library'
import { CollectionById } from '@app/models/state'
import queryClient from '@app/queryClient'
import { GetAlbumList2TypeBase } from '@app/subsonic/params'
import uniq from 'lodash.uniq'
import { useInfiniteQuery, useQueries, useQuery, UseQueryResult } from 'react-query'
import {
  useFetchAlbum,
  useFetchAlbumList,
  useFetchArtist,
  useFetchArtistInfo,
  useFetchArtists,
  useFetchArtistTopSongs,
  useFetchPlaylist,
  useFetchPlaylists,
} from './fetch'

export const useQueryArtists = () => useQuery('artists', useFetchArtists())

export const useQueryArtist = (id: string) => {
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

export const useQueryArtistInfo = (id: string) => {
  const fetchArtistInfo = useFetchArtistInfo()
  return useQuery(['artistInfo', id], () => fetchArtistInfo(id))
}

export const useQueryArtistTopSongs = (artistName?: string) => {
  const fetchArtistTopSongs = useFetchArtistTopSongs()
  const query = useQuery(['artistTopSongs', artistName], () => fetchArtistTopSongs(artistName as string), {
    enabled: !!artistName,
  })

  return useFixCoverArt(query)
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

export const useQueryAlbum = (id: string, initialAlbum?: Album) => {
  const fetchAlbum = useFetchAlbum()

  const query = useQuery(['album', id], () => fetchAlbum(id), {
    initialData: (): { album: Album; songs?: Song[] } | undefined =>
      initialAlbum ? { album: initialAlbum } : undefined,
  })

  return useFixCoverArt(query)
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

// song cover art comes back from the api as a unique id per song even if it all points to the same
// album art, which prevents us from caching it once, so we need to use the album's cover art
const useFixCoverArt = <T extends Song[] | { songs?: Song[] }>(query: UseQueryResult<T>) => {
  const fetchAlbum = useFetchAlbum()

  const songs = Array.isArray(query.data) ? (query.data as Song[]) : query.data?.songs
  const albumIds = uniq((songs || []).map(s => s.albumId).filter((id): id is string => id !== undefined))

  const coverArts = useQueries(
    albumIds.map(id => ({
      queryKey: ['albumCoverArt', id],
      queryFn: (): Promise<AlbumCoverArt> =>
        fetchAlbum(id).then(res => ({ albumId: res.album.id, coverArt: res.album.coverArt })),
      staleTime: Infinity,
      cacheTime: Infinity,
    })),
  )

  const mapSongCoverArt = (song: Song) => ({
    ...song,
    coverArt: coverArts.find(c => c.data?.albumId === song.albumId)?.data?.coverArt,
  })

  if (Array.isArray(query.data) && coverArts.some(c => c.data)) {
    query = {
      ...query,
      data: query.data.map(mapSongCoverArt),
    } as UseQueryResult<T>
  } else if (query.data && 'songs' in query.data && query.data.songs && coverArts.some(c => c.data)) {
    query = {
      ...query,
      data: {
        ...query.data,
        songs: query.data.songs.map(mapSongCoverArt),
      },
    } as UseQueryResult<T>
  }

  return query
}
