import React from 'react';
import { createNativeStackNavigator } from 'react-native-screens/native-stack';
import NowPlayingLayout from '../NowPlayingLayout';
import BottomTabNavigator from './BottomTabNavigator';

const RootStack = createNativeStackNavigator();

const RootNavigator = () => (
  <RootStack.Navigator>
    <RootStack.Screen name="Main" component={BottomTabNavigator} options={{ headerShown: false }} />
    <RootStack.Screen name="Now Playing" component={NowPlayingLayout} options={{ headerShown: false }} />
  </RootStack.Navigator>
);

export default RootNavigator;
