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
}
