import { useCoverArtUri } from '@app/hooks/music'
import { Song } from '@app/models/music'
import PromiseQueue from '@app/util/PromiseQueue'
import equal from 'fast-deep-equal'
import { atom } from 'jotai'
import { useAtomCallback, useAtomValue, useUpdateAtom } from 'jotai/utils'
import { atomWithStore } from 'jotai/zustand'
import { useCallback, useEffect } from 'react'
import TrackPlayer, { State, Track } from 'react-native-track-player'
import create from 'zustand'

type TrackExt = Track & {
  id: string
  coverArt?: string
}

type OptionalTrackExt = TrackExt | undefined

type Progress = {
  position: number
  duration: number
  buffered: number
}

type QueueStore = {
  name?: string
  setName: (name?: string) => void
  shuffleOrder?: number[]
  setShuffleOrder: (shuffleOrder?: number[]) => void
  shuffled: () => boolean
  reset: () => void
}

const useStore = create<QueueStore>((set, get) => ({
  name: undefined,
  setName: (name?: string) => set({ name }),
  shuffleOrder: undefined,
  setShuffleOrder: (shuffleOrder?: number[]) => set({ shuffleOrder }),
  shuffled: () => !!get().shuffleOrder,
  reset: () => set({ name: undefined, shuffleOrder: undefined }),
}))

const queueStoreAtom = atomWithStore(useStore)

export const queueNameAtom = atom<string | undefined, string | undefined>(
  get => get(queueStoreAtom).name,
  (get, set, update) => {
    get(queueStoreAtom).setName(update)
  },
)

const queueShuffleOrderAtom = atom<number[] | undefined, number[] | undefined>(
  get => get(queueStoreAtom).shuffleOrder,
  (get, set, update) => {
    get(queueStoreAtom).setShuffleOrder(update)
  },
)

export const queueShuffledAtom = atom<boolean>(get => get(queueStoreAtom).shuffled())

const playerState = atom<State>(State.None)
export const playerStateAtom = atom<State, State>(
  get => get(playerState),
  (get, set, update) => {
    if (get(playerState) !== update) {
      set(playerState, update)
    }
  },
)

const currentTrack = atom<OptionalTrackExt>(undefined)
export const currentTrackAtom = atom<OptionalTrackExt, OptionalTrackExt>(
  get => get(currentTrack),
  (get, set, update) => {
    if (!equal(get(currentTrack), update)) {
      set(currentTrack, update)
    }
  },
)

const _queue = atom<TrackExt[]>([])
export const queueAtom = atom<TrackExt[], TrackExt[]>(
  get => get(_queue),
  (get, set, update) => {
    if (!equal(get(_queue), update)) {
      set(_queue, update)
    }
  },
)

const _progress = atom<Progress>({ position: 0, duration: 0, buffered: 0 })
export const progressAtom = atom<Progress, Progress>(
  get => get(_progress),
  (get, set, update) => {
    if (!equal(get(_progress), update)) {
      set(_progress, update)
    }
  },
)

const progressSubs = atom(0)
export const progressSubsAtom = atom(get => get(progressSubs))
const addProgressSub = atom(null, (get, set) => {
  set(progressSubs, get(progressSubs) + 1)
})
const removeProgressSub = atom(null, (get, set) => {
  set(progressSubs, get(progressSubs) - 1)
})

export const trackPlayerCommands = new PromiseQueue(1)

const getQueue = async (): Promise<TrackExt[]> => {
  return ((await TrackPlayer.getQueue()) as TrackExt[]) || []
}

const getTrack = async (index: number): Promise<TrackExt> => {
  return ((await TrackPlayer.getTrack(index)) as TrackExt) || undefined
}

const getCurrentTrack = async (): Promise<number | undefined> => {
  const current = await TrackPlayer.getCurrentTrack()
  return typeof current === 'number' ? current : undefined
}

const getPlayerState = async (): Promise<State> => {
  return (await TrackPlayer.getState()) || State.None
}

const getProgress = async (): Promise<Progress> => {
  const [position, duration, buffered] = await Promise.all([
    TrackPlayer.getPosition(),
    TrackPlayer.getDuration(),
    TrackPlayer.getBufferedPosition(),
  ])
  return {
    position: position || 0,
    duration: duration || 0,
    buffered: buffered || 0,
  }
}

export const useRefreshQueue = () => {
  const setQueue = useUpdateAtom(queueAtom)

  return () =>
    trackPlayerCommands.enqueue(async () => {
      setQueue(await getQueue())
    })
}

export const useRefreshCurrentTrack = () => {
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)

  return () =>
    trackPlayerCommands.enqueue(async () => {
      const index = await TrackPlayer.getCurrentTrack()
      if (typeof index === 'number' && index >= 0) {
        setCurrentTrack(await getTrack(index))
      } else {
        setCurrentTrack(undefined)
      }
    })
}

export const useRefreshPlayerState = () => {
  const setPlayerState = useUpdateAtom(playerStateAtom)

  return () =>
    trackPlayerCommands.enqueue(async () => {
      setPlayerState(await getPlayerState())
    })
}

export const useRefreshProgress = () => {
  const setProgress = useUpdateAtom(progressAtom)

  return () =>
    trackPlayerCommands.enqueue(async () => {
      setProgress(await getProgress())
    })
}

export const usePlay = () => {
  return () => trackPlayerCommands.enqueue(() => TrackPlayer.play())
}

export const usePause = () => {
  return () => trackPlayerCommands.enqueue(() => TrackPlayer.pause())
}

export const usePrevious = () => {
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)

  return () =>
    trackPlayerCommands.enqueue(async () => {
      const [current, queue] = await Promise.all([await TrackPlayer.getCurrentTrack(), await getQueue()])
      if (current > 0) {
        await TrackPlayer.skipToPrevious()
        setCurrentTrack(queue[current - 1])
      } else {
        await TrackPlayer.seekTo(0)
      }
      await TrackPlayer.play()
    })
}

export const useNext = () => {
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)

  return () =>
    trackPlayerCommands.enqueue(async () => {
      const [current, queue] = await Promise.all([await TrackPlayer.getCurrentTrack(), await getQueue()])
      if (current >= queue.length - 1) {
        await TrackPlayer.skip(0)
        await TrackPlayer.pause()
        setCurrentTrack(queue[0])
      } else {
        await TrackPlayer.skipToNext()
        setCurrentTrack(queue[current + 1])
        await TrackPlayer.play()
      }
    })
}

export const useReset = (enqueue = true) => {
  const setQueue = useUpdateAtom(queueAtom)
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)
  const resetQueueStore = useStore(state => state.reset)

  const reset = async () => {
    await TrackPlayer.reset()
    setQueue([])
    setCurrentTrack(undefined)
    resetQueueStore()
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
  const setQueue = useUpdateAtom(queueAtom)
  const setQueueShuffleOrder = useUpdateAtom(queueShuffleOrderAtom)
  const getQueueShuffleOrder = useAtomCallback(useCallback(get => get(queueShuffleOrderAtom), []))

  return async () => {
    return trackPlayerCommands.enqueue(async () => {
      const queue = await getQueue()
      const current = await getCurrentTrack()
      const queueShuffleOrder = await getQueueShuffleOrder()

      await TrackPlayer.remove(queue.map((_t, i) => i).filter(i => i !== current))

      if (queueShuffleOrder === undefined) {
        let { tracks, shuffleOrder } = shuffleTracks(queue, current)
        if (tracks.length > 0) {
          tracks = tracks.slice(1)
        }

        await TrackPlayer.add(tracks)
        setQueueShuffleOrder(shuffleOrder)
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

        setQueueShuffleOrder(undefined)
      }

      setQueue(await getQueue())
    })
  }
}

export const useSetQueue = () => {
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)
  const setQueue = useUpdateAtom(queueAtom)
  const setQueueShuffleOrder = useUpdateAtom(queueShuffleOrderAtom)
  const setQueueName = useUpdateAtom(queueNameAtom)
  const reset = useReset(false)
  const getQueueShuffled = useAtomCallback(useCallback(get => get(queueShuffledAtom), []))
  const coverArtUri = useCoverArtUri()

  return async (songs: Song[], name: string, playTrack?: number, shuffle?: boolean) =>
    trackPlayerCommands.enqueue(async () => {
      const shuffled = shuffle !== undefined ? shuffle : await getQueueShuffled()

      await TrackPlayer.setupPlayer()
      await reset()

      if (songs.length === 0) {
        return
      }

      let queue = songs.map(s => mapSongToTrack(s, coverArtUri))

      if (shuffled) {
        const { tracks, shuffleOrder } = shuffleTracks(queue, playTrack)
        setQueueShuffleOrder(shuffleOrder)
        queue = tracks
        playTrack = 0
      }

      playTrack = playTrack || 0
      setCurrentTrack(queue[playTrack])

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

      setQueue(await getQueue())
      setQueueName(name)
    })
}

export const useProgress = () => {
  const progress = useAtomValue(progressAtom)
  const addSub = useUpdateAtom(addProgressSub)
  const removeSub = useUpdateAtom(removeProgressSub)

  useEffect(() => {
    addSub()
    return removeSub
  }, [addSub, removeSub])

  return progress
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
