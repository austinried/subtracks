export interface Server {
  id: string
  address: string
  username: string
  token: string
  salt: string
  scrobble: boolean
}

export interface AppSettings {
  servers: Server[]
  home: {
    lists: string[]
  }
  activeServer?: string
}
