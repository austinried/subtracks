import { atom } from 'jotai';
import { Track } from 'react-native-track-player';
import equal from 'fast-deep-equal';

type OptionalTrack = Track | undefined;

const currentTrack = atom<OptionalTrack>(undefined);
export const currentTrackAtom = atom<OptionalTrack, OptionalTrack>(
  get => get(currentTrack),
  (get, set, value) => {
    if (equal(get(currentTrack), value)) {
      return;
    }
    set(currentTrack, value);
  },
);
