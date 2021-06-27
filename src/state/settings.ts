import { atom, DefaultValue, selector } from 'recoil';
import { AppSettings, Server } from '../models/settings';
import { getAppSettings, setAppSettings } from '../storage/settings';

export const appSettingsState = atom<AppSettings>({
  key: 'appSettingsState',
  default: selector({
    key: 'appSettingsState/default',
    get: () => getAppSettings(),
  }),
  effects_UNSTABLE: [
    ({ onSet }) => {
      onSet((newValue) => {
        if (!(newValue instanceof DefaultValue)) {
          setAppSettings(newValue);
        }
      });
    }
  ],
});

export const activeServer = selector<Server | undefined>({
  key: 'activeServer',
  get: ({get}) => {
    const appSettings = get(appSettingsState);
    return appSettings.servers.find(x => x.id == appSettings.activeServer);
  }
});
