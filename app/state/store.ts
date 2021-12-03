import { createMusicSlice, MusicSlice } from '@app/state/music'
import { createSettingsSlice, SettingsSlice } from '@app/state/settings'
import AsyncStorage from '@react-native-async-storage/async-storage'
import create from 'zustand'
import { persist, StateStorage } from 'zustand/middleware'
import { CacheSlice, createCacheSlice } from './cache'
import migrations from './migrations'
import { createMusicMapSlice, MusicMapSlice } from './musicmap'
import { createTrackPlayerSlice, TrackPlayerSlice } from './trackplayer'
import { createTrackPlayerMapSlice, TrackPlayerMapSlice } from './trackplayermap'

const DB_VERSION = migrations.length

export type Store = SettingsSlice &
  MusicSlice &
  MusicMapSlice &
  TrackPlayerSlice &
  TrackPlayerMapSlice &
  CacheSlice & {
    hydrated: boolean
    setHydrated: (hydrated: boolean) => void
  }

const storage: StateStorage = {
  getItem: async name => {
    try {
      return await AsyncStorage.getItem(name)
    } catch (err) {
      console.error(`getItem error (key: ${name})`, err)
      return null
    }
  },
  setItem: async (name, item) => {
    try {
      await AsyncStorage.setItem(name, item)
    } catch (err) {
      console.error(`setItem error (key: ${name})`, err)
    }
  },
}

export const useStore = create<Store>(
  persist(
    (set, get) => ({
      ...createSettingsSlice(set, get),
      ...createMusicSlice(set, get),
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
      getStorage: () => storage,
      whitelist: ['settings', 'cacheFiles'],
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
)
