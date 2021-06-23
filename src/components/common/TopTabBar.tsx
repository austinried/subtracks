import { NavigationContext } from 'navigation-react';
import React, { useContext } from 'react';
import { Pressable, Text, View } from 'react-native';
import colors from '../../styles/colors';
import textStyles from '../../styles/text';

const TopTabButton: React.FC<{
  title: string,
  onPress: () => void,
  isFocused: boolean,
}> = ({ title, onPress, isFocused }) => {
  const color = isFocused ? colors.text.primary : colors.text.secondary;
  const borderBottomColor = isFocused ? colors.accent : colors.gradient.high;
  
  return (
    <Pressable
      onPress={onPress}
      style={{
        borderBottomColor,
        borderBottomWidth: 1.5,
        width: 94,
        height: 44,
        justifyContent: 'center',
        alignItems: 'center', 
    }}>
      <Text style={{
        ...textStyles.header, color,
      }}>{title}</Text>
    </Pressable>
  );
}

const TopTabBar = () => {
  const { stateNavigator } = useContext(NavigationContext);

  return (
    <View style={{
      backgroundColor: colors.gradient.high,
      flexDirection: 'row',
      justifyContent: 'space-evenly',
    }}>
      {Object.values(stateNavigator.states).map(state => (
        <TopTabButton
          key={state.key}
          title={state.title}
          onPress={() => {
            if (stateNavigator.stateContext.state.key !== state.key) {
              stateNavigator.navigate(state.key);
            }
          }}
          isFocused={stateNavigator.stateContext.state.key === state.key}
        />
      ))}
    </View>
  );
}

export default TopTabBar;
