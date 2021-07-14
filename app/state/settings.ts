import { atom } from 'jotai'
import { AppSettings } from '@app/models/settings'
import atomWithAsyncStorage from '@app/storage/atomWithAsyncStorage'
import { useEffect } from 'react'
import { useAtomValue } from 'jotai/utils'

export const appSettingsAtom = atomWithAsyncStorage<AppSettings>('@appSettings', {
  servers: [],
  home: {
    lists: ['recent', 'random', 'frequent', 'starred'],
  },
})

export const activeServerAtom = atom(get => {
  const appSettings = get(appSettingsAtom)
  return appSettings.servers.find(x => x.id === appSettings.activeServer)
})

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
