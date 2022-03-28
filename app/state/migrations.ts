import { Server } from '@app/models/settings'
import { ById } from '@app/models/state'

const migrations: Array<(state: any) => any> = [
  state => {
    for (let server of state.settings.servers) {
      server.usePlainPassword = false
    }

    return state
  },
  state => {
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
]

export default migrations
