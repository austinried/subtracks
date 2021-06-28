import { atom, useAtom } from 'jotai';
import { atomFamily, useAtomValue, useUpdateAtom } from 'jotai/utils';
import { Album, Artist } from '../models/music';
import { SubsonicApiClient } from '../subsonic/api';
import { activeServerAtom } from './settings';

export const artistsAtom = atom<Artist[]>([]);
export const artistsUpdatingAtom = atom(false);

export const useUpdateArtists = () => {
  const server = useAtomValue(activeServerAtom);
  const [updating, setUpdating] = useAtom(artistsUpdatingAtom);
  const setArtists = useUpdateAtom(artistsAtom);

  if (!server) {
    return () => Promise.resolve();
  }

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

export const albumsAtom = atom<Album[]>([]);
export const albumsUpdatingAtom = atom(false);

export const useUpdateAlbums = () => {
  const server = useAtomValue(activeServerAtom);
  const [updating, setUpdating] = useAtom(albumsUpdatingAtom);
  const setAlbums = useUpdateAtom(albumsAtom);

  if (!server) {
    return () => Promise.resolve();
  }

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

export const albumAtomFamily = atomFamily((id: string) => atom<Album | undefined>(async (get) => {
  const server = get(activeServerAtom);
  if (!server) {
    return undefined;
  }

  const client = new SubsonicApiClient(server);
  const response = await client.getAlbum({ id });

  return {
    id,
    name: response.data.album.name,
    artist: response.data.album.artist,
  };
}));
