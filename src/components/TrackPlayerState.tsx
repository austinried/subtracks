import React, { useCallback, useEffect } from 'react';
import TrackPlayer, { Event, useTrackPlayerEvents } from 'react-native-track-player';
import { useAppState } from '@react-native-community/hooks';
import { useUpdateAtom, useAtomValue } from 'jotai/utils';
import { currentQueueNameAtom, currentTrackAtom } from '../state/trackplayer';
import { View } from 'react-native';

const CurrentTrackState = () => {
  const setCurrentTrack = useUpdateAtom(currentTrackAtom);
  const appState = useAppState();

  const update = useCallback(async () => {
    const index = await TrackPlayer.getCurrentTrack();

    if (index !== null && index >= 0) {
      const track = await TrackPlayer.getTrack(index);
      if (track !== null) {
        setCurrentTrack(track);
        return;
      }
    }

    setCurrentTrack(undefined);
  }, [setCurrentTrack]);

  useTrackPlayerEvents(
    [
      // Event.PlaybackState,
      // Event.PlaybackTrackChanged,
      Event.PlaybackQueueEnded,
      Event.PlaybackMetadataReceived,
      Event.RemoteDuck,
      Event.RemoteNext,
      Event.RemotePrevious,
      Event.RemoteStop,
    ],
    event => {
      if (event.type === Event.PlaybackQueueEnded && 'track' in event) {
        setCurrentTrack(undefined);
        return;
      }
      update();
    },
  );

  useEffect(() => {
    if (appState === 'active') {
      update();
    }
  }, [appState, update]);

  return <></>;
};

const CurrentQueueName = () => {
  const setCurrentQueueName = useUpdateAtom(currentQueueNameAtom);
  const appState = useAppState();

  const update = useCallback(async () => {
    const queue = await TrackPlayer.getQueue();

    if (queue !== null && queue.length > 0) {
      setCurrentQueueName(queue[0].queueName);
      return;
    }

    setCurrentQueueName(undefined);
  }, [setCurrentQueueName]);

  useEffect(() => {
    if (appState === 'active') {
      update();
    }
  }, [appState, update]);

  return <></>;
};

const Debug = () => {
  const value = useAtomValue(currentQueueNameAtom);

  useEffect(() => {
    console.log(value);
  }, [value]);

  return <></>;
};

const TrackPlayerState = () => (
  <View>
    <CurrentTrackState />
    <CurrentQueueName />
    <Debug />
  </View>
);

export default TrackPlayerState;
