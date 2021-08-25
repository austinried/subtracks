import { NoClientError } from '@app/models/error'
import { Song } from '@app/models/music'
import PromiseQueue from '@app/util/PromiseQueue'
import produce from 'immer'
import TrackPlayer, { PlayerOptions, RepeatMode, State, Track } from 'react-native-track-player'
import { GetState, SetState } from 'zustand'
import { Store } from './store'

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

export type TrackPlayerSlice = {
  queueName?: string
  setQueueName: (name?: string) => void

  queueContextType?: QueueContextType
  setQueueContextType: (queueContextType?: QueueContextType) => void

  queueContextId?: string
  setQueueContextId: (queueContextId?: string) => void

  shuffleOrder?: number[]
  toggleShuffle: () => Promise<void>

  repeatMode: RepeatMode
  toggleRepeatMode: () => Promise<void>

  playerState: State
  setPlayerState: (playerState: State) => void

  currentTrack?: TrackExt
  currentTrackIdx?: number
  setCurrentTrackIdx: (idx?: number) => void

  queue: TrackExt[]
  setQueue: (
    songs: Song[],
    name: string,
    contextType: QueueContextType,
    contextId: string,
    playTrack?: number,
    shuffle?: boolean,
  ) => Promise<void>

  progress: Progress
  setProgress: (progress: Progress) => void

  scrobbleTrack: (id: string) => Promise<void>

  netState: 'mobile' | 'wifi'
  setNetState: (netState: 'mobile' | 'wifi') => Promise<void>

  rebuildQueue: (forcePlay?: boolean) => Promise<void>
  buildStreamUri: (id: string) => string
  resetTrackPlayerState: () => void

  getPlayerOptions: () => PlayerOptions
}

export const selectTrackPlayer = {
  queueName: (store: TrackPlayerSlice) => store.queueName,
  setQueueName: (store: TrackPlayerSlice) => store.setQueueName,

  queueContextType: (store: TrackPlayerSlice) => store.queueContextType,
  setQueueContextType: (store: TrackPlayerSlice) => store.setQueueContextType,

  queueContextId: (store: TrackPlayerSlice) => store.queueContextId,
  setQueueContextId: (store: TrackPlayerSlice) => store.setQueueContextId,

  shuffleOrder: (store: TrackPlayerSlice) => store.shuffleOrder,
  shuffled: (store: TrackPlayerSlice) => !!store.shuffleOrder,
  toggleShuffle: (store: TrackPlayerSlice) => store.toggleShuffle,

  repeatMode: (store: TrackPlayerSlice) => store.repeatMode,
  toggleRepeatMode: (store: TrackPlayerSlice) => store.toggleRepeatMode,

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

  resetTrackPlayerState: (store: TrackPlayerSlice) => store.resetTrackPlayerState,
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
  toggleShuffle: async () => {
    return trackPlayerCommands.enqueue(async () => {
      const queue = await getQueue()
      const current = await getCurrentTrack()
      const queueShuffleOrder = get().shuffleOrder

      await TrackPlayer.remove(queue.map((_t, i) => i).filter(i => i !== current))

      if (queueShuffleOrder === undefined) {
        let { tracks, shuffleOrder } = shuffleTracks(queue, current)
        if (tracks.length > 0) {
          tracks = tracks.slice(1)
        }

        await TrackPlayer.add(tracks)
        set({ shuffleOrder })
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

        set({ shuffleOrder: undefined })
      }

      set({ queue: await getQueue() })
      get().setCurrentTrackIdx(await getCurrentTrack())
    })
  },

  repeatMode: RepeatMode.Off,
  toggleRepeatMode: async () => {
    return trackPlayerCommands.enqueue(async () => {
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
      set({ repeatMode: nextMode })
    })
  },

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
  setQueue: async (songs, name, contextType, contextId, playTrack, shuffle) => {
    return trackPlayerCommands.enqueue(async () => {
      const shuffled = shuffle !== undefined ? shuffle : !!get().shuffleOrder

      await TrackPlayer.setupPlayer(get().getPlayerOptions())
      await TrackPlayer.reset()

      if (songs.length === 0) {
        return
      }

      let queue = await get().mapSongstoTrackExts(songs)

      try {
        for (const t of queue) {
          t.url = get().buildStreamUri(t.id)
        }
      } catch {
        return
      }

      if (shuffled) {
        const { tracks, shuffleOrder } = shuffleTracks(queue, playTrack)
        set({ shuffleOrder })
        queue = tracks
        playTrack = 0
      } else {
        set({ shuffleOrder: undefined })
      }

      playTrack = playTrack || 0

      try {
        set({
          queue,
          queueName: name,
          queueContextType: contextType,
          queueContextId: contextId,
        })
        get().setCurrentTrackIdx(playTrack)

        if (playTrack === 0) {
          await TrackPlayer.add(queue)
        } else {
          const tracks1 = queue.slice(0, playTrack)
          const tracks2 = queue.slice(playTrack)

          await TrackPlayer.add(tracks2)
          await TrackPlayer.add(tracks1, 0)
        }

        await TrackPlayer.play()
      } catch {
        get().resetTrackPlayerState()
        await TrackPlayer.reset()
      }
    })
  },

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

  rebuildQueue: async forcePlay => {
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
      await TrackPlayer.setupPlayer(get().getPlayerOptions())

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

      if (state === State.Playing || forcePlay) {
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

  resetTrackPlayerState: () => {
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

  getPlayerOptions: () => {
    return {
      minBuffer: get().settings.minBuffer,
      playBuffer: get().settings.minBuffer / 2,
      maxBuffer: get().settings.maxBuffer,
    }
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
