import { Artist, ArtistID3 } from "./element";

export type ResponseStatus = 'ok' | 'failed';

export class SubsonicResponse<T> {
  status: ResponseStatus;
  version: string;
  data: T;

  constructor(xml: Document, data: T) {
    this.data = data;
    this.status = xml.documentElement.getAttribute('status') as ResponseStatus;
    this.version = xml.documentElement.getAttribute('version') as string;
  }
}

export class GetArtistsResponse {
  ignoredArticles: string;
  artists: ArtistID3[] = [];

  constructor(xml: Document) {
    this.ignoredArticles = xml.getElementsByTagName('artists')[0].getAttribute('ignoredArticles') as string;

    const artistElements = xml.getElementsByTagName('artist');
    for (let i = 0; i < artistElements.length; i++) {
      this.artists.push(new ArtistID3(artistElements[i]));
    }
  }
}

export class GetIndexesResponse {
  ignoredArticles: string;
  lastModified: number;
  artists: Artist[] = [];

  constructor(xml: Document) {
    const indexesElement = xml.getElementsByTagName('indexes')[0];

    this.ignoredArticles = indexesElement.getAttribute('ignoredArticles') as string;
    this.lastModified = parseInt(indexesElement.getAttribute('lastModified') as string);

    const artistElements = xml.getElementsByTagName('artist');
    for (let i = 0; i < artistElements.length; i++) {
      this.artists.push(new Artist(artistElements[i]));
    }
  }
}
