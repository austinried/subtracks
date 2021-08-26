import BottomTabBar from '@app/navigation/BottomTabBar'
import LibraryTopTabNavigator from '@app/navigation/LibraryTopTabNavigator'
import ArtistView from '@app/screens/ArtistView'
import Home from '@app/screens/Home'
import Search from '@app/screens/Search'
import SearchResultsView from '@app/screens/SearchResultsView'
import ServerView from '@app/screens/ServerView'
import SettingsView from '@app/screens/Settings'
import SongListView from '@app/screens/SongListView'
import WebViewScreen from '@app/screens/WebViewScreen'
import { selectSettings } from '@app/state/settings'
import { useStore } from '@app/state/store'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { BottomTabNavigationProp, createBottomTabNavigator } from '@react-navigation/bottom-tabs'
import { RouteProp, StackActions } from '@react-navigation/native'
import React, { useEffect } from 'react'
import { StyleSheet } from 'react-native'
import { createNativeStackNavigator, NativeStackNavigationProp } from 'react-native-screens/native-stack'

type TabStackParamList = {
  main: undefined
  album: { id: string; title: string }
  artist: { id: string; title: string }
  playlist: { id: string; title: string }
  results: { query: string; type: 'album' | 'song' | 'artist' }
}

type AlbumScreenNavigationProp = NativeStackNavigationProp<TabStackParamList, 'album'>
type AlbumScreenRouteProp = RouteProp<TabStackParamList, 'album'>
type AlbumScreenProps = {
  route: AlbumScreenRouteProp
  navigation: AlbumScreenNavigationProp
}

const AlbumScreen: React.FC<AlbumScreenProps> = ({ route }) => (
  <SongListView id={route.params.id} title={route.params.title} type="album" />
)

type ArtistScreenNavigationProp = NativeStackNavigationProp<TabStackParamList, 'artist'>
type ArtistScreenRouteProp = RouteProp<TabStackParamList, 'artist'>
type ArtistScreenProps = {
  route: ArtistScreenRouteProp
  navigation: ArtistScreenNavigationProp
}

const ArtistScreen: React.FC<ArtistScreenProps> = ({ route }) => (
  <ArtistView id={route.params.id} title={route.params.title} />
)

type PlaylistScreenNavigationProp = NativeStackNavigationProp<TabStackParamList, 'playlist'>
type PlaylistScreenRouteProp = RouteProp<TabStackParamList, 'playlist'>
type PlaylistScreenProps = {
  route: PlaylistScreenRouteProp
  navigation: PlaylistScreenNavigationProp
}

const PlaylistScreen: React.FC<PlaylistScreenProps> = ({ route }) => (
  <SongListView id={route.params.id} title={route.params.title} type="playlist" />
)

type ResultsScreenNavigationProp = NativeStackNavigationProp<TabStackParamList, 'results'>
type ResultsScreenRouteProp = RouteProp<TabStackParamList, 'results'>
type ResultsScreenProps = {
  route: ResultsScreenRouteProp
  navigation: ResultsScreenNavigationProp
}

const ResultsScreen: React.FC<ResultsScreenProps> = ({ route }) => (
  <SearchResultsView query={route.params.query} type={route.params.type} />
)

const styles = StyleSheet.create({
  stackheaderStyle: {
    backgroundColor: colors.gradient.high,
    // backgroundColor: 'transparent',
  },
  stackheaderTitleStyle: {
    fontSize: 18,
    fontFamily: font.semiBold,
    color: colors.text.primary,
  },
})

const itemScreenOptions = {
  title: '',
  headerStyle: styles.stackheaderStyle,
  headerHideShadow: true,
  headerTintColor: 'white',
  headerTitleStyle: styles.stackheaderTitleStyle,
  // headerTranslucent: true,
}

function createTabStackNavigator(Component: React.ComponentType<any>) {
  const Stack = createNativeStackNavigator<TabStackParamList>()

  const Navigator: React.FC<{ navigation: BottomTabNavigationProp<{ a: undefined }, 'a'> }> = ({ navigation }) => {
    useEffect(() => {
      return navigation.addListener('tabPress', () => {
        navigation.dispatch(StackActions.popToTop())
      })
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    return (
      <Stack.Navigator initialRouteName="main">
        <Stack.Screen name="main" component={Component} options={{ headerShown: false }} />
        <Stack.Screen name="album" component={AlbumScreen} options={{ headerShown: false }} />
        <Stack.Screen name="artist" component={ArtistScreen} options={{ headerShown: false }} />
        <Stack.Screen name="playlist" component={PlaylistScreen} options={{ headerShown: false }} />
        <Stack.Screen name="results" component={ResultsScreen} options={itemScreenOptions} />
      </Stack.Navigator>
    )
  }

  return Navigator
}

const HomeTab = createTabStackNavigator(Home)
const LibraryTab = createTabStackNavigator(LibraryTopTabNavigator)
const SearchTab = createTabStackNavigator(Search)

type SettingsStackParamList = {
  main: undefined
  server?: { id?: string }
  web: { uri: string }
}

type ServerScreenNavigationProp = NativeStackNavigationProp<SettingsStackParamList, 'server'>
type ServerScreenRouteProp = RouteProp<SettingsStackParamList, 'server'>
type ServerScreenProps = {
  route: ServerScreenRouteProp
  navigation: ServerScreenNavigationProp
}
const ServerScreen: React.FC<ServerScreenProps> = ({ route }) => <ServerView id={route.params?.id} />

type WebScreenNavigationProp = NativeStackNavigationProp<SettingsStackParamList, 'web'>
type WebScreenRouteProp = RouteProp<SettingsStackParamList, 'web'>
type WebScreenProps = {
  route: WebScreenRouteProp
  navigation: WebScreenNavigationProp
}
const WebScreen: React.FC<WebScreenProps> = ({ route }) => <WebViewScreen uri={route.params.uri} />

const SettingsStack = createNativeStackNavigator()

const SettingsTab = () => {
  return (
    <SettingsStack.Navigator initialRouteName="main">
      <SettingsStack.Screen name="main" component={SettingsView} options={{ headerShown: false }} />
      <SettingsStack.Screen
        name="server"
        component={ServerScreen}
        options={{
          title: 'Edit Server',
          headerStyle: styles.stackheaderStyle,
          headerHideShadow: true,
          headerTintColor: 'white',
          headerTitleStyle: styles.stackheaderTitleStyle,
        }}
      />
      <SettingsStack.Screen
        name="web"
        component={WebScreen}
        options={{
          title: 'Web View',
          headerStyle: styles.stackheaderStyle,
          headerHideShadow: true,
          headerTintColor: 'white',
          headerTitleStyle: styles.stackheaderTitleStyle,
        }}
      />
    </SettingsStack.Navigator>
  )
}

const Tab = createBottomTabNavigator()

const BottomTabNavigator = () => {
  const firstRun = useStore(selectSettings.firstRun)

  return (
    <Tab.Navigator tabBar={BottomTabBar} initialRouteName={firstRun ? 'settings' : 'home'}>
      <Tab.Screen name="home" component={HomeTab} options={{ tabBarLabel: 'Home' }} />
      <Tab.Screen name="library" component={LibraryTab} options={{ tabBarLabel: 'Library' }} />
      <Tab.Screen name="search" component={SearchTab} options={{ tabBarLabel: 'Search' }} />
      <Tab.Screen name="settings" component={SettingsTab} options={{ tabBarLabel: 'Settings' }} />
    </Tab.Navigator>
  )
}

export default BottomTabNavigator
