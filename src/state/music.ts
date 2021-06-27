import { useEffect, useState } from 'react';
import { atom, DefaultValue, selector, useRecoilState, useRecoilValue, useSetRecoilState } from 'recoil';
import { musicDb } from '../clients';
import { Album, Artist, Song } from '../models/music';
import paths from '../paths';
import { SubsonicApiClient } from '../subsonic/api';
import { activeServer } from './settings';
import RNFS from 'react-native-fs';

export const artistsState = atom<Artist[]>({
  key: 'artistsState',
  default: [],
});

export const artistsUpdatingState = atom<boolean>({
  key: 'artistsUpdatingState',
  default: false,
})

export const useUpdateArtists = () => {
  const server = useRecoilValue(activeServer);
  if (!server) {
    return () => Promise.resolve();
  }

  const [updating, setUpdating] = useRecoilState(artistsUpdatingState);
  const setArtists = useSetRecoilState(artistsState);

  return async () => {
    if (updating) {
      return;
    }
    setUpdating(true);

    const client = new SubsonicApiClient(server);
    const response = await client.getArtists();

    setArtists(response.data.artists.map(x => ({
      id: x.id,
      name: x.name,
      starred: x.starred,
      coverArt: x.coverArt,
      coverArtUri: x.coverArt ? client.getCoverArtUri({ id: x.coverArt }) : undefined,
    })));
    setUpdating(false);
  }
}

export const albumsState = atom<Album[]>({
  key: 'albumsState',
  default: [],
});

export const albumsUpdatingState = atom<boolean>({
  key: 'albumsUpdatingState',
  default: false,
})

export const useUpdateAlbums = () => {
  const server = useRecoilValue(activeServer);
  if (!server) {
    return () => Promise.resolve();
  }

  const [updating, setUpdating] = useRecoilState(albumsUpdatingState);
  const setAlbums = useSetRecoilState(albumsState);

  return async () => {
    if (updating) {
      return;
    }
    setUpdating(true);

    const client = new SubsonicApiClient(server);
    const response = await client.getAlbumList2({ type: 'alphabeticalByArtist', size: 500 });

    setAlbums(response.data.albums.map(x => ({
      id: x.id,
      artistId: x.artistId,
      artist: x.artist,
      name: x.name,
      starred: x.starred,
      coverArt: x.coverArt,
      coverArtUri: x.coverArt ? client.getCoverArtUri({ id: x.coverArt }) : undefined,
      coverArtThumbUri: x.coverArt ? client.getCoverArtUri({ id: x.coverArt, size: '128' }) : undefined,
    })));
    setUpdating(false);
  }
}

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
