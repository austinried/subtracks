import {
  AlbumListItem,
  AlbumWithSongs,
  Artist,
  ArtistInfo,
  HomeLists,
  mapAlbumID3toAlbumListItem,
  mapAlbumID3WithSongstoAlbunWithSongs,
  mapArtistID3toArtist,
  mapArtistInfo,
  mapChildToSong,
  mapPlaylistListItem,
  mapPlaylistWithSongs,
  PlaylistListItem,
  PlaylistWithSongs,
  SearchResults,
} from '@app/models/music'
import { Store } from '@app/state/store'
import { GetAlbumList2Type } from '@app/subsonic/params'
import produce from 'immer'
import { GetState, SetState } from 'zustand'

export type MusicSlice = {
  //
  // family-style state
  //
  cacheSize: number

  artistInfo: { [id: string]: ArtistInfo | undefined }
  artistInfoCache: string[]
  fetchArtistInfo: (id: string) => Promise<ArtistInfo | undefined>

  albumsWithSongs: { [id: string]: AlbumWithSongs | undefined }
  albumsWithSongsCache: string[]
  fetchAlbumWithSongs: (id: string) => Promise<AlbumWithSongs | undefined>

  playlistsWithSongs: { [id: string]: PlaylistWithSongs | undefined }
  playlistsWithSongsCache: string[]
  fetchPlaylistWithSongs: (id: string) => Promise<PlaylistWithSongs | undefined>

  //
  // lists-style state
  //
  artists: Artist[]
  artistsUpdating: boolean
  fetchArtists: () => Promise<void>

  playlists: PlaylistListItem[]
  playlistsUpdating: boolean
  fetchPlaylists: () => Promise<void>

  albums: AlbumListItem[]
  albumsUpdating: boolean
  fetchAlbums: (size?: number, offset?: number) => Promise<void>

  searchResults: SearchResults
  searchResultsUpdating: boolean
  fetchSearchResults: (query: string) => Promise<void>
  clearSearchResults: () => void

  homeLists: HomeLists
  homeListsUpdating: boolean
  fetchHomeLists: () => Promise<void>
  clearHomeLists: () => void
}

export const selectMusic = {
  fetchArtistInfo: (state: Store) => state.fetchArtistInfo,
  fetchAlbumWithSongs: (state: Store) => state.fetchAlbumWithSongs,
  fetchPlaylistWithSongs: (state: Store) => state.fetchPlaylistWithSongs,

  artists: (store: MusicSlice) => store.artists,
  artistsUpdating: (store: MusicSlice) => store.artistsUpdating,
  fetchArtists: (store: MusicSlice) => store.fetchArtists,

  playlists: (store: MusicSlice) => store.playlists,
  playlistsUpdating: (store: MusicSlice) => store.playlistsUpdating,
  fetchPlaylists: (store: MusicSlice) => store.fetchPlaylists,

  albums: (store: MusicSlice) => store.albums,
  albumsUpdating: (store: MusicSlice) => store.albumsUpdating,
  fetchAlbums: (store: MusicSlice) => store.fetchAlbums,

  searchResults: (store: MusicSlice) => store.searchResults,
  searchResultsUpdating: (store: MusicSlice) => store.searchResultsUpdating,
  fetchSearchResults: (store: MusicSlice) => store.fetchSearchResults,
  clearSearchResults: (store: MusicSlice) => store.clearSearchResults,

  homeLists: (store: MusicSlice) => store.homeLists,
  homeListsUpdating: (store: MusicSlice) => store.homeListsUpdating,
  fetchHomeLists: (store: MusicSlice) => store.fetchHomeLists,
  clearHomeLists: (store: MusicSlice) => store.clearHomeLists,
}

export const createMusicSlice = (set: SetState<Store>, get: GetState<Store>): MusicSlice => ({
  cacheSize: 100,

  artistInfo: {},
  artistInfoCache: [],

  fetchArtistInfo: async id => {
    const client = get().client
    if (!client) {
      return undefined
    }

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
            delete state.albumsWithSongs[state.artistInfoCache.shift() as string]
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

  albumsWithSongs: {},
  albumsWithSongsCache: [],

  fetchAlbumWithSongs: async id => {
    const client = get().client
    if (!client) {
      return undefined
    }

    try {
      const response = await client.getAlbum({ id })
      const album = mapAlbumID3WithSongstoAlbunWithSongs(response.data.album, response.data.songs, client)

      set(
        produce<MusicSlice>(state => {
          if (state.albumsWithSongsCache.length >= state.cacheSize) {
            delete state.albumsWithSongs[state.albumsWithSongsCache.shift() as string]
          }
          state.albumsWithSongs[id] = album
          state.albumsWithSongsCache.push(id)
        }),
      )
      return album
    } catch {
      return undefined
    }
  },

  playlistsWithSongs: {},
  playlistsWithSongsCache: [],

  fetchPlaylistWithSongs: async id => {
    const client = get().client
    if (!client) {
      return undefined
    }

    try {
      const response = await client.getPlaylist({ id })
      const playlist = mapPlaylistWithSongs(response.data.playlist, client)

      set(
        produce<MusicSlice>(state => {
          if (state.playlistsWithSongsCache.length >= state.cacheSize) {
            delete state.playlistsWithSongs[state.playlistsWithSongsCache.shift() as string]
          }
          state.playlistsWithSongs[id] = playlist
          state.playlistsWithSongsCache.push(id)
        }),
      )
      return playlist
    } catch {
      return undefined
    }
  },

  artists: [],
  artistsUpdating: false,

  fetchArtists: async () => {
    const client = get().client
    if (!client) {
      return
    }

    if (get().artistsUpdating) {
      return
    }
    set({ artistsUpdating: true })

    try {
      const response = await client.getArtists()
      set({ artists: response.data.artists.map(mapArtistID3toArtist) })
    } finally {
      set({ artistsUpdating: false })
    }
  },

  playlists: [],
  playlistsUpdating: false,

  fetchPlaylists: async () => {
    const client = get().client
    if (!client) {
      return
    }

    if (get().playlistsUpdating) {
      return
    }
    set({ playlistsUpdating: true })

    try {
      const response = await client.getPlaylists()
      set({ playlists: response.data.playlists.map(mapPlaylistListItem) })
    } finally {
      set({ playlistsUpdating: false })
    }
  },

  albums: [],
  albumsUpdating: false,

  fetchAlbums: async (size = 500, offset = 0) => {
    const client = get().client
    if (!client) {
      return
    }

    if (get().albumsUpdating) {
      return
    }
    set({ albumsUpdating: true })

    try {
      const response = await client.getAlbumList2({ type: 'alphabeticalByArtist', size, offset })
      set({ albums: response.data.albums.map(mapAlbumID3toAlbumListItem) })
    } finally {
      set({ albumsUpdating: false })
    }
  },

  searchResults: {
    artists: [],
    albums: [],
    songs: [],
  },
  searchResultsUpdating: false,

  fetchSearchResults: async query => {
    if (query.length < 2) {
      return
    }

    const client = get().client
    if (!client) {
      return
    }

    if (get().searchResultsUpdating) {
      return
    }

    set({ searchResultsUpdating: true })

    try {
      const response = await client.search3({ query })
      set({
        searchResults: {
          artists: response.data.artists.map(mapArtistID3toArtist),
          albums: response.data.albums.map(mapAlbumID3toAlbumListItem),
          songs: response.data.songs.map(a => mapChildToSong(a, client)),
        },
      })
    } finally {
      set({ searchResultsUpdating: false })
    }
  },
  clearSearchResults: () => {
    set({
      searchResults: {
        artists: [],
        albums: [],
        songs: [],
      },
    })
  },

  homeLists: {},
  homeListsUpdating: false,

  fetchHomeLists: async () => {
    const client = get().client
    if (!client) {
      return
    }

    if (get().homeListsUpdating) {
      return
    }
    set({ homeListsUpdating: true })

    const types = get().settings.home.lists

    try {
      const promises: Promise<any>[] = []
      for (const type of types) {
        promises.push(
          client.getAlbumList2({ type: type as GetAlbumList2Type, size: 20 }).then(response => {
            set(
              produce<MusicSlice>(state => {
                state.homeLists[type] = response.data.albums.map(mapAlbumID3toAlbumListItem)
              }),
            )
          }),
        )
      }
      await Promise.all(promises)
    } finally {
      set({ homeListsUpdating: false })
    }
  },

  clearHomeLists: () => {
    set({ homeLists: {} })
  },
})
