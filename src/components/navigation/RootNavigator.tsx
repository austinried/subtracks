import React from 'react';
import { createNativeStackNavigator } from 'react-native-screens/native-stack';
import NowPlayingLayout from '../NowPlayingLayout';
import BottomTabNavigator from './BottomTabNavigator';

const RootStack = createNativeStackNavigator();

const RootNavigator = () => (
  <RootStack.Navigator
    screenOptions={{
      headerShown: false,
    }}>
    <RootStack.Screen name="Main" component={BottomTabNavigator} />
    <RootStack.Screen name="Now Playing" component={NowPlayingLayout} />
  </RootStack.Navigator>
);

export default RootNavigator;
