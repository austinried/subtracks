import React, { ReactComponentElement } from 'react';
import { Text, View, Image, FlatList } from 'react-native';
import { createMaterialTopTabNavigator, MaterialTopTabBarProps } from '@react-navigation/material-top-tabs';
import { NavigationContainer } from '@react-navigation/native';
import { Artist } from '../models/music';
import { useRecoilValue } from 'recoil';
import { artistsState } from '../state/artists';
import LinearGradient from 'react-native-linear-gradient';
import { primary } from '../styles/colors';
import text from '../styles/text';

const TabView: React.FC<{}> = ({ children }) => (
  <LinearGradient
    colors={['#383838', '#000000']}
    // colors={['#395266', '#06172d']}
    // start={{x: 0.0, y: 0.25}} end={{x: 0.5, y: 1.0}}
    locations={[0.05,0.75]}
    style={{
      flex: 1,
    }}
  >
    {children}
  </LinearGradient>
);

const Albums = () => (
  <TabView>
  </TabView>
);
const Playlists = () => (
  <TabView>
  </TabView>
);

const ArtistItem: React.FC<{ item: Artist } > = ({ item }) => (
  <View style={{
    flexDirection: 'row',
    alignItems: 'center',
    // height: 56,
    marginVertical: 6,
    marginLeft: 6,
  }}>
    <Image
      source={item.coverArt ? { uri: 'https://reactnative.dev/img/tiny_logo.png'  } : require('../../res/mic_on-fill.png')}
      style={{
        width: 56,
        height: 56,
        // tintColor: 'white',
      }}
    />
    <Text style={{
      ...text.regular,
      marginLeft: 12,
    }}>{item.name}</Text>
  </View>
);

const Artists = () => {
  const artists = useRecoilValue(artistsState);

  const renderItem: React.FC<{ item: Artist }> = ({ item }) => (
    <ArtistItem item={item} />
  );

  return (
    <TabView>
      <FlatList
        data={artists}
        renderItem={renderItem}
        keyExtractor={item => item.id}
      />
    </TabView>
  );
}

const TabBar: React.FC<MaterialTopTabBarProps> = ({ state, descriptors }) => {
  return (
    <View style={{
      height: 48,
      backgroundColor: '#383838',
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-around',
    }}>
      {state.routes.map((route, index) => {
        const { options } = descriptors[route.key];
        const label =
          options.tabBarLabel !== undefined
            ? options.tabBarLabel
            : options.title !== undefined
            ? options.title
            : route.name;
        
        const isFocused = state.index === index;
        const fontFamily = isFocused ? 'Ubuntu-Regular' : 'Ubuntu-Light';
        const color = isFocused ? primary.focused : primary.blurred;
        const borderBottomColor = isFocused ? primary.focused : '#383838';

        return (
          <View style={{
            borderBottomColor,
            borderBottomWidth: 2,
            borderBottomLeftRadius: 2,
            borderBottomEndRadius: 2,
            paddingBottom: 5,
            width: 100,
            justifyContent: 'center',
            flexDirection: 'row',
          }}>
            <Text style={{
              ...text.header,
              fontFamily, color
            }}>{label}</Text>
          </View>
        );
      })}
    </View>
  );
}

const Tab = createMaterialTopTabNavigator();

const Library = () => (
  <View style={{
    flex: 1,
  }}>
    <Tab.Navigator tabBar={TabBar}>
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
  </View>
);

export default Library;
