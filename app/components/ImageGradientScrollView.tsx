import React, { useState } from 'react'
import { LayoutRectangle, ScrollView, ScrollViewProps } from 'react-native'
import colors from '@app/styles/colors'
import ImageGradientBackground from '@app/components/ImageGradientBackground'

const ImageGradientScrollView: React.FC<ScrollViewProps & { imageUri?: string; imageKey?: string }> = props => {
  const [layout, setLayout] = useState<LayoutRectangle | undefined>(undefined)

  props.style = props.style || {}
  if (typeof props.style === 'object' && props.style !== null) {
    props.style = {
      ...props.style,
      backgroundColor: colors.gradient.low,
    }
  }

  return (
    <ScrollView
      overScrollMode="never"
      {...props}
      onLayout={event => {
        setLayout(event.nativeEvent.layout)
      }}>
      <ImageGradientBackground height={layout?.height} imageUri={props.imageUri} imageKey={props.imageKey} />
      {props.children}
    </ScrollView>
  )
}

export default ImageGradientScrollView
