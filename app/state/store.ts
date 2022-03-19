import { createMusicSlice, MusicSlice } from '@app/state/music'
import { createSettingsSlice, SettingsSlice } from '@app/state/settings'
import AsyncStorage from '@react-native-async-storage/async-storage'
import equal from 'fast-deep-equal/es6/react'
import create, { GetState, Mutate, SetState, StateSelector, StoreApi } from 'zustand'
import { persist, subscribeWithSelector } from 'zustand/middleware'
import { CacheSlice, createCacheSlice } from './cache'
import { createLibrarySlice, LibrarySlice } from './library'
import migrations from './migrations'
import { createMusicMapSlice, MusicMapSlice } from './musicmap'
import { createTrackPlayerSlice, TrackPlayerSlice } from './trackplayer'
import { createTrackPlayerMapSlice, TrackPlayerMapSlice } from './trackplayermap'

const DB_VERSION = migrations.length

export type Store = SettingsSlice &
  MusicSlice &
  LibrarySlice &
  MusicMapSlice &
  TrackPlayerSlice &
  TrackPlayerMapSlice &
  CacheSlice & {
    hydrated: boolean
    setHydrated: (hydrated: boolean) => void
  }

export const useStore = create<
  Store,
  SetState<Store>,
  GetState<Store>,
  Mutate<StoreApi<Store>, [['zustand/subscribeWithSelector', never], ['zustand/persist', Partial<Store>]]>
>(
  subscribeWithSelector(
    persist(
      (set, get) => ({
        ...createSettingsSlice(set, get),
        ...createMusicSlice(set, get),
        ...createLibrarySlice(set, get),
        ...createMusicMapSlice(set, get),
        ...createTrackPlayerSlice(set, get),
        ...createTrackPlayerMapSlice(set, get),
        ...createCacheSlice(set, get),

        hydrated: false,
        setHydrated: hydrated => set({ hydrated }),
      }),
      {
        name: '@appStore',
        version: DB_VERSION,
        getStorage: () => AsyncStorage,
        // whitelist: ['settings', 'cacheFiles'],
        partialize: state => ({ settings: state.settings, cacheFiles: state.cacheFiles }),
        onRehydrateStorage: _preState => {
          return async (postState, _error) => {
            await postState?.setActiveServer(postState.settings.activeServer, true)
            postState?.setHydrated(true)
          }
        },
        migrate: (persistedState, version) => {
          if (version > DB_VERSION) {
            throw new Error('cannot migrate db on a downgrade, delete all data first')
          }

          for (let i = version; i < DB_VERSION; i++) {
            persistedState = migrations[i](persistedState)
          }

          return persistedState
        },
      },
    ),
  ),
)

export const useStoreDeep = <U>(stateSelector: StateSelector<Store, U>) => useStore(stateSelector, equal)
