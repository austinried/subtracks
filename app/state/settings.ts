import { AlbumFilterSettings, ArtistFilterSettings, Server } from '@app/models/settings'
import { ById } from '@app/models/state'
import { GetStore, SetStore } from '@app/state/store'
import { SubsonicApiClient } from '@app/subsonic/api'
import uuid from 'react-native-uuid'

export type SettingsSlice = {
  settings: {
    servers: ById<Server>
    activeServerId?: string
    screens: {
      home: {
        listTypes: string[]
      }
      library: {
        albumsFilter: AlbumFilterSettings
        artistsFilter: ArtistFilterSettings
      }
    }
    scrobble: boolean
    maxBitrateWifi: number
    maxBitrateMobile: number
    minBuffer: number
    maxBuffer: number
    cacheBuster: string
  }

  client?: SubsonicApiClient
  resetServer: boolean

  changeCacheBuster: () => void

  setActiveServer: (id: string | undefined, force?: boolean) => Promise<void>
  addServer: (server: Server) => void
  removeServer: (id: string) => void
  updateServer: (server: Server) => void

  setScrobble: (scrobble: boolean) => void
  setMaxBitrateWifi: (maxBitrateWifi: number) => void
  setMaxBitrateMobile: (maxBitrateMobile: number) => void
  setMinBuffer: (minBuffer: number) => void
  setMaxBuffer: (maxBuffer: number) => void

  pingServer: (server?: Server) => Promise<boolean>

  setLibraryAlbumFilter: (filter: AlbumFilterSettings) => void
  setLibraryArtistFiler: (filter: ArtistFilterSettings) => void
}

export function newCacheBuster(): string {
  return (uuid.v4() as string).split('-')[0]
}

export const createSettingsSlice = (set: SetStore, get: GetStore): SettingsSlice => ({
  settings: {
    servers: {},
    screens: {
      home: {
        listTypes: ['frequent', 'recent', 'starred', 'random'],
      },
      library: {
        albumsFilter: {
          type: 'alphabeticalByArtist',
          fromYear: 1,
          toYear: 9999,
          genre: '',
        },
        artistsFilter: {
          type: 'alphabeticalByName',
        },
      },
    },
    scrobble: false,
    maxBitrateWifi: 0,
    maxBitrateMobile: 192,
    minBuffer: 6,
    maxBuffer: 60,
    cacheBuster: newCacheBuster(),
  },

  resetServer: false,

  changeCacheBuster: () => {
    set(store => {
      store.settings.cacheBuster = newCacheBuster()
    })
  },

  setActiveServer: async (id, force) => {
    const servers = get().settings.servers
    const currentActiveServerId = get().settings.activeServerId
    const newActiveServer = id ? servers[id] : undefined

    if (!newActiveServer) {
      set(state => {
        state.client = undefined
      })
      return
    }

    if (currentActiveServerId === id && !force) {
      return
    }

    set(state => {
      state.resetServer = true
    })

    set(state => {
      state.settings.activeServerId = newActiveServer.id
      state.client = new SubsonicApiClient(newActiveServer)
    })

    set(state => {
      state.resetServer = false
    })
  },

  addServer: server => {
    const serverIds = Object.keys(get().settings.servers)
    server.id =
      serverIds.length === 0 ? '0' : (serverIds.map(i => parseInt(i, 10)).sort((a, b) => b - a)[0] + 1).toString()

    set(state => {
      state.settings.servers[server.id] = server
    })

    if (Object.keys(get().settings.servers).length === 1) {
      get().setActiveServer(server.id)
    }
  },

  removeServer: id => {
    set(state => {
      delete state.settings.servers[id]
    })
  },

  updateServer: server => {
    set(state => {
      state.settings.servers[server.id] = server
    })

    if (get().settings.activeServerId === server.id) {
      get().setActiveServer(server.id, true)
    }
  },

  setScrobble: scrobble => {
    set(state => {
      state.settings.scrobble = scrobble
    })
  },

  setMaxBitrateWifi: maxBitrateWifi => {
    set(state => {
      state.settings.maxBitrateWifi = maxBitrateWifi
    })

    if (get().netState === 'wifi') {
      get().rebuildQueue()
    }
  },

  setMaxBitrateMobile: maxBitrateMobile => {
    set(state => {
      state.settings.maxBitrateMobile = maxBitrateMobile
    })

    if (get().netState === 'mobile') {
      get().rebuildQueue()
    }
  },

  setMinBuffer: minBuffer => {
    if (minBuffer === get().settings.minBuffer) {
      return
    }

    set(state => {
      state.settings.minBuffer = Math.max(1, Math.min(minBuffer, state.settings.maxBuffer / 2))
    })

    get().rebuildQueue()
  },

  setMaxBuffer: maxBuffer => {
    if (maxBuffer === get().settings.maxBuffer) {
      return
    }

    set(state => {
      state.settings.maxBuffer = Math.min(5 * 60, Math.max(maxBuffer, state.settings.minBuffer * 2))
    })

    get().rebuildQueue()
  },

  pingServer: async server => {
    let client: SubsonicApiClient
    if (server) {
      client = new SubsonicApiClient(server)
    } else {
      const currentClient = get().client
      if (!currentClient) {
        return false
      }
      client = currentClient
    }

    try {
      await client.ping()
      return true
    } catch {
      return false
    }
  },

  setLibraryAlbumFilter: filter => {
    set(state => {
      state.settings.screens.library.albumsFilter = filter
    })
  },

  setLibraryArtistFiler: filter => {
    set(state => {
      state.settings.screens.library.artistsFilter = filter
    })
  },
})
