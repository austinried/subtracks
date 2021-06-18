import SQLite, { DatabaseParams, ResultSet, SQLiteDatabase, Transaction } from 'react-native-sqlite-storage';

SQLite.enablePromise(true);

export abstract class DbStorage {
  private dbParams: DatabaseParams;
  
  constructor(dbParams: DatabaseParams) {
    this.dbParams = dbParams;
  }

  abstract createDb(): Promise<void>

  protected async initDb(scope: (tx: Transaction) => void): Promise<void> {
    await this.transaction(tx => {
      tx.executeSql(`
      CREATE TABLE db_version (
        version INTEGER NOT NULL
      );
      `);
      tx.executeSql(`
      INSERT INTO db_version (version)
      VALUES (?);
      `, [1]);

      scope(tx);
    });
  }

  async dbExists(): Promise<boolean> {
    const results = await this.executeSql(`
    SELECT name FROM sqlite_master
    WHERE type='table' AND name='db_version';
    `);

    return results[0].rows.length > 0;
  }

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
      return await db.executeSql(sql, params);
    } catch (err) {
      try { await db.close(); } catch {}
      throw err;
    } finally {
      try { await db.close(); } catch {}
    }
  }

  async transaction(scope: (tx: Transaction) => void): Promise<void> {
    const db = await this.openDb();
    try {
      await db.transaction(scope);
    } catch (err) {
      try { await db.close(); } catch {}
      throw err;
    } finally {
      try { await db.close(); } catch {}
    }
  }
}
