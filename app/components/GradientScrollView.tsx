import GradientBackground from '@app/components/GradientBackground'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import React, { ForwardedRef, PropsWithChildren } from 'react'
import { ScrollView, ScrollViewProps, useWindowDimensions } from 'react-native'
import Animated from 'react-native-reanimated'
import { useSafeAreaInsets } from 'react-native-safe-area-context'

export type GradientScrollViewProps = PropsWithChildren<
  Animated.AnimateProps<ScrollViewProps> & {
    offset?: number
  }
>

const GradientScrollView = React.forwardRef<ScrollView, GradientScrollViewProps>((props, ref) => {
  const layout = useWindowDimensions()
  const paddingTop = useSafeAreaInsets().top

  const minHeight = layout.height - (dimensions.header + paddingTop + dimensions.bottom())

  return (
    <Animated.ScrollView
      ref={ref as ForwardedRef<Animated.ScrollView>}
      overScrollMode="never"
      {...props}
      style={[props.style, { backgroundColor: colors.gradient.low }]}
      contentContainerStyle={[props.contentContainerStyle as any, { minHeight }]}>
      <GradientBackground style={{ top: props.offset }} />
      {props.children}
    </Animated.ScrollView>
  )
})

export default GradientScrollView
