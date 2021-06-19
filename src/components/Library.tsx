import React from 'react';
import { Text, View, Image } from 'react-native';
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';
import { NavigationContainer } from '@react-navigation/native';

const SectionHeader: React.FC<{ title: string }> = ({ title }) => {
  return (
    <View style={{
      height: 60,
      flexDirection: 'row',
      justifyContent: 'space-between',
      // backgroundColor: 'green',
      alignItems: 'center',
      paddingLeft: 15,
      paddingRight: 15,
    }}>
      <Text style={{
        color: 'white',
        fontSize: 22,
        fontFamily: 'Lato-Black',
      }}>{title}</Text>
      <Image 
        style={{
          width: 32,
          height: 32,
          tintColor: 'white',
        }}
        source={require('../../res/chevron_right-fill.png')}
      />
    </View>
  );
}

const Tab = createMaterialTopTabNavigator();

const Albums = () => (
  <SectionHeader title='Albums' />
);
const Artists = () => (
  <SectionHeader title='Artists' />
);
const Playlists = () => (
  <SectionHeader title='Playlists' />
);

const Library = () => (
  <Tab.Navigator>
    <Tab.Screen
      name='Albums'
      component={Albums}
    />
    <Tab.Screen
      name='Artists'
      component={Artists}
    />
    <Tab.Screen
      name='Playlists'
      component={Playlists}
    />
  </Tab.Navigator>
);

export default Library;
