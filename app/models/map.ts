import {
  AlbumID3Element,
  ArtistID3Element,
  ArtistInfo2Element,
  ChildElement,
  PlaylistElement,
} from '@app/subsonic/elements'
import { Album, Artist, ArtistInfo, Playlist, Song } from './library'
import { TrackExt } from './trackplayer'

export function mapArtist(artist: ArtistID3Element): Artist {
  return {
    itemType: 'artist',
    id: artist.id,
    name: artist.name,
    starred: artist.starred?.getTime(),
    coverArt: artist.coverArt,
  }
}

export function mapArtistInfo(id: string, info: ArtistInfo2Element): ArtistInfo {
  return {
    id,
    smallImageUrl: info.smallImageUrl,
    largeImageUrl: info.largeImageUrl,
  }
}

export function mapAlbum(album: AlbumID3Element): Album {
  return {
    itemType: 'album',
    id: album.id,
    name: album.name,
    artist: album.artist,
    artistId: album.artistId,
    starred: album.starred?.getTime(),
    coverArt: album.coverArt,
    year: album.year,
  }
}

export function mapPlaylist(playlist: PlaylistElement): Playlist {
  return {
    itemType: 'playlist',
    id: playlist.id,
    name: playlist.name,
    comment: playlist.comment,
    coverArt: playlist.coverArt,
  }
}

export function mapSong(song: ChildElement): Song {
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
    starred: song.starred?.getTime(),
    playCount: song.playCount,
    averageRating: song.averageRating,
    userRating: song.userRating,
  }
}

export function mapTrackExtToSong(track: TrackExt): Song {
  return {
    itemType: 'song',
    id: track.id,
    title: track.title as string,
    artist: track.artist,
    album: track.album,
    coverArt: track.coverArt,
    duration: track.duration,
    artistId: track.artistId,
    albumId: track.albumId,
    track: track.track,
    discNumber: track.discNumber,
  }
}
