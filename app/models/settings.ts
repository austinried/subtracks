export interface Server {
  id: string
  address: string
  username: string
  token: string
  salt: string
}

export interface AppSettings {
  servers: Server[]
  home: {
    lists: string[]
  }
  activeServer?: string
  scrobble: boolean
  estimateContentLength: boolean
  maxBitrateWifi: number
  maxBitrateMobile: number
}
