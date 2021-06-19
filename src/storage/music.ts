import { Artist } from '../models/music';
import { DbStorage } from './db';

export class MusicDb extends DbStorage {
  constructor() {
    super({ name: 'music.db', location: 'default' });
  }

  async createDb(): Promise<void> {
    await this.initDb(tx => {
      tx.executeSql(`
      CREATE TABLE artists (
        id TEXT PRIMARY KEY NOT NULL,
        name TEXT NOT NULL,
        starred INTEGER NOT NULL
      );
      `);
    });
  }

  async getArtists(): Promise<Artist[]> {
    return (await this.executeSql(`
    SELECT * FROM artists;
    `))[0].rows.raw().map(x => ({
      id: x.id,
      name: x.name,
    }));
  }

  async updateArtists(artists: Artist[]): Promise<void> {
    await this.transaction((tx) => {
      tx.executeSql(`
      DELETE FROM artists
      `);
      for (const a of artists) {
        tx.executeSql(`
        INSERT INTO artists (id, name, starred)
        VALUES (?, ?, ?);
        `, [a.id, a.name, false]);
      }
    });
  }
}
