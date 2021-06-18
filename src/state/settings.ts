import { atom, DefaultValue, selector } from 'recoil';
import { settingsDb } from '../clients';
import { AppSettings, ServerSettings } from '../storage/settings';

export const serversState = atom<ServerSettings[]>({
  key: 'serverState',
  default: selector({
    key: 'serversState/default',
    get: () => settingsDb.getServers(),
  }),
  effects_UNSTABLE: [
    ({ onSet }) => {
      onSet((newValue) => {
        if (!(newValue instanceof DefaultValue)) {
          settingsDb.updateServers(newValue);
        }
      });
    }
  ],
});

export const appSettingsState = atom<AppSettings>({
  key: 'appSettingsState',
  default: selector({
    key: 'appSettingsState/default',
    get: () => settingsDb.getApp(),
  }),
  effects_UNSTABLE: [
    ({ onSet }) => {
      onSet((newValue, oldValue) => {
        if (!(newValue instanceof DefaultValue)) {
          settingsDb.updateApp(newValue);
        }
      });
    }
  ],
});

export const activeServer = selector<ServerSettings | undefined>({
  key: 'activeServer',
  get: ({get}) => {
    const appSettings = get(appSettingsState);
    const servers = get(serversState);

    return servers.find(x => x.id == appSettings.server);
  }
});
