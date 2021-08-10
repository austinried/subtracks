import { getCurrentTrack, getPlayerState, TrackExt, trackPlayerCommands } from '@app/state/trackplayer'
import TrackPlayer, { Event, State } from 'react-native-track-player'
import { useStore } from './state/store'
import { unstable_batchedUpdates } from 'react-native'

const reset = () => {
  unstable_batchedUpdates(() => {
    useStore.getState().reset()
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

let serviceCreated = false
const createService = async () => {
  useStore.subscribe(
    (currentTrack?: TrackExt) => {
      if (currentTrack) {
        useStore.getState().scrobbleTrack(currentTrack.id)
      }
    },
    state => state.currentTrack,
  )

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

  TrackPlayer.addEventListener(Event.PlaybackQueueEnded, () => {
    trackPlayerCommands.enqueue(async () => {
      setCurrentTrackIdx(await getCurrentTrack())
    })
  })

  TrackPlayer.addEventListener(Event.PlaybackMetadataReceived, () => {
    setCurrentTrackIdx(useStore.getState().currentTrackIdx)
  })
}

module.exports = async function () {
  if (!serviceCreated) {
    createService()
    serviceCreated = true
  }
}
