import { Album, AlbumCoverArt, Artist, Playlist, Song, StarrableItemType } from '@app/models/library'
import { CollectionById } from '@app/models/state'
import queryClient from '@app/queryClient'
import { GetAlbumList2TypeBase, Search3Params, StarParams } from '@app/subsonic/params'
import uniq from 'lodash.uniq'
import {
  InfiniteData,
  useInfiniteQuery,
  UseInfiniteQueryResult,
  useMutation,
  useQueries,
  useQuery,
  UseQueryResult,
} from 'react-query'
import {
  useFetchAlbum,
  useFetchAlbumList,
  useFetchArtist,
  useFetchArtistInfo,
  useFetchArtists,
  useFetchArtistTopSongs,
  useFetchPlaylist,
  useFetchPlaylists,
  useFetchSearchResults,
  useFetchSong,
  useFetchStar,
  useFetchUnstar,
} from './fetch'
import qk from './queryKeys'

export const useQueryArtists = () => useQuery(qk.artists, useFetchArtists())

export const useQueryArtist = (id: string) => {
  const fetchArtist = useFetchArtist()

  return useQuery(qk.artist(id), () => fetchArtist(id), {
    initialData: () => {
      const artist = queryClient.getQueryData<CollectionById<Artist>>(qk.artists)?.byId[id]
      if (artist) {
        return { artist, albums: [] }
      }
    },
  })
}

export const useQueryArtistInfo = (id: string) => {
  const fetchArtistInfo = useFetchArtistInfo()
  return useQuery(qk.artistInfo(id), () => fetchArtistInfo(id))
}

export const useQueryArtistTopSongs = (artistName?: string) => {
  const fetchArtistTopSongs = useFetchArtistTopSongs()
  const query = useQuery(qk.artistTopSongs(artistName as string), () => fetchArtistTopSongs(artistName as string), {
    enabled: !!artistName,
  })

  return useFixCoverArt(query)
}

export const useQueryPlaylists = () => useQuery(qk.playlists, useFetchPlaylists())

export const useQueryPlaylist = (id: string, initialPlaylist?: Playlist) => {
  const fetchPlaylist = useFetchPlaylist()

  const query = useQuery(qk.playlist(id), () => fetchPlaylist(id), {
    initialData: () => {
      if (initialPlaylist) {
        return { playlist: initialPlaylist }
      }

      const playlist = queryClient.getQueryData<CollectionById<Playlist>>(qk.playlists)?.byId[id]
      if (playlist) {
        return { playlist, songs: [] }
      }
    },
  })

  return useFixCoverArt(query)
}

export const useQueryAlbum = (id: string, initialAlbum?: Album) => {
  const fetchAlbum = useFetchAlbum()

  const query = useQuery(qk.album(id), () => fetchAlbum(id), {
    initialData: (): { album: Album; songs?: Song[] } | undefined =>
      initialAlbum ? { album: initialAlbum } : undefined,
  })

  return useFixCoverArt(query)
}

export const useQueryAlbumList = (type: GetAlbumList2TypeBase, size: number) => {
  const fetchAlbumList = useFetchAlbumList()

  return useInfiniteQuery(
    qk.albumList(type, size),
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

export const useQuerySearchResults = (params: Search3Params) => {
  const fetchSearchResults = useFetchSearchResults()

  const query = useInfiniteQuery(
    qk.search(params.query, params.artistCount, params.albumCount, params.songCount),
    async context => {
      return await fetchSearchResults({
        ...params,
        artistOffset: context.pageParam?.artistOffset || 0,
        albumOffset: context.pageParam?.albumOffset || 0,
        songOffset: context.pageParam?.songOffset || 0,
      })
    },
    {
      getNextPageParam: (lastPage, allPages) => {
        if (lastPage.albums.length + lastPage.artists.length + lastPage.songs.length === 0) {
          return
        }
        return {
          artistOffset: allPages.reduce((acc, val) => (acc += val.artists.length), 0),
          albumOffset: allPages.reduce((acc, val) => (acc += val.albums.length), 0),
          songOffset: allPages.reduce((acc, val) => (acc += val.songs.length), 0),
        }
      },
      cacheTime: 1000 * 60,
      enabled: !!params.query && params.query.length > 1,
    },
  )

  return useFixCoverArt(query)
}

export const useQueryHomeLists = (types: GetAlbumList2TypeBase[], size: number) => {
  const fetchAlbumList = useFetchAlbumList()

  const listQueries = useQueries(
    types.map(type => {
      return {
        queryKey: qk.albumList(type, size),
        queryFn: async () => {
          const albums = await fetchAlbumList(size, 0, type as GetAlbumList2TypeBase)
          return { type, albums }
        },
      }
    }),
  )

  return listQueries
}

export const useStar = (id: string, type: StarrableItemType) => {
  const fetchStar = useFetchStar()
  const fetchUnstar = useFetchUnstar()
  const fetchSong = useFetchSong()
  const fetchAlbum = useFetchAlbum()
  const fetchArtist = useFetchArtist()

  const query = useQuery(
    qk.starredItems(id),
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
        queryClient.setQueryData<boolean>(qk.starredItems(id), !query.data)
      },
      onSuccess: () => {
        if (type === 'album') {
          queryClient.invalidateQueries(qk.albumList('starred'))
        }
      },
    },
  )

  return { query, toggle }
}

type WithSongs = Song[] | { songs?: Song[] }
type InfiniteWithSongs = { songs: Song[] }
type AnyDataWithSongs = WithSongs | InfiniteData<InfiniteWithSongs>
type AnyQueryWithSongs = UseQueryResult<WithSongs> | UseInfiniteQueryResult<{ songs: Song[] }>

function getSongs<T extends AnyDataWithSongs>(data: T | undefined): Song[] {
  if (!data) {
    return []
  }

  if (Array.isArray(data)) {
    return data
  }

  if ('pages' in data) {
    return data.pages.flatMap(p => p.songs)
  }

  return data.songs || []
}

function setSongCoverArt<T extends AnyQueryWithSongs>(query: T, coverArts: UseQueryResult<AlbumCoverArt>[]): void {
  if (!query.data) {
    return
  }

  const mapSongCoverArt = (song: Song) => ({
    ...song,
    coverArt: coverArts.find(c => c.data?.albumId === song.albumId)?.data?.coverArt,
  })

  if (Array.isArray(query.data)) {
    query.data = query.data.map(mapSongCoverArt)
    return
  }

  if ('pages' in query.data) {
    for (const p of query.data.pages) {
      p.songs = p.songs.map(mapSongCoverArt)
    }
    return
  }

  if (query.data.songs) {
    query.data.songs = query.data.songs.map(mapSongCoverArt)
    return
  }
}

// song cover art comes back from the api as a unique id per song even if it all points to the same
// album art, which prevents us from caching it once, so we need to use the album's cover art
const useFixCoverArt = <T extends AnyQueryWithSongs>(query: T) => {
  const fetchAlbum = useFetchAlbum()

  const songs = getSongs(query.data)
  const albumIds = uniq((songs || []).map(s => s.albumId).filter((id): id is string => id !== undefined))

  const coverArts = useQueries(
    albumIds.map(id => ({
      queryKey: qk.albumCoverArt(id),
      queryFn: async (): Promise<AlbumCoverArt> => {
        const res = await fetchAlbum(id)
        return { albumId: res.album.id, coverArt: res.album.coverArt }
      },
      staleTime: Infinity,
      cacheTime: Infinity,
    })),
  )

  if (coverArts.some(c => c.data)) {
    setSongCoverArt(query, coverArts)
  }

  return query
}
