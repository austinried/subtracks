import React from 'react'
import { createNativeStackNavigator } from 'react-native-screens/native-stack'
import NowPlayingLayout from '@app/screens/NowPlayingLayout'
import BottomTabNavigator from '@app/navigation/BottomTabNavigator'

const RootStack = createNativeStackNavigator()

const RootNavigator = () => (
  <RootStack.Navigator
    screenOptions={{
      headerShown: false,
    }}>
    <RootStack.Screen name="Main" component={BottomTabNavigator} />
    <RootStack.Screen name="NowPlaying" component={NowPlayingLayout} />
  </RootStack.Navigator>
)

export default RootNavigator
