import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import SettingsView from './Settings';
import NowPlayingLayout from './NowPlayingLayout';
import ArtistsList from './ArtistsList';
import FocusableIcon from './FocusableIcon';
import Library from './Library';

const Tab = createBottomTabNavigator();

const TabNavigator = () => {
  return (
    <Tab.Navigator
      tabBarOptions={{
        style: {
          backgroundColor: '#383838',
          borderTopColor: '#383838',
          height: 44,
        },
      }}
    >
      <Tab.Screen
        name='Home'
        component={ArtistsList}
        options={{
          tabBarIcon: ({ focused }) => FocusableIcon({ focused,
            source: require('../../res/home.png'),
            focusedSource: require('../../res/home-fill.png'),
          }),
        }}
      />
      <Tab.Screen
        name='Library'
        component={Library}
        options={{
          tabBarIcon: ({ focused }) => FocusableIcon({ focused,
            source: require('../../res/library.png'),
            focusedSource: require('../../res/library-fill.png'),
          }),
        }}
      />
      <Tab.Screen
        name='Search'
        component={NowPlayingLayout}
        options={{
          tabBarIcon: ({ focused }) => FocusableIcon({ focused,
            source: require('../../res/search.png'),
            focusedSource: require('../../res/search-fill.png'),
          }),
        }}
      />
      <Tab.Screen
        name='Settings'
        component={SettingsView}
        options={{
          tabBarIcon: ({ focused }) => FocusableIcon({ focused,
            source: require('../../res/settings.png'),
            focusedSource: require('../../res/settings-fill.png'),
          }),
        }}
      />
    </Tab.Navigator>
  );
}

export default TabNavigator;
