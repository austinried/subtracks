import { useAppState } from '@react-native-community/hooks'
import { useAtomValue, useUpdateAtom } from 'jotai/utils'
import React, { useEffect } from 'react'
import { View } from 'react-native'
import { Event, State, useProgress, useTrackPlayerEvents } from 'react-native-track-player'
import {
  currentTrackAtom,
  playerStateAtom,
  progressAtom,
  progressSubsAtom,
  queueAtom,
  useRefreshCurrentTrack,
  useRefreshPlayerState,
  useRefreshProgress,
  useRefreshQueue,
} from '@app/state/trackplayer'

const AppActiveResponder: React.FC<{
  update: () => void
}> = ({ update }) => {
  const appState = useAppState()

  useEffect(() => {
    if (appState === 'active') {
      update()
    }
  }, [appState, update])

  return <></>
}

type Payload = { type: Event; [key: string]: any }

const TrackPlayerEventResponder: React.FC<{
  update: (payload?: Payload) => void
  events: Event[]
}> = ({ update, events }) => {
  useTrackPlayerEvents(events, update)

  return <AppActiveResponder update={update} />
}

const CurrentTrackState = () => {
  const setCurrentTrack = useUpdateAtom(currentTrackAtom)
  const refreshCurrentTrack = useRefreshCurrentTrack()

  const update = async (payload?: Payload) => {
    const queueEnded = payload?.type === Event.PlaybackQueueEnded && 'track' in payload
    const remoteStop = payload?.type === Event.RemoteStop
    if (queueEnded || remoteStop) {
      setCurrentTrack(undefined)
      return
    }
    await refreshCurrentTrack()
  }

  return (
    <TrackPlayerEventResponder
      events={[
        Event.PlaybackQueueEnded,
        Event.PlaybackMetadataReceived,
        Event.RemoteDuck,
        Event.RemoteNext,
        Event.RemotePrevious,
        Event.RemoteStop,
      ]}
      update={update}
    />
  )
}

const PlayerState = () => {
  const setPlayerState = useUpdateAtom(playerStateAtom)
  const refreshPlayerState = useRefreshPlayerState()

  const update = async (payload?: Payload) => {
    if (payload?.type === Event.RemoteStop) {
      setPlayerState(State.None)
      return
    }
    await refreshPlayerState()
  }

  return <TrackPlayerEventResponder events={[Event.PlaybackState, Event.RemoteStop]} update={update} />
}

const QueueState = () => {
  const setQueue = useUpdateAtom(queueAtom)
  const refreshQueue = useRefreshQueue()

  const update = async (payload?: Payload) => {
    if (payload) {
      setQueue([])
      return
    }
    await refreshQueue()
  }

  return <TrackPlayerEventResponder events={[Event.RemoteStop]} update={update} />
}

const ProgressHook = () => {
  const setProgress = useUpdateAtom(progressAtom)
  const progress = useProgress(250)

  useEffect(() => {
    setProgress(progress)
  }, [setProgress, progress])

  return <></>
}

const ProgressState = () => {
  const setProgress = useUpdateAtom(progressAtom)
  const refreshProgress = useRefreshProgress()
  const progressSubs = useAtomValue(progressSubsAtom)

  const update = async (payload?: Payload) => {
    if (payload) {
      setProgress({ position: 0, duration: 0, buffered: 0 })
      return
    }
    await refreshProgress()
  }

  if (progressSubs > 0) {
    return (
      <>
        <ProgressHook />
        <TrackPlayerEventResponder events={[Event.RemoteStop, Event.PlaybackTrackChanged]} update={update} />
      </>
    )
  }
  return <TrackPlayerEventResponder events={[Event.RemoteStop]} update={update} />
}

const Debug = () => {
  const value = useAtomValue(currentTrackAtom)

  useEffect(() => {
    // ToastAndroid.show(value?.title || 'undefined', 1)
  }, [value])

  return <></>
}

const TrackPlayerState = () => (
  <View>
    <CurrentTrackState />
    <PlayerState />
    <QueueState />
    <ProgressState />
    <Debug />
  </View>
)

export default TrackPlayerState
