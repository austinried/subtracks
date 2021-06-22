import React from 'react';
import { Text, View } from 'react-native';
import { MaterialTopTabBarProps } from '@react-navigation/material-top-tabs';
import colors from '../../styles/colors';
import textStyles from '../../styles/text';

const TopTabBar: React.FC<MaterialTopTabBarProps> = ({ state, descriptors }) => {
  return (
    <View style={{
      backgroundColor: colors.gradient.high,
      flexDirection: 'row',
      alignItems: 'flex-start',
      justifyContent: 'flex-start',
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
        const color = isFocused ? colors.text.primary : colors.text.secondary;
        const borderBottomColor = isFocused ? colors.accent : colors.gradient.high;

        return (
          <View key={route.key} style={{
            borderBottomColor,
            borderBottomWidth: 1.5,
            paddingVertical: 8,
            width: 100,
            height: 38,
            justifyContent: 'center',
            alignItems: 'center', 
          }}>
            <Text style={{
              ...textStyles.header, color,
            }}>{label}</Text>
          </View>
        );
      })}
    </View>
  );
}

export default TopTabBar;
