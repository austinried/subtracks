import { getCurrentTrack, getPlayerState, TrackExt, trackPlayerCommands } from '@app/state/trackplayer'
import TrackPlayer, { Event, State } from 'react-native-track-player'
import { useStore } from './state/store'
import { unstable_batchedUpdates } from 'react-native'
import NetInfo, { NetInfoStateType } from '@react-native-community/netinfo'

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

const rebuildQueue = () => {
  unstable_batchedUpdates(() => {
    useStore.getState().rebuildQueue(useStore.getState().playerState === State.Playing)
  })
}

let serviceCreated = false

const createService = async () => {
  useStore.subscribe(
    (currentTrack?: TrackExt) => {
      if (currentTrack) {
        useStore.getState().scrobbleTrack(currentTrack.id)
      }
    },
    state => state.currentTrack,
    (prev, next) => prev?.id === next?.id,
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
      trackPlayerCommands.enqueue(TrackPlayer.pause)
    } else {
      trackPlayerCommands.enqueue(TrackPlayer.play)
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
      rebuildQueue()
    }
  })
}

module.exports = async function () {
  if (!serviceCreated) {
    createService()
    serviceCreated = true
  }
}
