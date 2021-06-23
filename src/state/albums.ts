import { atom, DefaultValue, selector, selectorFamily, useRecoilState, useRecoilValue, useSetRecoilState } from 'recoil';
import { SubsonicApiClient } from '../subsonic/api';
import { activeServer } from './settings'
import { Album } from '../models/music';
import { musicDb } from '../clients';
import { useEffect, useState } from 'react';
import RNFS from 'react-native-fs';

export const albumsState = atom<{ [id: string]: Album }>({
  key: 'albumsState',
  default: selector({
    key: 'albumsState/default',
    get: () => musicDb.getAlbums(),
  }),
  effects_UNSTABLE: [
    ({ onSet }) => {
      onSet((newValue) => {
        if (!(newValue instanceof DefaultValue)) {
          musicDb.updateAlbums(Object.values(newValue));
        }
      });
    },
  ],
});

export const albumIdsState = selector<string[]>({
  key: 'albumIdsState',
  get: ({get}) => Object.keys(get(albumsState)),
});

export const albumState = selectorFamily<Album, string>({
  key: 'albumState',
  get: id => ({ get }) => {
    return get(albumsState)[id];
  },
  // set: id => ({ set, get }, newValue) => {
  //   if (!(newValue instanceof DefaultValue)) {
  //     set(albumsState, prevState => ({ ...prevState, [id]: newValue }));
  //   }
  // }
});

export const useUpdateAlbums = () => {
  const setAlbums = useSetRecoilState(albumsState);
  const server = useRecoilValue(activeServer);

  return async () => {
    if (!server) {
      return;
    }

    const client = new SubsonicApiClient(server.address, server.username, server.token, server.salt);
    const response = await client.getAlbumList2({ type: 'alphabeticalByArtist', size: 50 });

    const albums = response.data.albums.reduce((acc, x) => {
      acc[x.id] = {
        id: x.id,
        name: x.name,
        coverArt: x.coverArt,
      };
      return acc;
    }, {} as { [id: string]: Album });

    setAlbums(albums);
  };
};

export function useCoverArtUri(id: string | undefined): string | undefined {
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

    const filePath = `${RNFS.DocumentDirectoryPath}/image_cache/${id}`;
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

    const client = new SubsonicApiClient(server.address, server.username, server.token, server.salt);
    await client.getCoverArt({ id, size: '32' });

    setCoverArtSource(fileUri);
  }

  useEffect(() => {
    getCoverArt();
  });

  return coverArtSource;
}
