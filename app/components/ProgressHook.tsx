import { useStore } from '@app/state/store'
import { selectTrackPlayer } from '@app/state/trackplayer'
import React, { useEffect } from 'react'
import { useProgress } from 'react-native-track-player'

const ProgressHook = () => {
  const setProgress = useStore(selectTrackPlayer.setProgress)
  const progress = useProgress(250)

  useEffect(() => {
    setProgress(progress)
  }, [setProgress, progress])

  return <></>
}

export default ProgressHook
