import { AppSettings } from '../models/settings';
import { getItem, setItem } from './asyncstorage';

const appSettingsKey = '@appSettings';

export async function getAppSettings(): Promise<AppSettings> {
  const item = await getItem(appSettingsKey);
  return item ? JSON.parse(item) : {
    servers: [],
  };
}

export async function setAppSettings(appSettings: AppSettings): Promise<void> {
  await setItem(appSettingsKey, JSON.stringify(appSettings));
}
