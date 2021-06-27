import AsyncStorage from '@react-native-async-storage/async-storage';

const key = {
  downloadedSongKeys: '@downloadedSongKeys',
  downloadedAlbumKeys: '@downloadedAlbumKeys',
  downloadedArtistKeys: '@downloadedArtistKeys',
  downloadedPlaylistKeys: '@downloadedPlaylistKeys',
};

export async function getItem(key: string): Promise<string | null> {
  try {
    return await AsyncStorage.getItem(key);
  } catch (e) {
    console.error(`getItem error (key: ${key})`, e);
    return null;
  }
}

export async function multiGet(keys: string[]): Promise<[string, string | null][]> {
  try {
    return await AsyncStorage.multiGet(keys);
  } catch (e) {
    console.error(`multiGet error`, e);
    return [];
  }
}

export async function setItem(key: string, item: string): Promise<void> {
  try {
    await AsyncStorage.setItem(key, item);
  } catch (e) {
    console.error(`setItem error (key: ${key})`, e);
  }
}

export async function multiSet(items: string[][]): Promise<void> {
  try {
    await AsyncStorage.multiSet(items);
  } catch (e) {
    console.error(`multiSet error`, e);
  }
}

type DownloadedSong = {
  id: string;
  type: 'song';
  name: string;
  album: string;
  artist: string;
};

type DownloadedAlbum = {
  id: string;
  type: 'album';
  songs: string[];
  name: string;
  artist: string;
};

type DownloadedArtist = {
  id: string;
  type: 'artist';
  songs: string[];
  name: string;
};

type DownloadedPlaylist = {
  id: string;
  type: 'playlist';
  songs: string[];
  name: string;
};

export async function getDownloadedSongs(): Promise<DownloadedSong[]> {
  const keysItem = await getItem(key.downloadedSongKeys);
  const keys: string[] = keysItem ? JSON.parse(keysItem) : [];

  const items = await multiGet(keys);
  return items.map(x => {
    const parsed = JSON.parse(x[1] as string);
    return {
      id: x[0],
      type: 'song',
      ...parsed,
    };
  });
}

export async function setDownloadedSongs(items: DownloadedSong[]): Promise<void> {
  await multiSet([
    [key.downloadedSongKeys, JSON.stringify(items.map(x => x.id))],
    ...items.map(x => [x.id, JSON.stringify({
      name: x.name,
      album: x.album,
      artist: x.artist,
    })]),
  ]);
}
