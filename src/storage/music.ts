import { DownloadedSong } from '../models/music';
import { getItem, multiGet, multiSet } from './asyncstorage';

const key = {
  downloadedSongKeys: '@downloadedSongKeys',
  downloadedAlbumKeys: '@downloadedAlbumKeys',
  downloadedArtistKeys: '@downloadedArtistKeys',
  downloadedPlaylistKeys: '@downloadedPlaylistKeys',
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
