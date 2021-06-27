export interface Server {
  id: string;
  address: string;
  username: string;
  token: string;
  salt: string;
}

export interface AppSettings {
  servers: Server[],
  activeServer?: string;
}
