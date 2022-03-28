import React from 'react'
import { FlatList, FlatListProps, useWindowDimensions, View, StyleSheet } from 'react-native'
import colors from '@app/styles/colors'
import GradientBackground, { GradientBackgroundProps } from '@app/components/GradientBackground'
import { useLayout } from '@react-native-community/hooks'
import NothingHere from './NothingHere'
import ImageGradientBackground, { ImageGradientBackgroundProps } from './ImageGradientBackground'

export type BackgroundHeaderFlatListPropsBase<ItemT> = FlatListProps<ItemT> & {
  contentMarginTop?: number
}

export type BackgroundHeaderFlatListProp<ItemT> = BackgroundHeaderFlatListPropsBase<ItemT> & {
  BackgroundComponent: typeof ImageGradientBackground | typeof GradientBackground
  backgroundProps?: ImageGradientBackgroundProps | GradientBackgroundProps
}

function BackgroundHeaderFlatList<ItemT>(props: BackgroundHeaderFlatListProp<ItemT>) {
  const window = useWindowDimensions()
  const headerLayout = useLayout()

  let marginBottom = -window.height + (props.contentMarginTop || 0)
  if (props.ListHeaderComponent) {
    marginBottom += headerLayout.height || window.height
  }

  const headerStyle = { marginBottom }

  return (
    <FlatList
      {...props}
      contentContainerStyle={[props.contentContainerStyle, { minHeight: window.height }]}
      style={[props.style, styles.list]}
      ListHeaderComponent={
        <props.BackgroundComponent position="relative" {...props.backgroundProps}>
          <View onLayout={headerLayout.onLayout}>{props.ListHeaderComponent}</View>
        </props.BackgroundComponent>
      }
      ListHeaderComponentStyle={[headerStyle]}
      ListEmptyComponent={props.ListEmptyComponent || <NothingHere style={styles.nothing} />}
    />
  )
}

const styles = StyleSheet.create({
  list: {
    backgroundColor: colors.gradient.low,
  },
  nothing: {
    width: '100%',
  },
})

export default BackgroundHeaderFlatList
