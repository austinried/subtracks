import { atom } from 'jotai'
import { AppSettings } from '@app/models/settings'
import atomWithAsyncStorage from '@app/storage/atomWithAsyncStorage'

export const appSettingsAtom = atomWithAsyncStorage<AppSettings>('@appSettings', {
  servers: [],
})

export const activeServerAtom = atom(get => {
  const appSettings = get(appSettingsAtom)
  return appSettings.servers.find(x => x.id === appSettings.activeServer)
})
