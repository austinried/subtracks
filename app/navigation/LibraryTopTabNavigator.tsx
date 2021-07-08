import React from 'react'
import { StatusBar, StyleSheet, View } from 'react-native'
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs'
import AlbumsTab from '@app/screens/LibraryAlbums'
import ArtistsTab from '@app/screens/LibraryArtists'
import PlaylistsTab from '@app/screens/LibraryPlaylists'
import { createNativeStackNavigator, NativeStackNavigationProp } from 'react-native-screens/native-stack'
import AlbumView from '@app/screens/AlbumView'
import { RouteProp } from '@react-navigation/native'
import font from '@app/styles/font'
import colors from '@app/styles/colors'
import ArtistView from '@app/screens/ArtistView'
import dimensions from '@app/styles/dimensions'

type LibraryStackParamList = {
  LibraryTopTabs: undefined
  AlbumView: { id: string; title: string }
  ArtistView: { id: string; title: string }
}

type AlbumScreenNavigationProp = NativeStackNavigationProp<LibraryStackParamList, 'AlbumView'>
type AlbumScreenRouteProp = RouteProp<LibraryStackParamList, 'AlbumView'>
type AlbumScreenProps = {
  route: AlbumScreenRouteProp
  navigation: AlbumScreenNavigationProp
}

const AlbumScreen: React.FC<AlbumScreenProps> = ({ route }) => (
  <AlbumView id={route.params.id} title={route.params.title} />
)

type ArtistScreenNavigationProp = NativeStackNavigationProp<LibraryStackParamList, 'ArtistView'>
type ArtistScreenRouteProp = RouteProp<LibraryStackParamList, 'ArtistView'>
type ArtistScreenProps = {
  route: ArtistScreenRouteProp
  navigation: ArtistScreenNavigationProp
}

const ArtistScreen: React.FC<ArtistScreenProps> = ({ route }) => (
  <ArtistView id={route.params.id} title={route.params.title} />
)

const Tab = createMaterialTopTabNavigator()

const LibraryTopTabNavigator = () => (
  <Tab.Navigator
    tabBarOptions={{
      style: styles.tabBar,
      labelStyle: styles.tablabelStyle,
      indicatorStyle: styles.tabindicatorStyle,
    }}>
    <Tab.Screen name="Albums" component={AlbumsTab} />
    <Tab.Screen name="Artists" component={ArtistsTab} />
    <Tab.Screen name="Playlists" component={PlaylistsTab} />
  </Tab.Navigator>
)

const Stack = createNativeStackNavigator<LibraryStackParamList>()

const LibraryStackNavigator = () => {
  const itemScreenOptions = {
    title: '',
    headerStyle: styles.stackheaderStyle,
    headerHideShadow: true,
    headerTintColor: 'white',
    headerTitleStyle: styles.stackheaderTitleStyle,
  }

  return (
    <View style={styles.stackContainer}>
      <Stack.Navigator>
        <Stack.Screen name="LibraryTopTabs" component={LibraryTopTabNavigator} options={{ headerShown: false }} />
        <Stack.Screen name="AlbumView" component={AlbumScreen} options={itemScreenOptions} />
        <Stack.Screen name="ArtistView" component={ArtistScreen} options={itemScreenOptions} />
      </Stack.Navigator>
    </View>
  )
}

const styles = StyleSheet.create({
  stackContainer: {
    flex: 1,
  },
  stackheaderStyle: {
    backgroundColor: colors.gradient.high,
  },
  stackheaderTitleStyle: {
    fontSize: 18,
    fontFamily: font.semiBold,
    color: colors.text.primary,
  },
  tabBar: {
    height: dimensions.header,
    marginTop: StatusBar.currentHeight,
    backgroundColor: colors.gradient.high,
    elevation: 0,
    justifyContent: 'center',
  },
  tablabelStyle: {
    fontSize: 18,
    fontFamily: font.semiBold,
    color: colors.text.primary,
    textTransform: null as any,
    marginTop: 0,
    marginHorizontal: 2,
  },
  tabindicatorStyle: {
    backgroundColor: colors.text.primary,
  },
})

export default LibraryStackNavigator
