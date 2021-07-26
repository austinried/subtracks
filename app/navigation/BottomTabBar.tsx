import NowPlayingBar from '@app/components/NowPlayingBar'
import PressableOpacity from '@app/components/PressableOpacity'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import font from '@app/styles/font'
import { BottomTabBarProps } from '@react-navigation/bottom-tabs'
import { BottomTabNavigationEventMap } from '@react-navigation/bottom-tabs/lib/typescript/src/types'
import { NavigationHelpers, ParamListBase } from '@react-navigation/native'
import React from 'react'
import { StyleSheet, Text, View } from 'react-native'
import FastImage from 'react-native-fast-image'

type TabButtonImage = {
  regular: number
  fill: number
}

const icons: { [key: string]: TabButtonImage } = {
  home: {
    regular: require('@res/icons/home.png'),
    fill: require('@res/icons/home-fill.png'),
  },
  library: {
    regular: require('@res/icons/library.png'),
    fill: require('@res/icons/library-fill.png'),
  },
  search: {
    regular: require('@res/icons/search.png'),
    fill: require('@res/icons/search-fill.png'),
  },
  settings: {
    regular: require('@res/icons/settings.png'),
    fill: require('@res/icons/settings-fill.png'),
  },
}

const BottomTabButton = React.memo<{
  routeKey: string
  label: string
  name: string
  isFocused: boolean
  img: TabButtonImage
  navigation: NavigationHelpers<ParamListBase, BottomTabNavigationEventMap>
}>(({ routeKey, label, name, isFocused, img, navigation }) => {
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
    <PressableOpacity onPress={onPress} style={styles.button}>
      <FastImage
        source={isFocused ? img.fill : img.regular}
        style={styles.image}
        tintColor={isFocused ? colors.text.primary : colors.text.secondary}
      />
      <Text
        style={{
          ...styles.text,
          color: isFocused ? colors.text.primary : colors.text.secondary,
        }}>
        {label}
      </Text>
    </PressableOpacity>
  )
})

const BottomTabBar: React.FC<BottomTabBarProps> = ({ state, descriptors, navigation }) => {
  return (
    <View>
      <NowPlayingBar />
      <View style={styles.container}>
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
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    height: dimensions.tabBar,
    backgroundColor: colors.gradient.high,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    paddingHorizontal: 28,
  },
  button: {
    alignItems: 'center',
    flex: 1,
    height: '100%',
  },
  image: {
    height: 26,
    width: 26,
  },
  text: {
    fontSize: 10,
    fontFamily: font.regular,
  },
})

export default BottomTabBar
