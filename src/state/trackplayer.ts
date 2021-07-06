import { atom } from 'jotai'
import TrackPlayer, { State, Track } from 'react-native-track-player'
import equal from 'fast-deep-equal'
import { useUpdateAtom } from 'jotai/utils'
import { Song } from '../models/music'

type TrackExt = Track & {
  id: string
  queueName: string
}

type OptionalTrackExt = TrackExt | undefined

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
export const queueAtom = atom<TrackExt[]>(get => get(_queue))
export const queueWriteAtom = atom<TrackExt[], TrackExt[]>(
  get => get(_queue),
  (get, set, update) => {
    if (get(_queue) !== update) {
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

export const getQueue = async (): Promise<TrackExt[]> => {
  return ((await TrackPlayer.getQueue()) as TrackExt[]) || []
}

export const getTrack = async (index: number): Promise<TrackExt> => {
  return ((await TrackPlayer.getTrack(index)) as TrackExt) || undefined
}

export const usePrevious = () => {
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)

  return async () => {
    try {
      const [current, queue] = await Promise.all([await TrackPlayer.getCurrentTrack(), await getQueue()])
      if (current > 0) {
        await TrackPlayer.skipToPrevious()
        setCurrentTrack(queue[current - 1])
      } else {
        await TrackPlayer.seekTo(0)
      }
      await TrackPlayer.play()
    } catch {}
  }
}

export const useNext = () => {
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)

  return async () => {
    try {
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
    } catch {}
  }
}

export const useAdd = () => {
  const setQueue = useUpdateAtom(queueWriteAtom)
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)

  return async (tracks: TrackExt | TrackExt[], insertBeforeindex?: number) => {
    await TrackPlayer.add(tracks, insertBeforeindex)

    const queue = await getQueue()
    setQueue(queue)
    setCurrentTrack(queue.length > 0 ? queue[await TrackPlayer.getCurrentTrack()] : undefined)
  }
}

export const useReset = () => {
  const setQueue = useUpdateAtom(queueWriteAtom)
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)

  return async () => {
    await TrackPlayer.reset()
    setQueue([])
    setCurrentTrack(undefined)
  }
}

export const useSetQueue = () => {
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)
  const setQueue = useUpdateAtom(queueWriteAtom)

  return async (songs: Song[], name: string, playId?: string) => {
    await TrackPlayer.reset()
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
  }
}

function mapSongToTrack(song: Song, queueName: string): TrackExt {
  return {
    id: song.id,
    queueName,
    title: song.title,
    artist: song.artist || 'Unknown Artist',
    url: song.streamUri,
    artwork: song.coverArtUri,
    artworkThumb: song.coverArtThumbUri,
    duration: song.duration,
  }
}
