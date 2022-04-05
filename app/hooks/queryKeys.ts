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
    const key: any[] = ['albumList', type]
    if (size !== undefined) {
      key.push(size)
    }
    return key
  },

  search: (query: string, artistCount?: number, albumCount?: number, songCount?: number) => [
    'search',
    query,
    artistCount,
    albumCount,
    songCount,
  ],

  coverArt: (coverArt: string, size: CacheImageSize) => ['coverArt', coverArt, size],
  artistArt: (artistId: string, size: CacheImageSize) => ['artistArt', artistId, size],
}

export default qk
