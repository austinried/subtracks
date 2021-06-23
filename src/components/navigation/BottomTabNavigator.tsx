import React from 'react';
import SettingsView from '../Settings';
import NowPlayingLayout from '../NowPlayingLayout';
import ArtistsList from '../ArtistsList';
import LibraryTopTabNavigator from './LibraryTopTabNavigator';
import BottomTabBar from '../common/BottomTabBar';
import { NavigationHandler } from 'navigation-react';
import { NavigationStack } from 'navigation-react-native';
import { StateNavigator } from 'navigation';

const stateNavigator = new StateNavigator([
  { key: 'home', title: 'Home' },
  { key: 'library', title: 'Library', trackCrumbTrail: true },
  { key: 'search', title: 'Search', trackCrumbTrail: true },
  { key: 'settings', title: 'Settings', trackCrumbTrail: true },
]);

const { home, library, search, settings } = stateNavigator.states;
home.renderScene = ArtistsList;
library.renderScene = LibraryTopTabNavigator;
search.renderScene = NowPlayingLayout;
settings.renderScene = SettingsView;

const BottomTabNavigator = () => (
  <NavigationHandler stateNavigator={stateNavigator}>
    <NavigationStack
      unmountStyle={from => from ? 'fade_in' : 'fade_out'}
      crumbStyle={from => from ? 'fade_in' : 'fade_out'}
    />
    <BottomTabBar />
  </NavigationHandler>
);

stateNavigator.navigate('home');

export default BottomTabNavigator;
