import { AlbumID3Element, ArtistElement, ArtistID3Element, BaseArtistElement, ChildElement, DirectoryElement } from "./elements";

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

// 
// Browsing
// 

export class GetArtistsResponse {
  ignoredArticles: string;
  artists: ArtistID3Element[] = [];

  constructor(xml: Document) {
    this.ignoredArticles = xml.getElementsByTagName('artists')[0].getAttribute('ignoredArticles') as string;

    const artistElements = xml.getElementsByTagName('artist');
    for (let i = 0; i < artistElements.length; i++) {
      this.artists.push(new ArtistID3Element(artistElements[i]));
    }
  }
}

export class GetIndexesResponse {
  ignoredArticles: string;
  lastModified: number;
  artists: ArtistElement[] = [];

  constructor(xml: Document) {
    const indexesElement = xml.getElementsByTagName('indexes')[0];

    this.ignoredArticles = indexesElement.getAttribute('ignoredArticles') as string;
    this.lastModified = parseInt(indexesElement.getAttribute('lastModified') as string);

    const artistElements = xml.getElementsByTagName('artist');
    for (let i = 0; i < artistElements.length; i++) {
      this.artists.push(new ArtistElement(artistElements[i]));
    }
  }
}

class BaseGetArtistInfoResponse<T extends BaseArtistElement> {
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

export class GetArtistInfoResponse extends BaseGetArtistInfoResponse<ArtistElement> {
  constructor(xml: Document) {
    super(xml, ArtistElement);
  }
}

export class GetArtistInfo2Response extends BaseGetArtistInfoResponse<ArtistID3Element> {
  constructor(xml: Document) {
    super(xml, ArtistID3Element);
  }
}

export class GetMusicDirectoryResponse {
  directory: DirectoryElement;
  children: ChildElement[] = [];

  constructor(xml: Document) {
    this.directory = new DirectoryElement(xml.getElementsByTagName('directory')[0]);

    const childElements = xml.getElementsByTagName('child');
    for (let i = 0; i < childElements.length; i++) {
      this.children.push(new ChildElement(childElements[i]));
    }
  }
}

export class GetAlbumResponse {
  album: AlbumID3Element;
  songs: ChildElement[] = [];

  constructor(xml: Document) {
    this.album = new AlbumID3Element(xml.getElementsByTagName('album')[0]);

    const childElements = xml.getElementsByTagName('song');
    for (let i = 0; i < childElements.length; i++) {
      this.songs.push(new ChildElement(childElements[i]));
    }
  }
}

// 
// Album/song lists
// 

class BaseGetAlbumListResponse<T> {
  albums: T[] = [];

  constructor(xml: Document, albumType: new (e: Element) => T) {
    const albumElements = xml.getElementsByTagName('album');
    for (let i = 0; i < albumElements.length; i++) {
      this.albums.push(new albumType(albumElements[i]));
    }
  }
}

export class GetAlbumListResponse extends BaseGetAlbumListResponse<ChildElement> {
  constructor(xml: Document) {
    super(xml, ChildElement);
  }
}

export class GetAlbumList2Response extends BaseGetAlbumListResponse<AlbumID3Element> {
  constructor(xml: Document) {
    super(xml, AlbumID3Element);
  }
}
