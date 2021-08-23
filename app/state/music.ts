import {
  AlbumListItem,
  AlbumWithSongs,
  Artist,
  ArtistInfo,
  HomeLists,
  PlaylistListItem,
  PlaylistWithSongs,
  SearchResults,
  StarrableItemType,
} from '@app/models/music'
import { Store } from '@app/state/store'
import { GetAlbumList2Params, GetAlbumList2TypeBase, Search3Params, StarParams } from '@app/subsonic/params'
import produce from 'immer'
import { GetState, SetState } from 'zustand'

export type MusicSlice = {
  //
  // family-style state
  //
  artistInfo: { [id: string]: ArtistInfo }
  fetchArtistInfo: (id: string) => Promise<ArtistInfo | undefined>

  albumsWithSongs: { [id: string]: AlbumWithSongs }
  fetchAlbumWithSongs: (id: string) => Promise<AlbumWithSongs | undefined>

  playlistsWithSongs: { [id: string]: PlaylistWithSongs }
  fetchPlaylistWithSongs: (id: string) => Promise<PlaylistWithSongs | undefined>

  //
  // lists-style state
  //
  fetchArtists: (size?: number, offset?: number) => Promise<Artist[]>
  fetchPlaylists: () => Promise<PlaylistListItem[]>
  fetchAlbums: () => Promise<AlbumListItem[]>
  fetchSearchResults: (
    query: string,
    type?: 'album' | 'song' | 'artist',
    size?: number,
    offset?: number,
  ) => Promise<SearchResults>

  homeLists: HomeLists
  homeListsUpdating: boolean
  fetchHomeLists: () => Promise<void>
  clearHomeLists: () => void

  //
  // actions, etc.
  //
  starredSongs: { [id: string]: boolean }
  starredAlbums: { [id: string]: boolean }
  starredArtists: { [id: string]: boolean }
  starItem: (id: string, type: StarrableItemType, unstar?: boolean) => Promise<void>

  albumIdCoverArt: { [id: string]: string | undefined }
  albumIdCoverArtRequests: { [id: string]: Promise<void> }
  fetchAlbumCoverArt: (id: string) => Promise<void>
  getAlbumCoverArt: (id: string | undefined) => Promise<string | undefined>
}

export const selectMusic = {
  fetchArtistInfo: (state: Store) => state.fetchArtistInfo,
  fetchAlbumWithSongs: (state: Store) => state.fetchAlbumWithSongs,
  fetchPlaylistWithSongs: (state: Store) => state.fetchPlaylistWithSongs,

  fetchArtists: (store: MusicSlice) => store.fetchArtists,
  fetchPlaylists: (store: MusicSlice) => store.fetchPlaylists,
  fetchAlbums: (store: MusicSlice) => store.fetchAlbums,
  fetchSearchResults: (store: MusicSlice) => store.fetchSearchResults,

  homeLists: (store: MusicSlice) => store.homeLists,
  homeListsUpdating: (store: MusicSlice) => store.homeListsUpdating,
  fetchHomeLists: (store: MusicSlice) => store.fetchHomeLists,
  clearHomeLists: (store: MusicSlice) => store.clearHomeLists,

  starItem: (store: MusicSlice) => store.starItem,
}

function reduceStarred(
  starredType: { [id: string]: boolean },
  items: { id: string; starred?: Date | boolean }[],
): { [id: string]: boolean } {
  return {
    ...starredType,
    ...items.reduce((acc, val) => {
      acc[val.id] = !!val.starred
      return acc
    }, {} as { [id: string]: boolean }),
  }
}

export const createMusicSlice = (set: SetState<Store>, get: GetState<Store>): MusicSlice => ({
  artistInfo: {},

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
      const artistInfo = await get().mapArtistInfo(
        artistResponse.data,
        artistInfoResponse.data.artistInfo,
        topSongsResponse.data.songs,
      )

      set(
        produce<MusicSlice>(state => {
          state.artistInfo[id] = artistInfo

          state.starredSongs = reduceStarred(state.starredSongs, artistInfo.topSongs)
          state.starredArtists = reduceStarred(state.starredArtists, [artistInfo])
          state.starredAlbums = reduceStarred(state.starredAlbums, artistInfo.albums)
        }),
      )
      return artistInfo
    } catch {
      return undefined
    }
  },

  albumsWithSongs: {},

  fetchAlbumWithSongs: async id => {
    const client = get().client
    if (!client) {
      return undefined
    }

    try {
      const response = await client.getAlbum({ id })
      const album = await get().mapAlbumID3WithSongstoAlbumWithSongs(response.data.album, response.data.songs)

      set(
        produce<MusicSlice>(state => {
          state.albumsWithSongs[id] = album
          state.starredSongs = reduceStarred(state.starredSongs, album.songs)
          state.starredAlbums = reduceStarred(state.starredAlbums, [album])
        }),
      )
      return album
    } catch {
      return undefined
    }
  },

  playlistsWithSongs: {},

  fetchPlaylistWithSongs: async id => {
    const client = get().client
    if (!client) {
      return undefined
    }

    try {
      const response = await client.getPlaylist({ id })
      const playlist = await get().mapPlaylistWithSongs(response.data.playlist)

      set(
        produce<MusicSlice>(state => {
          state.playlistsWithSongs[id] = playlist
          state.starredSongs = reduceStarred(state.starredSongs, playlist.songs)
        }),
      )
      return playlist
    } catch {
      return undefined
    }
  },

  fetchArtists: async () => {
    const client = get().client
    if (!client) {
      return []
    }

    try {
      const response = await client.getArtists()
      const artists = response.data.artists.map(get().mapArtistID3toArtist)

      set(
        produce<MusicSlice>(state => {
          state.starredArtists = reduceStarred(state.starredArtists, artists)
        }),
      )

      return artists
    } catch {
      return []
    }
  },

  fetchPlaylists: async () => {
    const client = get().client
    if (!client) {
      return []
    }

    try {
      const response = await client.getPlaylists()
      return response.data.playlists.map(get().mapPlaylistListItem)
    } catch {
      return []
    }
  },

  fetchAlbums: async (size = 500, offset = 0) => {
    const client = get().client
    if (!client) {
      return []
    }

    try {
      const filter = get().settings.screens.library.albums

      let params: GetAlbumList2Params
      switch (filter.type) {
        case 'byYear':
          params = {
            size,
            offset,
            type: filter.type,
            fromYear: filter.fromYear,
            toYear: filter.toYear,
          }
          break
        case 'byGenre':
          params = {
            size,
            offset,
            type: filter.type,
            genre: filter.genre,
          }
          break
        default:
          params = {
            size,
            offset,
            type: filter.type,
          }
          break
      }

      const response = await client.getAlbumList2(params)
      const albums = response.data.albums.map(get().mapAlbumID3toAlbumListItem)

      set(
        produce<MusicSlice>(state => {
          state.starredAlbums = reduceStarred(state.starredAlbums, albums)
        }),
      )

      return albums
    } catch {
      return []
    }
  },

  fetchSearchResults: async (query, type, size, offset) => {
    if (query.length < 2) {
      return { artists: [], albums: [], songs: [] }
    }

    const client = get().client
    if (!client) {
      return { artists: [], albums: [], songs: [] }
    }

    try {
      const params: Search3Params = { query }
      if (type === 'album') {
        params.albumCount = size
        params.albumOffset = offset
      } else if (type === 'artist') {
        params.artistCount = size
        params.artistOffset = offset
      } else if (type === 'song') {
        params.songCount = size
        params.songOffset = offset
      } else {
        params.albumCount = 5
        params.artistCount = 5
        params.songCount = 5
      }

      const response = await client.search3(params)

      const artists = response.data.artists.map(get().mapArtistID3toArtist)
      const albums = response.data.albums.map(get().mapAlbumID3toAlbumListItem)
      const songs = await get().mapChildrenToSongs(response.data.songs)

      set(
        produce<MusicSlice>(state => {
          state.starredSongs = reduceStarred(state.starredSongs, songs)
          state.starredArtists = reduceStarred(state.starredArtists, artists)
          state.starredAlbums = reduceStarred(state.starredAlbums, albums)
        }),
      )

      return { artists, albums, songs }
    } catch {
      return { artists: [], albums: [], songs: [] }
    }
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

    const types = get().settings.screens.home.lists

    try {
      const promises: Promise<any>[] = []
      for (const type of types) {
        promises.push(
          client
            .getAlbumList2({ type: type as GetAlbumList2TypeBase, size: 20 })
            .then(response => {
              const list = response.data.albums.map(get().mapAlbumID3toAlbumListItem)
              set(
                produce<MusicSlice>(state => {
                  state.homeLists[type] = list
                  state.starredAlbums = reduceStarred(state.starredAlbums, state.homeLists[type])
                }),
              )
            })
            .catch(() => {}),
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

  starredSongs: {},
  starredAlbums: {},
  starredArtists: {},

  starItem: async (id, type, unstar = false) => {
    const client = get().client
    if (!client) {
      return
    }

    let params: StarParams
    let setStarred: (starred: boolean) => void

    switch (type) {
      case 'song':
        params = { id }
        setStarred = starred => {
          set(
            produce<MusicSlice>(state => {
              state.starredSongs = reduceStarred(state.starredSongs, [{ id, starred }])
            }),
          )
        }
        break
      case 'album':
        params = { albumId: id }
        setStarred = starred => {
          set(
            produce<MusicSlice>(state => {
              state.starredAlbums = reduceStarred(state.starredAlbums, [{ id, starred }])
            }),
          )
        }
        break
      case 'artist':
        params = { artistId: id }
        setStarred = starred => {
          set(
            produce<MusicSlice>(state => {
              state.starredArtists = reduceStarred(state.starredArtists, [{ id, starred }])
            }),
          )
        }
        break
      default:
        return
    }

    try {
      setStarred(!unstar)
      if (unstar) {
        await client.unstar(params)
      } else {
        await client.star(params)
      }
    } catch {
      setStarred(unstar)
    }
  },

  albumIdCoverArt: {},
  albumIdCoverArtRequests: {},

  fetchAlbumCoverArt: async id => {
    const client = get().client
    if (!client) {
      return
    }

    const inProgress = get().albumIdCoverArtRequests[id]
    if (inProgress !== undefined) {
      return await inProgress
    }

    const promise = new Promise<void>(async resolve => {
      try {
        const response = await client.getAlbum({ id })
        set(
          produce<MusicSlice>(state => {
            state.albumIdCoverArt[id] = response.data.album.coverArt
          }),
        )
      } finally {
        resolve()
      }
    }).then(() => {
      set(
        produce<MusicSlice>(state => {
          delete state.albumIdCoverArtRequests[id]
        }),
      )
    })
    set(
      produce<MusicSlice>(state => {
        state.albumIdCoverArtRequests[id] = promise
      }),
    )

    return await promise
  },

  getAlbumCoverArt: async id => {
    if (!id) {
      return
    }

    const existing = get().albumIdCoverArt[id]
    if (existing) {
      return existing
    }

    await get().fetchAlbumCoverArt(id)
    return get().albumIdCoverArt[id]
  },
})
