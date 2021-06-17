import { MusicDb } from "./storage/music";
import { SettingsDb } from "./storage/settings";
import { SubsonicApiClient } from "./subsonic/api";

export const musicDb = new MusicDb();
export const settingsDb = new SettingsDb();
