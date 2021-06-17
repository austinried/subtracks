import SQLite, { DatabaseParams, ResultSet, SQLiteDatabase } from 'react-native-sqlite-storage';

SQLite.enablePromise(true);

abstract class DbStorage {
  private dbParams: DatabaseParams;
  
  constructor(dbParams: DatabaseParams) {
    this.dbParams = dbParams;
  }

  abstract createDb(): Promise<void>

  private async openDb(): Promise<SQLiteDatabase> {
    return await SQLite.openDatabase({ ...this.dbParams });
  }

  async deleteDb(): Promise<void> {
    await SQLite.deleteDatabase({ ...this.dbParams });
  }

  async executeSql(sql: string, params?: any[]): Promise<[ResultSet]> {
    const db = await this.openDb();
    try {
      // https://github.com/andpor/react-native-sqlite-storage/issues/410
      // if (params) {
      //   for (const p of params) {
      //     if (Array.isArray(p)) {
      //       throw new Error('param value cannot be an array');
      //     }
      //   }
      // }
      return await db.executeSql(sql, params);
    } catch (err) {
      try { await db.close(); } catch {}
      throw err;
    } finally {
      try { await db.close(); } catch {}
    }
  }
}

export class MusicDb extends DbStorage {
  constructor() {
    super({ name: 'music.db', location: 'default' });
  }

  async createDb(): Promise<void> {
    await this.executeSql(`
    CREATE TABLE artists (
      id PRIMARY KEY NOT NULL,
      name NOT NULL,
      starred
    );
    `);
  }
}

class SettingsDb extends DbStorage {
  constructor() {
    super({ name: 'settings.db', location: 'Library' });
  }

  async createDb(): Promise<void> {
    return;
  }
}
