import React from 'react'
import { ScrollView, ScrollViewProps, ViewStyle } from 'react-native'
import colors from '@app/styles/colors'
import GradientBackground from '@app/components/GradientBackground'

const GradientScrollView: React.FC<ScrollViewProps> = props => {
  props.style = props.style || {}
  ;(props.style as ViewStyle).backgroundColor = colors.gradient.low

  return (
    <ScrollView overScrollMode="never" {...props}>
      <GradientBackground />
      {props.children}
    </ScrollView>
  )
}

export default GradientScrollView
