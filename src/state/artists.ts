import md5 from 'md5';
import { atom, selector, useSetRecoilState } from 'recoil';
import { SubsonicApiClient } from '../subsonic/api';
import { MusicDb } from '../storage/music';

const db = new MusicDb();

const password = 'test';
const salt = 'salty';
const token = md5(password + salt);

const client = new SubsonicApiClient('http://navidrome.home', 'austin', token, salt);

export interface ArtistState {
  id: string;
  name: string;
}

export const artistsState = atom<ArtistState[]>({
  key: 'artistsState',
  default: selector({
    key: 'artistsState/default',
    get: async () => {
      await prepopulate();

      const results = await db.executeSql(`
      SELECT * FROM artists;
      `);

      return results[0].rows.raw().map(i => ({
        id: i.id,
        name: i.name,
      }));
    },
  }),
});

export const useUpdateArtists = () => {
  const setArtists = useSetRecoilState(artistsState);
  return async () => {
    const response = await client.getArtists();

    setArtists(response.data.artists.map(i => ({
      id: i.id,
      name: i.name,
    })));
  };
};

async function prepopulate() {
  try { await db.deleteDb() } catch {}
  await db.createDb();
  await db.executeSql(`
    INSERT INTO artists (id, name, starred)
    VALUES (?, ?, ?);
    `,
    [1, 'good guy', true]
  );
  await db.executeSql(`
    INSERT INTO artists (id, name, starred)
    VALUES (?, ?, ?);
    `,
    [2, 'bad guy', false]
  );
}