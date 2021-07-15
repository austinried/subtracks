import BottomTabBar from '@app/navigation/BottomTabBar'
import LibraryTopTabNavigator from '@app/navigation/LibraryTopTabNavigator'
import AlbumView from '@app/screens/AlbumView'
import ArtistsList from '@app/screens/ArtistsList'
import ArtistView from '@app/screens/ArtistView'
import Home from '@app/screens/Home'
import SettingsView from '@app/screens/Settings'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { BottomTabNavigationProp, createBottomTabNavigator } from '@react-navigation/bottom-tabs'
import { RouteProp, StackActions } from '@react-navigation/native'
import React, { useEffect } from 'react'
import { StyleSheet } from 'react-native'
import { createNativeStackNavigator, NativeStackNavigationProp } from 'react-native-screens/native-stack'

type TabStackParamList = {
  TabMain: { toTop?: boolean }
  AlbumView: { id: string; title: string }
  ArtistView: { id: string; title: string }
}

type AlbumScreenNavigationProp = NativeStackNavigationProp<TabStackParamList, 'AlbumView'>
type AlbumScreenRouteProp = RouteProp<TabStackParamList, 'AlbumView'>
type AlbumScreenProps = {
  route: AlbumScreenRouteProp
  navigation: AlbumScreenNavigationProp
}

const AlbumScreen: React.FC<AlbumScreenProps> = ({ route }) => (
  <AlbumView id={route.params.id} title={route.params.title} />
)

type ArtistScreenNavigationProp = NativeStackNavigationProp<TabStackParamList, 'ArtistView'>
type ArtistScreenRouteProp = RouteProp<TabStackParamList, 'ArtistView'>
type ArtistScreenProps = {
  route: ArtistScreenRouteProp
  navigation: ArtistScreenNavigationProp
}

const ArtistScreen: React.FC<ArtistScreenProps> = ({ route }) => (
  <ArtistView id={route.params.id} title={route.params.title} />
)

const styles = StyleSheet.create({
  stackheaderStyle: {
    backgroundColor: colors.gradient.high,
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
}

function createTabStackNavigator(Component: React.ComponentType<any>) {
  const Stack = createNativeStackNavigator<TabStackParamList>()

  const Navigator: React.FC<{ navigation: BottomTabNavigationProp<{ a: undefined }, 'a'> }> = ({ navigation }) => {
    useEffect(() => {
      return navigation.addListener('tabPress', () => {
        navigation.dispatch(StackActions.popToTop())
      })
    })

    return (
      <Stack.Navigator>
        <Stack.Screen name="TabMain" component={Component} options={{ headerShown: false }} />
        <Stack.Screen name="AlbumView" component={AlbumScreen} options={itemScreenOptions} />
        <Stack.Screen name="ArtistView" component={ArtistScreen} options={itemScreenOptions} />
      </Stack.Navigator>
    )
  }

  return Navigator
}

const HomeTab = createTabStackNavigator(Home)
const LibraryTab = createTabStackNavigator(LibraryTopTabNavigator)
const SearchTab = createTabStackNavigator(ArtistsList)

const Tab = createBottomTabNavigator()

const BottomTabNavigator = () => {
  return (
    <Tab.Navigator tabBar={BottomTabBar}>
      <Tab.Screen name="Home" component={HomeTab} options={{ icon: 'home' } as any} />
      <Tab.Screen name="Library" component={LibraryTab} options={{ icon: 'library' } as any} />
      <Tab.Screen name="Search" component={SearchTab} options={{ icon: 'search' } as any} />
      <Tab.Screen name="Settings" component={SettingsView} options={{ icon: 'settings' } as any} />
    </Tab.Navigator>
  )
}

export default BottomTabNavigator
