import { DOMParser } from 'xmldom';
import RNFS from 'react-native-fs';
import { GetAlbumList2Params, GetAlbumListParams, GetArtistInfo2Params, GetArtistInfoParams, GetCoverArtParams, GetIndexesParams } from './params';
import { GetAlbumList2Response, GetAlbumListResponse, GetArtistInfo2Response, GetArtistInfoResponse, GetArtistsResponse, GetIndexesResponse, SubsonicResponse } from './responses';

export class SubsonicApiError extends Error {
  method: string;
  code: string;

  constructor(method: string, xml: Document) {
    const errorElement = xml.getElementsByTagName('error')[0];

    super(errorElement.getAttribute('message') as string);

    this.name = method;
    this.method = method;
    this.code = errorElement.getAttribute('code') as string;
  }
}

export class SubsonicApiClient {
  address: string;
  username: string;

  private params: URLSearchParams

  constructor(address: string, username: string, token: string, salt: string) {
    this.address = address;
    this.username = username;

    this.params = new URLSearchParams();
    this.params.append('u', username);
    this.params.append('t', token);
    this.params.append('s', salt);
    this.params.append('v', '1.15.0');
    this.params.append('c', 'subsonify-cool-unique-app-string')
  }

  private buildUrl(method: string, params?: {[key: string]: any}): string {
    let query = this.params.toString();
    if (params) {
      const urlParams = this.obj2Params(params);
      if (urlParams) {
        query += '&' + urlParams.toString();
      }
    }

    const url = `${this.address}/rest/${method}?${query}`;
    console.log(url);
    return url;
  }

  private async apiDownload(method: string, path: string, params?: {[key: string]: any}): Promise<string> {
    await RNFS.downloadFile({
      fromUrl: this.buildUrl(method, params),
      toFile: path,
    }).promise;
    
    return path;
  }

  private async apiGetXml(method: string, params?: {[key: string]: any}): Promise<Document> {
    const response = await fetch(this.buildUrl(method, params));
    const text = await response.text();

    console.log(text);

    const xml = new DOMParser().parseFromString(text);
    if (xml.documentElement.getAttribute('status') !== 'ok') {
      throw new SubsonicApiError(method, xml);
    }

    return xml;
  }

  private obj2Params(obj: {[key: string]: any}): URLSearchParams | undefined {
    const keys = Object.keys(obj);
    if (keys.length === 0) {
      return undefined;
    }

    const params = new URLSearchParams();
    for (const key of keys) {
      params.append(key, String(obj[key]));
    }

    return params;
  }

  // 
  // System
  // 

  async ping(): Promise<SubsonicResponse<null>> {
    const xml = await this.apiGetXml('ping');
    return new SubsonicResponse<null>(xml, null);
  }

  // 
  // Browsing
  // 

  async getArtists(): Promise<SubsonicResponse<GetArtistsResponse>> {
    const xml = await this.apiGetXml('getArtists');
    return new SubsonicResponse<GetArtistsResponse>(xml, new GetArtistsResponse(xml));
  }

  async getIndexes(params?: GetIndexesParams): Promise<SubsonicResponse<GetIndexesResponse>> {
    const xml = await this.apiGetXml('getIndexes', params);
    return new SubsonicResponse<GetIndexesResponse>(xml, new GetIndexesResponse(xml));
  }

  async getArtistInfo(params: GetArtistInfoParams): Promise<SubsonicResponse<GetArtistInfoResponse>> {
    const xml = await this.apiGetXml('getArtistInfo', params);
    return new SubsonicResponse<GetArtistInfoResponse>(xml, new GetArtistInfoResponse(xml));
  }

  async getArtistInfo2(params: GetArtistInfo2Params): Promise<SubsonicResponse<GetArtistInfo2Response>> {
    const xml = await this.apiGetXml('getArtistInfo2', params);
    return new SubsonicResponse<GetArtistInfo2Response>(xml, new GetArtistInfo2Response(xml));
  }

  // 
  // Album/song lists
  // 

  async getAlbumList(params: GetAlbumListParams): Promise<SubsonicResponse<GetAlbumListResponse>> {
    const xml = await this.apiGetXml('getAlbumList', params);
    return new SubsonicResponse<GetAlbumListResponse>(xml, new GetAlbumListResponse(xml));
  }

  async getAlbumList2(params: GetAlbumList2Params): Promise<SubsonicResponse<GetAlbumList2Response>> {
    const xml = await this.apiGetXml('getAlbumList2', params);
    return new SubsonicResponse<GetAlbumList2Response>(xml, new GetAlbumList2Response(xml));
  }

  // 
  // Media retrieval
  // 

  async getCoverArt(params: GetCoverArtParams): Promise<string> {
    const path = `${RNFS.DocumentDirectoryPath}/image_cache/${params.id}`;
    return await this.apiDownload('getCoverArt', path, params);
  }
}
