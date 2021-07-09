import React from 'react'
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs'
import SettingsView from '@app/screens/Settings'
import NowPlayingLayout from '@app/screens/NowPlayingLayout'
import LibraryTopTabNavigator from '@app/navigation/LibraryTopTabNavigator'
import BottomTabBar from '@app/navigation/BottomTabBar'
import Home from '@app/screens/Home'

const Tab = createBottomTabNavigator()

const BottomTabNavigator = () => {
  return (
    <Tab.Navigator tabBar={BottomTabBar}>
      <Tab.Screen name="Home" component={Home} options={{ icon: 'home' } as any} />
      <Tab.Screen name="Library" component={LibraryTopTabNavigator} options={{ icon: 'library' } as any} />
      <Tab.Screen name="Search" component={NowPlayingLayout} options={{ icon: 'search' } as any} />
      <Tab.Screen name="Settings" component={SettingsView} options={{ icon: 'settings' } as any} />
    </Tab.Navigator>
  )
}

export default BottomTabNavigator
