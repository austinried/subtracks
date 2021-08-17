import { Song } from '@app/models/music'
import { GetState, SetState } from 'zustand'
import { Store } from './store'
import { TrackExt } from './trackplayer'

export type TrackPlayerMapSlice = {
  mapSongtoTrackExt: (song: Song) => Promise<TrackExt>
  mapSongstoTrackExts: (songs: Song[]) => Promise<TrackExt[]>
  mapTrackExtToSong: (song: TrackExt) => Song
}

export const selectTrackPlayerMap = {
  mapTrackExtToSong: (store: TrackPlayerMapSlice) => store.mapTrackExtToSong,
}

export const createTrackPlayerMapSlice = (set: SetState<Store>, get: GetState<Store>): TrackPlayerMapSlice => ({
  mapSongtoTrackExt: async song => {
    return {
      id: song.id,
      title: song.title,
      artist: song.artist || 'Unknown Artist',
      album: song.album || 'Unknown Album',
      url: song.streamUri,
      artwork: song.coverArt ? await get().fetchCoverArtFilePath(song.coverArt) : require('@res/fallback.png'),
      coverArt: song.coverArt,
      duration: song.duration,
      artistId: song.artistId,
      albumId: song.albumId,
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
    }
  },
})
