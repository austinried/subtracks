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

export type DownloadedSong = {
  id: string;
  type: 'song';
  name: string;
  album: string;
  artist: string;
};

export type DownloadedAlbum = {
  id: string;
  type: 'album';
  songs: string[];
  name: string;
  artist: string;
};

export type DownloadedArtist = {
  id: string;
  type: 'artist';
  songs: string[];
  name: string;
};

export type DownloadedPlaylist = {
  id: string;
  type: 'playlist';
  songs: string[];
  name: string;
};