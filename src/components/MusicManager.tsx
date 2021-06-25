import React, { useEffect, useState } from 'react';
import { ActivityIndicator, useWindowDimensions, View } from 'react-native';
import { useSetRecoilState, useRecoilValue, useRecoilState } from 'recoil';
import { Album, Artist, Song } from '../models/music';
import { albumsState, artistsState, isLibraryRefreshingState, libraryRefreshState, songsState } from '../state/music';
import { activeServer } from '../state/settings';
import colors from '../styles/colors';
import { SubsonicApiClient } from '../subsonic/api';

const RefreshManager = () => {
  const setArtists = useSetRecoilState(artistsState);
  const setAlbums = useSetRecoilState(albumsState);
  const setSongs = useSetRecoilState(songsState);

  const server = useRecoilValue(activeServer);

  const [libraryRefresh, setLibraryRefresh] = useRecoilState(libraryRefreshState);
  const [isLibraryRefreshing, setIsLibraryRefreshing] = useRecoilState(isLibraryRefreshingState);

  const updateLibrary = async () => {
    if (!libraryRefresh) {
      return;
    }
    setLibraryRefresh(false);
    
    if (isLibraryRefreshing) {
      return;
    }
    setIsLibraryRefreshing(true);

    if (!server) {
      return;
    }
    const client = new SubsonicApiClient(server);

    const artistsResponse = await client.getArtists();
    const artists: Artist[] = artistsResponse.data.artists.map(x => ({
      id: x.id,
      name: x.name,
      starred: x.starred,
      coverArt: x.coverArt,
    }));
    setArtists(artists);

    const albumsResponse = await client.getAlbumList2({ type: 'alphabeticalByArtist', size: 500 });
    const albums: Album[] = albumsResponse.data.albums
      .filter(x => x.artistId !== undefined)
      .map(x => ({
        id: x.id,
        artistId: x.artistId as string,
        name: x.name,
        coverArt: x.coverArt,
    }));
    setAlbums(albums);

    const songs: Song[] = [];
    for (const album of albums) {
      const songsResponse = await client.getAlbum({ id: album.id });
      const albumSongs: Song[] = songsResponse.data.songs.map(x => ({
        id: x.id,
        albumId: album.id,
        artistId: album.artistId,
        name: x.title,
        starred: x.starred,
      }));
      songs.push(...albumSongs);
    }
    setSongs(songs);

    setIsLibraryRefreshing(false);
  }

  useEffect(() => {
    updateLibrary();
  });

  return <></>;
}

const MusicManager = () => {
  const isLibraryRefreshing = useRecoilValue(isLibraryRefreshingState);
  const layout = useWindowDimensions();

  const RefreshIndicator = () => (
    <ActivityIndicator size={'large'} color={colors.accent} style={{
      backgroundColor: colors.accentLow,
      position: 'absolute',
      left: layout.width / 2 - 18,
      top: layout.height / 2 - 18,
      elevation: 999,
    }}/>
  );

  return (
    <View>
      {isLibraryRefreshing ? <RefreshIndicator /> : <></>}
      <React.Suspense fallback={<></>}>
        <RefreshManager />
      </React.Suspense>
    </View>
  )
}

export default MusicManager;
