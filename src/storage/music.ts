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
        starred TEXT,
        coverArt TEXT
      );
      `);
      tx.executeSql(`
      CREATE TABLE albums (
        id TEXT PRIMARY KEY NOT NULL,
        name TEXT NOT NULL,
        starred TEXT,
        coverArt TEXT
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
      starred: x.starred ? new Date(x.starred) : undefined,
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
        INSERT INTO artists (
          id,
          name,
          starred,
          coverArt
        )
        VALUES (?, ?, ?, ?);
        `, [
          a.id, 
          a.name, 
          a.starred ? a.starred.toISOString() : null, 
          a.coverArt || null
        ]);
      }
    });
  }

  async getAlbum(id: string): Promise<Album> {
    const results = await this.executeSql(`
    SELECT * FROM albums
    WHERE id = ?;
    `, [id]);
    
    const rows = results[0].rows.raw();
    return rows.map(x => ({
      id: x.id,
      name: x.name,
      starred: x.starred ? new Date(x.starred) : undefined,
      coverArt: x.coverArt || undefined,
    }))[0];
  }

  async getAlbumIds(): Promise<string[]> {
    return (await this.executeSql(`
    SELECT id FROM albums;
    `))[0].rows.raw().map(x => x.id);
  }

  async getAlbums(): Promise<{[id: string]: Album}> {
    return (await this.executeSql(`
    SELECT * FROM albums;
    `))[0].rows.raw().reduce((acc, x) => {
      acc[x.id] = {
        id: x.id,
        name: x.name,
        starred: x.starred ? new Date(x.starred) : undefined,
        coverArt: x.coverArt || undefined,
      };
      return acc;
    }, {});
  }

  async updateAlbums(albums: Album[]): Promise<void> {
    await this.transaction((tx) => {
      tx.executeSql(`
      DELETE FROM albums
      `);
      for (const a of albums) {
        tx.executeSql(`
        INSERT INTO albums (
          id,
          name,
          starred,
          coverArt
        )
        VALUES (?, ?, ?, ?);
        `, [
          a.id, 
          a.name, 
          a.starred ? a.starred.toISOString() : null, 
          a.coverArt || null
        ]);
      }
    });
  }
}
