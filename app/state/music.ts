import {
  Album,
  AlbumListItem,
  AlbumWithSongs,
  Artist,
  ArtistInfo,
  PlaylistListItem,
  PlaylistWithSongs,
  SearchResults,
  Song,
} from '@app/models/music'
import { activeServerAtom, homeListTypesAtom } from '@app/state/settings'
import { SubsonicApiClient } from '@app/subsonic/api'
import {
  AlbumID3Element,
  ArtistID3Element,
  ArtistInfo2Element,
  ChildElement,
  PlaylistElement,
  PlaylistWithSongsElement,
} from '@app/subsonic/elements'
import { GetAlbumList2Type, GetCoverArtParams } from '@app/subsonic/params'
import { GetArtistResponse } from '@app/subsonic/responses'
import { atom, useAtom } from 'jotai'
import { atomFamily, useAtomValue, useUpdateAtom } from 'jotai/utils'

export const artistsAtom = atom<Artist[]>([])
export const artistsUpdatingAtom = atom(false)

export const useUpdateArtists = () => {
  const server = useAtomValue(activeServerAtom)
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
    const response = await client.getArtists()

    setArtists(response.data.artists.map(mapArtistID3toArtist))
    setUpdating(false)
  }
}

export type HomeLists = { [key: string]: AlbumListItem[] }

export const homeListsUpdatingAtom = atom(false)
export const homeListsAtom = atom<HomeLists>({})
const homeListsWriteAtom = atom<HomeLists, { type: string; albums: AlbumListItem[] }>(
  get => get(homeListsAtom),
  (get, set, { type, albums }) => {
    const lists = get(homeListsAtom)
    set(homeListsAtom, {
      ...lists,
      [type]: albums,
    })
  },
)

export const useUpdateHomeLists = () => {
  const server = useAtomValue(activeServerAtom)
  const types = useAtomValue(homeListTypesAtom)
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

    const promises: Promise<any>[] = []
    for (const type of types) {
      promises.push(
        client.getAlbumList2({ type: type as GetAlbumList2Type, size: 20 }).then(response => {
          updateHomeList({ type, albums: response.data.albums.map(mapAlbumID3toAlbumListItem) })
        }),
      )
    }
    await Promise.all(promises)

    setUpdating(false)
  }
}

export const searchResultsUpdatingAtom = atom(false)
export const searchResultsAtom = atom<SearchResults>({
  artists: [],
  albums: [],
  songs: [],
})

export const useUpdateSearchResults = () => {
  const server = useAtomValue(activeServerAtom)
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
    const response = await client.search3({ query })

    updateList({
      artists: response.data.artists.map(mapArtistID3toArtist),
      albums: response.data.albums.map(mapAlbumID3toAlbumListItem),
      songs: response.data.songs.map(a => mapChildToSong(a, client)),
    })
    setUpdating(false)
  }
}

export const playlistsUpdatingAtom = atom(false)
export const playlistsAtom = atom<PlaylistListItem[]>([])

export const useUpdatePlaylists = () => {
  const server = useAtomValue(activeServerAtom)
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
    const response = await client.getPlaylists()

    updateList(response.data.playlists.map(mapPlaylistListItem))
    setUpdating(false)
  }
}

export const playlistAtomFamily = atomFamily((id: string) =>
  atom<PlaylistWithSongs | undefined>(async get => {
    const server = get(activeServerAtom)
    if (!server) {
      return undefined
    }

    const client = new SubsonicApiClient(server)
    const response = await client.getPlaylist({ id })
    return mapPlaylistWithSongs(response.data.playlist, client)
  }),
)

export const albumListUpdatingAtom = atom(false)
export const albumListAtom = atom<AlbumListItem[]>([])

export const useUpdateAlbumList = () => {
  const server = useAtomValue(activeServerAtom)
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
    const response = await client.getAlbumList2({ type: 'alphabeticalByArtist', size: 500 })

    updateList(response.data.albums.map(mapAlbumID3toAlbumListItem))
    setUpdating(false)
  }
}

export const albumAtomFamily = atomFamily((id: string) =>
  atom<AlbumWithSongs | undefined>(async get => {
    const server = get(activeServerAtom)
    if (!server) {
      return undefined
    }

    const client = new SubsonicApiClient(server)
    const response = await client.getAlbum({ id })
    return mapAlbumID3WithSongstoAlbunWithSongs(response.data.album, response.data.songs, client)
  }),
)

export const artistInfoAtomFamily = atomFamily((id: string) =>
  atom<ArtistInfo | undefined>(async get => {
    const server = get(activeServerAtom)
    if (!server) {
      return undefined
    }

    const client = new SubsonicApiClient(server)
    const [artistResponse, artistInfoResponse] = await Promise.all([
      client.getArtist({ id }),
      client.getArtistInfo2({ id }),
    ])
    const topSongsResponse = await client.getTopSongs({ artist: artistResponse.data.artist.name, count: 50 })

    return mapArtistInfo(artistResponse.data, artistInfoResponse.data.artistInfo, topSongsResponse.data.songs, client)
  }),
)

export const useCoverArtUri = () => {
  const server = useAtomValue(activeServerAtom)

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

function mapArtistID3toArtist(artist: ArtistID3Element): Artist {
  return {
    itemType: 'artist',
    id: artist.id,
    name: artist.name,
    starred: artist.starred,
    coverArt: artist.coverArt,
  }
}

function mapArtistInfo(
  artistResponse: GetArtistResponse,
  info: ArtistInfo2Element,
  topSongs: ChildElement[],
  client: SubsonicApiClient,
): ArtistInfo {
  const { artist, albums } = artistResponse

  const mappedAlbums = albums.map(mapAlbumID3toAlbum)

  return {
    ...mapArtistID3toArtist(artist),
    albums: mappedAlbums,
    smallImageUrl: info.smallImageUrl,
    mediumImageUrl: info.mediumImageUrl,
    largeImageUrl: info.largeImageUrl,
    topSongs: topSongs.map(s => mapChildToSong(s, client)).slice(0, 5),
  }
}

function mapAlbumID3toAlbumListItem(album: AlbumID3Element): AlbumListItem {
  return {
    itemType: 'album',
    id: album.id,
    name: album.name,
    artist: album.artist,
    starred: album.starred,
    coverArt: album.coverArt,
  }
}

function mapAlbumID3toAlbum(album: AlbumID3Element): Album {
  return {
    ...mapAlbumID3toAlbumListItem(album),
    coverArt: album.coverArt,
    year: album.year,
  }
}

function mapChildToSong(child: ChildElement, client: SubsonicApiClient): Song {
  return {
    itemType: 'song',
    id: child.id,
    album: child.album,
    artist: child.artist,
    title: child.title,
    track: child.track,
    duration: child.duration,
    starred: child.starred,
    coverArt: child.coverArt,
    streamUri: client.streamUri({ id: child.id }),
  }
}

function mapAlbumID3WithSongstoAlbunWithSongs(
  album: AlbumID3Element,
  songs: ChildElement[],
  client: SubsonicApiClient,
): AlbumWithSongs {
  return {
    ...mapAlbumID3toAlbum(album),
    songs: songs.map(s => mapChildToSong(s, client)),
  }
}

function mapPlaylistListItem(playlist: PlaylistElement): PlaylistListItem {
  return {
    itemType: 'playlist',
    id: playlist.id,
    name: playlist.name,
    comment: playlist.comment,
    coverArt: playlist.coverArt,
  }
}

function mapPlaylistWithSongs(playlist: PlaylistWithSongsElement, client: SubsonicApiClient): PlaylistWithSongs {
  return {
    ...mapPlaylistListItem(playlist),
    songs: playlist.songs.map(s => mapChildToSong(s, client)),
    coverArt: playlist.coverArt,
  }
}
