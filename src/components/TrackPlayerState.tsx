import React, { useCallback, useEffect } from 'react';
import TrackPlayer, { Event, State, Track, useTrackPlayerEvents } from 'react-native-track-player';
import { useAppState } from '@react-native-community/hooks';
import { useUpdateAtom, useAtomValue } from 'jotai/utils';
import { currentTrackAtom } from '../state/trackplayer';
import { View } from 'react-native';

const TrackPlayerState = () => {
  const setCurrentTrack = useUpdateAtom(currentTrackAtom);
  const appState = useAppState();

  const updateCurrentTrack = useCallback(async () => {
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
      updateCurrentTrack();
    },
  );

  useEffect(() => {
    if (appState === 'active') {
      updateCurrentTrack();
    }
  }, [appState, updateCurrentTrack]);

  return <></>;
};

const CurrentTrack = () => {
  const currentTrack = useAtomValue(currentTrackAtom);

  useEffect(() => {
    console.log(currentTrack?.title);
  }, [currentTrack]);

  return <></>;
};

const ASDFSADFSAF = () => (
  <View>
    <TrackPlayerState />
    <CurrentTrack />
  </View>
);

export default ASDFSADFSAF;
