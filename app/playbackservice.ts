import { getCurrentTrack, getPlayerState, trackPlayerCommands } from '@app/state/trackplayer'
import NetInfo, { NetInfoStateType } from '@react-native-community/netinfo'
import _ from 'lodash'
import { unstable_batchedUpdates } from 'react-native'
import TrackPlayer, { Event, State } from 'react-native-track-player'
import qk from './hooks/queryKeys'
import queryClient from './queryClient'
import queueService from './queueservice'
import { useStore } from './state/store'

const reset = () => {
  unstable_batchedUpdates(() => {
    useStore.getState().resetTrackPlayerState()
  })
}

const setPlayerState = (state: State) => {
  unstable_batchedUpdates(() => {
    useStore.getState().setPlayerState(state)
  })
}

const setCurrentTrackIdx = (idx?: number) => {
  unstable_batchedUpdates(() => {
    useStore.getState().setCurrentTrackIdx(idx)
  })
}

const setNetState = (netState: 'mobile' | 'wifi') => {
  unstable_batchedUpdates(() => {
    useStore.getState().setNetState(netState)
  })
}

const rebuildQueue = (forcePlay?: boolean) => {
  unstable_batchedUpdates(() => {
    useStore.getState().rebuildQueue(forcePlay)
  })
}

const updateQueue = () => {
  unstable_batchedUpdates(() => {
    useStore.getState().updateQueue()
  })
}

const setDuckPaused = (duckPaused: boolean) => {
  unstable_batchedUpdates(() => {
    useStore.getState().setDuckPaused(duckPaused)
  })
}

let serviceCreated = false

const createService = async () => {
  useStore.subscribe(
    state => state.currentTrack?.id,
    (currentTrackId?: string) => {
      if (currentTrackId) {
        useStore.getState().scrobbleTrack(currentTrackId)
      }
    },
  )

  NetInfo.fetch().then(state => {
    setNetState(state.type === NetInfoStateType.cellular ? 'mobile' : 'wifi')
  })

  NetInfo.addEventListener(state => {
    const currentType = useStore.getState().netState
    const newType = state.type === NetInfoStateType.cellular ? 'mobile' : 'wifi'
    if (currentType !== newType) {
      setNetState(newType)
    }
  })

  TrackPlayer.addEventListener(Event.RemoteStop, () => {
    reset()
    trackPlayerCommands.enqueue(TrackPlayer.destroy)
  })

  TrackPlayer.addEventListener(Event.RemotePlay, () => trackPlayerCommands.enqueue(TrackPlayer.play))
  TrackPlayer.addEventListener(Event.RemotePause, () => trackPlayerCommands.enqueue(TrackPlayer.pause))

  TrackPlayer.addEventListener(Event.RemoteNext, () =>
    trackPlayerCommands.enqueue(() => TrackPlayer.skipToNext().catch(() => {})),
  )
  TrackPlayer.addEventListener(Event.RemotePrevious, () =>
    trackPlayerCommands.enqueue(() => TrackPlayer.skipToPrevious().catch(() => {})),
  )

  TrackPlayer.addEventListener(Event.RemoteDuck, data => {
    if (data.permanent) {
      trackPlayerCommands.enqueue(TrackPlayer.stop)
      return
    }

    if (data.paused) {
      let state = useStore.getState().playerState
      if (state === State.Playing || state === State.Buffering || state === State.Connecting) {
        trackPlayerCommands.enqueue(TrackPlayer.pause)
        setDuckPaused(true)
      }
    } else if (useStore.getState().duckPaused) {
      trackPlayerCommands.enqueue(TrackPlayer.play)
      setDuckPaused(false)
    }
  })

  TrackPlayer.addEventListener(Event.PlaybackState, () => {
    trackPlayerCommands.enqueue(async () => {
      setPlayerState(await getPlayerState())
    })
  })

  TrackPlayer.addEventListener(Event.PlaybackTrackChanged, () => {
    useStore.getState().setProgress({ position: 0, duration: 0, buffered: 0 })
    trackPlayerCommands.enqueue(async () => {
      setCurrentTrackIdx(await getCurrentTrack())
    })
  })

  TrackPlayer.addEventListener(Event.PlaybackQueueEnded, event => {
    const { position, track } = event

    // bogus event that fires when queue is changed
    if (!track && position === 0) {
      return
    }

    trackPlayerCommands.enqueue(async () => {
      await TrackPlayer.stop()
      await TrackPlayer.skip(0)
    })
  })

  TrackPlayer.addEventListener(Event.PlaybackMetadataReceived, () => {
    setCurrentTrackIdx(useStore.getState().currentTrackIdx)
  })

  TrackPlayer.addEventListener(Event.RemoteSeek, data => {
    trackPlayerCommands.enqueue(async () => {
      await TrackPlayer.seekTo(data.position)
    })
  })

  TrackPlayer.addEventListener(Event.PlaybackError, data => {
    const { code, message } = data as Record<string, string>

    // fix for ExoPlayer aborting playback while esimating content length
    if (code === 'playback-source' && message.includes('416')) {
      rebuildQueue(true)
    }
  })

  queueService.addListener('set', async ({ queue }) => {
    const albumIds = _.uniq(queue.map(s => s.albumId)).filter((id): id is string => id !== undefined)

    const albumCoverArts = albumIds.reduce((acc, id) => {
      acc[id] = queryClient.getQueryData<string>(qk.albumCoverArt(id))
      return acc
    }, {} as { [albumId: string]: string | undefined })

    const filePaths = albumIds.reduce((acc, id) => {
      const coverArt = albumCoverArts[id]

      acc[id] = queryClient.getQueryData<string>(qk.existingFiles('coverArtThumb', coverArt))

      if (!acc[id]) {
        acc[id] = queryClient.getQueryData<string>(qk.coverArt(coverArt, 'thumbnail'))
      }

      return acc
    }, {} as { [albumId: string]: string | undefined })

    trackPlayerCommands.enqueue(async () => {
      for (let i = 0; i < queue.length; i++) {
        const track = queue[i]
        if (track.albumId && filePaths[track.albumId]) {
          await TrackPlayer.updateMetadataForTrack(i, { ...track, artwork: `file://${filePaths[track.albumId]}` })
        }
      }
      updateQueue()
    })
  })
}

module.exports = async function () {
  if (!serviceCreated) {
    createService()
    serviceCreated = true
  }
}
