import { Track } from 'react-native-track-player'

export type TrackExt = Track & {
  id: string
  coverArt?: string
  artistId?: string
  albumId?: string
  track?: number
  discNumber?: number
}

export type Progress = {
  position: number
  duration: number
  buffered: number
}

export type QueueContextType = 'album' | 'playlist' | 'song' | 'artist'
