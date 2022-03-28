import { GetAlbumList2Type } from '@app/subsonic/params'

export type Server = (TokenPassword | PlainPassword) & {
  id: string
  address: string
  username: string
  usePlainPassword: boolean
}

interface PlainPassword {
  usePlainPassword: true
  plainPassword: string
}

interface TokenPassword {
  usePlainPassword: false
  token: string
  salt: string
}

export interface AlbumFilterSettings {
  type: GetAlbumList2Type
  fromYear: number
  toYear: number
  genre: string
}

export type ArtistFilterType = 'random' | 'starred' | 'alphabeticalByName'

export interface ArtistFilterSettings {
  type: ArtistFilterType
}
