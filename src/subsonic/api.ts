import { DOMParser } from 'xmldom';
import RNFS from 'react-native-fs';
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
  StreamParams,
} from './params';
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
  SubsonicResponse,
} from './responses';
import { Server } from '../models/settings';
import paths from '../paths';

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

type QueuePromise = () => Promise<any>;

class Queue {
  maxSimultaneously: number;

  private active = 0;
  private queue: QueuePromise[] = [];

  constructor(maxSimultaneously = 1) {
    this.maxSimultaneously = maxSimultaneously;
  }

  async enqueue(func: QueuePromise) {
    if (++this.active > this.maxSimultaneously) {
      await new Promise(resolve => this.queue.push(resolve as QueuePromise));
    }

    try {
      return await func();
    } catch (err) {
      throw err;
    } finally {
      this.active--;
      if (this.queue.length) {
        (this.queue.shift() as QueuePromise)();
      }
    }
  }
}

const downloadQueue = new Queue(1);

export class SubsonicApiClient {
  address: string;
  username: string;

  private params: URLSearchParams;

  constructor(server: Server) {
    this.address = server.address;
    this.username = server.username;

    this.params = new URLSearchParams();
    this.params.append('u', server.username);
    this.params.append('t', server.token);
    this.params.append('s', server.salt);
    this.params.append('v', '1.15.0');
    this.params.append('c', 'subsonify-cool-unique-app-string');
  }

  private buildUrl(method: string, params?: { [key: string]: any }): string {
    let query = this.params.toString();
    if (params) {
      const urlParams = this.obj2Params(params);
      if (urlParams) {
        query += '&' + urlParams.toString();
      }
    }

    const url = `${this.address}/rest/${method}?${query}`;
    // console.log(url);
    return url;
  }

  private async apiDownload(method: string, path: string, params?: { [key: string]: any }): Promise<string> {
    const download = RNFS.downloadFile({
      fromUrl: this.buildUrl(method, params),
      toFile: path,
    }).promise;

    await downloadQueue.enqueue(() => download);
    await downloadQueue.enqueue(() => new Promise(resolve => setTimeout(resolve, 100)));

    return path;
  }

  private async apiGetXml(method: string, params?: { [key: string]: any }): Promise<Document> {
    const response = await fetch(this.buildUrl(method, params));
    const text = await response.text();

    // console.log(text);

    const xml = new DOMParser().parseFromString(text);
    if (xml.documentElement.getAttribute('status') !== 'ok') {
      throw new SubsonicApiError(method, xml);
    }

    return xml;
  }

  private obj2Params(obj: { [key: string]: any }): URLSearchParams | undefined {
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

  async getMusicDirectory(params: GetMusicDirectoryParams): Promise<SubsonicResponse<GetMusicDirectoryResponse>> {
    const xml = await this.apiGetXml('getMusicDirectory', params);
    return new SubsonicResponse<GetMusicDirectoryResponse>(xml, new GetMusicDirectoryResponse(xml));
  }

  async getAlbum(params: GetAlbumParams): Promise<SubsonicResponse<GetAlbumResponse>> {
    const xml = await this.apiGetXml('getAlbum', params);
    return new SubsonicResponse<GetAlbumResponse>(xml, new GetAlbumResponse(xml));
  }

  async getArtistInfo(params: GetArtistInfoParams): Promise<SubsonicResponse<GetArtistInfoResponse>> {
    const xml = await this.apiGetXml('getArtistInfo', params);
    return new SubsonicResponse<GetArtistInfoResponse>(xml, new GetArtistInfoResponse(xml));
  }

  async getArtistInfo2(params: GetArtistInfo2Params): Promise<SubsonicResponse<GetArtistInfo2Response>> {
    const xml = await this.apiGetXml('getArtistInfo2', params);
    return new SubsonicResponse<GetArtistInfo2Response>(xml, new GetArtistInfo2Response(xml));
  }

  async getArtist(params: GetArtistParams): Promise<SubsonicResponse<GetArtistResponse>> {
    const xml = await this.apiGetXml('getArtist', params);
    return new SubsonicResponse<GetArtistResponse>(xml, new GetArtistResponse(xml));
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
    const path = `${paths.songCache}/${params.id}`;
    return await this.apiDownload('getCoverArt', path, params);
  }

  getCoverArtUri(params: GetCoverArtParams): string {
    return this.buildUrl('getCoverArt', params);
  }

  streamUri(params: StreamParams): string {
    return this.buildUrl('stream', params);
  }
}
