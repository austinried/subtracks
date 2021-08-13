import { SubsonicApiClient } from '@app/subsonic/api'
import {
  AlbumID3Element,
  ArtistID3Element,
  ArtistInfo2Element,
  ChildElement,
  PlaylistElement,
  PlaylistWithSongsElement,
} from '@app/subsonic/elements'
import { GetArtistResponse } from '@app/subsonic/responses'

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
  coverArt?: string
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

export function mapArtistID3toArtist(artist: ArtistID3Element): Artist {
  return {
    itemType: 'artist',
    id: artist.id,
    name: artist.name,
    starred: artist.starred,
    coverArt: artist.coverArt,
  }
}

export function mapArtistInfo(
  artistResponse: GetArtistResponse,
  info: ArtistInfo2Element,
  topSongs: ChildElement[],
  client: SubsonicApiClient,
): ArtistInfo {
  const { artist, albums } = artistResponse

  const mappedAlbums = albums.map(mapAlbumID3toAlbum)

  return {
    ...mapArtistID3toArtist(artist),
    albums: mappedAlbums,
    smallImageUrl: info.smallImageUrl,
    mediumImageUrl: info.mediumImageUrl,
    largeImageUrl: info.largeImageUrl,
    topSongs: topSongs.map(s => mapChildToSong(s, client)).slice(0, 5),
  }
}

export function mapAlbumID3toAlbumListItem(album: AlbumID3Element): AlbumListItem {
  return {
    itemType: 'album',
    id: album.id,
    name: album.name,
    artist: album.artist,
    artistId: album.artistId,
    starred: album.starred,
    coverArt: album.coverArt,
  }
}

export function mapAlbumID3toAlbum(album: AlbumID3Element): Album {
  return {
    ...mapAlbumID3toAlbumListItem(album),
    coverArt: album.coverArt,
    year: album.year,
  }
}

export function mapChildToSong(child: ChildElement, client: SubsonicApiClient): Song {
  return {
    itemType: 'song',
    id: child.id,
    album: child.album,
    albumId: child.albumId,
    artist: child.artist,
    artistId: child.artistId,
    title: child.title,
    track: child.track,
    duration: child.duration,
    starred: child.starred,
    coverArt: child.coverArt,
    streamUri: client.streamUri({ id: child.id }),
  }
}

export function mapAlbumID3WithSongstoAlbumWithSongs(
  album: AlbumID3Element,
  songs: ChildElement[],
  client: SubsonicApiClient,
): AlbumWithSongs {
  return {
    ...mapAlbumID3toAlbum(album),
    songs: songs.map(s => mapChildToSong(s, client)),
  }
}

export function mapPlaylistListItem(playlist: PlaylistElement): PlaylistListItem {
  return {
    itemType: 'playlist',
    id: playlist.id,
    name: playlist.name,
    comment: playlist.comment,
    coverArt: playlist.coverArt,
  }
}

export function mapPlaylistWithSongs(playlist: PlaylistWithSongsElement, client: SubsonicApiClient): PlaylistWithSongs {
  return {
    ...mapPlaylistListItem(playlist),
    songs: playlist.songs.map(s => mapChildToSong(s, client)),
    coverArt: playlist.coverArt,
  }
}
