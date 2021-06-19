import { Album, Artist } from '../models/music';
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
        starred INTEGER NOT NULL,
        coverArt TEXT
      );
      `);
      tx.executeSql(`
      CREATE TABLE albums (
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
      coverArt: x.coverArt || undefined,
    }));
  }

  async updateArtists(artists: Artist[]): Promise<void> {
    await this.transaction((tx) => {
      tx.executeSql(`
      DELETE FROM artists
      `);
      for (const a of artists) {
        tx.executeSql(`
        INSERT INTO artists (id, name, starred, coverArt)
        VALUES (?, ?, ?, ?);
        `, [a.id, a.name, false, a.coverArt || null]);
      }
    });
  }

  async getAlbums(): Promise<Album[]> {
    return (await this.executeSql(`
    SELECT * FROM albums;
    `))[0].rows.raw().map(x => ({
      id: x.id,
      name: x.name,
    }));
  }

  async updateAlbums(albums: Album[]): Promise<void> {
    await this.transaction((tx) => {
      tx.executeSql(`
      DELETE FROM albums
      `);
      for (const a of albums) {
        tx.executeSql(`
        INSERT INTO albums (id, name, starred)
        VALUES (?, ?, ?);
        `, [a.id, a.name, false]);
      }
    });
  }
}
