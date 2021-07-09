import GradientBackground from '@app/components/GradientBackground'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import React from 'react'
import { ScrollView, ScrollViewProps, useWindowDimensions } from 'react-native'

const GradientScrollView: React.FC<ScrollViewProps> = props => {
  const layout = useWindowDimensions()

  const minHeight = layout.height - (dimensions.top() + dimensions.bottom())

  return (
    <ScrollView
      overScrollMode="never"
      {...props}
      style={[props.style, { backgroundColor: colors.gradient.low }]}
      contentContainerStyle={[props.contentContainerStyle, { minHeight }]}>
      <GradientBackground />
      {props.children}
    </ScrollView>
  )
}

export default GradientScrollView
