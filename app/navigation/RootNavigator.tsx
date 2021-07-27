import BottomTabNavigator from '@app/navigation/BottomTabNavigator'
import NowPlayingLayout from '@app/screens/NowPlayingLayout'
import colors from '@app/styles/colors'
import { DarkTheme, NavigationContainer } from '@react-navigation/native'
import React from 'react'
import { createNativeStackNavigator } from 'react-native-screens/native-stack'

const RootStack = createNativeStackNavigator()

const theme = { ...DarkTheme }
theme.colors.background = colors.gradient.high

const RootNavigator = () => (
  <NavigationContainer
    theme={theme}
    linking={{
      prefixes: ['trackplayer'],
      config: {
        screens: {
          main: {
            path: ':/main',
          },
          'now-playing': {
            path: ':/notification.click',
          },
        },
      },
    }}>
    <RootStack.Navigator
      screenOptions={{
        headerShown: false,
      }}
      initialRouteName="main">
      <RootStack.Screen name="main" component={BottomTabNavigator} />
      <RootStack.Screen name="now-playing" component={NowPlayingLayout} />
    </RootStack.Navigator>
  </NavigationContainer>
)

export default RootNavigator
