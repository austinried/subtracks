import TrackPlayer, { Event } from 'react-native-track-player'
import { trackPlayerCommands } from '@app/state/trackplayer'

module.exports = async function () {
  TrackPlayer.addEventListener(Event.RemotePlay, () => trackPlayerCommands.enqueue(TrackPlayer.play))
  TrackPlayer.addEventListener(Event.RemotePause, () => trackPlayerCommands.enqueue(TrackPlayer.pause))
  TrackPlayer.addEventListener(Event.RemoteStop, () => trackPlayerCommands.enqueue(TrackPlayer.destroy))

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

  TrackPlayer.addEventListener(Event.RemoteNext, () =>
    trackPlayerCommands.enqueue(() => TrackPlayer.skipToNext().catch(() => {})),
  )
  TrackPlayer.addEventListener(Event.RemotePrevious, () =>
    trackPlayerCommands.enqueue(() => TrackPlayer.skipToPrevious().catch(() => {})),
  )
}
