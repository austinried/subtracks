import { CacheFileType } from '@app/models/music'
import { AppSettings, Server } from '@app/models/settings'
import { Store } from '@app/state/store'
import { SubsonicApiClient } from '@app/subsonic/api'
import produce from 'immer'
import RNFS from 'react-native-fs'
import { GetState, SetState } from 'zustand'

async function mkdir(path: string): Promise<void> {
  const exists = await RNFS.exists(path)
  if (exists) {
    const isDir = (await RNFS.stat(path)).isDirectory()
    if (!isDir) {
      throw new Error(`path exists and is not a directory: ${path}`)
    } else {
      return
    }
  }

  return await RNFS.mkdir(path)
}

export type SettingsSlice = {
  settings: AppSettings
  client?: SubsonicApiClient
  setActiveServer: (id: string | undefined, force?: boolean) => Promise<void>
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
  setActiveServer: async (id, force) => {
    const servers = get().settings.servers
    const currentActiveServerId = get().settings.activeServer
    const newActiveServer = servers.find(s => s.id === id)

    if (!newActiveServer) {
      set({
        client: undefined,
      })
      return
    }
    if (currentActiveServerId === id && !force) {
      return
    }

    for (const type in CacheFileType) {
      await mkdir(`${RNFS.DocumentDirectoryPath}/servers/${id}/${type}`)
    }

    set(
      produce<Store>(state => {
        state.settings.activeServer = newActiveServer.id
        state.client = new SubsonicApiClient(newActiveServer)

        if (!state.cacheDirs[newActiveServer.id]) {
          state.cacheDirs[newActiveServer.id] = {
            song: `${RNFS.DocumentDirectoryPath}/servers/${id}/song`,
            coverArt: `${RNFS.DocumentDirectoryPath}/servers/${id}/coverArt`,
            artistArt: `${RNFS.DocumentDirectoryPath}/servers/${id}/artistArt`,
          }
        }
        if (!state.cacheFiles[newActiveServer.id]) {
          state.cacheFiles[newActiveServer.id] = {
            song: {},
            coverArt: {},
            artistArt: {},
          }
        }
        if (!state.cacheRequests[newActiveServer.id]) {
          state.cacheRequests[newActiveServer.id] = {
            song: {},
            coverArt: {},
            artistArt: {},
          }
        }
      }),
    )
  },
  getActiveServer: () => get().settings.servers.find(s => s.id === get().settings.activeServer),
  setServers: servers => {
    set(
      produce<SettingsSlice>(state => {
        state.settings.servers = servers
      }),
    )
    const activeServer = servers.find(s => s.id === get().settings.activeServer)
    get().setActiveServer(activeServer?.id)
  },
})

export const selectSettings = {
  client: (state: SettingsSlice) => state.client,
  activeServer: (state: SettingsSlice) => state.settings.servers.find(s => s.id === state.settings.activeServer),
  setActiveServer: (state: SettingsSlice) => state.setActiveServer,
  servers: (state: SettingsSlice) => state.settings.servers,
  setServers: (state: SettingsSlice) => state.setServers,
  homeLists: (state: SettingsSlice) => state.settings.home.lists,
}
