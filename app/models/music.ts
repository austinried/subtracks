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
  largeImageUrl?: string
  topSongs: Song[]
}

export interface AlbumListItem {
  itemType: 'album'
  id: string
  name: string
  artist?: string
  artistId?: string
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
}

export interface Song {
  itemType: 'song'
  id: string
  album?: string
  albumId?: string
  artist?: string
  artistId?: string
  title: string
  track?: number
  duration?: number
  starred?: Date

  streamUri: string
  coverArt?: string
}

export type ListableItem = Song | AlbumListItem | Artist | PlaylistListItem

export type HomeLists = { [key: string]: AlbumListItem[] }

export type StarrableItemType = 'song' | 'album' | 'artist'
