import SQLite, { DatabaseParams, ResultSet, SQLiteDatabase, Transaction } from 'react-native-sqlite-storage';

SQLite.enablePromise(true);

export abstract class DbStorage {
  private dbParams: DatabaseParams;
  private db: SQLiteDatabase | undefined;
  
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

  async openDb(): Promise<void> {
    this.db = await SQLite.openDatabase({ ...this.dbParams });
  }

  async deleteDb(): Promise<void> {
    if (this.db) {
      await this.db.close();
    }
    await SQLite.deleteDatabase({ ...this.dbParams });
  }

  async executeSql(sql: string, params?: any[]): Promise<[ResultSet]> {
    // https://github.com/andpor/react-native-sqlite-storage/issues/410
    return await (this.db as SQLiteDatabase).executeSql(sql, params);
  }

  async transaction(scope: (tx: Transaction) => void): Promise<void> {
    await (this.db as SQLiteDatabase).transaction(scope);
  }
}
