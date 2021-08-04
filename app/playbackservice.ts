import { getCurrentTrack, getPlayerState, trackPlayerCommands } from '@app/state/trackplayer'
import TrackPlayer, { Event } from 'react-native-track-player'
import { useStore } from './state/store'

module.exports = async function () {
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

  TrackPlayer.addEventListener(Event.RemoteStop, () => {
    useStore.getState().reset()
    trackPlayerCommands.enqueue(TrackPlayer.destroy)
  })

  TrackPlayer.addEventListener(Event.PlaybackState, () => {
    trackPlayerCommands.enqueue(async () => {
      useStore.getState().setPlayerState(await getPlayerState())
    })
  })

  TrackPlayer.addEventListener(Event.PlaybackTrackChanged, () => {
    useStore.getState().setProgress({ position: 0, duration: 0, buffered: 0 })
    trackPlayerCommands.enqueue(async () => {
      useStore.getState().setCurrentTrackIdx(await getCurrentTrack())
    })
  })

  TrackPlayer.addEventListener(Event.PlaybackQueueEnded, () => {
    trackPlayerCommands.enqueue(async () => {
      useStore.getState().setCurrentTrackIdx(await getCurrentTrack())
    })
  })

  TrackPlayer.addEventListener(Event.PlaybackMetadataReceived, () => {
    useStore.getState().setCurrentTrackIdx(useStore.getState().currentTrackIdx)
  })
}
