import { AlbumFilterSettings, AppSettings, ArtistFilterSettings, Server } from '@app/models/settings'
import { GetStore, SetStore } from '@app/state/store'
import { SubsonicApiClient } from '@app/subsonic/api'

export type SettingsSlice = {
  settings: AppSettings
  client?: SubsonicApiClient

  setActiveServer: (id: string | undefined, force?: boolean) => Promise<void>
  addServer: (server: Server) => Promise<void>
  removeServer: (id: string) => Promise<void>
  updateServer: (server: Server) => void

  setScrobble: (scrobble: boolean) => void
  setEstimateContentLength: (estimateContentLength: boolean) => void
  setMaxBitrateWifi: (maxBitrateWifi: number) => void
  setMaxBitrateMobile: (maxBitrateMobile: number) => void
  setMinBuffer: (minBuffer: number) => void
  setMaxBuffer: (maxBuffer: number) => void

  pingServer: (server?: Server) => Promise<boolean>

  setLibraryAlbumFilter: (filter: AlbumFilterSettings) => void
  setLibraryArtistFiler: (filter: ArtistFilterSettings) => void
}

export const selectSettings = {
  client: (state: SettingsSlice) => state.client,

  firstRun: (state: SettingsSlice) => Object.keys(state.settings.servers).length === 0,

  activeServer: (state: SettingsSlice) =>
    state.settings.activeServer ? state.settings.servers[state.settings.activeServer] : undefined,
  setActiveServer: (state: SettingsSlice) => state.setActiveServer,

  servers: (state: SettingsSlice) => state.settings.servers,
  addServer: (state: SettingsSlice) => state.addServer,
  removeServer: (state: SettingsSlice) => state.removeServer,
  updateServer: (state: SettingsSlice) => state.updateServer,

  homeLists: (state: SettingsSlice) => state.settings.screens.home.lists,

  scrobble: (state: SettingsSlice) => state.settings.scrobble,
  setScrobble: (state: SettingsSlice) => state.setScrobble,

  estimateContentLength: (state: SettingsSlice) => state.settings.estimateContentLength,
  setEstimateContentLength: (state: SettingsSlice) => state.setEstimateContentLength,

  maxBitrateWifi: (state: SettingsSlice) => state.settings.maxBitrateWifi,
  setMaxBitrateWifi: (state: SettingsSlice) => state.setMaxBitrateWifi,
  maxBitrateMobile: (state: SettingsSlice) => state.settings.maxBitrateMobile,
  setMaxBitrateMobile: (state: SettingsSlice) => state.setMaxBitrateMobile,

  minBuffer: (state: SettingsSlice) => state.settings.minBuffer,
  setMinBuffer: (state: SettingsSlice) => state.setMinBuffer,
  maxBuffer: (state: SettingsSlice) => state.settings.maxBuffer,
  setMaxBuffer: (state: SettingsSlice) => state.setMaxBuffer,

  pingServer: (state: SettingsSlice) => state.pingServer,

  setLibraryAlbumFilter: (state: SettingsSlice) => state.setLibraryAlbumFilter,
  libraryAlbumFilter: (state: SettingsSlice) => state.settings.screens.library.albums,
  setLibraryArtistFiler: (state: SettingsSlice) => state.setLibraryArtistFiler,
  libraryArtistFilter: (state: SettingsSlice) => state.settings.screens.library.artists,
}

export const createSettingsSlice = (set: SetStore, get: GetStore): SettingsSlice => ({
  settings: {
    servers: {},
    screens: {
      home: {
        lists: ['frequent', 'recent', 'starred', 'random'],
      },
      library: {
        albums: {
          type: 'alphabeticalByArtist',
          fromYear: 1,
          toYear: 9999,
          genre: '',
        },
        artists: {
          type: 'alphabeticalByName',
        },
      },
    },
    scrobble: false,
    estimateContentLength: true,
    maxBitrateWifi: 0,
    maxBitrateMobile: 192,
    minBuffer: 6,
    maxBuffer: 60,
  },

  setActiveServer: async (id, force) => {
    const servers = get().settings.servers
    const currentActiveServerId = get().settings.activeServer
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

    get().prepareCache(newActiveServer.id)

    set(state => {
      state.settings.activeServer = newActiveServer.id
      state.client = new SubsonicApiClient(newActiveServer)
      get().resetLibrary(state)
    })
  },

  addServer: async server => {
    await get().createCache(server.id)

    set(state => {
      state.settings.servers[server.id] = server
    })

    if (Object.keys(get().settings.servers).length === 1) {
      get().setActiveServer(server.id)
    }
  },

  removeServer: async id => {
    await get().removeCache(id)

    set(state => {
      delete state.settings.servers[id]
    })
  },

  updateServer: server => {
    set(state => {
      state.settings.servers[server.id] = server
    })

    if (get().settings.activeServer === server.id) {
      get().setActiveServer(server.id, true)
    }
  },

  setScrobble: scrobble => {
    set(state => {
      state.settings.scrobble = scrobble
    })
  },

  setEstimateContentLength: estimateContentLength => {
    set(state => {
      state.settings.estimateContentLength = estimateContentLength
    })
    get().rebuildQueue()
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
      state.settings.screens.library.albums = filter
    })
  },

  setLibraryArtistFiler: filter => {
    set(state => {
      state.settings.screens.library.artists = filter
    })
  },
})
