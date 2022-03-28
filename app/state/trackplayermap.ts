import { Song } from '@app/models/library'
import { TrackExt } from '@app/models/trackplayer'
import userAgent from '@app/util/userAgent'
import { GetStore, SetStore } from '@app/state/store'

export type TrackPlayerMapSlice = {
  mapSongtoTrackExt: (song: Song) => Promise<TrackExt>
  mapSongstoTrackExts: (songs: Song[]) => Promise<TrackExt[]>
  mapTrackExtToSong: (song: TrackExt) => Song
}

export const createTrackPlayerMapSlice = (set: SetStore, get: GetStore): TrackPlayerMapSlice => ({
  mapSongtoTrackExt: async song => {
    let artwork = require('@res/fallback.png')
    if (song.coverArt) {
      const filePath = await get().fetchCoverArtFilePath(song.coverArt)
      if (filePath) {
        artwork = filePath
      }
    }

    return {
      id: song.id,
      title: song.title,
      artist: song.artist || 'Unknown Artist',
      album: song.album || 'Unknown Album',
      url: get().buildStreamUri(song.id),
      userAgent,
      artwork,
      coverArt: song.coverArt,
      duration: song.duration,
      artistId: song.artistId,
      albumId: song.albumId,
      track: song.track,
      discNumber: song.discNumber,
    }
  },

  mapSongstoTrackExts: async songs => {
    return await Promise.all(songs.map(get().mapSongtoTrackExt))
  },

  mapTrackExtToSong: track => {
    return {
      itemType: 'song',
      id: track.id,
      title: track.title as string,
      artist: track.artist,
      album: track.album,
      streamUri: track.url as string,
      coverArt: track.coverArt,
      duration: track.duration,
      artistId: track.artistId,
      albumId: track.albumId,
      track: track.track,
      discNumber: track.discNumber,
    }
  },
})
