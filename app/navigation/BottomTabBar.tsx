import NowPlayingBar from '@app/components/NowPlayingBar'
import PressableOpacity from '@app/components/PressableOpacity'
import { useFirstRun } from '@app/hooks/settings'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import font from '@app/styles/font'
import { bottomTabIcons, OutlineFillIcon } from '@app/styles/icons'
import { BottomTabBarProps } from '@react-navigation/bottom-tabs'
import { BottomTabNavigationEventMap } from '@react-navigation/bottom-tabs/lib/typescript/src/types'
import { NavigationHelpers, ParamListBase } from '@react-navigation/native'
import React from 'react'
import { Image, ImageStyle, StyleSheet, Text, View } from 'react-native'

const BottomTabButton = React.memo<{
  routeKey: string
  label: string
  name: string
  isFocused: boolean
  icon: OutlineFillIcon
  navigation: NavigationHelpers<ParamListBase, BottomTabNavigationEventMap>
  disabled?: boolean
}>(({ routeKey, label, name, isFocused, icon, navigation, disabled }) => {
  const firstRun = useFirstRun()

  disabled = !!disabled || (firstRun && name !== 'settings')

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

  const focusColor = isFocused ? colors.text.primary : colors.text.secondary
  const imgSource = isFocused ? icon.fill : icon.outline
  const imgFocusStyle: ImageStyle = {
    tintColor: focusColor,
    opacity: isFocused ? 1 : 0.6,
  }
  const imgStyle = [styles.image, imgFocusStyle]
  const textStyle = [styles.text, { color: focusColor }]

  return (
    <PressableOpacity onPress={onPress} style={styles.button} disabled={disabled}>
      <Image source={imgSource} style={imgStyle} fadeDuration={0} />
      <Text style={textStyle} numberOfLines={1} ellipsizeMode="clip">
        {label}
      </Text>
    </PressableOpacity>
  )
})

const BottomTabBar: React.FC<BottomTabBarProps> = ({ state, descriptors, navigation }) => (
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

        let iconKey = route.name
        let disabled = false
        if (route.name.endsWith('-disabled')) {
          iconKey = route.name.split('-')[0]
          disabled = true
        }

        return (
          <BottomTabButton
            key={route.key}
            routeKey={route.key}
            label={label}
            name={route.name}
            isFocused={state.index === index}
            icon={bottomTabIcons[iconKey]}
            navigation={navigation}
            disabled={disabled}
          />
        )
      })}
    </View>
  </View>
)

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
    flexGrow: 1,
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
