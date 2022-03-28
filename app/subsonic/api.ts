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

  async ping(): Promise<NullResponse> {
    return new NullResponse(await this.apiGetXml('ping'))
  }

  //
  // Browsing
  //

  async getArtists(): Promise<GetArtistsResponse> {
    return new GetArtistsResponse(await this.apiGetXml('getArtists'))
  }

  async getIndexes(params?: GetIndexesParams): Promise<GetIndexesResponse> {
    return new GetIndexesResponse(await this.apiGetXml('getIndexes', params))
  }

  async getMusicDirectory(params: GetMusicDirectoryParams): Promise<GetMusicDirectoryResponse> {
    return new GetMusicDirectoryResponse(await this.apiGetXml('getMusicDirectory', params))
  }

  async getAlbum(params: GetAlbumParams): Promise<GetAlbumResponse> {
    return new GetAlbumResponse(await this.apiGetXml('getAlbum', params))
  }

  async getArtistInfo(params: GetArtistInfoParams): Promise<GetArtistInfoResponse> {
    return new GetArtistInfoResponse(await this.apiGetXml('getArtistInfo', params))
  }

  async getArtistInfo2(params: GetArtistInfo2Params): Promise<GetArtistInfo2Response> {
    return new GetArtistInfo2Response(await this.apiGetXml('getArtistInfo2', params))
  }

  async getArtist(params: GetArtistParams): Promise<GetArtistResponse> {
    return new GetArtistResponse(await this.apiGetXml('getArtist', params))
  }

  async getTopSongs(params: GetTopSongsParams): Promise<GetTopSongsResponse> {
    return new GetTopSongsResponse(await this.apiGetXml('getTopSongs', params))
  }

  async getSong(params: GetSongParams): Promise<GetSongResponse> {
    return new GetSongResponse(await this.apiGetXml('getSong', params))
  }

  //
  // Album/song lists
  //

  async getAlbumList(params: GetAlbumListParams): Promise<GetAlbumListResponse> {
    return new GetAlbumListResponse(await this.apiGetXml('getAlbumList', params))
  }

  async getAlbumList2(params: GetAlbumList2Params): Promise<GetAlbumList2Response> {
    return new GetAlbumList2Response(await this.apiGetXml('getAlbumList2', params))
  }

  //
  // Playlists
  //

  async getPlaylists(params?: GetPlaylistsParams): Promise<GetPlaylistsResponse> {
    return new GetPlaylistsResponse(await this.apiGetXml('getPlaylists', params))
  }

  async getPlaylist(params: GetPlaylistParams): Promise<GetPlaylistResponse> {
    return new GetPlaylistResponse(await this.apiGetXml('getPlaylist', params))
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

  async scrobble(params: ScrobbleParams): Promise<NullResponse> {
    return new NullResponse(await this.apiGetXml('scrobble', params))
  }

  async star(params: StarParams): Promise<NullResponse> {
    return new NullResponse(await this.apiGetXml('star', params))
  }

  async unstar(params: StarParams): Promise<NullResponse> {
    return new NullResponse(await this.apiGetXml('unstar', params))
  }

  //
  // Searching
  //

  async search3(params: Search3Params): Promise<Search3Response> {
    return new Search3Response(await this.apiGetXml('search3', params))
  }
}
