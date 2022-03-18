import { Store } from '@app/state/store'
import {
  AlbumID3Element,
  ArtistID3Element,
  ArtistInfo2Element,
  ChildElement,
  PlaylistElement,
} from '@app/subsonic/elements'
import { GetAlbumList2Params, Search3Params, StarParams } from '@app/subsonic/params'
import {
  GetAlbumList2Response,
  GetAlbumResponse,
  GetArtistInfo2Response,
  GetArtistResponse,
  GetArtistsResponse,
  GetPlaylistResponse,
  GetPlaylistsResponse,
  GetTopSongsResponse,
  Search3Response,
  SubsonicResponse,
} from '@app/subsonic/responses'
import produce from 'immer'
import merge from 'lodash.merge'
import pick from 'lodash.pick'
import { GetState, SetState } from 'zustand'

export interface ById<T> {
  [id: string]: T
}

export type OneToMany = ById<string[]>

export interface OrderedById<T> {
  byId: ById<T>
  allIds: string[]
}

export interface PaginatedList {
  [offset: number]: string[]
}

export interface Artist {
  itemType: 'artist'
  id: string
  name: string
  starred?: Date
  coverArt?: string
}

export interface ArtistInfo {
  id: string
  smallImageUrl?: string
  largeImageUrl?: string
}

export interface Album {
  itemType: 'album'
  id: string
  name: string
  artist?: string
  artistId?: string
  starred?: Date
  coverArt?: string
  year?: number
}

export interface Playlist {
  itemType: 'playlist'
  id: string
  name: string
  comment?: string
  coverArt?: string
}

export interface Song {
  itemType: 'song'
  id: string
  album?: string
  albumId?: string
  artist?: string
  artistId?: string
  title: string
  track?: number
  discNumber?: number
  duration?: number
  starred?: Date
  coverArt?: string
}

export interface SearchResults {
  artists: string[]
  albums: string[]
  songs: string[]
}

function mapArtist(artist: ArtistID3Element): Artist {
  return {
    itemType: 'artist',
    id: artist.id,
    name: artist.name,
    starred: artist.starred,
    coverArt: artist.coverArt,
  }
}

function mapArtistInfo(id: string, info: ArtistInfo2Element): ArtistInfo {
  return {
    id,
    smallImageUrl: info.smallImageUrl,
    largeImageUrl: info.largeImageUrl,
  }
}

function mapAlbum(album: AlbumID3Element): Album {
  return {
    itemType: 'album',
    id: album.id,
    name: album.name,
    artist: album.artist,
    artistId: album.artist,
    starred: album.starred,
    coverArt: album.coverArt,
    year: album.year,
  }
}

function mapPlaylist(playlist: PlaylistElement): Playlist {
  return {
    itemType: 'playlist',
    id: playlist.id,
    name: playlist.name,
    comment: playlist.comment,
    coverArt: playlist.coverArt,
  }
}

function mapSong(song: ChildElement): Song {
  return {
    itemType: 'song',
    id: song.id,
    album: song.album,
    albumId: song.albumId,
    artist: song.artist,
    artistId: song.artistId,
    title: song.title,
    track: song.track,
    discNumber: song.discNumber,
    duration: song.duration,
    starred: song.starred,
    coverArt: song.coverArt,
  }
}

function mapId(entities: { id: string }[]): string[] {
  return entities.map(e => e.id)
}

export type LibrarySlice = {
  entities: {
    artists: ById<Artist>
    artistInfo: ById<ArtistInfo>
    artistAlbums: OneToMany
    artistNameTopSongs: OneToMany

    albums: ById<Album>
    albumSongs: OneToMany

    // todo: remove these and store in component state
    albumsList: PaginatedList
    albumsListSize: number

    playlists: ById<Playlist>
    playlistSongs: OneToMany

    songs: ById<Song>
  }

  resetLibrary: () => void

  fetchLibraryArtists: () => Promise<void>
  fetchLibraryArtist: (id: string) => Promise<void>
  fetchLibraryArtistInfo: (artistId: string) => Promise<void>
  fetchLibraryArtistTopSongs: (artistName: string) => Promise<void>
  resetLibraryArtists: () => void

  fetchLibraryAlbum: (id: string) => Promise<void>

  fetchLibraryPlaylists: () => Promise<void>
  fetchLibraryPlaylist: (id: string) => Promise<void>

  fetchLibraryAlbumList: (params: GetAlbumList2Params) => Promise<string[]>
  fetchLibrarySearchResults: (params: Search3Params) => Promise<SearchResults>
  star: (params: StarParams) => Promise<void>
  unstar: (params: StarParams) => Promise<void>
}

function reduceById<T extends { id: string }>(collection: T[]): ById<T> {
  return collection.reduce((acc, value) => {
    acc[value.id] = value
    return acc
  }, {} as ById<T>)
}

function mergeById<T extends { [id: string]: unknown }>(object: T, source: T): void {
  merge(object, source)
}

const defaultEntities = () => ({
  artists: {},
  artistAlbums: {},
  artistInfo: {},
  artistNameTopSongs: {},

  albums: {},
  albumsList: {},
  albumsListSize: 300,
  albumSongs: {},

  playlists: {},
  playlistSongs: {},

  songs: {},
})

export const createLibrarySlice = (set: SetState<Store>, get: GetState<Store>): LibrarySlice => ({
  entities: defaultEntities(),

  resetLibrary: () => {
    set(store => {
      store.entities = defaultEntities()
    })
  },

  fetchLibraryArtists: async () => {
    const client = get().client
    if (!client) {
      return
    }

    let response: SubsonicResponse<GetArtistsResponse>
    try {
      response = await client.getArtists()
    } catch {
      return
    }

    const artists = response.data.artists.map(mapArtist)
    const artistsById = reduceById(artists)

    set(
      produce<LibrarySlice>(state => {
        state.entities.artists = artistsById
        state.entities.artistAlbums = pick(state.entities.artistAlbums, mapId(artists))
      }),
    )
  },

  fetchLibraryArtist: async id => {
    const client = get().client
    if (!client) {
      return
    }

    let response: SubsonicResponse<GetArtistResponse>
    try {
      response = await client.getArtist({ id })
    } catch {
      return
    }

    const artist = mapArtist(response.data.artist)
    const albums = response.data.albums.map(mapAlbum)
    const albumsById = reduceById(albums)

    set(
      produce<LibrarySlice>(state => {
        state.entities.artists[id] = artist
        state.entities.artistAlbums[id] = mapId(albums)
        mergeById(state.entities.albums, albumsById)
      }),
    )
  },

  resetLibraryArtists: () => {
    set(
      produce<LibrarySlice>(state => {
        state.entities.artists = {}
        state.entities.artistAlbums = {}
      }),
    )
  },

  fetchLibraryArtistInfo: async id => {
    const client = get().client
    if (!client) {
      return
    }

    let response: SubsonicResponse<GetArtistInfo2Response>
    try {
      response = await client.getArtistInfo2({ id })
    } catch {
      return
    }

    const info = mapArtistInfo(id, response.data.artistInfo)

    set(
      produce<LibrarySlice>(state => {
        state.entities.artistInfo[id] = info
      }),
    )
  },

  fetchLibraryArtistTopSongs: async artistName => {
    const client = get().client
    if (!client) {
      return
    }

    let response: SubsonicResponse<GetTopSongsResponse>
    try {
      response = await client.getTopSongs({ artist: artistName, count: 50 })
    } catch {
      return
    }

    const topSongs = response.data.songs.map(mapSong)
    const topSongsById = reduceById(topSongs)

    set(
      produce<LibrarySlice>(state => {
        mergeById(state.entities.songs, topSongsById)
        state.entities.artistNameTopSongs[artistName] = mapId(topSongs)
      }),
    )
  },

  fetchLibraryAlbum: async id => {
    const client = get().client
    if (!client) {
      return
    }

    let response: SubsonicResponse<GetAlbumResponse>
    try {
      response = await client.getAlbum({ id })
    } catch {
      return
    }

    const album = mapAlbum(response.data.album)
    const songs = response.data.songs.map(mapSong)
    const songsById = reduceById(songs)

    set(
      produce<LibrarySlice>(state => {
        state.entities.albums[id] = album
        state.entities.albumSongs[id] = mapId(songs)
        mergeById(state.entities.songs, songsById)
      }),
    )
  },

  fetchLibraryPlaylists: async () => {
    const client = get().client
    if (!client) {
      return
    }

    let response: SubsonicResponse<GetPlaylistsResponse>
    try {
      response = await client.getPlaylists()
    } catch {
      return
    }

    const playlists = response.data.playlists.map(mapPlaylist)
    const playlistsById = reduceById(playlists)

    set(
      produce<LibrarySlice>(state => {
        state.entities.playlists = playlistsById
        state.entities.playlistSongs = pick(state.entities.playlistSongs, mapId(playlists))
      }),
    )
  },

  fetchLibraryPlaylist: async id => {
    const client = get().client
    if (!client) {
      return
    }

    let response: SubsonicResponse<GetPlaylistResponse>
    try {
      response = await client.getPlaylist({ id })
    } catch {
      return
    }

    const playlist = mapPlaylist(response.data.playlist)
    const songs = response.data.playlist.songs.map(mapSong)
    const songsById = reduceById(songs)

    set(
      produce<LibrarySlice>(state => {
        state.entities.playlists[id] = playlist
        state.entities.playlistSongs[id] = mapId(songs)
        mergeById(state.entities.songs, songsById)
      }),
    )
  },

  fetchLibraryAlbumList: async params => {
    const client = get().client
    if (!client) {
      return []
    }

    let response: SubsonicResponse<GetAlbumList2Response>
    try {
      response = await client.getAlbumList2(params)
    } catch {
      return []
    }

    const albums = response.data.albums.map(mapAlbum)
    const albumsById = reduceById(albums)

    set(
      produce<LibrarySlice>(state => {
        mergeById(state.entities.albums, albumsById)
      }),
    )

    return mapId(albums)
  },

  fetchLibrarySearchResults: async params => {
    const empty = { artists: [], albums: [], songs: [] }

    const client = get().client
    if (!client) {
      return empty
    }

    let response: SubsonicResponse<Search3Response>
    try {
      response = await client.search3(params)
    } catch {
      return empty
    }

    const artists = response.data.artists.map(mapArtist)
    const artistsById = reduceById(artists)
    const albums = response.data.albums.map(mapAlbum)
    const albumsById = reduceById(albums)
    const songs = response.data.songs.map(mapSong)
    const songsById = reduceById(songs)

    set(
      produce<LibrarySlice>(state => {
        mergeById(state.entities.artists, artistsById)
        mergeById(state.entities.albums, albumsById)
        mergeById(state.entities.songs, songsById)
      }),
    )

    return {
      artists: mapId(artists),
      albums: mapId(albums),
      songs: mapId(songs),
    }
  },

  star: async params => {
    const client = get().client
    if (!client) {
      return
    }

    try {
      await client.star(params)
    } catch {
      return
    }

    set(
      produce<LibrarySlice>(state => {
        if (params.id) {
          state.entities.songs[params.id].starred = new Date()
        } else if (params.albumId) {
          state.entities.albums[params.albumId].starred = new Date()
        } else if (params.artistId) {
          state.entities.artists[params.artistId].starred = new Date()
        }
      }),
    )
  },

  unstar: async params => {
    const client = get().client
    if (!client) {
      return
    }

    try {
      await client.unstar(params)
    } catch {
      return
    }

    set(
      produce<LibrarySlice>(state => {
        if (params.id) {
          state.entities.songs[params.id].starred = undefined
        } else if (params.albumId) {
          state.entities.albums[params.albumId].starred = undefined
        } else if (params.artistId) {
          state.entities.artists[params.artistId].starred = undefined
        }
      }),
    )
  },
})
