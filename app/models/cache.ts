import { Album, PlaylistListItem, Artist, Song } from './music'

export enum CacheItemType {
  coverArt = 'coverArt',
  coverArtThumb = 'coverArtThumb',
  artistArt = 'artistArt',
  artistArtThumb = 'artistArtThumb',
  song = 'song',
}

export type CacheItemTypeKey = keyof typeof CacheItemType

export type CacheImageSize = 'thumbnail' | 'original'

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

export type DownloadedPlaylist = PlaylistListItem & {
  songs: string[]
}

export type DownloadedArtist = Artist & {
  topSongs: string[]
  albums: string[]
}

export type DownloadedSong = Song
