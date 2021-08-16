import {
  AlbumListItem,
  AlbumWithSongs,
  Artist,
  ArtistInfo,
  PlaylistListItem,
  PlaylistWithSongs,
  Song,
} from '@app/models/music'
import {
  AlbumID3Element,
  ArtistID3Element,
  ArtistInfo2Element,
  ChildElement,
  PlaylistElement,
  PlaylistWithSongsElement,
} from '@app/subsonic/elements'
import { GetArtistResponse } from '@app/subsonic/responses'
import { GetState, SetState } from 'zustand'
import { Store } from './store'

export type MusicMapSlice = {
  mapChildToSong: (child: ChildElement) => Promise<Song>
  mapChildrenToSongs: (children: ChildElement[]) => Promise<Song[]>
  mapArtistID3toArtist: (artist: ArtistID3Element) => Artist
  mapArtistInfo: (
    artistResponse: GetArtistResponse,
    info: ArtistInfo2Element,
    topSongs: ChildElement[],
  ) => Promise<ArtistInfo>
  mapAlbumID3toAlbumListItem: (album: AlbumID3Element) => AlbumListItem
  mapAlbumID3toAlbum: (album: AlbumID3Element) => AlbumListItem
  mapAlbumID3WithSongstoAlbumWithSongs: (album: AlbumID3Element, songs: ChildElement[]) => Promise<AlbumWithSongs>
  mapPlaylistListItem: (playlist: PlaylistElement) => PlaylistListItem
  mapPlaylistWithSongs: (playlist: PlaylistWithSongsElement) => Promise<PlaylistWithSongs>
}

class NoClientError extends Error {
  constructor() {
    super('no client in state')
  }
}

export const createMusicMapSlice = (set: SetState<Store>, get: GetState<Store>): MusicMapSlice => ({
  mapChildToSong: async child => {
    const client = get().client
    if (!client) {
      throw new NoClientError()
    }

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
      coverArt: await get().getAlbumCoverArt(child.albumId),
      streamUri: client.streamUri({ id: child.id }),
    }
  },

  mapChildrenToSongs: async children => {
    const songMaps: Promise<Song>[] = []
    for (const child of children) {
      songMaps.push(get().mapChildToSong(child))
    }
    return await Promise.all(songMaps)
  },

  mapArtistID3toArtist: artist => {
    return {
      itemType: 'artist',
      id: artist.id,
      name: artist.name,
      starred: artist.starred,
      coverArt: artist.coverArt,
    }
  },

  mapArtistInfo: async (artistResponse, info, topSongs) => {
    const { artist, albums } = artistResponse

    const mappedAlbums = albums.map(get().mapAlbumID3toAlbum)

    return {
      ...get().mapArtistID3toArtist(artist),
      albums: mappedAlbums,
      largeImageUrl: info.largeImageUrl,
      topSongs: (await get().mapChildrenToSongs(topSongs)).slice(0, 5),
    }
  },

  mapAlbumID3toAlbumListItem: album => {
    return {
      itemType: 'album',
      id: album.id,
      name: album.name,
      artist: album.artist,
      artistId: album.artistId,
      starred: album.starred,
      coverArt: album.coverArt,
    }
  },

  mapAlbumID3toAlbum: album => {
    return {
      ...get().mapAlbumID3toAlbumListItem(album),
      coverArt: album.coverArt,
      year: album.year,
    }
  },

  mapAlbumID3WithSongstoAlbumWithSongs: async (album, songs) => {
    return {
      ...get().mapAlbumID3toAlbum(album),
      songs: await get().mapChildrenToSongs(songs),
    }
  },

  mapPlaylistListItem: playlist => {
    return {
      itemType: 'playlist',
      id: playlist.id,
      name: playlist.name,
      comment: playlist.comment,
      coverArt: playlist.coverArt,
    }
  },

  mapPlaylistWithSongs: async playlist => {
    return {
      ...get().mapPlaylistListItem(playlist),
      songs: await get().mapChildrenToSongs(playlist.songs),
      coverArt: playlist.coverArt,
    }
  },
})
