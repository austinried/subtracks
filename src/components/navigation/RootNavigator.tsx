import { StateNavigator } from 'navigation';
import { NavigationHandler } from 'navigation-react';
import { NavigationStack } from 'navigation-react-native';
import React from 'react';
import { View } from 'react-native';
import NowPlayingLayout from '../NowPlayingLayout';
import BottomTabNavigator from './BottomTabNavigator';

const stateNavigator = new StateNavigator([
  { key: 'main' },
  { key: 'nowplaying', trackCrumbTrail: true },
]);

const { main, nowplaying } = stateNavigator.states;
main.renderScene = () => <BottomTabNavigator />;
nowplaying.renderScene = () => <NowPlayingLayout />;

const RootNavigator = () => (
  <NavigationHandler stateNavigator={stateNavigator}>
    <NavigationStack />
  </NavigationHandler>
);

stateNavigator.navigate('main');

export default RootNavigator;
