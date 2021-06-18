import React from 'react';
import { createStackNavigator } from '@react-navigation/stack';
import TabNavigator from './TabNavigator';
import NowPlayingLayout from './NowPlayingLayout';

const RootStack = createStackNavigator();

const RootNavigator = () => (
  <RootStack.Navigator>
    <RootStack.Screen
      name='Main'
      component={TabNavigator}
      options={{ headerShown: false }}
    />
    <RootStack.Screen
      name='Now Playing'
      component={NowPlayingLayout}
      options={{ headerShown: false }}
    />
  </RootStack.Navigator>
);

export default RootNavigator;
