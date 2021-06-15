import { DOMParser } from 'xmldom';
import { GetArtistsResponse, GetIndexesResponse, SubsonicResponse } from './response';

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

  private async apiRequest(method: string, params?: URLSearchParams): Promise<Document> {
    let query = this.params.toString();
    if (params !== undefined && Array.from(params as any).length > 0) {
      query += '&' + params.toString();
    }

    const url = `${this.address}/rest/${method}?${query}`;
    console.log(url);

    const response = await fetch(url);
    const text = await response.text();

    console.log(text);

    const xml = new DOMParser().parseFromString(text);
    if (xml.documentElement.getAttribute('status') !== 'ok') {
      throw new SubsonicApiError(method, xml);
    }

    return xml;
  }

  async ping(): Promise<SubsonicResponse<null>> {
    const xml = await this.apiRequest('ping');
    const response = new SubsonicResponse<null>(xml, null);

    return response;
  }

  async getArtists(): Promise<SubsonicResponse<GetArtistsResponse>> {
    const xml = await this.apiRequest('getArtists');
    const data = new GetArtistsResponse(xml);
    const response = new SubsonicResponse<GetArtistsResponse>(xml, data);

    return response;
  }

  async getIndexes(ifModifiedSince?: number): Promise<SubsonicResponse<GetIndexesResponse>> {
    const params = new URLSearchParams();
    console.log(params);
    if (ifModifiedSince !== undefined) {
      params.append('ifModifiedSince', ifModifiedSince.toString());
    }

    const xml = await this.apiRequest('getIndexes', params);
    const data = new GetIndexesResponse(xml);
    const response = new SubsonicResponse<GetIndexesResponse>(xml, data);
    
    console.log(response.status);
    console.log(response.version);
    console.log(response.data.lastModified);

    return response;
  }
}

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
