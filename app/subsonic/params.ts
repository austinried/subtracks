//
// Browsing
//

export type GetIndexesParams = {
  musicFolderId?: string
  ifModifiedSince?: number
}

export type GetArtistInfoParams = {
  id: string
  count?: number
  includeNotPresent?: boolean
}

export type GetArtistInfo2Params = GetArtistInfoParams

export type GetMusicDirectoryParams = {
  id: string
}

export type GetAlbumParams = {
  id: string
}

export type GetArtistParams = {
  id: string
}

export type GetTopSongsParams = {
  artist: string
  count?: number
}

//
// Album/song lists
//

export type GetAlbumList2TypeBase =
  | 'random'
  | 'newest'
  | 'frequent'
  | 'recent'
  | 'starred'
  | 'alphabeticalByName'
  | 'alphabeticalByArtist'
export type GetAlbumListTypeBase = GetAlbumList2TypeBase | ' highest'

type GetAlbumListBase = {
  size?: number
  offset?: number
  musicFolderId?: string
}

export type GetAlbumList2TypeByYear = GetAlbumListBase & {
  type: 'byYear'
  fromYear: number
  toYear: number
}

export type GetAlbumList2TypeByGenre = GetAlbumListBase & {
  type: 'byGenre'
  genre: string
}

export type GetAlbumList2Params =
  | (GetAlbumListBase & { type: GetAlbumList2TypeBase })
  | GetAlbumList2TypeByYear
  | GetAlbumList2TypeByGenre

export type GetAlbumListParams = GetAlbumList2Params

export type GetAlbumList2Type = GetAlbumList2TypeBase | 'byYear' | 'byGenre'
export type GetAlbumListType = GetAlbumListTypeBase | 'byYear' | 'byGenre'

//
// Playlists
//

export type GetPlaylistsParams = {
  username?: string
}

export type GetPlaylistParams = {
  id: string
}

//
// Media retrieval
//

export type GetCoverArtParams = {
  id: string
  size?: string
}

export type StreamParams = {
  id: string
  maxBitRate?: number
  format?: string
  estimateContentLength?: boolean
}

//
// Media annotation
//

export type ScrobbleParams = {
  id: string
  time?: Date
  submission?: boolean
}

export type StarParams = {
  id?: string
  albumId?: string
  artistId?: string
}

//
// Searching
//

export type Search3Params = {
  query: string
  artistCount?: number
  artistOffset?: number
  albumCount?: number
  albumOffset?: number
  songCount?: number
  songOffset?: number
  musicFolderId?: string
}
