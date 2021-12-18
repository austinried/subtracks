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
  GetTopSongsResponse,
  Search3Response,
  SubsonicResponse,
} from '@app/subsonic/responses'
import toast from '@app/util/toast'
import userAgent from '@app/util/userAgent'
import { DOMParser } from 'xmldom'

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

  //
  // System
  //

  async ping(): Promise<SubsonicResponse<null>> {
    const xml = await this.apiGetXml('ping')
    return new SubsonicResponse<null>(xml, null)
  }

  //
  // Browsing
  //

  async getArtists(): Promise<SubsonicResponse<GetArtistsResponse>> {
    const xml = await this.apiGetXml('getArtists')
    return new SubsonicResponse<GetArtistsResponse>(xml, new GetArtistsResponse(xml))
  }

  async getIndexes(params?: GetIndexesParams): Promise<SubsonicResponse<GetIndexesResponse>> {
    const xml = await this.apiGetXml('getIndexes', params)
    return new SubsonicResponse<GetIndexesResponse>(xml, new GetIndexesResponse(xml))
  }

  async getMusicDirectory(params: GetMusicDirectoryParams): Promise<SubsonicResponse<GetMusicDirectoryResponse>> {
    const xml = await this.apiGetXml('getMusicDirectory', params)
    return new SubsonicResponse<GetMusicDirectoryResponse>(xml, new GetMusicDirectoryResponse(xml))
  }

  async getAlbum(params: GetAlbumParams): Promise<SubsonicResponse<GetAlbumResponse>> {
    const xml = await this.apiGetXml('getAlbum', params)
    return new SubsonicResponse<GetAlbumResponse>(xml, new GetAlbumResponse(xml))
  }

  async getArtistInfo(params: GetArtistInfoParams): Promise<SubsonicResponse<GetArtistInfoResponse>> {
    const xml = await this.apiGetXml('getArtistInfo', params)
    return new SubsonicResponse<GetArtistInfoResponse>(xml, new GetArtistInfoResponse(xml))
  }

  async getArtistInfo2(params: GetArtistInfo2Params): Promise<SubsonicResponse<GetArtistInfo2Response>> {
    const xml = await this.apiGetXml('getArtistInfo2', params)
    return new SubsonicResponse<GetArtistInfo2Response>(xml, new GetArtistInfo2Response(xml))
  }

  async getArtist(params: GetArtistParams): Promise<SubsonicResponse<GetArtistResponse>> {
    const xml = await this.apiGetXml('getArtist', params)
    return new SubsonicResponse<GetArtistResponse>(xml, new GetArtistResponse(xml))
  }

  async getTopSongs(params: GetTopSongsParams): Promise<SubsonicResponse<GetTopSongsResponse>> {
    const xml = await this.apiGetXml('getTopSongs', params)
    return new SubsonicResponse<GetTopSongsResponse>(xml, new GetTopSongsResponse(xml))
  }

  //
  // Album/song lists
  //

  async getAlbumList(params: GetAlbumListParams): Promise<SubsonicResponse<GetAlbumListResponse>> {
    const xml = await this.apiGetXml('getAlbumList', params)
    return new SubsonicResponse<GetAlbumListResponse>(xml, new GetAlbumListResponse(xml))
  }

  async getAlbumList2(params: GetAlbumList2Params): Promise<SubsonicResponse<GetAlbumList2Response>> {
    const xml = await this.apiGetXml('getAlbumList2', params)
    return new SubsonicResponse<GetAlbumList2Response>(xml, new GetAlbumList2Response(xml))
  }

  //
  // Playlists
  //

  async getPlaylists(params?: GetPlaylistsParams): Promise<SubsonicResponse<GetPlaylistsResponse>> {
    const xml = await this.apiGetXml('getPlaylists', params)
    return new SubsonicResponse<GetPlaylistsResponse>(xml, new GetPlaylistsResponse(xml))
  }

  async getPlaylist(params: GetPlaylistParams): Promise<SubsonicResponse<GetPlaylistResponse>> {
    const xml = await this.apiGetXml('getPlaylist', params)
    return new SubsonicResponse<GetPlaylistResponse>(xml, new GetPlaylistResponse(xml))
  }

  //
  // Media retrieval
  //

  getCoverArtUri(params?: GetCoverArtParams): string {
    return this.buildUrl('getCoverArt', params)
  }

  streamUri(params: StreamParams): string {
    return this.buildUrl('stream', params)
  }

  //
  // Media annotation
  //

  async scrobble(params: ScrobbleParams): Promise<SubsonicResponse<undefined>> {
    const xml = await this.apiGetXml('scrobble', params)
    return new SubsonicResponse<undefined>(xml, undefined)
  }

  async star(params: StarParams): Promise<SubsonicResponse<undefined>> {
    const xml = await this.apiGetXml('star', params)
    return new SubsonicResponse<undefined>(xml, undefined)
  }

  async unstar(params: StarParams): Promise<SubsonicResponse<undefined>> {
    const xml = await this.apiGetXml('unstar', params)
    return new SubsonicResponse<undefined>(xml, undefined)
  }

  //
  // Searching
  //

  async search3(params: Search3Params): Promise<SubsonicResponse<Search3Response>> {
    const xml = await this.apiGetXml('search3', params)
    return new SubsonicResponse<Search3Response>(xml, new Search3Response(xml))
  }
}
