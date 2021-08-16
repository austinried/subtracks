import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/core'
import React, { useCallback } from 'react'
import { View, StatusBar, Text, StyleSheet, ViewStyle } from 'react-native'
import Animated from 'react-native-reanimated'
import PressableOpacity from './PressableOpacity'
import IconMat from 'react-native-vector-icons/MaterialIcons'
import { ReactComponentLike } from 'prop-types'
import { Song } from '@app/models/music'
import { NowPlayingContextPressable } from './ContextMenu'

const HeaderBar = React.memo<{
  title?: string
  headerStyle?: Animated.AnimatedStyleProp<ViewStyle> | Animated.AnimatedStyleProp<ViewStyle>[]
  HeaderCenter?: ReactComponentLike
  contextItem?: Song
}>(({ title, headerStyle, HeaderCenter, contextItem }) => {
  const navigation = useNavigation()

  const back = useCallback(() => {
    navigation.goBack()
  }, [navigation])

  const _headerStyle = Array.isArray(headerStyle) ? headerStyle : [headerStyle]

  const moreIcon = <IconMat name="more-vert" color="white" size={25} />
  let more = <></>
  if (contextItem) {
    more = (
      <NowPlayingContextPressable
        menuStyle={styles.icons}
        triggerWrapperStyle={styles.icons}
        song={contextItem}
        triggerOnLongPress={false}>
        {moreIcon}
      </NowPlayingContextPressable>
    )
  }

  return (
    <Animated.View style={[styles.container, ..._headerStyle]}>
      <PressableOpacity onPress={back} style={styles.icons}>
        <IconMat name="arrow-back" color="white" size={25} />
      </PressableOpacity>
      <View style={styles.center}>
        {HeaderCenter ? (
          <HeaderCenter />
        ) : (
          <Text numberOfLines={1} style={styles.title}>
            {title}
          </Text>
        )}
      </View>
      {more}
    </Animated.View>
  )
})

const styles = StyleSheet.create({
  container: {
    height: dimensions.top(),
    paddingTop: StatusBar.currentHeight,
    backgroundColor: colors.gradient.high,
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  icons: {
    height: 42,
    width: 42,
    marginHorizontal: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  center: {
    flex: 1,
  },
  title: {
    fontFamily: font.semiBold,
    fontSize: 18,
    color: colors.text.primary,
    flex: 1,
    textAlignVertical: 'center',
    paddingLeft: 14,
  },
})

export default HeaderBar
