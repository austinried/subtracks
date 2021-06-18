import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';

import { Image } from 'react-native';
import { RecoilRoot } from 'recoil';

import SettingsView from './src/components/Settings';
import ArtistsList from './src/components/ArtistsList';
import NowPlayingLayout from './src/components/NowPlayingLayout';


const SettingsIcon: React.FC<{ focused: boolean }> = ({ focused }) => (
  <Image
    style={{
      height: 32,
      width: 32,
    }}
    source={focused ? require('./res/settings-fill.png') : require('./res/settings.png')} />
);

const NowPlayingIcon: React.FC<{ focused: boolean }> = ({ focused }) => (
  <Image
    style={{
      height: 32,
      width: 32,
    }}
    source={focused ? require('./res/music_notes-fill.png') : require('./res/music_notes.png')} />
);

const ArtistsIcon: React.FC<{ focused: boolean }> = ({ focused }) => (
  <Image
    style={{
      height: 32,
      width: 32,
    }}
    source={focused ? require('./res/mic_on-fill.png') : require('./res/mic_on.png')} />
);

const Tab = createBottomTabNavigator();

const App = () => (
  <RecoilRoot>
    <NavigationContainer>
      <Tab.Navigator>
        <Tab.Screen
          name='Settings'
          component={SettingsView}
          options={{
            tabBarIcon: SettingsIcon,
          }}
        />
        <Tab.Screen
          name='Artists'
          component={ArtistsList}
          options={{
            tabBarIcon: ArtistsIcon,
            tabBarVisibilityAnimationConfig: {
              show: {
                animation: 'spring',
              },
              hide: {
                animation: 'spring',
              }
            }
          }}
        />
        <Tab.Screen
          name='Now Playing'
          component={NowPlayingLayout}
          options={{
            tabBarIcon: NowPlayingIcon,
          }}
        />
      </Tab.Navigator>
    </NavigationContainer>
  </RecoilRoot>
);

export default App;
