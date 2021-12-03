const migrations: Array<(state: any) => any> = [
  state => {
    for (let server of state.settings.servers) {
      server.usePlainPassword = false
    }

    return state
  },
]

export default migrations
