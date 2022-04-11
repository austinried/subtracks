function requiredString(e: Element, name: string): string {
  return e.getAttribute(name) as string
}

function optionalString(e: Element, name: string): string | undefined {
  return e.hasAttribute(name) ? requiredString(e, name) : undefined
}

function requiredBoolean(e: Element, name: string): boolean {
  return (e.getAttribute(name) as string).toLowerCase() === 'true'
}

function optionalBoolean(e: Element, name: string): boolean | undefined {
  return e.hasAttribute(name) ? requiredBoolean(e, name) : undefined
}

function requiredInt(e: Element, name: string): number {
  return parseInt(e.getAttribute(name) as string, 10)
}

function optionalInt(e: Element, name: string): number | undefined {
  return e.hasAttribute(name) ? requiredInt(e, name) : undefined
}

function requiredFloat(e: Element, name: string): number {
  return parseFloat(e.getAttribute(name) as string)
}

function optionalFloat(e: Element, name: string): number | undefined {
  return e.hasAttribute(name) ? requiredFloat(e, name) : undefined
}

function requiredDate(e: Element, name: string): Date {
  return new Date(e.getAttribute(name) as string)
}

function optionalDate(e: Element, name: string): Date | undefined {
  return e.hasAttribute(name) ? requiredDate(e, name) : undefined
}

export class BaseArtistElement {
  id: string
  name: string
  starred?: Date

  constructor(e: Element) {
    this.id = requiredString(e, 'id')
    this.name = requiredString(e, 'name')
    this.starred = optionalDate(e, 'starred')
  }
}

export class ArtistID3Element extends BaseArtistElement {
  coverArt?: string
  albumCount?: number

  constructor(e: Element) {
    super(e)
    this.coverArt = optionalString(e, 'coverArt')
    this.albumCount = optionalInt(e, 'albumCount')
  }
}

export class ArtistElement extends BaseArtistElement {
  userRating?: number
  averageRating?: number

  constructor(e: Element) {
    super(e)
    this.userRating = optionalInt(e, 'userRating')
    this.averageRating = optionalFloat(e, 'averageRating')
  }
}

export class BaseArtistInfoElement<T> {
  similarArtists: T[] = []
  biography?: string
  musicBrainzId?: string
  lastFmUrl?: string
  smallImageUrl?: string
  mediumImageUrl?: string
  largeImageUrl?: string

  constructor(e: Element, artistType: new (e: Element) => T) {
    if (e.getElementsByTagName('biography').length > 0) {
      this.biography = e.getElementsByTagName('biography')[0].textContent as string
    }
    if (e.getElementsByTagName('musicBrainzId').length > 0) {
      this.musicBrainzId = e.getElementsByTagName('musicBrainzId')[0].textContent as string
    }
    if (e.getElementsByTagName('lastFmUrl').length > 0) {
      this.lastFmUrl = e.getElementsByTagName('lastFmUrl')[0].textContent as string
    }
    if (e.getElementsByTagName('smallImageUrl').length > 0) {
      this.smallImageUrl = e.getElementsByTagName('smallImageUrl')[0].textContent as string
    }
    if (e.getElementsByTagName('mediumImageUrl').length > 0) {
      this.mediumImageUrl = e.getElementsByTagName('mediumImageUrl')[0].textContent as string
    }
    if (e.getElementsByTagName('largeImageUrl').length > 0) {
      this.largeImageUrl = e.getElementsByTagName('largeImageUrl')[0].textContent as string
    }

    this.similarArtists = Array.from(e.getElementsByTagName('similarArtist')).map(i => new artistType(i))
  }
}

export class ArtistInfoElement extends BaseArtistInfoElement<ArtistElement> {
  constructor(e: Element) {
    super(e, ArtistElement)
  }
}
export class ArtistInfo2Element extends BaseArtistInfoElement<ArtistID3Element> {
  constructor(e: Element) {
    super(e, ArtistID3Element)
  }
}

export class DirectoryElement {
  id: string
  parent?: string
  name: string
  starred?: Date
  userRating?: number
  averageRating?: number
  playCount?: number

  constructor(e: Element) {
    this.id = requiredString(e, 'id')
    this.parent = optionalString(e, 'parent')
    this.name = requiredString(e, 'name')
    this.starred = optionalDate(e, 'starred')
    this.userRating = optionalInt(e, 'userRating')
    this.averageRating = optionalFloat(e, 'averageRating')
  }
}

export class ChildElement {
  id: string
  parent?: string
  isDir: boolean
  title: string
  album?: string
  artist?: string
  track?: number
  year?: number
  genre?: string
  coverArt?: string
  size?: number
  contentType?: string
  suffix?: string
  transcodedContentType?: string
  transcodedSuffix?: string
  duration?: number
  bitRate?: number
  path?: string
  isVideo?: boolean
  userRating?: number
  averageRating?: number
  playCount?: number
  discNumber?: number
  created?: Date
  starred?: Date
  albumId?: string
  artistId?: string
  type?: string
  bookmarkPosition?: number
  originalWidth?: number
  originalHeight?: number

  constructor(e: Element) {
    this.id = requiredString(e, 'id')
    this.parent = optionalString(e, 'parent')
    this.isDir = requiredBoolean(e, 'isDir')
    this.title = requiredString(e, 'title')
    this.album = optionalString(e, 'album')
    this.artist = optionalString(e, 'artist')
    this.track = optionalInt(e, 'track')
    this.year = optionalInt(e, 'year')
    this.genre = optionalString(e, 'genre')
    this.coverArt = optionalString(e, 'coverArt')
    this.size = optionalInt(e, 'size')
    this.contentType = optionalString(e, 'contentType')
    this.suffix = optionalString(e, 'suffix')
    this.transcodedContentType = optionalString(e, 'transcodedContentType')
    this.transcodedSuffix = optionalString(e, 'transcodedSuffix')
    this.duration = optionalInt(e, 'duration')
    this.bitRate = optionalInt(e, 'bitRate')
    this.path = optionalString(e, 'path')
    this.isVideo = optionalBoolean(e, 'isVideo')
    this.userRating = optionalInt(e, 'userRating')
    this.averageRating = optionalFloat(e, 'averageRating')
    this.playCount = optionalInt(e, 'playCount')
    this.discNumber = optionalInt(e, 'discNumber')
    this.created = optionalDate(e, 'created')
    this.starred = optionalDate(e, 'starred')
    this.albumId = optionalString(e, 'albumId')
    this.artistId = optionalString(e, 'artistId')
    this.type = optionalString(e, 'type')
    this.bookmarkPosition = optionalInt(e, 'bookmarkPosition')
    this.originalWidth = optionalInt(e, 'originalWidth')
    this.originalHeight = optionalInt(e, 'originalHeight')
  }
}

export class AlbumID3Element {
  id: string
  name: string
  artist?: string
  artistId?: string
  coverArt?: string
  songCount: number
  duration: number
  playCount?: number
  created: Date
  starred?: Date
  year?: number
  genre?: string

  constructor(e: Element) {
    this.id = requiredString(e, 'id')
    this.name = requiredString(e, 'name')
    this.artist = optionalString(e, 'artist')
    this.artistId = optionalString(e, 'artistId')
    this.coverArt = optionalString(e, 'coverArt')
    this.songCount = requiredInt(e, 'songCount')
    this.duration = requiredInt(e, 'duration')
    this.playCount = optionalInt(e, 'playCount')
    this.created = requiredDate(e, 'created')
    this.starred = optionalDate(e, 'starred')
    this.year = optionalInt(e, 'year')
    this.genre = optionalString(e, 'genre')
  }
}

export class PlaylistElement {
  allowedUser: string[] = []
  id: string
  name: string
  comment?: string
  owner?: string
  public?: boolean
  songCount: number
  duration: number
  created: Date
  changed: Date
  coverArt?: string

  constructor(e: Element) {
    this.allowedUser = Array.from(e.getElementsByTagName('allowedUser')).map(i => i.textContent as string)

    this.id = requiredString(e, 'id')
    this.name = requiredString(e, 'name')
    this.comment = optionalString(e, 'comment')
    this.owner = optionalString(e, 'owner')
    this.public = optionalBoolean(e, 'public')
    this.songCount = requiredInt(e, 'songCount')
    this.duration = requiredInt(e, 'duration')
    this.created = requiredDate(e, 'created')
    this.changed = requiredDate(e, 'changed')
    this.coverArt = optionalString(e, 'coverArt')
  }
}

export class PlaylistWithSongsElement extends PlaylistElement {
  songs: ChildElement[] = []

  constructor(e: Element) {
    super(e)

    this.songs = Array.from(e.getElementsByTagName('entry')).map(i => new ChildElement(i))
  }
}
