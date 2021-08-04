import { AppSettings, Server } from '@app/models/settings'
import { Store } from '@app/state/store'
import { SubsonicApiClient } from '@app/subsonic/api'
import produce from 'immer'
import { GetState, SetState } from 'zustand'

export type SettingsSlice = {
  settings: AppSettings
  client?: SubsonicApiClient
  createClient: (id?: string) => void
  setActiveServer: (id?: string) => void
  getActiveServer: () => Server | undefined
  setServers: (servers: Server[]) => void
}

export const createSettingsSlice = (set: SetState<Store>, get: GetState<Store>): SettingsSlice => ({
  settings: {
    servers: [],
    home: {
      lists: ['recent', 'random', 'frequent', 'starred'],
    },
  },
  createClient: (id?: string) => {
    if (!id) {
      set({ client: undefined })
      return
    }

    const server = get().getActiveServer()
    if (!server) {
      set({ client: undefined })
      return
    }

    set({ client: new SubsonicApiClient(server) })
  },
  setActiveServer: id => {
    const servers = get().settings.servers
    const currentActiveServerId = get().settings.activeServer
    const newActiveServer = servers.find(s => s.id === id)

    if (!newActiveServer) {
      return
    }
    if (currentActiveServerId === id) {
      return
    }

    set(
      produce<SettingsSlice>(state => {
        state.settings.activeServer = id
        state.client = new SubsonicApiClient(newActiveServer)
      }),
    )
  },
  getActiveServer: () => get().settings.servers.find(s => s.id === get().settings.activeServer),
  setServers: servers =>
    set(
      produce<SettingsSlice>(state => {
        state.settings.servers = servers
        const activeServer = servers.find(s => s.id === state.settings.activeServer)
        if (activeServer) {
          state.client = new SubsonicApiClient(activeServer)
        }
      }),
    ),
})

export const selectSettings = {
  activeServer: (state: SettingsSlice) => state.settings.servers.find(s => s.id === state.settings.activeServer),
  setActiveServer: (state: SettingsSlice) => state.setActiveServer,
  servers: (state: SettingsSlice) => state.settings.servers,
  setServers: (state: SettingsSlice) => state.setServers,
  homeLists: (state: SettingsSlice) => state.settings.home.lists,
}
