import { CacheImageSize } from '@app/models/cache'
import { GetAlbumList2TypeBase } from '@app/subsonic/params'

const qk = {
  starredItems: (id: string) => ['starredItems', id],
  albumCoverArt: (id: string) => ['albumCoverArt', id],

  artists: 'artists',
  artist: (id: string) => ['artist', id],
  artistInfo: (id: string) => ['artistInfo', id],
  artistTopSongs: (artistName: string) => ['artistTopSongs', artistName],

  playlists: 'playlists',
  playlist: (id: string) => ['playlist', id],

  album: (id: string) => ['album', id],
  albumList: (type: GetAlbumList2TypeBase, size?: number) => {
    const key: (string | number)[] = ['albumList', type]
    size !== undefined && key.push(size)
    return key
  },

  search: (query: string, artistCount?: number, albumCount?: number, songCount?: number) => [
    'search',
    query,
    artistCount,
    albumCount,
    songCount,
  ],

  coverArt: (coverArt?: string, size?: CacheImageSize) => {
    const key: string[] = ['coverArt']
    coverArt !== undefined && key.push(coverArt)
    size !== undefined && key.push(size)
    return key
  },
  artistArt: (artistId?: string, size?: CacheImageSize) => {
    const key: string[] = ['artistArt']
    artistId !== undefined && key.push(artistId)
    size !== undefined && key.push(size)
    return key
  },
}

export default qk
