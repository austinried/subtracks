import { atom, useRecoilState, useRecoilValue, useSetRecoilState } from 'recoil';
import { Album, Artist } from '../models/music';
import { SubsonicApiClient } from '../subsonic/api';
import { activeServer } from './settings';

export const artistsState = atom<Artist[]>({
  key: 'artistsState',
  default: [],
});

export const artistsUpdatingState = atom<boolean>({
  key: 'artistsUpdatingState',
  default: false,
});

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
});

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
