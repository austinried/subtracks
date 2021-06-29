import { atom, useAtom } from 'jotai';
import { atomFamily, useAtomValue, useUpdateAtom } from 'jotai/utils';
import { Album as Album, AlbumWithSongs, Artist, Song } from '../models/music';
import { SubsonicApiClient } from '../subsonic/api';
import { AlbumID3Element, ChildElement } from '../subsonic/elements';
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

    setAlbums(response.data.albums.map(a => mapAlbumID3(a, client)));
    setUpdating(false);
  }
}

export const albumAtomFamily = atomFamily((id: string) => atom<AlbumWithSongs | undefined>(async (get) => {
  const server = get(activeServerAtom);
  if (!server) {
    return undefined;
  }

  const client = new SubsonicApiClient(server);
  const response = await client.getAlbum({ id });
  return mapAlbumID3WithSongs(response.data.album, response.data.songs, client);
}));

function mapAlbumID3(album: AlbumID3Element, client: SubsonicApiClient): Album {
  return { 
    ...album,
    coverArtUri: album.coverArt ? client.getCoverArtUri({ id: album.coverArt }) : undefined,
    coverArtThumbUri: album.coverArt ? client.getCoverArtUri({ id: album.coverArt, size: '256' }) : undefined,
  }
}

function mapChildToSong(child: ChildElement, client: SubsonicApiClient): Song {
  return {
    ...child,
    streamUri: client.streamUri({ id: child.id }),
    coverArtUri: child.coverArt ? client.getCoverArtUri({ id: child.coverArt }) : undefined,
  }
}

function mapAlbumID3WithSongs(
  album: AlbumID3Element,
  songs: ChildElement[],
  client: SubsonicApiClient
): AlbumWithSongs {
  return {
    ...mapAlbumID3(album, client),
    songs: songs.map(s => mapChildToSong(s, client)),
  }
}
