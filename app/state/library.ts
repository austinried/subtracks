import { Store } from '@app/state/store'
import { AlbumID3Element, ArtistID3Element, ArtistInfo2Element, ChildElement } from '@app/subsonic/elements'
import {
  GetArtistInfo2Response,
  GetArtistResponse,
  GetArtistsResponse,
  GetTopSongsResponse,
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

  // streamUri: string
  coverArt?: string
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

export type LibrarySlice = {
  entities: {
    artists: ById<Artist>
    artistAlbums: OneToMany

    albums: ById<Album>

    artistInfo: ById<ArtistInfo>
    artistNameTopSongs: OneToMany

    songs: ById<Song>
  }

  fetchLibraryArtists: () => Promise<void>
  fetchLibraryArtist: (id: string) => Promise<void>
  resetLibraryArtists: () => Promise<void>
  // fetchAlbums: (artistId: string) => Promise<void>
  fetchLibraryArtistInfo: (artistId: string) => Promise<void>
  fetchLibraryTopSongs: (artistName: string) => Promise<void>
}

export const createLibrarySlice = (set: SetState<Store>, get: GetState<Store>): LibrarySlice => ({
  entities: {
    artists: {},
    artistAlbums: {},

    albums: {},

    artistInfo: {},
    artistNameTopSongs: {},

    songs: {},
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

    const artists = response.data.artists.reduce((acc, value) => {
      acc[value.id] = mapArtist(value)
      return acc
    }, {} as ById<Artist>)

    set(
      produce<LibrarySlice>(state => {
        state.entities.artists = artists
        state.entities.artistAlbums = pick(state.entities.artistAlbums, Object.keys(artists))
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

    const albums = response.data.albums.reduce((acc, value) => {
      acc[value.id] = mapAlbum(value)
      return acc
    }, {} as ById<Album>)

    const artist = mapArtist(response.data.artist)

    set(
      produce<LibrarySlice>(state => {
        state.entities.artists[id] = artist
        state.entities.artistAlbums[id] = Object.keys(albums)
        state.entities.albums = merge(state.entities.albums, albums)
      }),
    )
  },

  resetLibraryArtists: async () => {
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

  fetchLibraryTopSongs: async artistName => {
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

    set(
      produce<LibrarySlice>(state => {
        state.entities.songs = merge(state.entities.songs, topSongs)
        state.entities.artistNameTopSongs[artistName] = topSongs.map(s => s.id)
      }),
    )
  },
})
