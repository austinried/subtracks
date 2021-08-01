import {
  AlbumListItem,
  AlbumWithSongs,
  Artist,
  ArtistInfo,
  mapAlbumID3WithSongstoAlbunWithSongs,
  mapArtistInfo,
  mapPlaylistWithSongs,
  PlaylistListItem,
  PlaylistWithSongs,
  SearchResults,
} from '@app/models/music'
import { Server } from '@app/models/settings'
import { Store } from '@app/state/store'
import { SubsonicApiClient } from '@app/subsonic/api'
import produce from 'immer'
import { atom } from 'jotai'
import { GetState, SetState } from 'zustand'

export type MusicSlice = {
  cacheSize: number
  artistInfo: { [id: string]: ArtistInfo | undefined }
  artistInfoCache: string[]
  albums: { [id: string]: AlbumWithSongs | undefined }
  albumsCache: string[]
  playlists: { [id: string]: PlaylistWithSongs | undefined }
  playlistsCache: string[]
  fetchArtistInfo: (server: Server, id: string) => Promise<ArtistInfo | undefined>
  fetchAlbum: (server: Server, id: string) => Promise<AlbumWithSongs | undefined>
  fetchPlaylist: (server: Server, id: string) => Promise<PlaylistWithSongs | undefined>
}

export const createMusicSlice = (set: SetState<Store>, _get: GetState<Store>): MusicSlice => ({
  cacheSize: 100,
  artistInfo: {},
  artistInfoCache: [],
  albums: {},
  albumsCache: [],
  playlists: {},
  playlistsCache: [],
  fetchArtistInfo: async (server, id) => {
    const client = new SubsonicApiClient(server)

    try {
      const [artistResponse, artistInfoResponse] = await Promise.all([
        client.getArtist({ id }),
        client.getArtistInfo2({ id }),
      ])
      const topSongsResponse = await client.getTopSongs({ artist: artistResponse.data.artist.name, count: 50 })
      const artistInfo = mapArtistInfo(
        artistResponse.data,
        artistInfoResponse.data.artistInfo,
        topSongsResponse.data.songs,
        client,
      )

      set(
        produce<MusicSlice>(state => {
          if (state.artistInfoCache.length >= state.cacheSize) {
            delete state.albums[state.artistInfoCache.shift() as string]
          }
          state.artistInfo[id] = artistInfo
          state.artistInfoCache.push(id)
        }),
      )
      return artistInfo
    } catch {
      return undefined
    }
  },
  fetchAlbum: async (server, id) => {
    const client = new SubsonicApiClient(server)

    try {
      const response = await client.getAlbum({ id })
      const album = mapAlbumID3WithSongstoAlbunWithSongs(response.data.album, response.data.songs, client)

      set(
        produce<MusicSlice>(state => {
          if (state.albumsCache.length >= state.cacheSize) {
            delete state.albums[state.albumsCache.shift() as string]
          }
          state.albums[id] = album
          state.albumsCache.push(id)
        }),
      )
      return album
    } catch {
      return undefined
    }
  },
  fetchPlaylist: async (server, id) => {
    const client = new SubsonicApiClient(server)

    try {
      const response = await client.getPlaylist({ id })
      const playlist = mapPlaylistWithSongs(response.data.playlist, client)

      set(
        produce<MusicSlice>(state => {
          if (state.playlistsCache.length >= state.cacheSize) {
            delete state.playlists[state.playlistsCache.shift() as string]
          }
          state.playlists[id] = playlist
          state.playlistsCache.push(id)
        }),
      )
      return playlist
    } catch {
      return undefined
    }
  },
})

export const artistsAtom = atom<Artist[]>([])
export const artistsUpdatingAtom = atom(false)

export type HomeLists = { [key: string]: AlbumListItem[] }

export const homeListsUpdatingAtom = atom(false)
export const homeListsAtom = atom<HomeLists>({})
export const homeListsWriteAtom = atom<HomeLists, { type: string; albums: AlbumListItem[] }>(
  get => get(homeListsAtom),
  (get, set, { type, albums }) => {
    const lists = get(homeListsAtom)
    set(homeListsAtom, {
      ...lists,
      [type]: albums,
    })
  },
)

export const searchResultsUpdatingAtom = atom(false)
export const searchResultsAtom = atom<SearchResults>({
  artists: [],
  albums: [],
  songs: [],
})

export const playlistsUpdatingAtom = atom(false)
export const playlistsAtom = atom<PlaylistListItem[]>([])

export const albumListUpdatingAtom = atom(false)
export const albumListAtom = atom<AlbumListItem[]>([])
