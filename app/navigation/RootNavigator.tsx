import BottomTabNavigator from '@app/navigation/BottomTabNavigator'
import NowPlayingQueue from '@app/screens/NowPlayingQueue'
import NowPlayingView from '@app/screens/NowPlayingView'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { DarkTheme, NavigationContainer } from '@react-navigation/native'
import React from 'react'
import { createNativeStackNavigator } from 'react-native-screens/native-stack'

const NowPlayingStack = createNativeStackNavigator()

const NowPlayingNavigator = () => (
  <NowPlayingStack.Navigator>
    <NowPlayingStack.Screen name="main" component={NowPlayingView} options={{ headerShown: false }} />
    <NowPlayingStack.Screen
      name="queue"
      component={NowPlayingQueue}
      options={{
        title: 'Queue',
        headerStyle: {
          backgroundColor: colors.gradient.high,
        },
        headerTitleStyle: {
          fontSize: 18,
          fontFamily: font.semiBold,
          color: colors.text.primary,
        },
        headerHideShadow: true,
        headerTintColor: 'white',
      }}
    />
  </NowPlayingStack.Navigator>
)

const RootStack = createNativeStackNavigator()

const theme = { ...DarkTheme }
theme.colors.background = colors.gradient.high

const RootNavigator = () => (
  <NavigationContainer
    theme={theme}
    linking={{
      prefixes: ['trackplayer'],
    }}>
    <RootStack.Navigator
      screenOptions={{
        headerShown: false,
      }}
      initialRouteName="top">
      <RootStack.Screen name="top" component={BottomTabNavigator} />
      <RootStack.Screen name="now-playing" component={NowPlayingNavigator} />
    </RootStack.Navigator>
  </NavigationContainer>
)

export default RootNavigator
