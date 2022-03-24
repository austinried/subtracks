import { Server } from '@app/models/settings'
import {
  GetAlbumList2Params,
  GetAlbumListParams,
  GetAlbumParams,
  GetArtistInfo2Params,
  GetArtistInfoParams,
  GetArtistParams,
  GetCoverArtParams,
  GetIndexesParams,
  GetMusicDirectoryParams,
  GetPlaylistParams,
  GetPlaylistsParams,
  GetSongParams,
  GetTopSongsParams,
  ScrobbleParams,
  Search3Params,
  StarParams,
  StreamParams,
} from '@app/subsonic/params'
import {
  GetAlbumList2Response,
  GetAlbumListResponse,
  GetAlbumResponse,
  GetArtistInfo2Response,
  GetArtistInfoResponse,
  GetArtistResponse,
  GetArtistsResponse,
  GetIndexesResponse,
  GetMusicDirectoryResponse,
  GetPlaylistResponse,
  GetPlaylistsResponse,
  GetSongResponse,
  GetTopSongsResponse,
  NullResponse,
  Search3Response,
  SubsonicResponse,
} from '@app/subsonic/responses'
import toast from '@app/util/toast'
import userAgent from '@app/util/userAgent'
import { DOMParser } from '@xmldom/xmldom'

export class SubsonicApiError extends Error {
  method: string
  code: string

  constructor(method: string, xml: Document) {
    const errorElement = xml.getElementsByTagName('error')[0]

    super(errorElement.getAttribute('message') as string)

    this.name = method
    this.method = method
    this.code = errorElement.getAttribute('code') as string
  }
}

type ResponseType<T extends SubsonicResponse> = (xml: Document) => T

type RequestParams = {
  getIndexes: GetIndexesParams
  getMusicDirectory: GetMusicDirectoryParams
  getAlbum: GetAlbumParams
  getArtistInfo: GetArtistInfoParams
  getArtistInfo2: GetArtistInfo2Params
  getArtist: GetArtistParams
  getTopSongs: GetTopSongsParams
  getSong: GetSongParams
  getAlbumList: GetAlbumListParams
  getAlbumList2: GetAlbumList2Params
  getPlaylists: GetPlaylistsParams
  getPlaylist: GetPlaylistParams
  scrobble: ScrobbleParams
  star: StarParams
  unstar: StarParams
  search3: Search3Params
}

const Methods = {
  ping: (xml => new NullResponse(xml)) as ResponseType<NullResponse>,
  getArtists: (xml => new GetArtistsResponse(xml)) as ResponseType<GetArtistsResponse>,
  getIndexes: (xml => new GetIndexesResponse(xml)) as ResponseType<GetIndexesResponse>,
  getMusicDirectory: (xml => new GetMusicDirectoryResponse(xml)) as ResponseType<GetMusicDirectoryResponse>,
  getAlbum: (xml => new GetAlbumResponse(xml)) as ResponseType<GetAlbumResponse>,
  getArtistInfo: (xml => new GetArtistInfoResponse(xml)) as ResponseType<GetArtistInfoResponse>,
  getArtistInfo2: (xml => new GetArtistInfo2Response(xml)) as ResponseType<GetArtistInfo2Response>,
  getArtist: (xml => new GetArtistResponse(xml)) as ResponseType<GetArtistResponse>,
  getTopSongs: (xml => new GetTopSongsResponse(xml)) as ResponseType<GetTopSongsResponse>,
  getSong: (xml => new GetSongResponse(xml)) as ResponseType<GetSongResponse>,
  getAlbumList: (xml => new GetAlbumListResponse(xml)) as ResponseType<GetAlbumListResponse>,
  getAlbumList2: (xml => new GetAlbumList2Response(xml)) as ResponseType<GetAlbumList2Response>,
  getPlaylists: (xml => new GetPlaylistsResponse(xml)) as ResponseType<GetPlaylistsResponse>,
  getPlaylist: (xml => new GetPlaylistResponse(xml)) as ResponseType<GetPlaylistResponse>,
  scrobble: (xml => new NullResponse(xml)) as ResponseType<NullResponse>,
  star: (xml => new NullResponse(xml)) as ResponseType<NullResponse>,
  unstar: (xml => new NullResponse(xml)) as ResponseType<NullResponse>,
  search3: (xml => new Search3Response(xml)) as ResponseType<Search3Response>,
}

export class SubsonicApiClient {
  address: string
  username: string

  private params: URLSearchParams

  constructor(server: Server) {
    this.address = server.address
    this.username = server.username

    this.params = new URLSearchParams()
    this.params.append('u', server.username)

    if (server.usePlainPassword) {
      this.params.append('p', server.plainPassword)
    } else {
      this.params.append('t', server.token)
      this.params.append('s', server.salt)
    }

    this.params.append('v', '1.13.0')
    this.params.append('c', 'subtracks')
  }

  private buildUrl(method: string, params?: { [key: string]: any }): string {
    let query = this.params.toString()
    if (params) {
      const urlParams = this.obj2Params(params)
      if (urlParams) {
        query += '&' + urlParams.toString()
      }
    }

    // *.view was present on all method names in API 1.14.0 and earlier
    return `${this.address}/rest/${method}.view?${query}`
  }

  private async apiGetXml(method: string, params?: { [key: string]: any }): Promise<Document> {
    let text: string

    try {
      const response = await fetch(this.buildUrl(method, params), {
        headers: { 'User-Agent': userAgent },
      })
      text = await response.text()
    } catch (err) {
      toast(`Network error: ${this.address}`)
      throw err
    }

    const xml = new DOMParser().parseFromString(text)
    if (xml.documentElement.getAttribute('status') !== 'ok') {
      throw new SubsonicApiError(method, xml)
    }

    return xml
  }

  private obj2Params(obj: { [key: string]: any }): URLSearchParams | undefined {
    const keys = Object.keys(obj)
    if (keys.length === 0) {
      return undefined
    }

    const params = new URLSearchParams()
    for (const key of keys) {
      if (obj[key] === undefined || obj[key] === null) {
        continue
      }
      params.append(key, String(obj[key]))
    }

    return params
  }

  async fetch<T extends keyof typeof Methods>(
    method: T,
    ...params: T extends Extract<keyof RequestParams, T> ? [RequestParams[Extract<keyof RequestParams, T>]] : []
  ): Promise<ReturnType<typeof Methods[T]>> {
    const xml = await this.apiGetXml(method, params.length > 0 ? params[0] : undefined)
    return Methods[method](xml) as ReturnType<typeof Methods[T]>
  }

  getCoverArtUri(params?: GetCoverArtParams): string {
    return this.buildUrl('getCoverArt', params)
  }

  streamUri(params: StreamParams): string {
    return this.buildUrl('stream', params)
  }
}
