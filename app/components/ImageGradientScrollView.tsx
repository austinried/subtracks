import ImageGradientBackground from '@app/components/ImageGradientBackground'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import React from 'react'
import { ScrollView, ScrollViewProps, useWindowDimensions } from 'react-native'

const ImageGradientScrollView: React.FC<ScrollViewProps & { imageUri?: string; imageKey?: string }> = props => {
  const layout = useWindowDimensions()

  const minHeight = layout.height - (dimensions.top() + dimensions.bottom())

  return (
    <ScrollView
      overScrollMode="never"
      {...props}
      style={[
        props.style,
        {
          backgroundColor: colors.gradient.low,
        },
      ]}
      contentContainerStyle={[{ minHeight }, props.contentContainerStyle]}>
      <ImageGradientBackground height={minHeight} imageUri={props.imageUri} imageKey={props.imageKey} />
      {props.children}
    </ScrollView>
  )
}

export default ImageGradientScrollView
