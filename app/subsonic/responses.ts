import {
  AlbumID3Element,
  ArtistElement,
  ArtistID3Element,
  ArtistInfo2Element,
  ArtistInfoElement,
  ChildElement,
  DirectoryElement,
  PlaylistElement,
  PlaylistWithSongsElement,
} from '@app/subsonic/elements'

export type ResponseStatus = 'ok' | 'failed'

export class SubsonicResponse {
  status: ResponseStatus
  version: string

  constructor(xml: Document) {
    this.status = xml.documentElement.getAttribute('status') as ResponseStatus
    this.version = xml.documentElement.getAttribute('version') as string
  }
}

export class NullResponse extends SubsonicResponse {
  data = null
}

//
// Browsing
//

export class GetArtistsResponse extends SubsonicResponse {
  data: {
    ignoredArticles: string
    artists: ArtistID3Element[]
  }

  constructor(xml: Document) {
    super(xml)

    this.data = {
      ignoredArticles: xml.getElementsByTagName('artists')[0].getAttribute('ignoredArticles') || '',
      artists: Array.from(xml.getElementsByTagName('artist')).map(i => new ArtistID3Element(i)),
    }
  }
}

export class GetArtistResponse extends SubsonicResponse {
  data: {
    artist: ArtistID3Element
    albums: AlbumID3Element[]
  }

  constructor(xml: Document) {
    super(xml)

    this.data = {
      artist: new ArtistID3Element(xml.getElementsByTagName('artist')[0]),
      albums: Array.from(xml.getElementsByTagName('album')).map(i => new AlbumID3Element(i)),
    }
  }
}

export class GetIndexesResponse extends SubsonicResponse {
  data: {
    ignoredArticles: string
    lastModified: number
    artists: ArtistElement[]
  }

  constructor(xml: Document) {
    super(xml)

    const indexesElement = xml.getElementsByTagName('indexes')[0]

    this.data = {
      ignoredArticles: indexesElement.getAttribute('ignoredArticles') || '',
      lastModified: parseInt(indexesElement.getAttribute('lastModified') || '0', 10),
      artists: Array.from(xml.getElementsByTagName('artist')).map(i => new ArtistElement(i)),
    }
  }
}

export class GetArtistInfoResponse extends SubsonicResponse {
  data: {
    artistInfo: ArtistInfoElement
  }

  constructor(xml: Document) {
    super(xml)

    this.data = {
      artistInfo: new ArtistInfoElement(xml.getElementsByTagName('artistInfo')[0]),
    }
  }
}

export class GetArtistInfo2Response extends SubsonicResponse {
  data: {
    artistInfo: ArtistInfo2Element
  }

  constructor(xml: Document) {
    super(xml)

    this.data = {
      artistInfo: new ArtistInfo2Element(xml.getElementsByTagName('artistInfo2')[0]),
    }
  }
}

export class GetMusicDirectoryResponse extends SubsonicResponse {
  data: {
    directory: DirectoryElement
    children: ChildElement[]
  }

  constructor(xml: Document) {
    super(xml)

    this.data = {
      directory: new DirectoryElement(xml.getElementsByTagName('directory')[0]),
      children: Array.from(xml.getElementsByTagName('child')).map(i => new ChildElement(i)),
    }
  }
}

export class GetAlbumResponse extends SubsonicResponse {
  data: {
    album: AlbumID3Element
    songs: ChildElement[]
  }

  constructor(xml: Document) {
    super(xml)

    this.data = {
      album: new AlbumID3Element(xml.getElementsByTagName('album')[0]),
      songs: Array.from(xml.getElementsByTagName('song')).map(i => new ChildElement(i)),
    }
  }
}

export class GetTopSongsResponse extends SubsonicResponse {
  data: {
    songs: ChildElement[]
  }

  constructor(xml: Document) {
    super(xml)

    this.data = {
      songs: Array.from(xml.getElementsByTagName('song')).map(i => new ChildElement(i)),
    }
  }
}

export class GetSongResponse extends SubsonicResponse {
  data: {
    song: ChildElement
  }

  constructor(xml: Document) {
    super(xml)

    this.data = {
      song: new ChildElement(xml.getElementsByTagName('song')[0]),
    }
  }
}

//
// Album/song lists
//

class BaseGetAlbumListResponse<T> extends SubsonicResponse {
  data: {
    albums: T[]
  }

  constructor(xml: Document, AlbumType: new (e: Element) => T) {
    super(xml)

    this.data = {
      albums: Array.from(xml.getElementsByTagName('album')).map(i => new AlbumType(i)),
    }
  }
}

export class GetAlbumListResponse extends BaseGetAlbumListResponse<ChildElement> {
  constructor(xml: Document) {
    super(xml, ChildElement)
  }
}

export class GetAlbumList2Response extends BaseGetAlbumListResponse<AlbumID3Element> {
  constructor(xml: Document) {
    super(xml, AlbumID3Element)
  }
}

//
// Playlists
//

export class GetPlaylistsResponse extends SubsonicResponse {
  data: {
    playlists: PlaylistElement[]
  }

  constructor(xml: Document) {
    super(xml)

    this.data = {
      playlists: Array.from(xml.getElementsByTagName('playlist')).map(i => new PlaylistElement(i)),
    }
  }
}

export class GetPlaylistResponse extends SubsonicResponse {
  data: {
    playlist: PlaylistWithSongsElement
  }

  constructor(xml: Document) {
    super(xml)

    this.data = {
      playlist: new PlaylistWithSongsElement(xml.getElementsByTagName('playlist')[0]),
    }
  }
}

//
// Searching
//

export class Search3Response extends SubsonicResponse {
  data: {
    artists: ArtistID3Element[]
    albums: AlbumID3Element[]
    songs: ChildElement[]
  }

  constructor(xml: Document) {
    super(xml)

    this.data = {
      artists: Array.from(xml.getElementsByTagName('artist')).map(i => new ArtistID3Element(i)),
      albums: Array.from(xml.getElementsByTagName('album')).map(i => new AlbumID3Element(i)),
      songs: Array.from(xml.getElementsByTagName('song')).map(i => new ChildElement(i)),
    }
  }
}
