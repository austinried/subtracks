import { Atom, atom, useAtom, WritableAtom } from 'jotai'
import { atomFamily, useAtomValue, useUpdateAtom } from 'jotai/utils'
import { Album, AlbumListItem, AlbumWithSongs, Artist, ArtistArt, ArtistInfo, Song } from '@app/models/music'
import { SubsonicApiClient } from '@app/subsonic/api'
import { AlbumID3Element, ArtistInfo2Element, ChildElement } from '@app/subsonic/elements'
import { GetArtistResponse } from '@app/subsonic/responses'
import { activeServerAtom } from '@app/state/settings'
import { GetAlbumList2Type } from '@app/subsonic/params'

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

    setArtists(
      response.data.artists.map(x => ({
        id: x.id,
        name: x.name,
        starred: x.starred,
      })),
    )
    setUpdating(false)
  }
}

const useUpdateAlbumListBase = (
  type: GetAlbumList2Type,
  albumListAtom: WritableAtom<AlbumListItem[], AlbumListItem[]>,
  updatingAtom: WritableAtom<boolean, boolean>,
  size: number,
) => {
  const server = useAtomValue(activeServerAtom)
  const setAlbumList = useUpdateAtom(albumListAtom)
  const [updating, setUpdating] = useAtom(updatingAtom)

  if (!server) {
    return async () => {}
  }

  return async () => {
    if (updating) {
      return
    }
    setUpdating(true)

    const client = new SubsonicApiClient(server)
    const response = await client.getAlbumList2({ type, size })

    setAlbumList(response.data.albums.map(a => mapAlbumID3toAlbumListItem(a, client)))
    setUpdating(false)
  }
}

function createAlbumList(type: GetAlbumList2Type, size = 20) {
  const listAtom = atom<AlbumListItem[]>([])
  const listReadAtom = atom(get => get(listAtom))
  const updatingAtom = atom(false)
  const updatingReadAtom = atom(get => get(updatingAtom))
  const useUpdateAlbumList = () => useUpdateAlbumListBase(type, listAtom, updatingAtom, size)

  return { listAtom, listReadAtom, updatingAtom, updatingReadAtom, useUpdateAlbumList }
}

type ListState<T> = {
  listAtom: Atom<T[]>
  updatingAtom: Atom<boolean>
  useUpdateList: () => () => Promise<void>
}

const alphabeticalByArtist = createAlbumList('alphabeticalByArtist', 500)
const recent = createAlbumList('recent')
const starred = createAlbumList('starred')
const frequent = createAlbumList('frequent')
const random = createAlbumList('random')
const newest = createAlbumList('newest')

export const albumLists: { [key: string]: ListState<AlbumListItem> } = {
  alphabeticalByArtist: {
    listAtom: alphabeticalByArtist.listReadAtom,
    updatingAtom: alphabeticalByArtist.updatingReadAtom,
    useUpdateList: alphabeticalByArtist.useUpdateAlbumList,
  },
  recent: {
    listAtom: recent.listReadAtom,
    updatingAtom: recent.updatingReadAtom,
    useUpdateList: recent.useUpdateAlbumList,
  },
  starred: {
    listAtom: starred.listReadAtom,
    updatingAtom: starred.updatingReadAtom,
    useUpdateList: starred.useUpdateAlbumList,
  },
  frequent: {
    listAtom: frequent.listReadAtom,
    updatingAtom: frequent.updatingReadAtom,
    useUpdateList: frequent.useUpdateAlbumList,
  },
  random: {
    listAtom: random.listReadAtom,
    updatingAtom: random.updatingReadAtom,
    useUpdateList: random.useUpdateAlbumList,
  },
  newest: {
    listAtom: newest.listReadAtom,
    updatingAtom: newest.updatingReadAtom,
    useUpdateList: newest.useUpdateAlbumList,
  },
}

export const albumAtomFamily = atomFamily((id: string) =>
  atom<AlbumWithSongs | undefined>(async get => {
    const server = get(activeServerAtom)
    if (!server) {
      return undefined
    }

    const client = new SubsonicApiClient(server)
    const response = await client.getAlbum({ id })
    return mapAlbumID3WithSongs(response.data.album, response.data.songs, client)
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
    return mapArtistInfo(artistResponse.data, artistInfoResponse.data.artistInfo, client)
  }),
)

export const artistArtAtomFamily = atomFamily((id: string) =>
  atom<ArtistArt | undefined>(async get => {
    const artistInfo = get(artistInfoAtomFamily(id))
    if (!artistInfo) {
      return undefined
    }

    const coverArtUris = artistInfo.albums
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
      coverArtUris,
      uri: artistInfo.mediumImageUrl,
    }
  }),
)

function mapArtistInfo(
  artistResponse: GetArtistResponse,
  artistInfo: ArtistInfo2Element,
  client: SubsonicApiClient,
): ArtistInfo {
  const info = { ...artistInfo } as any
  delete info.similarArtists

  const { artist, albums } = artistResponse

  const mappedAlbums = albums.map(a => mapAlbumID3(a, client))
  const coverArtUris = mappedAlbums
    .sort((a, b) => {
      if (a.year && b.year) {
        return a.year - b.year
      } else {
        return a.name.localeCompare(b.name) - 9000
      }
    })
    .map(a => a.coverArtThumbUri)

  return {
    ...artist,
    ...info,
    albums: mappedAlbums,
    coverArtUris,
  }
}

function mapAlbumID3(album: AlbumID3Element, client: SubsonicApiClient): Album {
  return {
    ...album,
    coverArtUri: album.coverArt ? client.getCoverArtUri({ id: album.coverArt }) : undefined,
    coverArtThumbUri: album.coverArt ? client.getCoverArtUri({ id: album.coverArt, size: '256' }) : undefined,
  }
}

function mapAlbumID3toAlbumListItem(album: AlbumID3Element, client: SubsonicApiClient): AlbumListItem {
  return {
    id: album.id,
    name: album.name,
    artist: album.artist,
    starred: album.starred,
    coverArtThumbUri: album.coverArt ? client.getCoverArtUri({ id: album.coverArt, size: '256' }) : undefined,
  }
}

function mapChildToSong(child: ChildElement, client: SubsonicApiClient): Song {
  return {
    ...child,
    streamUri: client.streamUri({ id: child.id }),
    coverArtUri: child.coverArt ? client.getCoverArtUri({ id: child.coverArt }) : undefined,
    coverArtThumbUri: child.coverArt ? client.getCoverArtUri({ id: child.coverArt, size: '256' }) : undefined,
  }
}

function mapAlbumID3WithSongs(
  album: AlbumID3Element,
  songs: ChildElement[],
  client: SubsonicApiClient,
): AlbumWithSongs {
  return {
    ...mapAlbumID3(album, client),
    songs: songs.map(s => mapChildToSong(s, client)),
  }
}
