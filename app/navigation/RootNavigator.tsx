import BottomTabNavigator from '@app/navigation/BottomTabNavigator'
import NowPlayingView from '@app/screens/NowPlayingView'
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
      <RootStack.Screen name="now-playing" component={NowPlayingView} />
    </RootStack.Navigator>
  </NavigationContainer>
)

export default RootNavigator
