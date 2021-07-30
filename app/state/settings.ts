import { atom } from 'jotai'
import { AppSettings, Server } from '@app/models/settings'
import atomWithAsyncStorage from '@app/storage/atomWithAsyncStorage'
import { useEffect } from 'react'
import { useAtomValue } from 'jotai/utils'
import equal from 'fast-deep-equal'

export const appSettingsAtom = atomWithAsyncStorage<AppSettings>('@appSettings', {
  servers: [],
  home: {
    lists: ['recent', 'random', 'frequent', 'starred'],
  },
})

export const activeServerAtom = atom<Server | undefined, string>(
  get => {
    const appSettings = get(appSettingsAtom)
    return appSettings.servers.find(x => x.id === appSettings.activeServer)
  },
  (get, set, update) => {
    const appSettings = get(appSettingsAtom)
    if (!appSettings.servers.find(s => s.id === update)) {
      return
    }
    if (appSettings.activeServer === update) {
      return
    }
    set(appSettingsAtom, {
      ...appSettings,
      activeServer: update,
    })
  },
)

export const serversAtom = atom<Server[], Server[]>(
  get => get(appSettingsAtom).servers,
  (get, set, update) => {
    const settings = get(appSettingsAtom)
    if (!equal(settings.servers, update)) {
      set(appSettingsAtom, {
        ...settings,
        servers: update,
      })
    }
  },
)

export const useActiveServerRefresh = (update: () => any) => {
  const activeServer = useAtomValue(activeServerAtom)

  useEffect(() => {
    if (activeServer) {
      update()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeServer])
}

export const homeListTypesAtom = atom(get => get(appSettingsAtom).home.lists)
