import { Artist, ArtistID3, BaseArtist } from "./element";

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

class BaseGetArtistInfoResponse<T extends BaseArtist> {
  similarArtists: T[] = [];
  biography?: string;
  musicBrainzId?: string;
  lastFmUrl?: string;
  smallImageUrl?: string;
  mediumImageUrl?: string;
  largeImageUrl?: string;

  constructor(xml: Document, artistType: new (e: Element) => T) {
    if (xml.getElementsByTagName('biography').length > 0) {
      this.biography = xml.getElementsByTagName('biography')[0].textContent as string;
    }
    if (xml.getElementsByTagName('musicBrainzId').length > 0) {
      this.musicBrainzId = xml.getElementsByTagName('musicBrainzId')[0].textContent as string;
    }
    if (xml.getElementsByTagName('lastFmUrl').length > 0) {
      this.lastFmUrl = xml.getElementsByTagName('lastFmUrl')[0].textContent as string;
    }
    if (xml.getElementsByTagName('smallImageUrl').length > 0) {
      this.smallImageUrl = xml.getElementsByTagName('smallImageUrl')[0].textContent as string;
    }
    if (xml.getElementsByTagName('mediumImageUrl').length > 0) {
      this.mediumImageUrl = xml.getElementsByTagName('mediumImageUrl')[0].textContent as string;
    }
    if (xml.getElementsByTagName('largeImageUrl').length > 0) {
      this.largeImageUrl = xml.getElementsByTagName('largeImageUrl')[0].textContent as string;
    }

    const similarArtistElements = xml.getElementsByTagName('similarArtist');
    for (let i = 0; i < similarArtistElements.length; i++) {
      this.similarArtists.push(new artistType(similarArtistElements[i]));
    }
  }
}

export class GetArtistInfoResponse extends BaseGetArtistInfoResponse<Artist> {
  constructor(xml: Document) {
    super(xml, Artist);
  }
}

export class GetArtistInfo2Response extends BaseGetArtistInfoResponse<ArtistID3> {
  constructor(xml: Document) {
    super(xml, ArtistID3);
  }
}
