import { useAppState } from '@react-native-community/hooks'
import { useAtomValue, useUpdateAtom } from 'jotai/utils'
import React, { useEffect } from 'react'
import { View } from 'react-native'
import TrackPlayer, { Event, State, useTrackPlayerEvents } from 'react-native-track-player'
import {
  currentTrackAtom,
  playerStateAtom,
  queueWriteAtom,
  useRefreshCurrentTrack,
  useRefreshQueue,
} from '../state/trackplayer'

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

  const update = async (payload?: Payload) => {
    if (payload?.type === Event.RemoteStop) {
      setPlayerState(State.None)
      return
    }
    setPlayerState(payload?.state || (await TrackPlayer.getState()))
  }

  return <TrackPlayerEventResponder events={[Event.PlaybackState, Event.RemoteStop]} update={update} />
}

const QueueState = () => {
  const setQueue = useUpdateAtom(queueWriteAtom)
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
    <Debug />
  </View>
)

export default TrackPlayerState
