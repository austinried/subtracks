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
        coverArtDir: undefined,
        artistArtDir: undefined,
        songsDir: undefined,
      })
      return
    }
    if (currentActiveServerId === id && !force) {
      return
    }

    const coverArtDir = `${RNFS.DocumentDirectoryPath}/cover-art/${id}`
    const artistArtDir = `${RNFS.DocumentDirectoryPath}/artist-art/${id}`
    const songsDir = `${RNFS.DocumentDirectoryPath}/songs/${id}`
    await mkdir(coverArtDir)
    await mkdir(artistArtDir)
    await mkdir(songsDir)

    set(
      produce<Store>(state => {
        state.settings.activeServer = id
        state.client = new SubsonicApiClient(newActiveServer)
        state.coverArtDir = coverArtDir
        state.artistArtDir = artistArtDir
        state.songsDir = songsDir
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
  activeServer: (state: SettingsSlice) => state.settings.servers.find(s => s.id === state.settings.activeServer),
  setActiveServer: (state: SettingsSlice) => state.setActiveServer,
  servers: (state: SettingsSlice) => state.settings.servers,
  setServers: (state: SettingsSlice) => state.setServers,
  homeLists: (state: SettingsSlice) => state.settings.home.lists,
}
