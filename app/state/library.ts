import { Album, Artist, ArtistInfo, Playlist, SearchResults, Song } from '@app/models/library'
import { ById, OneToMany } from '@app/models/state'
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
  GetSongResponse,
  GetTopSongsResponse,
  Search3Response,
  SubsonicResponse,
} from '@app/subsonic/responses'
import PromiseQueue from '@app/util/PromiseQueue'
import { reduceById, mergeById, mapId } from '@app/util/state'
import produce from 'immer'
import { WritableDraft } from 'immer/dist/types/types-external'
import pick from 'lodash.pick'
import { GetState, SetState } from 'zustand'

const songCoverArtQueue = new PromiseQueue(2)

export type LibrarySlice = {
  entities: {
    artists: ById<Artist>
    artistInfo: ById<ArtistInfo>
    artistAlbums: OneToMany
    artistNameTopSongs: OneToMany

    albums: ById<Album>
    albumSongs: OneToMany

    playlists: ById<Playlist>
    playlistSongs: OneToMany

    songs: ById<Song>
  }

  resetLibrary: (state?: WritableDraft<Store>) => void

  fetchArtists: () => Promise<void>
  fetchArtist: (id: string) => Promise<void>
  fetchArtistInfo: (artistId: string) => Promise<void>
  fetchArtistTopSongs: (artistName: string) => Promise<void>

  fetchAlbum: (id: string) => Promise<void>

  fetchPlaylists: () => Promise<void>
  fetchPlaylist: (id: string) => Promise<void>

  fetchSong: (id: string) => Promise<void>

  fetchAlbumList: (params: GetAlbumList2Params) => Promise<string[]>
  fetchSearchResults: (params: Search3Params) => Promise<SearchResults>
  star: (params: StarParams) => Promise<void>
  unstar: (params: StarParams) => Promise<void>

  _fixSongCoverArt: (songs: Song[]) => Promise<void>
}

const defaultEntities = () => ({
  artists: {},
  artistAlbums: {},
  artistInfo: {},
  artistNameTopSongs: {},

  albums: {},
  albumSongs: {},

  playlists: {},
  playlistSongs: {},

  songs: {},
})

export const createLibrarySlice = (set: SetState<Store>, get: GetState<Store>): LibrarySlice => ({
  entities: defaultEntities(),

  resetLibrary: state => {
    if (state) {
      state.entities = defaultEntities()
      return
    }
    set(store => {
      store.entities = defaultEntities()
    })
  },

  fetchArtists: async () => {
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

  fetchArtist: async id => {
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

  fetchArtistInfo: async id => {
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

  fetchArtistTopSongs: async artistName => {
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

    get()._fixSongCoverArt(topSongs)

    set(
      produce<LibrarySlice>(state => {
        mergeById(state.entities.songs, topSongsById)
        state.entities.artistNameTopSongs[artistName] = mapId(topSongs)
      }),
    )
  },

  fetchAlbum: async id => {
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

    get()._fixSongCoverArt(songs)

    set(
      produce<LibrarySlice>(state => {
        state.entities.albums[id] = album
        state.entities.albumSongs[id] = mapId(songs)
        mergeById(state.entities.songs, songsById)
      }),
    )
  },

  fetchPlaylists: async () => {
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

  fetchPlaylist: async id => {
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

    get()._fixSongCoverArt(songs)

    set(
      produce<LibrarySlice>(state => {
        state.entities.playlists[id] = playlist
        state.entities.playlistSongs[id] = mapId(songs)
        mergeById(state.entities.songs, songsById)
      }),
    )
  },

  fetchSong: async id => {
    const client = get().client
    if (!client) {
      return
    }

    let response: SubsonicResponse<GetSongResponse>
    try {
      response = await client.getSong({ id })
    } catch {
      return
    }

    const song = mapSong(response.data.song)

    get()._fixSongCoverArt([song])

    set(
      produce<LibrarySlice>(state => {
        state.entities.songs[id] = song
      }),
    )
  },

  fetchAlbumList: async params => {
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

  fetchSearchResults: async params => {
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

    get()._fixSongCoverArt(songs)

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

    let id = '-1'
    let entity: 'songs' | 'artists' | 'albums' = 'songs'
    if (params.id) {
      id = params.id
      entity = 'songs'
    } else if (params.albumId) {
      id = params.albumId
      entity = 'albums'
    } else if (params.artistId) {
      id = params.artistId
      entity = 'artists'
    } else {
      return
    }

    const item = get().entities[entity][id]
    const originalValue = item ? item.starred : null

    set(
      produce<LibrarySlice>(state => {
        state.entities[entity][id].starred = new Date()
      }),
    )

    try {
      await client.star(params)
    } catch {
      set(
        produce<LibrarySlice>(state => {
          if (originalValue !== null) {
            state.entities[entity][id].starred = originalValue
          }
        }),
      )
    }
  },

  unstar: async params => {
    const client = get().client
    if (!client) {
      return
    }

    let id = '-1'
    let entity: 'songs' | 'artists' | 'albums' = 'songs'
    if (params.id) {
      id = params.id
      entity = 'songs'
    } else if (params.albumId) {
      id = params.albumId
      entity = 'albums'
    } else if (params.artistId) {
      id = params.artistId
      entity = 'artists'
    } else {
      return
    }

    const item = get().entities[entity][id]
    const originalValue = item ? item.starred : null

    set(
      produce<LibrarySlice>(state => {
        state.entities[entity][id].starred = undefined
      }),
    )

    try {
      await client.unstar(params)
    } catch {
      set(
        produce<LibrarySlice>(state => {
          if (originalValue !== null) {
            state.entities[entity][id].starred = originalValue
          }
        }),
      )
    }
  },

  // song cover art comes back from the api as a unique id per song even if it all points to the same
  // album art, which prevents us from caching it once, so we need to use the album's cover art
  _fixSongCoverArt: async songs => {
    const client = get().client
    if (!client) {
      return
    }

    const albumsToGet: ById<Song[]> = {}
    for (const song of songs) {
      if (!song.albumId) {
        continue
      }

      let album = get().entities.albums[song.albumId]
      if (album) {
        song.coverArt = album.coverArt
        continue
      }

      albumsToGet[song.albumId] = albumsToGet[song.albumId] || []
      albumsToGet[song.albumId].push(song)
    }

    for (const id in albumsToGet) {
      songCoverArtQueue
        .enqueue(() => client.getAlbum({ id }))
        .then(res => {
          const album = mapAlbum(res.data.album)

          set(
            produce<LibrarySlice>(state => {
              state.entities.albums[album.id] = album
              for (const song of albumsToGet[album.id]) {
                state.entities.songs[song.id].coverArt = album.coverArt
              }
            }),
          )
        })
    }
  },
})

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
    largeImageUrl: info.largeImageUrl,
  }
}

function mapAlbum(album: AlbumID3Element): Album {
  return {
    itemType: 'album',
    id: album.id,
    name: album.name,
    artist: album.artist,
    artistId: album.artistId,
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
  }
}
