import React from 'react';
import { View } from 'react-native';
import AlbumsTab from '../library/AlbumsTab';
import ArtistsTab from '../library/ArtistsTab';
import PlaylistsTab from '../library/PlaylistsTab';
import TopTabBar from '../common/TopTabBar';
import { StateNavigator } from 'navigation';
import { NavigationHandler } from 'navigation-react';
import { NavigationStack } from 'navigation-react-native';

const stateNavigator = new StateNavigator([
  { key: 'albums', title: 'Albums' },
  { key: 'artists', title: 'Artists', trackCrumbTrail: true, },
  { key: 'playlists', title: 'Playlists', trackCrumbTrail: true, },
]);

const { albums, artists, playlists } = stateNavigator.states;
albums.renderScene = AlbumsTab;
artists.renderScene = ArtistsTab;
playlists.renderScene = PlaylistsTab;

const LibraryTopTabNavigator = () => (
  <NavigationHandler stateNavigator={stateNavigator}>
    <TopTabBar />
    <NavigationStack
      unmountStyle={from => from ? 'slide_in' : 'slide_out'}
      crumbStyle={from => from ? 'slide_in' : 'slide_out'}
    />
  </NavigationHandler>
);

stateNavigator.navigate('albums');

export default LibraryTopTabNavigator;
