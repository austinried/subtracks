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

    return state
  },
]

export default migrations
