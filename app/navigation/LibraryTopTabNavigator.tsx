import { withSuspense } from '@app/components/withSuspense'
import AlbumsTab from '@app/screens/LibraryAlbums'
import ArtistsTab from '@app/screens/LibraryArtists'
import PlaylistsTab from '@app/screens/LibraryPlaylists'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import font from '@app/styles/font'
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs'
import React from 'react'
import { useTranslation } from 'react-i18next'
import { StyleSheet } from 'react-native'
import { useSafeAreaInsets } from 'react-native-safe-area-context'

const Tab = createMaterialTopTabNavigator()

const LibraryTopTabNavigator = withSuspense(() => {
  const { t } = useTranslation()
  const marginTop = useSafeAreaInsets().top

  return (
    <Tab.Navigator
      tabBarOptions={{
        style: [styles.tabBar, { marginTop }],
        labelStyle: styles.tablabelStyle,
        indicatorStyle: styles.tabindicatorStyle,
      }}
      initialRouteName="albums">
      <Tab.Screen
        name="albums"
        component={AlbumsTab}
        options={{ tabBarLabel: t('resources.album.name', { count: 2 }) }}
      />
      <Tab.Screen
        name="artists"
        component={ArtistsTab}
        options={{ tabBarLabel: t('resources.artist.name', { count: 2 }) }}
      />
      <Tab.Screen
        name="playlists"
        component={PlaylistsTab}
        options={{ tabBarLabel: t('resources.playlist.name', { count: 2 }) }}
      />
    </Tab.Navigator>
  )
})

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
