import { useAppState } from '@react-native-community/hooks'
import { useAtomValue, useUpdateAtom } from 'jotai/utils'
import React, { useEffect } from 'react'
import { View } from 'react-native'
import TrackPlayer, { Event, useTrackPlayerEvents } from 'react-native-track-player'
import { currentTrackAtom, getQueue, getTrack, playerStateAtom, queueWriteAtom } from '../state/trackplayer'

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

  const update = async (payload?: Payload) => {
    if (payload?.type === Event.PlaybackQueueEnded && 'track' in payload) {
      setCurrentTrack(undefined)
      return
    }

    const index = await TrackPlayer.getCurrentTrack()
    if (index !== null && index >= 0) {
      const track = await getTrack(index)
      if (track !== null) {
        setCurrentTrack(track)
        return
      }
    }

    setCurrentTrack(undefined)
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
    setPlayerState(payload?.state || (await TrackPlayer.getState()))
  }

  return <TrackPlayerEventResponder events={[Event.PlaybackState]} update={update} />
}

const QueueState = () => {
  const setQueue = useUpdateAtom(queueWriteAtom)

  const update = async (payload?: Payload) => {
    if (payload) {
      setQueue([])
      return
    }
    setQueue(await getQueue())
  }

  return <TrackPlayerEventResponder events={[Event.RemoteStop]} update={update} />
}

const Debug = () => {
  const value = useAtomValue(queueWriteAtom)

  useEffect(() => {
    console.log(value.map(t => t.title))
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
