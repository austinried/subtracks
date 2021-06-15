export class BaseArtist {
  id: string;
  name: string;
  starred?: string;

  constructor(e: Element) {
    this.id = e.getAttribute('id') as string;
    this.name = e.getAttribute('name') as string;

    if (e.getAttribute('starred') !== null) {
      this.starred = e.getAttribute('starred') as string;
    }
  }
}

export class ArtistID3 extends BaseArtist {
  coverArt?: string;
  albumCount?: number;

  constructor(e: Element) {
    super(e);

    if (e.getAttribute('coverArt') !== null) {
      this.coverArt = e.getAttribute('coverArt') as string;
    }
    if (e.getAttribute('albumCount') !== null) {
      this.albumCount = parseInt(e.getAttribute('albumCount') as string);
    }
  }
}

export class Artist extends BaseArtist {
  userRating?: number;
  averageRating?: number;

  constructor(e: Element) {
    super(e);

    if (e.getAttribute('userRating') !== null) {
      this.userRating = parseInt(e.getAttribute('userRating') as string);
    }
    if (e.getAttribute('averageRating') !== null) {
      this.averageRating = parseFloat(e.getAttribute('averageRating') as string);
    }
  }
}
