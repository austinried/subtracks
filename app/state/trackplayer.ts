import { NoClientError } from '@app/models/error'
import { Progress, QueueContextType, TrackExt } from '@app/models/trackplayer'
import PromiseQueue from '@app/util/PromiseQueue'
import produce from 'immer'
import TrackPlayer, { PlayerOptions, RepeatMode, State } from 'react-native-track-player'
import { GetStore, SetStore } from './store'

export type SetQueueOptions = {
  title: string
  playTrack?: number
  shuffle?: boolean
}

export type SetQueueOptionsInternal = SetQueueOptions & {
  queue: TrackExt[]
  contextId: string
  type: QueueContextType
}

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

  duckPaused: boolean
  setDuckPaused: (duckPaused: boolean) => void

  currentTrack?: TrackExt
  currentTrackIdx?: number
  setCurrentTrackIdx: (idx?: number) => void

  queue: TrackExt[]
  setQueue: (options: SetQueueOptionsInternal) => Promise<void>

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

export const trackPlayerCommands = new PromiseQueue(1)

export const createTrackPlayerSlice = (set: SetStore, get: GetStore): TrackPlayerSlice => ({
  queueName: undefined,
  setQueueName: name =>
    set(state => {
      state.queueName = name
    }),

  queueContextType: undefined,
  setQueueContextType: queueContextType =>
    set(state => {
      state.queueContextType = queueContextType
    }),

  queueContextId: undefined,
  setQueueContextId: queueContextId =>
    set(state => {
      state.queueContextId = queueContextId
    }),

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
        set(state => {
          state.shuffleOrder = shuffleOrder
        })
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

        set(state => {
          state.shuffleOrder = undefined
        })
      }

      const newQueue = await getQueue()
      const newCurrentTrackIdx = await getCurrentTrack()

      set(state => {
        state.queue = newQueue
      })
      get().setCurrentTrackIdx(newCurrentTrackIdx)
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
      set(state => {
        state.repeatMode = nextMode
      })
    })
  },

  playerState: State.None,
  setPlayerState: playerState =>
    set(state => {
      state.playerState = playerState
    }),

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

  duckPaused: false,
  setDuckPaused: duckPaused =>
    set(state => {
      state.duckPaused = duckPaused
    }),

  queue: [],
  setQueue: async ({ queue, title, type, contextId, playTrack, shuffle }) => {
    return trackPlayerCommands.enqueue(async () => {
      const shuffled = shuffle !== undefined ? shuffle : !!get().shuffleOrder

      await TrackPlayer.setupPlayer(get().getPlayerOptions())
      await TrackPlayer.reset()

      if (queue.length === 0) {
        return
      }

      if (shuffled) {
        const { tracks, shuffleOrder } = shuffleTracks(queue, playTrack)
        set(state => {
          state.shuffleOrder = shuffleOrder
        })
        queue = tracks
        playTrack = 0
      } else {
        set(state => {
          state.shuffleOrder = undefined
        })
      }

      playTrack = playTrack || 0

      try {
        set(state => {
          state.queue = queue
          state.queueName = title
          state.queueContextType = type
          state.queueContextId = contextId
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
  setProgress: progress =>
    set(state => {
      state.progress = progress
    }),

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
    set(state => {
      state.netState = netState
    })
    get().rebuildQueue()
  },

  rebuildQueue: async forcePlay => {
    return trackPlayerCommands.enqueue(async () => {
      const queue = await getQueue()
      if (!queue.length) {
        return
      }

      const currentTrack = await getCurrentTrack()
      const playerState = await getPlayerState()
      const position = (await TrackPlayer.getPosition()) || 0
      const repeatMode = await getRepeatMode()

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

      set(state => {
        state.queue = queue
        state.queueName = queueName
        state.queueContextType = queueContextType
        state.queueContextId = queueContextId
      })
      get().setCurrentTrackIdx(currentTrack)

      await TrackPlayer.add(queue)

      if (currentTrack) {
        await TrackPlayer.skip(currentTrack)
      }

      await TrackPlayer.setRepeatMode(repeatMode)
      await TrackPlayer.seekTo(position)

      if (playerState === State.Playing || forcePlay) {
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
      estimateContentLength: true,
      maxBitRate: get().netState === 'mobile' ? get().settings.maxBitrateMobile : get().settings.maxBitrateWifi,
    })
  },

  resetTrackPlayerState: () => {
    set(state => {
      state.queueName = undefined
      state.queueContextType = undefined
      state.queueContextId = undefined
      state.shuffleOrder = undefined
      state.repeatMode = RepeatMode.Off
      state.playerState = State.None
      state.currentTrack = undefined
      state.currentTrackIdx = undefined
      state.queue = []
      state.progress = { position: 0, duration: 0, buffered: 0 }
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
