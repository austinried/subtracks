import { NoClientError } from '@app/models/error'
import PromiseQueue from '@app/util/PromiseQueue'
import produce from 'immer'
import { ToastAndroid } from 'react-native'
import TrackPlayer, { RepeatMode, State, Track } from 'react-native-track-player'
import { GetState, SetState } from 'zustand'
import { Store } from './store'

export type TrackExt = Track & {
  id: string
  coverArt?: string
  artistId?: string
  albumId?: string
}

export type Progress = {
  position: number
  duration: number
  buffered: number
}

export type QueueContextType = 'album' | 'playlist' | 'song' | 'artist'

export type TrackPlayerSlice = {
  queueName?: string
  setQueueName: (name?: string) => void

  queueContextType?: QueueContextType
  setQueueContextType: (queueContextType?: QueueContextType) => void

  queueContextId?: string
  setQueueContextId: (queueContextId?: string) => void

  shuffleOrder?: number[]
  setShuffleOrder: (shuffleOrder?: number[]) => void

  repeatMode: RepeatMode
  setRepeatMode: (repeatMode: RepeatMode) => void

  playerState: State
  setPlayerState: (playerState: State) => void

  currentTrack?: TrackExt
  currentTrackIdx?: number
  setCurrentTrackIdx: (idx?: number) => void

  queue: TrackExt[]
  setQueue: (queue: TrackExt[]) => void

  progress: Progress
  setProgress: (progress: Progress) => void

  scrobbleTrack: (id: string) => Promise<void>

  netState: 'mobile' | 'wifi'
  setNetState: (netState: 'mobile' | 'wifi') => Promise<void>

  rebuildQueue: () => Promise<void>
  buildStreamUri: (id: string) => string
  reset: () => void
}

export const selectTrackPlayer = {
  queueName: (store: TrackPlayerSlice) => store.queueName,
  setQueueName: (store: TrackPlayerSlice) => store.setQueueName,

  queueContextType: (store: TrackPlayerSlice) => store.queueContextType,
  setQueueContextType: (store: TrackPlayerSlice) => store.setQueueContextType,

  queueContextId: (store: TrackPlayerSlice) => store.queueContextId,
  setQueueContextId: (store: TrackPlayerSlice) => store.setQueueContextId,

  shuffleOrder: (store: TrackPlayerSlice) => store.shuffleOrder,
  setShuffleOrder: (store: TrackPlayerSlice) => store.setShuffleOrder,
  shuffled: (store: TrackPlayerSlice) => !!store.shuffleOrder,

  repeatMode: (store: TrackPlayerSlice) => store.repeatMode,
  setRepeatMode: (store: TrackPlayerSlice) => store.setRepeatMode,

  playerState: (store: TrackPlayerSlice) => store.playerState,
  setPlayerState: (store: TrackPlayerSlice) => store.setPlayerState,

  currentTrack: (store: TrackPlayerSlice) => store.currentTrack,
  currentTrackIdx: (store: TrackPlayerSlice) => store.currentTrackIdx,
  setCurrentTrackIdx: (store: TrackPlayerSlice) => store.setCurrentTrackIdx,

  queue: (store: TrackPlayerSlice) => store.queue,
  setQueue: (store: TrackPlayerSlice) => store.setQueue,

  progress: (store: TrackPlayerSlice) => store.progress,
  setProgress: (store: TrackPlayerSlice) => store.setProgress,

  scrobbleTrack: (store: TrackPlayerSlice) => store.scrobbleTrack,

  setNetState: (store: TrackPlayerSlice) => store.setNetState,
  buildStreamUri: (store: TrackPlayerSlice) => store.buildStreamUri,

  reset: (store: TrackPlayerSlice) => store.reset,
}

export const trackPlayerCommands = new PromiseQueue(1)

export const createTrackPlayerSlice = (set: SetState<Store>, get: GetState<Store>): TrackPlayerSlice => ({
  queueName: undefined,
  setQueueName: name => set({ queueName: name }),

  queueContextType: undefined,
  setQueueContextType: queueContextType => set({ queueContextType }),

  queueContextId: undefined,
  setQueueContextId: queueContextId => set({ queueContextId }),

  shuffleOrder: undefined,
  setShuffleOrder: shuffleOrder => set({ shuffleOrder }),

  repeatMode: RepeatMode.Off,
  setRepeatMode: repeatMode => set({ repeatMode }),

  playerState: State.None,
  setPlayerState: playerState => set({ playerState }),

  currentTrack: undefined,
  currentTrackIdx: undefined,
  setCurrentTrackIdx: idx => {
    set(
      produce<TrackPlayerSlice>(state => {
        state.currentTrackIdx = idx
        state.currentTrack = idx !== undefined ? state.queue[idx] : undefined
      }),
    )
  },

  queue: [],
  setQueue: queue => set({ queue }),

  progress: { position: 0, duration: 0, buffered: 0 },
  setProgress: progress => set({ progress }),

  scrobbleTrack: async id => {
    const client = get().client
    if (!client) {
      return
    }

    if (!get().settings.scrobble) {
      return
    }

    try {
      await client.scrobble({ id })
    } catch {}
  },

  netState: 'mobile',
  setNetState: async netState => {
    if (netState === get().netState) {
      return
    }
    set({ netState })
    get().rebuildQueue()
  },

  rebuildQueue: async () => {
    return trackPlayerCommands.enqueue(async () => {
      const queue = await getQueue()
      if (!queue.length) {
        return
      }

      const currentTrack = await getCurrentTrack()
      const state = await getPlayerState()
      const position = (await TrackPlayer.getPosition()) || 0

      const queueName = get().queueName
      const queueContextId = get().queueContextId
      const queueContextType = get().queueContextType

      await TrackPlayer.reset()

      try {
        for (const track of queue) {
          track.url = get().buildStreamUri(track.id)
        }
      } catch {
        return
      }

      set({
        queue,
        queueName,
        queueContextId,
        queueContextType,
      })
      get().setCurrentTrackIdx(currentTrack)

      await TrackPlayer.add(queue)
      if (currentTrack) {
        await TrackPlayer.skip(currentTrack)
      }
      await TrackPlayer.seekTo(position)
      if (state === State.Playing) {
        await TrackPlayer.play()
      }
    })
  },

  buildStreamUri: id => {
    const client = get().client
    if (!client) {
      throw new NoClientError()
    }

    return client.streamUri({
      id,
      estimateContentLength: get().settings.estimateContentLength,
      maxBitRate: get().netState === 'mobile' ? get().settings.maxBitrateMobile : get().settings.maxBitrateWifi,
    })
  },

  reset: () => {
    set({
      queueName: undefined,
      queueContextType: undefined,
      queueContextId: undefined,
      shuffleOrder: undefined,
      repeatMode: RepeatMode.Off,
      playerState: State.None,
      currentTrack: undefined,
      currentTrackIdx: undefined,
      queue: [],
      progress: { position: 0, duration: 0, buffered: 0 },
    })
  },
})

export const getQueue = async (): Promise<TrackExt[]> => {
  return ((await TrackPlayer.getQueue()) as TrackExt[]) || []
}

export const getCurrentTrack = async (): Promise<number | undefined> => {
  const current = await TrackPlayer.getCurrentTrack()
  return typeof current === 'number' ? current : undefined
}

export const getPlayerState = async (): Promise<State> => {
  return (await TrackPlayer.getState()) || State.None
}

export const getRepeatMode = async (): Promise<RepeatMode> => {
  return (await TrackPlayer.getRepeatMode()) || RepeatMode.Off
}
