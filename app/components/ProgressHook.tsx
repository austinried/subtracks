import { useStore } from '@app/state/store'
import React, { useEffect } from 'react'
import { State, useProgress } from 'react-native-track-player'

const ProgressHook = () => {
  const playerState = useStore(store => store.playerState)
  const setProgress = useStore(store => store.setProgress)
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
