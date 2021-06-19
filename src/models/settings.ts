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
