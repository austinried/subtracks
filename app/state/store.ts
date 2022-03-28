import { createSettingsSlice, SettingsSlice } from '@app/state/settings'
import AsyncStorage from '@react-native-async-storage/async-storage'
import equal from 'fast-deep-equal'
import create, { GetState, Mutate, SetState, State, StateCreator, StateSelector, StoreApi } from 'zustand'
import { persist, subscribeWithSelector } from 'zustand/middleware'
import { CacheSlice, createCacheSlice } from './cache'
import { createLibrarySlice, LibrarySlice } from './library'
import migrations from './migrations'
import { createTrackPlayerSlice, TrackPlayerSlice } from './trackplayer'
import { createTrackPlayerMapSlice, TrackPlayerMapSlice } from './trackplayermap'
import produce, { Draft } from 'immer'
import { WritableDraft } from 'immer/dist/internal'

const DB_VERSION = migrations.length

export type Store = SettingsSlice &
  LibrarySlice &
  TrackPlayerSlice &
  TrackPlayerMapSlice &
  CacheSlice & {
    hydrated: boolean
    setHydrated: (hydrated: boolean) => void
  }

// taken from zustand test examples:
// https://github.com/pmndrs/zustand/blob/v3.7.1/tests/middlewareTypes.test.tsx#L20
const immer =
  <
    T extends State,
    CustomSetState extends SetState<T>,
    CustomGetState extends GetState<T>,
    CustomStoreApi extends StoreApi<T>,
  >(
    config: StateCreator<
      T,
      (partial: ((draft: Draft<T>) => void) | T, replace?: boolean) => void,
      CustomGetState,
      CustomStoreApi
    >,
  ): StateCreator<T, CustomSetState, CustomGetState, CustomStoreApi> =>
  (set, get, api) =>
    config(
      (partial, replace) => {
        const nextState = typeof partial === 'function' ? produce(partial as (state: Draft<T>) => T) : (partial as T)
        return set(nextState, replace)
      },
      get,
      api,
    )

export type SetStore = (partial: Store | ((draft: WritableDraft<Store>) => void), replace?: boolean | undefined) => void
export type GetStore = GetState<Store>

// types taken from zustand test examples:
// https://github.com/pmndrs/zustand/blob/v3.7.1/tests/middlewareTypes.test.tsx#L584
export const useStore = create<
  Store,
  SetState<Store>,
  GetState<Store>,
  Mutate<StoreApi<Store>, [['zustand/subscribeWithSelector', never], ['zustand/persist', Partial<Store>]]>
>(
  subscribeWithSelector(
    persist(
      immer((set, get) => ({
        ...createSettingsSlice(set, get),
        ...createLibrarySlice(set, get),
        ...createTrackPlayerSlice(set, get),
        ...createTrackPlayerMapSlice(set, get),
        ...createCacheSlice(set, get),

        hydrated: false,
        setHydrated: hydrated =>
          set(state => {
            state.hydrated = hydrated
          }),
      })),
      {
        name: '@appStore',
        version: DB_VERSION,
        getStorage: () => AsyncStorage,
        partialize: state => ({ settings: state.settings, cacheFiles: state.cacheFiles }),
        onRehydrateStorage: _preState => {
          return async (postState, _error) => {
            await postState?.setActiveServer(postState.settings.activeServerId, true)
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
