export interface Artist {
  id: string
  name: string
  starred?: Date
}

export interface ArtistInfo extends Artist {
  albums: Album[]

  mediumImageUrl?: string
  largeImageUrl?: string
  coverArtUris: string[]
}

export interface ArtistArt {
  uri?: string
  coverArtUris: string[]
}

export interface Album {
  id: string
  artistId?: string
  artist?: string
  name: string
  starred?: Date
  coverArt?: string
  coverArtUri?: string
  coverArtThumbUri?: string
  year?: number
}

export interface AlbumArt {
  uri?: string
  thumbUri?: string
}

export interface AlbumWithSongs extends Album {
  songs: Song[]
}

export interface Song {
  id: string
  album?: string
  artist?: string
  title: string
  track?: number
  year?: number
  genre?: string
  coverArt?: string
  size?: number
  contentType?: string
  suffix?: string
  duration?: number
  bitRate?: number
  userRating?: number
  averageRating?: number
  playCount?: number
  discNumber?: number
  created?: Date
  starred?: Date

  streamUri: string
  coverArtUri?: string
  coverArtThumbUri?: string
}

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