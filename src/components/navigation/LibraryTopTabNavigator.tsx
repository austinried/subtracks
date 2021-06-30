import React from 'react';
import { View } from 'react-native';
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';
import AlbumsTab from '../library/AlbumsTab';
import ArtistsTab from '../library/ArtistsTab';
import PlaylistsTab from '../library/PlaylistsTab';
import { createStackNavigator, StackNavigationProp } from '@react-navigation/stack';
import AlbumView from '../common/AlbumView';
import { RouteProp } from '@react-navigation/native';
import text from '../../styles/text';
import colors from '../../styles/colors';
import FastImage from 'react-native-fast-image';
import ArtistView from '../common/ArtistView';

const Tab = createMaterialTopTabNavigator();

const LibraryTopTabNavigator = () => (
  <Tab.Navigator tabBarOptions={{
    style: {
      height: 48,
      backgroundColor: colors.gradient.high,
      elevation: 0,
    },
    labelStyle: {
      ...text.header,
      textTransform: null as any,
      marginTop: 0,
      marginHorizontal: 2,
    },
    indicatorStyle: {
      backgroundColor: colors.accent,
    },
  }}>
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
);

type LibraryStackParamList = {
  LibraryTopTabs: undefined,
  AlbumView: { id: string, title: string };
  ArtistView: { id: string, title: string };
}

type AlbumScreenNavigationProp = StackNavigationProp<LibraryStackParamList, 'AlbumView'>;
type AlbumScreenRouteProp = RouteProp<LibraryStackParamList, 'AlbumView'>;
type AlbumScreenProps = {
  route: AlbumScreenRouteProp,
  navigation: AlbumScreenNavigationProp,
};

const AlbumScreen: React.FC<AlbumScreenProps> = ({ route }) => (
  <AlbumView id={route.params.id} title={route.params.title} />
);

type ArtistScreenNavigationProp = StackNavigationProp<LibraryStackParamList, 'ArtistView'>;
type ArtistScreenRouteProp = RouteProp<LibraryStackParamList, 'ArtistView'>;
type ArtistScreenProps = {
  route: ArtistScreenRouteProp,
  navigation: ArtistScreenNavigationProp,
};

const ArtistScreen: React.FC<ArtistScreenProps> = ({ route }) => (
  <ArtistView id={route.params.id} title={route.params.title} />
);

const Stack = createStackNavigator<LibraryStackParamList>();

const itemScreenOptions = {
  title: '',
  headerStyle: { 
    height: 50,
    backgroundColor: colors.gradient.high,
  }, 
  headerTitleContainerStyle: {
    marginLeft: -14,
  },
  headerLeftContainerStyle: {
    marginLeft: 8,
  },
  headerTitleStyle: {
    ...text.header,
  },
  headerBackImage: () => <FastImage
    source={require('../../../res/arrow_left-fill.png')}
    tintColor={colors.text.primary}
    style={{ height: 22, width: 22 }}
  />,
}

const LibraryStackNavigator = () => (
  <View style={{ flex: 1 }}>
    <Stack.Navigator>
      <Stack.Screen
        name='LibraryTopTabs'
        component={LibraryTopTabNavigator}
        options={{ headerShown: false }}
      />
      <Stack.Screen
        name='AlbumView'
        component={AlbumScreen}
        options={itemScreenOptions}
      />
      <Stack.Screen
        name='ArtistView'
        component={ArtistScreen}
        options={itemScreenOptions}
      />
    </Stack.Navigator>
  </View>
);

export default LibraryStackNavigator;
