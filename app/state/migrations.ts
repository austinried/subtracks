import { Server } from '@app/models/settings'
import { ById } from '@app/models/state'
import { newCacheBuster } from './settings'
import RNFS from 'react-native-fs'

const migrations: Array<(state: any) => Promise<any>> = [
  // 1
  async state => {
    for (let server of state.settings.servers) {
      server.usePlainPassword = false
    }

    return state
  },

  // 2
  async state => {
    state.settings.servers = state.settings.servers.reduce((acc: ById<Server>, server: Server) => {
      acc[server.id] = server
      return acc
    }, {} as ById<Server>)

    state.settings.activeServerId = state.settings.activeServer
    delete state.settings.activeServer

    state.settings.screens.home.listTypes = [...state.settings.screens.home.lists]
    delete state.settings.screens.home.lists

    state.settings.screens.library.albumsFilter = { ...state.settings.screens.library.albums }
    delete state.settings.screens.library.albums

    state.settings.screens.library.artistsFilter = { ...state.settings.screens.library.artists }
    delete state.settings.screens.library.artists

    delete state.settings.estimateContentLength

    return state
  },

  // 3
  async state => {
    state.settings.cacheBuster = newCacheBuster()

    state.settings.servers = Object.values(state.settings.servers as Record<string, Server>).reduce(
      (acc, server, i) => {
        const newId = i.toString()

        if (server.id === state.settings.activeServerId) {
          state.settings.activeServerId = newId
        }

        server.id = newId
        acc[newId] = server
        return acc
      },
      {} as Record<string, Server>,
    )

    try {
      await RNFS.unlink(`${RNFS.DocumentDirectoryPath}/servers`)
    } catch (err) {
      console.error(err)
    }

    return state
  },
]

export default migrations
