import { AppSettings, Server } from '@app/models/settings'
import { Store } from '@app/state/store'
import produce from 'immer'
import { GetState, SetState } from 'zustand'

export type SettingsSlice = {
  settings: AppSettings
  setActiveServer: (id?: string) => void
  setServers: (servers: Server[]) => void
}

export const createSettingsSlice = (set: SetState<Store>, _get: GetState<Store>): SettingsSlice => ({
  settings: {
    servers: [],
    home: {
      lists: ['recent', 'random', 'frequent', 'starred'],
    },
  },
  setActiveServer: id =>
    set(
      produce<Store>(state => {
        if (!state.settings.servers.find(s => s.id === id)) {
          console.log('could not find')
          return
        }
        if (state.settings.activeServer === id) {
          console.log('already active')
          return
        }
        state.settings.activeServer = id
      }),
    ),
  setServers: servers =>
    set(
      produce<Store>(state => {
        state.settings.servers = servers
      }),
    ),
})

export const selectSettings = {
  activeServer: (state: Store) => state.settings.servers.find(s => s.id === state.settings.activeServer),
  setActiveServer: (state: Store) => state.setActiveServer,
  servers: (state: Store) => state.settings.servers,
  setServers: (state: Store) => state.setServers,
  homeLists: (state: Store) => state.settings.home.lists,
}
