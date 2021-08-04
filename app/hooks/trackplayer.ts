import { useCoverArtUri } from '@app/hooks/music'
import { Song } from '@app/models/music'
import { useStore } from '@app/state/store'
import { getCurrentTrack, getQueue, selectTrackPlayer, TrackExt, trackPlayerCommands } from '@app/state/trackplayer'
import { useCallback } from 'react'
import TrackPlayer from 'react-native-track-player'

export const usePlay = () => {
  return () => trackPlayerCommands.enqueue(() => TrackPlayer.play())
}

export const usePause = () => {
  return () => trackPlayerCommands.enqueue(() => TrackPlayer.pause())
}

export const usePrevious = () => {
  // const setCurrentTrackIdx = useStore(selectTrackPlayer.setCurrentTrackIdx)

  return () =>
    trackPlayerCommands.enqueue(async () => {
      const [current] = await Promise.all([await TrackPlayer.getCurrentTrack(), await getQueue()])
      if (current > 0) {
        await TrackPlayer.skipToPrevious()
        // setCurrentTrackIdx(current - 1)
      } else {
        await TrackPlayer.seekTo(0)
      }
      await TrackPlayer.play()
    })
}

export const useNext = () => {
  // const setCurrentTrack = useUpdateAtom(currentTrackAtom)

  return () =>
    trackPlayerCommands.enqueue(async () => {
      const [current, queue] = await Promise.all([await TrackPlayer.getCurrentTrack(), await getQueue()])
      if (current >= queue.length - 1) {
        await TrackPlayer.skip(0)
        await TrackPlayer.pause()
        // setCurrentTrack(queue[0])
      } else {
        await TrackPlayer.skipToNext()
        // setCurrentTrack(queue[current + 1])
        await TrackPlayer.play()
      }
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
  const setQueue = useStore(selectTrackPlayer.setQueue)
  const setShuffleOrder = useStore(selectTrackPlayer.setShuffleOrder)
  const getShuffleOrder = useCallback(() => useStore.getState().shuffleOrder, [])

  return async () => {
    return trackPlayerCommands.enqueue(async () => {
      const queue = await getQueue()
      const current = await getCurrentTrack()
      const queueShuffleOrder = getShuffleOrder()

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
    })
  }
}

export const useSetQueue = () => {
  const setCurrentTrackIdx = useStore(selectTrackPlayer.setCurrentTrackIdx)
  const setQueue = useStore(selectTrackPlayer.setQueue)
  const setShuffleOrder = useStore(selectTrackPlayer.setShuffleOrder)
  const setQueueName = useStore(selectTrackPlayer.setName)
  const getQueueShuffled = useCallback(() => !!useStore.getState().shuffleOrder, [])
  const coverArtUri = useCoverArtUri()

  return async (songs: Song[], name: string, playTrack?: number, shuffle?: boolean) =>
    trackPlayerCommands.enqueue(async () => {
      const shuffled = shuffle !== undefined ? shuffle : getQueueShuffled()

      await TrackPlayer.setupPlayer()
      await TrackPlayer.reset()

      if (songs.length === 0) {
        return
      }

      let queue = songs.map(s => mapSongToTrack(s, coverArtUri))

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

      // setQueue(await getQueue())
      // setCurrentTrackIdx(playTrack)
      // setQueueName(name)
    })
}

function mapSongToTrack(song: Song, coverArtUri: (coverArt?: string) => string | undefined): TrackExt {
  return {
    id: song.id,
    title: song.title,
    artist: song.artist || 'Unknown Artist',
    album: song.album || 'Unknown Album',
    url: song.streamUri,
    artwork: coverArtUri(song.coverArt),
    coverArt: song.coverArt,
    duration: song.duration,
  }
}
