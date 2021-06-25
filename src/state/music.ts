import { useEffect, useState } from 'react';
import { atom, DefaultValue, selector, selectorFamily, useRecoilState, useRecoilValue, useSetRecoilState } from 'recoil';
import { musicDb } from '../clients';
import { Album, Artist, Song } from '../models/music';
import paths from '../paths';
import { SubsonicApiClient } from '../subsonic/api';
import { activeServer } from './settings';
import RNFS from 'react-native-fs';

export const artistsState = atom<Artist[]>({
  key: 'artistsState',
  default: selector({
    key: 'artistsState/default',
    get: () => musicDb.getArtists(),
  }),
  effects_UNSTABLE: [
    ({ onSet }) => {
      onSet((newValue) => {
        if (!(newValue instanceof DefaultValue)) {
          musicDb.updateArtists(newValue);
        }
      });
    },
  ],
});

export const albumsState = atom<Album[]>({
  key: 'albumsState',
  default: selector({
    key: 'albumsState/default',
    get: () => musicDb.getAlbums(),
  }),
  effects_UNSTABLE: [
    ({ onSet }) => {
      onSet((newValue) => {
        if (!(newValue instanceof DefaultValue)) {
          musicDb.updateAlbums(newValue);
        }
      });
    },
  ],
});

export const songsState = atom<Song[]>({
  key: 'songsState',
  default: selector({
    key: 'songsState/default',
    get: () => musicDb.getSongs(),
  }),
  effects_UNSTABLE: [
    ({ onSet }) => {
      onSet((newValue) => {
        if (!(newValue instanceof DefaultValue)) {
          musicDb.updateSongs(newValue);
        }
      });
    },
  ],
});

export const libraryRefreshState = atom<boolean>({
  key: 'libraryRefreshState',
  default: false,
});

export const isLibraryRefreshingState = atom<boolean>({
  key: 'isLibraryRefreshingState',
  default: false,
});

export function useCoverArtUri(id?: string): string | undefined {
  if (!id) {
    return undefined;
  }

  const server = useRecoilValue(activeServer);

  const [downloadAttempted, setdownloadAttempted] = useState(false);
  const [coverArtSource, setCoverArtSource] = useState<string | undefined>(undefined);

  const getCoverArt = async () => {
    if (coverArtSource) {
      return;
    }

    const filePath = `${paths.songCache}/${id}`;
    const fileUri = `file://${filePath}`;

    if (await RNFS.exists(filePath)) {
      // file already in cache, return the file
      setCoverArtSource(fileUri);
      return;
    }

    if (!server) {
      // can't download without server set
      return;
    }

    setdownloadAttempted(true);
    if (downloadAttempted) {
      // don't try to download more than once using this hook
      return;
    }

    const client = new SubsonicApiClient(server);
    await client.getCoverArt({ id });

    setCoverArtSource(fileUri);
  }

  useEffect(() => {
    getCoverArt();
  });

  return coverArtSource;
}
