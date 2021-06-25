import React from 'react';
import { View } from 'react-native';
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';
import AlbumsTab from '../library/AlbumsTab';
import ArtistsTab from '../library/ArtistsTab';
import PlaylistsTab from '../library/PlaylistsTab';
import TopTabBar from '../common/TopTabBar';

const Tab = createMaterialTopTabNavigator();

const LibraryTopTabNavigator = () => (
  <View style={{
    flex: 1,
  }}>
    <Tab.Navigator tabBar={TopTabBar}>
      <Tab.Screen
        name='Albums'
        component={AlbumsTab}
      />
      <Tab.Screen
        name='Artists'
        component={ArtistsTab}
      />
      <Tab.Screen
        name='Playlists'
        component={PlaylistsTab}
      />
    </Tab.Navigator>
  </View>
);

export default LibraryTopTabNavigator;
