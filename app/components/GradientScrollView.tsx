import GradientBackground from '@app/components/GradientBackground'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import React from 'react'
import { ScrollViewProps, useWindowDimensions } from 'react-native'
import Animated from 'react-native-reanimated'

const GradientScrollView: React.FC<
  Animated.AnimateProps<ScrollViewProps> & {
    offset?: number
  }
> = props => {
  const layout = useWindowDimensions()

  const minHeight = layout.height - (dimensions.top() + dimensions.bottom())

  return (
    <Animated.ScrollView
      overScrollMode="never"
      {...props}
      style={[props.style, { backgroundColor: colors.gradient.low }]}
      contentContainerStyle={[props.contentContainerStyle as any, { minHeight }]}>
      <GradientBackground style={{ top: props.offset }} />
      {props.children}
    </Animated.ScrollView>
  )
}

export default GradientScrollView
