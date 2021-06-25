export interface Artist {
  id: string;
  name: string;
  starred?: Date;
  coverArt?: string;
}

export interface Album {
  id: string;
  artistId: string;
  name: string;
  starred?: Date;
  coverArt?: string;
}

export interface Song {
  id: string;
  albumId: string;
  artistId: string;
  name: string;
  starred?: Date;
}
