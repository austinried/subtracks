export interface Artist {
  id: string;
  name: string;
  starred?: Date;
  coverArt?: string;
  coverArtUri?: string,
}

export interface Album {
  id: string;
  artistId?: string;
  artist?: string;
  name: string;
  starred?: Date;
  coverArt?: string;
  coverArtUri?: string,
  coverArtThumbUri?: string,
}

export interface Song {
  id: string;
  albumId: string;
  artistId: string;
  name: string;
  starred?: Date;
}
