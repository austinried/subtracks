import React from 'react';
import { Text, View, Image, Pressable } from 'react-native';
import textStyles from '../../styles/text';
import colors from '../../styles/colors';
import { useContext } from 'react';
import { NavigationContext } from 'navigation-react';
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

const BottomTabButton: React.FC<{
  route: string,
  title: string,
  onPress: () => void,
  isFocused: boolean,
}> = ({ route, title, onPress, isFocused }) => {
  const img = icons[route];

  return (
    <Pressable
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
        {title}
      </Text>
    </Pressable>
  );
}

const BottomTabBar = () => {
  const { stateNavigator } = useContext(NavigationContext);

  return (
    <View style={{
      height: 54,
      backgroundColor: colors.gradient.high,
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-around',
      paddingHorizontal: 28,
    }}>
      {Object.values(stateNavigator.states).map(state => (
        <BottomTabButton 
          key={state.key}
          route={state.key}
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

export default BottomTabBar;
