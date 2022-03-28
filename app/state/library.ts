import { Album, Artist, ArtistInfo, Playlist, SearchResults, Song } from '@app/models/library'
import { ById, OneToMany } from '@app/models/state'
import { GetStore, SetStore, Store } from '@app/state/store'
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
} from '@app/subsonic/responses'
import PromiseQueue from '@app/util/PromiseQueue'
import { mapId, mergeById, reduceById } from '@app/util/state'
import { WritableDraft } from 'immer/dist/types/types-external'
import pick from 'lodash.pick'

const songCoverArtQueue = new PromiseQueue(2)

export type LibrarySlice = {
  library: {
    artists: ById<Artist>
    artistInfo: ById<ArtistInfo>
    artistAlbums: OneToMany
    artistNameTopSongs: OneToMany
    artistOrder: string[]

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

const defaultLibrary = () => ({
  artists: {},
  artistAlbums: {},
  artistInfo: {},
  artistNameTopSongs: {},
  artistOrder: [],

  albums: {},
  albumSongs: {},

  playlists: {},
  playlistSongs: {},

  songs: {},
})

export const createLibrarySlice = (set: SetStore, get: GetStore): LibrarySlice => ({
  library: defaultLibrary(),

  resetLibrary: state => {
    if (state) {
      state.library = defaultLibrary()
      return
    }
    set(store => {
      store.library = defaultLibrary()
    })
  },

  fetchArtists: async () => {
    const client = get().client
    if (!client) {
      return
    }

    let response: GetArtistsResponse
    try {
      response = await client.getArtists()
    } catch {
      return
    }

    const artists = response.data.artists.map(mapArtist)
    const artistsById = reduceById(artists)
    const artistIds = mapId(artists)

    set(state => {
      state.library.artists = artistsById
      state.library.artistAlbums = pick(state.library.artistAlbums, artistIds)
      state.library.artistOrder = artistIds
    })
  },

  fetchArtist: async id => {
    const client = get().client
    if (!client) {
      return
    }

    let response: GetArtistResponse
    try {
      response = await client.getArtist({ id })
    } catch {
      return
    }

    const artist = mapArtist(response.data.artist)
    const albums = response.data.albums.map(mapAlbum)
    const albumsById = reduceById(albums)

    set(state => {
      state.library.artists[id] = artist
      state.library.artistAlbums[id] = mapId(albums)
      mergeById(state.library.albums, albumsById)
    })
  },

  fetchArtistInfo: async id => {
    const client = get().client
    if (!client) {
      return
    }

    let response: GetArtistInfo2Response
    try {
      response = await client.getArtistInfo2({ id })
    } catch {
      return
    }

    const info = mapArtistInfo(id, response.data.artistInfo)

    set(state => {
      state.library.artistInfo[id] = info
    })
  },

  fetchArtistTopSongs: async artistName => {
    const client = get().client
    if (!client) {
      return
    }

    let response: GetTopSongsResponse
    try {
      response = await client.getTopSongs({ artist: artistName, count: 50 })
    } catch {
      return
    }

    const topSongs = response.data.songs.map(mapSong)
    const topSongsById = reduceById(topSongs)

    get()._fixSongCoverArt(topSongs)

    set(state => {
      mergeById(state.library.songs, topSongsById)
      state.library.artistNameTopSongs[artistName] = mapId(topSongs)
    })
  },

  fetchAlbum: async id => {
    const client = get().client
    if (!client) {
      return
    }

    let response: GetAlbumResponse
    try {
      response = await client.getAlbum({ id })
    } catch {
      return
    }

    const album = mapAlbum(response.data.album)
    const songs = response.data.songs.map(mapSong)
    const songsById = reduceById(songs)

    get()._fixSongCoverArt(songs)

    set(state => {
      state.library.albums[id] = album
      state.library.albumSongs[id] = mapId(songs)
      mergeById(state.library.songs, songsById)
    })
  },

  fetchPlaylists: async () => {
    const client = get().client
    if (!client) {
      return
    }

    let response: GetPlaylistsResponse
    try {
      response = await client.getPlaylists()
    } catch {
      return
    }

    const playlists = response.data.playlists.map(mapPlaylist)
    const playlistsById = reduceById(playlists)

    set(state => {
      state.library.playlists = playlistsById
      state.library.playlistSongs = pick(state.library.playlistSongs, mapId(playlists))
    })
  },

  fetchPlaylist: async id => {
    const client = get().client
    if (!client) {
      return
    }

    let response: GetPlaylistResponse
    try {
      response = await client.getPlaylist({ id })
    } catch {
      return
    }

    const playlist = mapPlaylist(response.data.playlist)
    const songs = response.data.playlist.songs.map(mapSong)
    const songsById = reduceById(songs)

    get()._fixSongCoverArt(songs)

    set(state => {
      state.library.playlists[id] = playlist
      state.library.playlistSongs[id] = mapId(songs)
      mergeById(state.library.songs, songsById)
    })
  },

  fetchSong: async id => {
    const client = get().client
    if (!client) {
      return
    }

    let response: GetSongResponse
    try {
      response = await client.getSong({ id })
    } catch {
      return
    }

    const song = mapSong(response.data.song)

    get()._fixSongCoverArt([song])

    set(state => {
      state.library.songs[id] = song
    })
  },

  fetchAlbumList: async params => {
    const client = get().client
    if (!client) {
      return []
    }

    let response: GetAlbumList2Response
    try {
      response = await client.getAlbumList2(params)
    } catch {
      return []
    }

    const albums = response.data.albums.map(mapAlbum)
    const albumsById = reduceById(albums)

    set(state => {
      mergeById(state.library.albums, albumsById)
    })

    return mapId(albums)
  },

  fetchSearchResults: async params => {
    const empty = { artists: [], albums: [], songs: [] }

    const client = get().client
    if (!client) {
      return empty
    }

    let response: Search3Response
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

    set(state => {
      mergeById(state.library.artists, artistsById)
      mergeById(state.library.albums, albumsById)
      mergeById(state.library.songs, songsById)
    })

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

    const item = get().library[entity][id]
    const originalValue = item ? item.starred : null

    set(state => {
      state.library[entity][id].starred = new Date()
    })

    try {
      await client.star(params)
    } catch {
      set(state => {
        if (originalValue !== null) {
          state.library[entity][id].starred = originalValue
        }
      })
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

    const item = get().library[entity][id]
    const originalValue = item ? item.starred : null

    set(state => {
      state.library[entity][id].starred = undefined
    })

    try {
      await client.unstar(params)
    } catch {
      set(state => {
        if (originalValue !== null) {
          state.library[entity][id].starred = originalValue
        }
      })
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

      let album = get().library.albums[song.albumId]
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

          set(state => {
            state.library.albums[album.id] = album
            for (const song of albumsToGet[album.id]) {
              state.library.songs[song.id].coverArt = album.coverArt
            }
          })
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
