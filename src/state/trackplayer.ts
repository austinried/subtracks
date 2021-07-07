import equal from 'fast-deep-equal'
import { atom } from 'jotai'
import { useAtomValue, useUpdateAtom } from 'jotai/utils'
import { useEffect } from 'react'
import TrackPlayer, { State, Track } from 'react-native-track-player'
import { Song } from '../models/music'
import { PromiseQueue } from '../util'

type TrackExt = Track & {
  id: string
  queueName: string
  artworkThumb?: string
}

type OptionalTrackExt = TrackExt | undefined

type Progress = {
  position: number
  duration: number
  buffered: number
}

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
export const queueReadAtom = atom<TrackExt[]>(get => get(_queue))
export const queueWriteAtom = atom<TrackExt[], TrackExt[]>(
  get => get(_queue),
  (get, set, update) => {
    if (!equal(get(_queue), update)) {
      set(_queue, update)
    }
  },
)

export const queueNameAtom = atom<string | undefined>(get => {
  const queue = get(_queue)
  if (queue.length > 0) {
    return queue[0].queueName
  }
  return undefined
})

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
  const setQueue = useUpdateAtom(queueWriteAtom)

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

export const useAdd = () => {
  const setQueue = useUpdateAtom(queueWriteAtom)
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)

  return (tracks: TrackExt | TrackExt[], insertBeforeindex?: number) =>
    trackPlayerCommands.enqueue(async () => {
      await TrackPlayer.add(tracks, insertBeforeindex)

      const queue = await getQueue()
      setQueue(queue)
      setCurrentTrack(queue.length > 0 ? queue[await TrackPlayer.getCurrentTrack()] : undefined)
    })
}

export const useReset = (enqueue = true) => {
  const setQueue = useUpdateAtom(queueWriteAtom)
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)

  const reset = async () => {
    await TrackPlayer.reset()
    setQueue([])
    setCurrentTrack(undefined)
  }

  return enqueue ? () => trackPlayerCommands.enqueue(reset) : reset
}

export const useSetQueue = () => {
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)
  const setQueue = useUpdateAtom(queueWriteAtom)
  const reset = useReset(false)

  return async (songs: Song[], name: string, playId?: string) =>
    trackPlayerCommands.enqueue(async () => {
      await TrackPlayer.setupPlayer()
      await reset()
      const tracks = songs.map(s => mapSongToTrack(s, name))

      if (playId) {
        setCurrentTrack(tracks.find(t => t.id === playId))
      }

      if (!playId) {
        await TrackPlayer.add(tracks)
      } else if (playId === tracks[0].id) {
        await TrackPlayer.add(tracks)
        await TrackPlayer.play()
      } else {
        const playIndex = tracks.findIndex(t => t.id === playId)
        const tracks1 = tracks.slice(0, playIndex)
        const tracks2 = tracks.slice(playIndex)

        await TrackPlayer.add(tracks2)
        await TrackPlayer.play()

        await TrackPlayer.add(tracks1, 0)
      }

      setQueue(await getQueue())
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

function mapSongToTrack(song: Song, queueName: string): TrackExt {
  return {
    id: song.id,
    queueName,
    title: song.title,
    artist: song.artist || 'Unknown Artist',
    album: song.album || 'Unknown Album',
    url: song.streamUri,
    artwork: song.coverArtUri,
    artworkThumb: song.coverArtThumbUri,
    duration: song.duration,
  }
}
