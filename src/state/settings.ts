import { atom } from 'jotai'
import { AppSettings } from '../models/settings'
import atomWithAsyncStorage from '../storage/atomWithAsyncStorage'

export const appSettingsAtom = atomWithAsyncStorage<AppSettings>('@appSettings', {
  servers: [],
})

export const activeServerAtom = atom(get => {
  const appSettings = get(appSettingsAtom)
  return appSettings.servers.find(x => x.id === appSettings.activeServer)
})
