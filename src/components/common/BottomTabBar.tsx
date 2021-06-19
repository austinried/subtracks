import React from 'react';
import { Text, View, Image, Pressable } from 'react-native';
import { BottomTabBarProps } from '@react-navigation/bottom-tabs';
import textStyles from '../../styles/text';

const icons: {[key: string]: any} = {
  home: {
    regular: require('../../../res/home.png'),
    fill: require('../../../res/home-fill.png'),
  },
  library: {
    regular: require('../../../res/library.png'),
    fill: require('../../../res/library-fill.png'),
  },
  search: {
    regular: require('../../../res/search.png'),
    fill: require('../../../res/search-fill.png'),
  },
  settings: {
    regular: require('../../../res/settings.png'),
    fill: require('../../../res/settings-fill.png'),
  },
}

const BottomTabBar: React.FC<BottomTabBarProps> = ({ state, descriptors, navigation }) => {
  return (
    <View style={{
      height: 54,
      backgroundColor: '#383838',
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-around',
      paddingHorizontal: 12,
    }}>
      {state.routes.map((route, index) => {
        const { options } = descriptors[route.key] as any;
        const label =
          options.tabBarLabel !== undefined
            ? options.tabBarLabel as string
            : options.title !== undefined
              ? options.title
              : route.name;

        const isFocused = state.index === index;
        const img = icons[options.icon];

        const onPress = () => {
          const event = navigation.emit({
            type: 'tabPress',
            target: route.key,
            canPreventDefault: true,
          });

          if (!isFocused && !event.defaultPrevented) {
            navigation.navigate(route.name);
          }
        };

        return (
          <Pressable
            key={route.key}
            onPress={onPress}
            style={{
              alignItems: 'center',
              flex: 1,
            }}
          >
            <Image
              source={isFocused ? img.fill : img.regular}
              style={{
                height: 24,
                width: 24,
                tintColor: isFocused ? 'white' : '#a0a0a0',
              }}
            />
            <Text style={{ 
              ...textStyles.small,
              color: isFocused ? 'white' : '#a0a0a0',
              fontFamily: isFocused ? 'Ubuntu-Medium' : 'Ubuntu-Light',
            }}>
              {label}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}

export default BottomTabBar;
