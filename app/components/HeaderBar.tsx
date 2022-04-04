import { Album, Song } from '@app/models/library'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/core'
import { ReactComponentLike } from 'prop-types'
import React, { useCallback } from 'react'
import { StyleSheet, Text, View, ViewStyle } from 'react-native'
import Animated from 'react-native-reanimated'
import { useSafeAreaInsets } from 'react-native-safe-area-context'
import IconMat from 'react-native-vector-icons/MaterialIcons'
import { AlbumContextPressable, NowPlayingContextPressable } from './ContextMenu'
import PressableOpacity from './PressableOpacity'

export type HeaderContextItem = Song | Album

const More = React.memo<{ contextItem?: HeaderContextItem }>(({ contextItem }) => {
  const moreIcon = <IconMat name="more-vert" color="white" size={25} />
  let context: JSX.Element
  switch (contextItem?.itemType) {
    case 'song':
      context = (
        <NowPlayingContextPressable
          menuStyle={styles.icons}
          triggerWrapperStyle={styles.icons}
          song={contextItem}
          triggerOnLongPress={false}>
          {moreIcon}
        </NowPlayingContextPressable>
      )
      break
    case 'album':
      context = (
        <AlbumContextPressable
          menuStyle={styles.icons}
          triggerWrapperStyle={styles.icons}
          album={contextItem}
          triggerOnLongPress={false}>
          {moreIcon}
        </AlbumContextPressable>
      )
      break
    default:
      context = <></>
  }

  return context
})

const HeaderBar = React.memo<{
  title?: string
  headerStyle?: Animated.AnimateStyle<ViewStyle> | Animated.AnimateStyle<ViewStyle>[]
  HeaderCenter?: ReactComponentLike
  contextItem?: HeaderContextItem
}>(({ title, headerStyle, HeaderCenter, contextItem }) => {
  const navigation = useNavigation()
  const top = useSafeAreaInsets().top

  const back = useCallback(() => {
    navigation.goBack()
  }, [navigation])

  const _headerStyle = Array.isArray(headerStyle) ? headerStyle : [headerStyle]

  return (
    <Animated.View style={[styles.container, { paddingTop: top, height: dimensions.header + top }, ..._headerStyle]}>
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
      <More contextItem={contextItem} />
    </Animated.View>
  )
})

const styles = StyleSheet.create({
  container: {
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
