import { AppSettings, ArtistFilterSettings, AlbumFilterSettings, Server } from '@app/models/settings'
import { Store } from '@app/state/store'
import { SubsonicApiClient } from '@app/subsonic/api'
import produce from 'immer'
import { GetState, SetState } from 'zustand'

export type SettingsSlice = {
  settings: AppSettings
  client?: SubsonicApiClient

  setActiveServer: (id: string | undefined, force?: boolean) => Promise<void>
  getActiveServer: () => Server | undefined
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

  firstRun: (state: SettingsSlice) => state.settings.servers.length === 0,

  activeServer: (state: SettingsSlice) => state.settings.servers.find(s => s.id === state.settings.activeServer),
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

export const createSettingsSlice = (set: SetState<Store>, get: GetState<Store>): SettingsSlice => ({
  settings: {
    servers: [],
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

    get().prepareCache(newActiveServer.id)

    set(
      produce<Store>(state => {
        state.settings.activeServer = newActiveServer.id
        state.client = new SubsonicApiClient(newActiveServer)
      }),
    )
  },

  getActiveServer: () => get().settings.servers.find(s => s.id === get().settings.activeServer),

  addServer: async server => {
    await get().createCache(server.id)

    set(
      produce<SettingsSlice>(state => {
        state.settings.servers.push(server)
      }),
    )

    if (get().settings.servers.length === 1) {
      get().setActiveServer(server.id)
    }
  },

  removeServer: async id => {
    await get().removeCache(id)

    set(
      produce<SettingsSlice>(state => {
        state.settings.servers = state.settings.servers.filter(s => s.id !== id)
      }),
    )
  },

  updateServer: server => {
    set(
      produce<SettingsSlice>(state => {
        state.settings.servers = replaceIndex(
          state.settings.servers,
          state.settings.servers.findIndex(s => s.id === server.id),
          server,
        )
      }),
    )

    if (get().settings.activeServer === server.id) {
      get().setActiveServer(server.id)
    }
  },

  setScrobble: scrobble => {
    set(
      produce<SettingsSlice>(state => {
        state.settings.scrobble = scrobble
      }),
    )
  },

  setEstimateContentLength: estimateContentLength => {
    set(
      produce<SettingsSlice>(state => {
        state.settings.estimateContentLength = estimateContentLength
      }),
    )

    get().rebuildQueue()
  },

  setMaxBitrateWifi: maxBitrateWifi => {
    set(
      produce<SettingsSlice>(state => {
        state.settings.maxBitrateWifi = maxBitrateWifi
      }),
    )

    if (get().netState === 'wifi') {
      get().rebuildQueue()
    }
  },

  setMaxBitrateMobile: maxBitrateMobile => {
    set(
      produce<SettingsSlice>(state => {
        state.settings.maxBitrateMobile = maxBitrateMobile
      }),
    )

    if (get().netState === 'mobile') {
      get().rebuildQueue()
    }
  },

  setMinBuffer: minBuffer => {
    if (minBuffer === get().settings.minBuffer) {
      return
    }

    set(
      produce<SettingsSlice>(state => {
        state.settings.minBuffer = Math.max(1, Math.min(minBuffer, state.settings.maxBuffer / 2))
      }),
    )

    get().rebuildQueue()
  },

  setMaxBuffer: maxBuffer => {
    if (maxBuffer === get().settings.maxBuffer) {
      return
    }

    set(
      produce<SettingsSlice>(state => {
        state.settings.maxBuffer = Math.min(5 * 60, Math.max(maxBuffer, state.settings.minBuffer * 2))
      }),
    )

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
    set(
      produce<SettingsSlice>(state => {
        state.settings.screens.library.albums = filter
      }),
    )
  },

  setLibraryArtistFiler: filter => {
    set(
      produce<SettingsSlice>(state => {
        state.settings.screens.library.artists = filter
      }),
    )
  },
})

function replaceIndex<T>(array: T[], index: number, replacement: T): T[] {
  const start = array.slice(0, index)
  const end = array.slice(index + 1)
  return [...start, replacement, ...end]
}
