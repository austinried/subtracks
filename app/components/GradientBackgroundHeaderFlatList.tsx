import React from 'react'
import { FlatList, FlatListProps, useWindowDimensions, View, StyleSheet } from 'react-native'
import colors from '@app/styles/colors'
import GradientBackground, { GradientBackgroundProps } from '@app/components/GradientBackground'
import { useLayout } from '@react-native-community/hooks'
import NothingHere from './NothingHere'
import GradientImageBackground, { GradientImageBackgroundProps } from './GradientImageBackground'

export type GradientBackgroundHeaderFlatListPropsBase<ItemT> = FlatListProps<ItemT> & {
  contentMarginTop?: number
}

export type GradientBackgroundHeaderFlatListProp<ItemT> = GradientBackgroundHeaderFlatListPropsBase<ItemT> & {
  BackgroundComponent: typeof GradientImageBackground | typeof GradientBackground
  backgroundProps?: GradientImageBackgroundProps | GradientBackgroundProps
}

function GradientBackgroundHeaderFlatList<ItemT>(props: GradientBackgroundHeaderFlatListProp<ItemT>) {
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

export default GradientBackgroundHeaderFlatList
