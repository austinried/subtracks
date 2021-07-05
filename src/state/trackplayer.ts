import { atom } from 'jotai'
import { State, Track } from 'react-native-track-player'
import equal from 'fast-deep-equal'

type OptionalTrack = Track | undefined

const currentTrack = atom<OptionalTrack>(undefined)
export const currentTrackAtom = atom<OptionalTrack, OptionalTrack>(
  get => get(currentTrack),
  (get, set, value) => {
    if (!equal(get(currentTrack), value)) {
      set(currentTrack, value)
    }
  },
)

type OptionalString = string | undefined

const currentQueueName = atom<OptionalString>(undefined)
export const currentQueueNameAtom = atom<OptionalString, OptionalString>(
  get => get(currentQueueName),
  (get, set, value) => {
    if (get(currentQueueName) !== value) {
      set(currentQueueName, value)
    }
  },
)

const playerState = atom<State>(State.None)
export const playerStateAtom = atom<State, State>(
  get => get(playerState),
  (get, set, value) => {
    if (get(playerState) !== value) {
      set(playerState, value)
    }
  },
)
