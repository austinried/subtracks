import { Song } from '@app/models/music'
import { selectMusic } from '@app/state/music'
import { useStore } from '@app/state/store'
import {
  getCurrentTrack,
  getQueue,
  getRepeatMode,
  QueueContextType,
  selectTrackPlayer,
  TrackExt,
  trackPlayerCommands,
} from '@app/state/trackplayer'
import { useCallback } from 'react'
import TrackPlayer, { RepeatMode } from 'react-native-track-player'

export const usePlay = () => {
  return () => trackPlayerCommands.enqueue(() => TrackPlayer.play())
}

export const usePause = () => {
  return () => trackPlayerCommands.enqueue(() => TrackPlayer.pause())
}

export const usePrevious = () => {
  return () =>
    trackPlayerCommands.enqueue(async () => {
      const [current] = await Promise.all([await TrackPlayer.getCurrentTrack(), await getQueue()])
      if (current > 0) {
        await TrackPlayer.skipToPrevious()
      } else {
        await TrackPlayer.seekTo(0)
      }
      await TrackPlayer.play()
    })
}

export const useNext = () => {
  return () =>
    trackPlayerCommands.enqueue(async () => {
      const [current, queue] = await Promise.all([await TrackPlayer.getCurrentTrack(), await getQueue()])
      if (current >= queue.length - 1) {
        await TrackPlayer.skip(0)
        await TrackPlayer.pause()
      } else {
        await TrackPlayer.skipToNext()
        await TrackPlayer.play()
      }
    })
}

export const useSkipTo = () => {
  return (track: number) =>
    trackPlayerCommands.enqueue(async () => {
      const queue = await getQueue()
      if (track < 0 || track >= queue.length) {
        return
      }
      await TrackPlayer.skip(track)
      await TrackPlayer.play()
    })
}

export const useSeekTo = () => {
  return (position: number) =>
    trackPlayerCommands.enqueue(async () => {
      await TrackPlayer.seekTo(position)
    })
}

export const useToggleRepeat = () => {
  const setRepeatMode = useStore(selectTrackPlayer.setRepeatMode)

  return () =>
    trackPlayerCommands.enqueue(async () => {
      const repeatMode = await getRepeatMode()
      let nextMode = RepeatMode.Off

      switch (repeatMode) {
        case RepeatMode.Off:
          nextMode = RepeatMode.Queue
          break
        case RepeatMode.Queue:
          nextMode = RepeatMode.Track
          break
        default:
          nextMode = RepeatMode.Off
          break
      }

      await TrackPlayer.setRepeatMode(nextMode)
      setRepeatMode(nextMode)
    })
}

export const useReset = (enqueue = true) => {
  const resetStore = useStore(selectTrackPlayer.reset)

  const reset = async () => {
    await TrackPlayer.reset()
    resetStore()
  }

  return enqueue ? () => trackPlayerCommands.enqueue(reset) : reset
}

function shuffleTracks(tracks: TrackExt[], firstTrack?: number) {
  if (tracks.length === 0) {
    return { tracks, shuffleOrder: [] }
  }

  const trackIndexes = tracks.map((_t, i) => i)
  let shuffleOrder: number[] = []

  for (let i = trackIndexes.length; i--; i > 0) {
    const randi = Math.floor(Math.random() * (i + 1))
    shuffleOrder.push(trackIndexes.splice(randi, 1)[0])
  }

  if (firstTrack !== undefined) {
    shuffleOrder.splice(shuffleOrder.indexOf(firstTrack), 1)
    shuffleOrder = [firstTrack, ...shuffleOrder]
  }

  tracks = shuffleOrder.map(i => tracks[i])

  return { tracks, shuffleOrder }
}

function unshuffleTracks(tracks: TrackExt[], shuffleOrder: number[]): TrackExt[] {
  if (tracks.length === 0 || shuffleOrder.length === 0) {
    return tracks
  }

  return shuffleOrder.map((_v, i) => tracks[shuffleOrder.indexOf(i)])
}

export const useToggleShuffle = () => {
  const setCurrentTrackIdx = useStore(selectTrackPlayer.setCurrentTrackIdx)
  const setQueue = useStore(selectTrackPlayer.setQueue)
  const setShuffleOrder = useStore(selectTrackPlayer.setShuffleOrder)
  const getShuffleOrder = useCallback(() => useStore.getState().shuffleOrder, [])
  const setProgress = useStore(selectTrackPlayer.setProgress)
  const getProgress = useCallback(() => useStore.getState().progress, [])

  return async () => {
    return trackPlayerCommands.enqueue(async () => {
      const queue = await getQueue()
      const current = await getCurrentTrack()
      const queueShuffleOrder = getShuffleOrder()
      const progress = getProgress()

      await TrackPlayer.remove(queue.map((_t, i) => i).filter(i => i !== current))

      if (queueShuffleOrder === undefined) {
        let { tracks, shuffleOrder } = shuffleTracks(queue, current)
        if (tracks.length > 0) {
          tracks = tracks.slice(1)
        }

        await TrackPlayer.add(tracks)
        setShuffleOrder(shuffleOrder)
      } else {
        const tracks = unshuffleTracks(queue, queueShuffleOrder)

        if (current !== undefined) {
          const shuffledCurrent = queueShuffleOrder[current]
          const tracks1 = tracks.slice(0, shuffledCurrent)
          const tracks2 = tracks.slice(shuffledCurrent + 1)

          await TrackPlayer.add(tracks2)
          await TrackPlayer.add(tracks1, 0)
        } else {
          await TrackPlayer.add(tracks)
        }

        setShuffleOrder(undefined)
      }

      setQueue(await getQueue())
      setCurrentTrackIdx(await getCurrentTrack())
      setProgress(progress)
    })
  }
}

export const useSetQueue = () => {
  const setCurrentTrackIdx = useStore(selectTrackPlayer.setCurrentTrackIdx)
  const setQueue = useStore(selectTrackPlayer.setQueue)
  const setShuffleOrder = useStore(selectTrackPlayer.setShuffleOrder)
  const setQueueName = useStore(selectTrackPlayer.setName)
  const getQueueShuffled = useCallback(() => !!useStore.getState().shuffleOrder, [])
  const setQueueContextType = useStore(selectTrackPlayer.setQueueContextType)
  const setQueueContextId = useStore(selectTrackPlayer.setQueueContextId)
  const getCoverArtPath = useStore(selectMusic.getCoverArtPath)

  return async (
    songs: Song[],
    name: string,
    contextType: QueueContextType,
    contextId: string,
    playTrack?: number,
    shuffle?: boolean,
  ) =>
    trackPlayerCommands.enqueue(async () => {
      const shuffled = shuffle !== undefined ? shuffle : getQueueShuffled()

      await TrackPlayer.setupPlayer()
      await TrackPlayer.reset()

      if (songs.length === 0) {
        return
      }

      const coverArtPaths: { [coverArt: string]: string } = {}
      for (const s of songs) {
        if (!s.coverArt) {
          continue
        }

        coverArtPaths[s.coverArt] = await getCoverArtPath(s.coverArt)
      }

      let queue = songs.map(s => mapSongToTrack(s, coverArtPaths))

      if (shuffled) {
        const { tracks, shuffleOrder } = shuffleTracks(queue, playTrack)
        setShuffleOrder(shuffleOrder)
        queue = tracks
        playTrack = 0
      } else {
        setShuffleOrder(undefined)
      }

      playTrack = playTrack || 0

      setQueue(queue)
      setCurrentTrackIdx(playTrack)
      setQueueName(name)
      setQueueContextType(contextType)
      setQueueContextId(contextId)

      if (playTrack === 0) {
        await TrackPlayer.add(queue)
        await TrackPlayer.play()
      } else {
        const tracks1 = queue.slice(0, playTrack)
        const tracks2 = queue.slice(playTrack)

        await TrackPlayer.add(tracks2)
        await TrackPlayer.play()

        await TrackPlayer.add(tracks1, 0)
      }
    })
}

export const useIsPlaying = (contextId: string | undefined, track: number) => {
  const queueContextId = useStore(selectTrackPlayer.queueContextId)
  const currentTrackIdx = useStore(selectTrackPlayer.currentTrackIdx)
  const shuffleOrder = useStore(selectTrackPlayer.shuffleOrder)

  if (contextId === undefined) {
    return track === currentTrackIdx
  }

  if (shuffleOrder) {
    const shuffledTrack = shuffleOrder.findIndex(i => i === track)
    track = shuffledTrack !== undefined ? shuffledTrack : -1
  }

  return contextId === queueContextId && track === currentTrackIdx
}

function mapSongToTrack(song: Song, coverArtPaths: { [coverArt: string]: string }): TrackExt {
  return {
    id: song.id,
    title: song.title,
    artist: song.artist || 'Unknown Artist',
    album: song.album || 'Unknown Album',
    url: song.streamUri,
    artwork: song.coverArt ? `file://${coverArtPaths[song.coverArt]}` : require('@res/fallback.png'),
    coverArt: song.coverArt,
    duration: song.duration,
  }
}

export function mapTrackExtToSong(track: TrackExt): Song {
  return {
    itemType: 'song',
    id: track.id,
    title: track.title as string,
    artist: track.artist,
    album: track.album,
    streamUri: track.url as string,
    coverArt: track.coverArt,
    duration: track.duration,
  }
}
