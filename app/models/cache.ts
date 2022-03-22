import { Album, Playlist, Artist, Song } from './library'

export enum CacheItemType {
  coverArt = 'coverArt',
  artistArt = 'artistArt',
  song = 'song',
}

export type CacheItemTypeKey = keyof typeof CacheItemType

export type CacheFile = {
  path: string
  date: number
  permanent: boolean
}

export type CacheRequest = {
  progress: number
  promise?: Promise<void>
}

export type DownloadedAlbum = Album & {
  songs: string[]
}

export type DownloadedPlaylist = Playlist & {
  songs: string[]
}

export type DownloadedArtist = Artist & {
  topSongs: string[]
  albums: string[]
}

export type DownloadedSong = Song
