import { Album, AlbumCoverArt, Artist, Playlist, Song } from '@app/models/library'
import { CollectionById } from '@app/models/state'
import queryClient from '@app/queryClient'
import { GetAlbumList2TypeBase, StarParams } from '@app/subsonic/params'
import uniq from 'lodash.uniq'
import { useInfiniteQuery, useMutation, useQueries, useQuery, UseQueryResult } from 'react-query'
import {
  useFetchAlbum,
  useFetchAlbumList,
  useFetchArtist,
  useFetchArtistInfo,
  useFetchArtists,
  useFetchArtistTopSongs,
  useFetchPlaylist,
  useFetchPlaylists,
  useFetchSong,
  useFetchStar,
  useFetchUnstar,
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
    ['albumList', type, size],
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

export const useStar = (id: string, type: 'song' | 'album' | 'artist') => {
  const fetchStar = useFetchStar()
  const fetchUnstar = useFetchUnstar()
  const fetchSong = useFetchSong()
  const fetchAlbum = useFetchAlbum()
  const fetchArtist = useFetchArtist()

  const query = useQuery(
    ['starredItems', id],
    async () => {
      switch (type) {
        case 'album':
          console.log('fetch album starred', id)
          return !!(await fetchAlbum(id)).album.starred
        case 'artist':
          console.log('fetch artist starred', id)
          return !!(await fetchArtist(id)).artist.starred
        default:
          console.log('fetch song starred', id)
          return !!(await fetchSong(id)).starred
      }
    },
    {
      cacheTime: Infinity,
      staleTime: Infinity,
    },
  )

  const toggle = useMutation(
    () => {
      const params: StarParams = {
        id: type === 'song' ? id : undefined,
        albumId: type === 'album' ? id : undefined,
        artistId: type === 'artist' ? id : undefined,
      }
      return !query.data ? fetchStar(params) : fetchUnstar(params)
    },
    {
      onMutate: () => {
        queryClient.setQueryData<boolean>(['starredItems', id], !query.data)
      },
      onSuccess: () => {
        if (type === 'album') {
          queryClient.invalidateQueries(['albumList', 'starred'])
        }
      },
    },
  )

  return { query, toggle }
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
      queryFn: (): Promise<AlbumCoverArt> => {
        console.log('fetch album coverArt')
        return fetchAlbum(id).then(res => ({ albumId: res.album.id, coverArt: res.album.coverArt }))
      },
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