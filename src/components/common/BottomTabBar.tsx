import React, { useState } from 'react'
import { Text, View, Pressable } from 'react-native'
import { BottomTabBarProps } from '@react-navigation/bottom-tabs'
import textStyles from '../../styles/text'
import colors from '../../styles/colors'
import FastImage from 'react-native-fast-image'

const icons: { [key: string]: any } = {
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
  routeKey: string
  label: string
  name: string
  isFocused: boolean
  img: { regular: number; fill: number }
  navigation: any
}> = ({ routeKey, label, name, isFocused, img, navigation }) => {
  const [opacity, setOpacity] = useState(1)

  const onPress = () => {
    const event = navigation.emit({
      type: 'tabPress',
      target: routeKey,
      canPreventDefault: true,
    })

    if (!isFocused && !event.defaultPrevented) {
      navigation.navigate(name)
    }
  }

  return (
    <Pressable
      onPress={onPress}
      onPressIn={() => setOpacity(0.6)}
      onPressOut={() => setOpacity(1)}
      style={{
        alignItems: 'center',
        flex: 1,
        opacity,
      }}>
      <FastImage
        source={isFocused ? img.fill : img.regular}
        style={{
          height: 26,
          width: 26,
        }}
        tintColor={isFocused ? colors.text.primary : colors.text.secondary}
      />
      <Text
        style={{
          ...textStyles.xsmall,
          color: isFocused ? colors.text.primary : colors.text.secondary,
        }}>
        {label}
      </Text>
    </Pressable>
  )
}

const BottomTabBar: React.FC<BottomTabBarProps> = ({ state, descriptors, navigation }) => {
  return (
    <View
      style={{
        height: 54,
        backgroundColor: colors.gradient.high,
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-around',
        paddingHorizontal: 28,
      }}>
      {state.routes.map((route, index) => {
        const { options } = descriptors[route.key] as any
        const label =
          options.tabBarLabel !== undefined
            ? (options.tabBarLabel as string)
            : options.title !== undefined
            ? options.title
            : route.name

        return (
          <BottomTabButton
            key={route.key}
            routeKey={route.key}
            label={label}
            name={route.name}
            isFocused={state.index === index}
            img={icons[options.icon]}
            navigation={navigation}
          />
        )
      })}
    </View>
  )
}

export default BottomTabBar
