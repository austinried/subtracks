import AlbumsTab from '@app/screens/LibraryAlbums'
import ArtistsTab from '@app/screens/LibraryArtists'
import PlaylistsTab from '@app/screens/LibraryPlaylists'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import font from '@app/styles/font'
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs'
import React from 'react'
import { StyleSheet } from 'react-native'
import { useSafeAreaInsets } from 'react-native-safe-area-context'

const Tab = createMaterialTopTabNavigator()

const LibraryTopTabNavigator = () => {
  const marginTop = useSafeAreaInsets().top

  return (
    <Tab.Navigator
      tabBarOptions={{
        style: [styles.tabBar, { marginTop }],
        labelStyle: styles.tablabelStyle,
        indicatorStyle: styles.tabindicatorStyle,
      }}
      initialRouteName="albums">
      <Tab.Screen name="albums" component={AlbumsTab} options={{ tabBarLabel: 'Albums' }} />
      <Tab.Screen name="artists" component={ArtistsTab} options={{ tabBarLabel: 'Artists' }} />
      <Tab.Screen name="playlists" component={PlaylistsTab} options={{ tabBarLabel: 'Playlists' }} />
    </Tab.Navigator>
  )
}

const styles = StyleSheet.create({
  tabBar: {
    height: dimensions.header,
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

export default LibraryTopTabNavigator
