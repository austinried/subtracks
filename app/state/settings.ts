import { AppSettings, Server } from '@app/models/settings'
import { Store } from '@app/state/store'
import { SubsonicApiClient } from '@app/subsonic/api'
import produce from 'immer'
import { GetState, SetState } from 'zustand'

export type SettingsSlice = {
  settings: AppSettings
  client?: SubsonicApiClient
  createClient: () => void
  setActiveServer: (id?: string) => void
  setServers: (servers: Server[]) => void
}

export const createSettingsSlice = (set: SetState<Store>, get: GetState<Store>): SettingsSlice => ({
  settings: {
    servers: [],
    home: {
      lists: ['recent', 'random', 'frequent', 'starred'],
    },
  },
  createClient: () => {
    const server = get().settings.servers.find(s => s.id === get().settings.activeServer)
    if (!server) {
      return
    }

    set(
      produce<Store>(state => {
        state.client = new SubsonicApiClient(server)
      }),
    )
  },
  setActiveServer: id => {
    const servers = get().settings.servers
    const currentActiveServerId = get().settings.activeServer

    if (!servers.find(s => s.id === id)) {
      return
    }
    if (currentActiveServerId === id) {
      return
    }

    set(
      produce<Store>(state => {
        state.settings.activeServer = id
      }),
    )

    get().createClient()
  },
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
