import React from 'react';
import { Text, View } from 'react-native';
import { MaterialTopTabBarProps } from '@react-navigation/material-top-tabs';
import { primary } from '../../styles/colors';
import text from '../../styles/text';

const TopTabBar: React.FC<MaterialTopTabBarProps> = ({ state, descriptors }) => {
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

export default TopTabBar;
