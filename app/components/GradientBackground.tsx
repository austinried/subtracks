import React from 'react'
import { useWindowDimensions, ViewStyle } from 'react-native'
import LinearGradient from 'react-native-linear-gradient'
import colorStyles from '@app/styles/colors'

export type GradientBackgroundPropsBase = {
  height?: number | string
  width?: number | string
  position?: 'relative' | 'absolute'
  style?: ViewStyle
}

export type GradientBackgroundProps = GradientBackgroundPropsBase & {
  colors?: string[]
  locations?: number[]
}

const GradientBackground: React.FC<GradientBackgroundProps> = ({
  height,
  width,
  position,
  style,
  colors,
  locations,
  children,
}) => {
  const layout = useWindowDimensions()

  return (
    <LinearGradient
      colors={colors || [colorStyles.gradient.high, colorStyles.gradient.low]}
      locations={locations || [0.01, 0.7]}
      style={[
        style,
        {
          width: width || '100%',
          height: height || layout.height,
          position: position || 'absolute',
        },
      ]}>
      {children}
    </LinearGradient>
  )
}

export default GradientBackground
