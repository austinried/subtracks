import {
  Album,
  AlbumListItem,
  AlbumWithSongs,
  Artist,
  ArtistArt,
  ArtistInfo,
  PlaylistListItem,
  PlaylistWithSongs,
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
import { GetAlbumList2Type } from '@app/subsonic/params'
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
          updateHomeList({ type, albums: response.data.albums.map(a => mapAlbumID3toAlbumListItem(a, client)) })
        }),
      )
    }
    await Promise.all(promises)

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

    updateList(response.data.playlists.map(a => mapPlaylistListItem(a, client)))
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

    updateList(response.data.albums.map(a => mapAlbumID3toAlbumListItem(a, client)))
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

export const artistArtAtomFamily = atomFamily((id: string) =>
  atom<ArtistArt | undefined>(async get => {
    const artistInfo = get(artistInfoAtomFamily(id))
    if (!artistInfo) {
      return undefined
    }

    const albumCoverUris = artistInfo.albums
      .filter(a => a.coverArtThumbUri !== undefined)
      .sort((a, b) => {
        if (b.year && a.year) {
          return b.year - a.year
        } else {
          return a.name.localeCompare(b.name)
        }
      })
      .map(a => a.coverArtThumbUri) as string[]

    return {
      albumCoverUris,
      uri: artistInfo.largeImageUrl,
    }
  }),
)

function mapArtistID3toArtist(artist: ArtistID3Element): Artist {
  return {
    id: artist.id,
    name: artist.name,
    starred: artist.starred,
  }
}

function mapArtistInfo(
  artistResponse: GetArtistResponse,
  info: ArtistInfo2Element,
  topSongs: ChildElement[],
  client: SubsonicApiClient,
): ArtistInfo {
  const { artist, albums } = artistResponse

  const mappedAlbums = albums.map(a => mapAlbumID3toAlbum(a, client))
  const albumCoverUris = mappedAlbums
    .sort((a, b) => {
      if (a.year && b.year) {
        return b.year - a.year
      } else {
        return a.name.localeCompare(b.name) - 9000
      }
    })
    .map(a => a.coverArtThumbUri)
    .filter(a => a !== undefined) as string[]

  return {
    ...mapArtistID3toArtist(artist),
    albums: mappedAlbums,
    albumCoverUris,
    mediumImageUrl: info.mediumImageUrl,
    largeImageUrl: info.largeImageUrl,
    topSongs: topSongs.map(c => mapChildToSong(c, client)).slice(0, 5),
  }
}

function mapCoverArtUri(item: { coverArt?: string }, client: SubsonicApiClient) {
  return {
    coverArtUri: item.coverArt ? client.getCoverArtUri({ id: item.coverArt }) : client.getCoverArtUri({ id: '-1' }),
  }
}

function mapCoverArtThumbUri(item: { coverArt?: string }, client: SubsonicApiClient) {
  return {
    coverArtThumbUri: item.coverArt
      ? client.getCoverArtUri({ id: item.coverArt, size: '256' })
      : client.getCoverArtUri({ id: '-1', size: '256' }),
  }
}

function mapAlbumID3toAlbumListItem(album: AlbumID3Element, client: SubsonicApiClient): AlbumListItem {
  return {
    id: album.id,
    name: album.name,
    artist: album.artist,
    starred: album.starred,
    ...mapCoverArtThumbUri(album, client),
  }
}

function mapAlbumID3toAlbum(album: AlbumID3Element, client: SubsonicApiClient): Album {
  return {
    ...mapAlbumID3toAlbumListItem(album, client),
    ...mapCoverArtUri(album, client),
    ...mapCoverArtThumbUri(album, client),
    year: album.year,
  }
}

function mapChildToSong(child: ChildElement, client: SubsonicApiClient): Song {
  return {
    id: child.id,
    album: child.album,
    artist: child.artist,
    title: child.title,
    track: child.track,
    duration: child.duration,
    starred: child.starred,
    streamUri: client.streamUri({ id: child.id }),
    ...mapCoverArtUri(child, client),
    ...mapCoverArtThumbUri(child, client),
  }
}

function mapAlbumID3WithSongstoAlbunWithSongs(
  album: AlbumID3Element,
  songs: ChildElement[],
  client: SubsonicApiClient,
): AlbumWithSongs {
  return {
    ...mapAlbumID3toAlbum(album, client),
    songs: songs.map(s => mapChildToSong(s, client)),
  }
}

function mapPlaylistListItem(playlist: PlaylistElement, client: SubsonicApiClient): PlaylistListItem {
  return {
    id: playlist.id,
    name: playlist.name,
    comment: playlist.comment,
    ...mapCoverArtThumbUri(playlist, client),
  }
}

function mapPlaylistWithSongs(playlist: PlaylistWithSongsElement, client: SubsonicApiClient): PlaylistWithSongs {
  return {
    ...mapPlaylistListItem(playlist, client),
    songs: playlist.songs.map(s => mapChildToSong(s, client)),
    ...mapCoverArtUri(playlist, client),
  }
}
