// 
// Browsing
// 

export type GetIndexesParams = {
  musicFolderId?: string;
  ifModifiedSince?: number;
}

export type GetArtistInfoParams = {
  id: string;
  count?: number;
  includeNotPresent?: boolean;
}

export type GetArtistInfo2Params = GetArtistInfoParams;

export type GetMusicDirectoryParams = {
  id: string;
}

export type GetAlbumParams = {
  id: string;
}


// 
// Album/song lists
// 

export type GetAlbumList2Type = 'random' | 'newest' | 'frequent' | 'recent' | 'starred' | 'alphabeticalByName' | 'alphabeticalByArtist';
export type GetAlbumListType = GetAlbumList2Type | ' highest';

export type GetAlbumList2TypeByYear = {
  type: 'byYear';
  fromYear: string;
  toYear: string;
}

export type GetAlbumList2TypeByGenre = {
  type: 'byGenre';
  genre: string;
}

export type GetAlbumList2Params = {
  type: GetAlbumList2Type;
  size?: number;
  offset?: number;
  fromYear?: string;
  toYear?: string;
  genre?: string;
  musicFolderId?: string;
} | GetAlbumList2TypeByYear | GetAlbumList2TypeByGenre;

export type GetAlbumListParams = GetAlbumList2Params;

// 
// Media retrieval
// 

export type GetCoverArtParams = {
  id: string;
  size?: string;
}

export type StreamParams = {
  id: string;
  maxBitRate?: number;
  format?: string;
  estimateContentLength?: boolean;
}
