import { atom, DefaultValue, selector, useRecoilValue, useSetRecoilState } from 'recoil';
import { SubsonicApiClient } from '../subsonic/api';
import { activeServer } from './settings'
import { Artist } from '../models/music';
import { musicDb } from '../clients';

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
    }
  ],
});

export const useUpdateArtists = () => {
  const setArtists = useSetRecoilState(artistsState);
  const server = useRecoilValue(activeServer);

  return async () => {
    if (!server) {
      return;
    }

    const client = new SubsonicApiClient(server.address, server.username, server.token, server.salt);
    const response = await client.getArtists();

    setArtists(response.data.artists.map(x => ({
      id: x.id,
      name: x.name,
      coverArt: x.coverArt,
    })));
  };
};
