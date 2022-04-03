import ImageGradientBackground, { ImageGradientBackgroundProps } from '@app/components/ImageGradientBackground'
import colors from '@app/styles/colors'
import dimensions from '@app/styles/dimensions'
import React from 'react'
import { ScrollView, ScrollViewProps, useWindowDimensions } from 'react-native'
import { useSafeAreaInsets } from 'react-native-safe-area-context'

const ImageGradientScrollView: React.FC<ScrollViewProps & ImageGradientBackgroundProps> = props => {
  const layout = useWindowDimensions()
  const paddingTop = useSafeAreaInsets().top

  const minHeight = layout.height - (dimensions.header + paddingTop + dimensions.bottom())

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
      <ImageGradientBackground height={minHeight} imagePath={props.imagePath} onGetColor={props.onGetColor} />
      {props.children}
    </ScrollView>
  )
}

export default ImageGradientScrollView
