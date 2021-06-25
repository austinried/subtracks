import React from 'react';
import { Text, View, Image, Pressable } from 'react-native';
import { BottomTabBarProps } from '@react-navigation/bottom-tabs';
import textStyles from '../../styles/text';
import colors from '../../styles/colors';
import FastImage from 'react-native-fast-image';

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
      backgroundColor: colors.gradient.high,
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-around',
      paddingHorizontal: 28,
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
            <FastImage
              source={isFocused ? img.fill : img.regular}
              style={{
                height: 26,
                width: 26,
              }}
              tintColor={isFocused ? colors.text.primary : colors.text.secondary}
            />
            <Text style={{
              ...textStyles.xsmall,
              color: isFocused ? colors.text.primary : colors.text.secondary,
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
