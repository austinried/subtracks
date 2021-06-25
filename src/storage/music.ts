import { Album, Artist, Song } from '../models/music';
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
        artistId TEXT NOT NULL,
        name TEXT NOT NULL,
        starred TEXT,
        coverArt TEXT
      );
      `);
      tx.executeSql(`
      CREATE TABLE songs (
        id TEXT PRIMARY KEY NOT NULL,
        albumId TEXT NOT NULL,
        artistId TEXT NOT NULL,
        name TEXT NOT NULL,
        starred TEXT,
        artist TEXT
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

  async getAlbums(): Promise<Album[]> {
    return (await this.executeSql(`
    SELECT * FROM albums;
    `))[0].rows.raw().map(x => ({
      id: x.id,
      artistId: x.artistid,
      name: x.name,
      starred: x.starred ? new Date(x.starred) : undefined,
      coverArt: x.coverArt || undefined,
    }));
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
          artistId,
          name,
          starred,
          coverArt
        )
        VALUES (?, ?, ?, ?, ?);
        `, [
          a.id,
          a.artistId,
          a.name,
          a.starred ? a.starred.toISOString() : null,
          a.coverArt || null
        ]);
      }
    });
  }

  async getSongs(): Promise<Song[]> {
    return (await this.executeSql(`
    SELECT * FROM songs;
    `))[0].rows.raw().map(x => ({
      id: x.id,
      artistId: x.artistid,
      albumId: x.albumId,
      name: x.name,
      starred: x.starred ? new Date(x.starred) : undefined,
    }));
  }

  async updateSongs(songs: Song[]): Promise<void> {
    await this.transaction((tx) => {
      tx.executeSql(`
      DELETE FROM songs
      `);
      for (const x of songs) {
        tx.executeSql(`
        INSERT INTO songs (
          id,
          artistId,
          albumId,
          name,
          starred
        )
        VALUES (?, ?, ?, ?, ?);
        `, [
          x.id,
          x.artistId,
          x.albumId,
          x.name,
          x.starred ? x.starred.toISOString() : null,
        ]);
      }
    });
  }
}
