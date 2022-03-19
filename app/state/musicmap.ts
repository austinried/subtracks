import { AlbumListItem, Artist, PlaylistListItem, Song } from '@app/models/music'
import { AlbumID3Element, ArtistID3Element, ChildElement, PlaylistElement } from '@app/subsonic/elements'
import { GetState, SetState } from 'zustand'
import { Store } from './store'

export type MusicMapSlice = {
  mapChildToSong: (child: ChildElement, coverArt?: string) => Promise<Song>
  mapChildrenToSongs: (children: ChildElement[], coverArt?: string) => Promise<Song[]>
  mapArtistID3toArtist: (artist: ArtistID3Element) => Artist
  mapAlbumID3toAlbumListItem: (album: AlbumID3Element) => AlbumListItem
  mapAlbumID3toAlbum: (album: AlbumID3Element) => AlbumListItem
  mapPlaylistListItem: (playlist: PlaylistElement) => PlaylistListItem
}

export const createMusicMapSlice = (set: SetState<Store>, get: GetState<Store>): MusicMapSlice => ({
  mapChildToSong: async (child, coverArt) => {
    return {
      itemType: 'song',
      id: child.id,
      album: child.album,
      albumId: child.albumId,
      artist: child.artist,
      artistId: child.artistId,
      title: child.title,
      track: child.track,
      discNumber: child.discNumber,
      duration: child.duration,
      starred: child.starred,
      coverArt: coverArt || (await get().getAlbumCoverArt(child.albumId)),
      streamUri: get().buildStreamUri(child.id),
    }
  },

  mapChildrenToSongs: async (children, coverArt) => {
    const albumIds = children.reduce((acc, val) => {
      if (val.albumId && !(val.albumId in acc)) {
        acc[val.albumId] = get().getAlbumCoverArt(val.albumId)
      }
      return acc
    }, {} as Record<string, Promise<string | undefined>>)

    await Promise.all(Object.values(albumIds))

    const songs: Song[] = []
    for (const child of children) {
      songs.push(await get().mapChildToSong(child, coverArt || (await get().getAlbumCoverArt(child.albumId))))
    }
    return songs
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

  mapPlaylistListItem: playlist => {
    return {
      itemType: 'playlist',
      id: playlist.id,
      name: playlist.name,
      comment: playlist.comment,
      coverArt: playlist.coverArt,
    }
  },
})
