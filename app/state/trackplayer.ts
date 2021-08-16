import PromiseQueue from '@app/util/PromiseQueue'
import produce from 'immer'
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
  name?: string
  setName: (name?: string) => void

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

  reset: () => void
}

export const selectTrackPlayer = {
  name: (store: TrackPlayerSlice) => store.name,
  setName: (store: TrackPlayerSlice) => store.setName,

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

  reset: (store: TrackPlayerSlice) => store.reset,
}

export const trackPlayerCommands = new PromiseQueue(1)

export const createTrackPlayerSlice = (set: SetState<Store>, get: GetState<Store>): TrackPlayerSlice => ({
  name: undefined,
  setName: name => set({ name }),

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

    if (!get().getActiveServer()?.scrobble) {
      return
    }

    try {
      await client.scrobble({ id })
    } catch {}
  },

  reset: () => {
    set({
      name: undefined,
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
