import { useStore } from '@app/state/store'
import { selectTrackPlayer } from '@app/state/trackplayer'
import React, { useEffect } from 'react'
import { State, useProgress } from 'react-native-track-player'

const ProgressHook = () => {
  const playerState = useStore(selectTrackPlayer.playerState)
  const setProgress = useStore(selectTrackPlayer.setProgress)
  const progress = useProgress(250)

  useEffect(() => {
    if (playerState !== State.Playing) {
      return
    }
    if (progress.buffered === 0 && progress.duration === 0 && progress.position === 0) {
      return
    }

    setProgress(progress)
  }, [setProgress, progress, playerState])

  return <></>
}

export default ProgressHook
