export interface Artist {
  id: string;
  name: string;
  starred?: Date;
  coverArt?: string;
}

export interface Album {
  id: string;
  name: string;
  starred?: Date;
  coverArt?: string;
}
