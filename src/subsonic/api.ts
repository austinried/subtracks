import { DOMParser } from 'xmldom';

export class SubsonicApiClient {
  public address: string;
  public username: string;

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
    const url = `${this.address}/rest/${method}?${(params || this.params).toString()}`;

    const response = await fetch(url);
    const text = await response.text();

    console.log(text);

    const xml = new DOMParser().parseFromString(text);
    if (xml.documentElement.getAttribute('status') !== 'ok') {
      throw new SubsonicApiException();
    }

    return xml;
  }

  public async ping(): Promise<SubsonicResponse<null>> {
    const xml = await this.apiRequest('ping');
    const response = new SubsonicResponse<null>(xml, null);

    console.log(response.status);
    console.log(response.version);

    return response;
  }

  public async getArtists(): Promise<SubsonicResponse<ArtistID3Response>> {
    const xml = await this.apiRequest('getArtists');
    const data = new ArtistID3Response(xml);
    const response = new SubsonicResponse<ArtistID3Response>(xml, data);
    
    console.log(response.status);
    console.log(response.version);
    console.log(response.data.artists);

    return response;
  }
}

class SubsonicApiException {

}

type ResponseStatus = 'ok' | 'failed';

class SubsonicResponse<T> {
  public status: ResponseStatus;
  public version: string;
  public data: T;

  constructor(xml: Document, data: T) {
    this.data = data;
    this.status = xml.documentElement.getAttribute('status') as ResponseStatus;
    this.version = xml.documentElement.getAttribute('version') as string;
  }
}

interface ArtistID3 {
  id: string;
  name: string;
  coverArt?: string;
  albumCount: number;
  starred?: Date;
}

class ArtistID3Response {
  public ignoredArticles: string;
  public artists: ArtistID3[] = [];

  constructor(xml: Document) {
    this.ignoredArticles = xml.getElementsByTagName('artists')[0].getAttribute('ignoredArticles') as string;

    const artistElements = xml.getElementsByTagName('artist');
    for (let i = 0; i < artistElements.length; i++) {
      const a = artistElements[i];

      this.artists.push({
        id: a.getAttribute('id') as string,
        name: a.getAttribute('name') as string,
        coverArt: a.getAttribute('coverArt') || undefined,
        albumCount: parseInt(a.getAttribute('albumCount') as string),
        starred: a.getAttribute('starred') ? new Date(a.getAttribute('starred') as string) : undefined,
      });
    }
  }
}
