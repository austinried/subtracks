import React from 'react';
import { Pressable, StatusBar, View } from 'react-native';
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';
import AlbumsTab from '../library/AlbumsTab';
import ArtistsTab from '../library/ArtistsTab';
import PlaylistsTab from '../library/PlaylistsTab';
import { createNativeStackNavigator, NativeStackNavigationProp } from 'react-native-screens/native-stack';
import AlbumView from '../common/AlbumView';
import { RouteProp } from '@react-navigation/native';
import text from '../../styles/text';
import colors from '../../styles/colors';
import ArtistView from '../common/ArtistView';
import FastImage from 'react-native-fast-image';

const Tab = createMaterialTopTabNavigator();

const LibraryTopTabNavigator = () => (
  <Tab.Navigator
    tabBarOptions={{
      style: {
        height: 48,
        backgroundColor: colors.gradient.high,
        elevation: 0,
        marginTop: StatusBar.currentHeight,
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
    <Tab.Screen name="Albums" component={AlbumsTab} />
    <Tab.Screen name="Artists" component={ArtistsTab} />
    <Tab.Screen name="Playlists" component={PlaylistsTab} />
  </Tab.Navigator>
);

type LibraryStackParamList = {
  LibraryTopTabs: undefined;
  AlbumView: { id: string; title: string };
  ArtistView: { id: string; title: string };
};

type AlbumScreenNavigationProp = NativeStackNavigationProp<LibraryStackParamList, 'AlbumView'>;
type AlbumScreenRouteProp = RouteProp<LibraryStackParamList, 'AlbumView'>;
type AlbumScreenProps = {
  route: AlbumScreenRouteProp;
  navigation: AlbumScreenNavigationProp;
};

const AlbumScreen: React.FC<AlbumScreenProps> = ({ route }) => (
  <AlbumView id={route.params.id} title={route.params.title} />
);

type ArtistScreenNavigationProp = NativeStackNavigationProp<LibraryStackParamList, 'ArtistView'>;
type ArtistScreenRouteProp = RouteProp<LibraryStackParamList, 'ArtistView'>;
type ArtistScreenProps = {
  route: ArtistScreenRouteProp;
  navigation: ArtistScreenNavigationProp;
};

const ArtistScreen: React.FC<ArtistScreenProps> = ({ route }) => (
  <ArtistView id={route.params.id} title={route.params.title} />
);

const Stack = createNativeStackNavigator<LibraryStackParamList>();

const itemScreenOptions = {
  title: '',
  headerStyle: {
    height: 500,
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
    color: colors.text.primary,
  },
  // headerBackImage: () => (
  //   <FastImage
  //     source={require('../../../res/arrow_left-fill.png')}
  //     tintColor={colors.text.primary}
  //     style={{ height: 22, width: 22 }}
  //   />
  // ),
};

const LibraryStackNavigator = () => (
  <View style={{ flex: 1 }}>
    <Stack.Navigator>
      <Stack.Screen name="LibraryTopTabs" component={LibraryTopTabNavigator} options={{ headerShown: false }} />
      <Stack.Screen
        name="AlbumView"
        component={AlbumScreen}
        options={{
          title: '',
          headerStyle: {
            // height: 500,
            backgroundColor: colors.gradient.high,
          },
          headerHideShadow: true,
          // headerTitleContainerStyle: {
          //   marginLeft: -14,
          // },
          // headerLeftContainerStyle: {
          //   marginLeft: 8,
          // },
          // headerTitleStyle: {
          //   ...text.header,
          //   color: colors.text.primary,
          // },
          headerLeft: () => (
            <Pressable>
              <FastImage
                source={require('../../../res/arrow_left-fill.png')}
                tintColor="white"
                style={{ height: 24, width: 24 }}
              />
            </Pressable>
          ),
          // headerBackImage: () => (
          //   <FastImage
          //     source={require('../../../res/arrow_left-fill.png')}
          //     tintColor={colors.text.primary}
          //     style={{ height: 22, width: 22 }}
          //   />
          // ),
        }}
      />
      <Stack.Screen name="ArtistView" component={ArtistScreen} options={itemScreenOptions} />
    </Stack.Navigator>
  </View>
);

export default LibraryStackNavigator;
