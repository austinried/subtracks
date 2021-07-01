import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import SettingsView from '../Settings';
import NowPlayingLayout from '../NowPlayingLayout';
import ArtistsList from '../ArtistsList';
import LibraryTopTabNavigator from './LibraryTopTabNavigator';
import BottomTabBar from '../common/BottomTabBar';

const Tab = createBottomTabNavigator();

const BottomTabNavigator = () => {
  return (
    <Tab.Navigator tabBar={BottomTabBar}>
      <Tab.Screen name="Home" component={ArtistsList} options={{ icon: 'home' } as any} />
      <Tab.Screen name="Library" component={LibraryTopTabNavigator} options={{ icon: 'library' } as any} />
      <Tab.Screen name="Search" component={NowPlayingLayout} options={{ icon: 'search' } as any} />
      <Tab.Screen name="Settings" component={SettingsView} options={{ icon: 'settings' } as any} />
    </Tab.Navigator>
  );
};

export default BottomTabNavigator;
