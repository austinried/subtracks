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

export type GetAlbumList2Type =
  | 'random'
  | 'newest'
  | 'frequent'
  | 'recent'
  | 'starred'
  | 'alphabeticalByName'
  | 'alphabeticalByArtist'
export type GetAlbumListType = GetAlbumList2Type | ' highest'

export type GetAlbumList2TypeByYear = {
  type: 'byYear'
  fromYear: string
  toYear: string
}

export type GetAlbumList2TypeByGenre = {
  type: 'byGenre'
  genre: string
}

export type GetAlbumList2Params =
  | {
      type: GetAlbumList2Type
      size?: number
      offset?: number
      fromYear?: string
      toYear?: string
      genre?: string
      musicFolderId?: string
    }
  | GetAlbumList2TypeByYear
  | GetAlbumList2TypeByGenre

export type GetAlbumListParams = GetAlbumList2Params

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
