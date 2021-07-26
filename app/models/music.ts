export interface Artist {
  itemType: 'artist'
  id: string
  name: string
  starred?: Date
  coverArt?: string
}

export interface ArtistInfo extends Artist {
  albums: Album[]

  smallImageUrl?: string
  mediumImageUrl?: string
  largeImageUrl?: string

  topSongs: Song[]
}

export interface AlbumListItem {
  itemType: 'album'
  id: string
  name: string
  artist?: string
  starred?: Date
  coverArt?: string
}

export interface Album extends AlbumListItem {
  coverArt?: string
  year?: number
}

export interface AlbumWithSongs extends Album {
  songs: Song[]
}

export interface SearchResults {
  artists: Artist[]
  albums: AlbumListItem[]
  songs: Song[]
}

export interface PlaylistListItem {
  itemType: 'playlist'
  id: string
  name: string
  comment?: string
  coverArt?: string
}

export interface PlaylistWithSongs extends PlaylistListItem {
  songs: Song[]
  coverArt?: string
}

export interface Song {
  itemType: 'song'
  id: string
  album?: string
  artist?: string
  title: string
  track?: number
  duration?: number
  starred?: Date

  streamUri: string
  coverArt?: string
}

export type ListableItem = Song | AlbumListItem | Artist | PlaylistListItem

export type DownloadedSong = {
  id: string
  type: 'song'
  name: string
  album: string
  artist: string
}

export type DownloadedAlbum = {
  id: string
  type: 'album'
  songs: string[]
  name: string
  artist: string
}

export type DownloadedArtist = {
  id: string
  type: 'artist'
  songs: string[]
  name: string
}

export type DownloadedPlaylist = {
  id: string
  type: 'playlist'
  songs: string[]
  name: string
}
