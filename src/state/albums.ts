import { atom, DefaultValue, selector, useRecoilValue, useSetRecoilState } from 'recoil';
import { SubsonicApiClient } from '../subsonic/api';
import { activeServer } from './settings'
import { Artist } from '../models/music';
import { musicDb } from '../clients';

export const albumsState = atom<Artist[]>({
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
    }
  ],
});

// export const useUpdateAlbums = () => {
//   const setAlbums = useSetRecoilState(albumsState);
//   const server = useRecoilValue(activeServer);

//   return async () => {
//     if (!server) {
//       return;
//     }

//     const client = new SubsonicApiClient(server.address, server.username, server.token, server.salt);
//     const response = await client.getAlbums();

//     setAlbums(response.data.albums.map(i => ({
//       id: i.id,
//       name: i.name,
//     })));
//   };
// };
