import { DbStorage } from './db';

export interface ServerSettings {
  id: string;
  address: string;
  username: string;
  token: string;
  salt: string;
}

export interface AppSettings {
  server?: string;
}

export class SettingsDb extends DbStorage {
  constructor() {
    super({ name: 'settings.db', location: 'Library' });
  }

  async createDb(): Promise<void> {
    await this.initDb(tx => {
      tx.executeSql(`
      CREATE TABLE servers (
        id TEXT PRIMARY KEY NOT NULL,
        address TEXT NOT NULL,
        username TEXT NOT NULL,
        token TEXT NOT NULL,
        salt TEXT NOT NULL
      );
      `);
      tx.executeSql(`
      CREATE TABLE app (
        server TEXT
      );
      `);
      tx.executeSql(`
      INSERT INTO app (server)
      VALUES (NULL);
      `);
    });
  }

  async getServers(): Promise<ServerSettings[]> {
    return (await this.executeSql(`
    SELECT * FROM servers;
    `))[0].rows.raw().map(x => ({
      id: x.id,
      address: x.address,
      username: x.username,
      token: x.token,
      salt: x.salt,
    }));
  }

  async getServer(id: string): Promise<ServerSettings | undefined> {
    return (await this.getServers()).find(x => x.id === id);
  }

  // async addServer(server: ServerSettings): Promise<void> {
  //   await this.executeSql(`
  //   INSERT INTO servers (id, address, username, token, salt)
  //   VALUES (?, ?, ?, ?, ?);
  //   `, [server.id, server.address, server.username, server.token, server.salt]);
  // }

  // async removeServer(id: string): Promise<void> {
  //   await this.executeSql(`
  //   DELETE FROM servers
  //   WHERE id = ?;
  //   `, [id]);
  // }

  async updateServers(servers: ServerSettings[]): Promise<void> {
    await this.transaction((tx) => {
      tx.executeSql(`
      DELETE FROM servers
      `);
      for (const s of servers) {
        tx.executeSql(`
        INSERT INTO servers (id, address, username, token, salt)
        VALUES (?, ?, ?, ?, ?);
        `, [s.id, s.address, s.username, s.token, s.salt]);
      }
    });
  }

  async getApp(): Promise<AppSettings> {
    return (await this.executeSql(`
    SELECT * FROM app;
    `))[0].rows.raw().map(x => ({
      server: x.server || undefined,
    }))[0];
  }

  async updateApp(app: AppSettings): Promise<void> {
    await this.executeSql(`
    UPDATE app SET
      server = ?;
    `, [
      app.server || null,
    ]);
  }
}
