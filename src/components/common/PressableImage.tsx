import React, { useEffect, useState } from 'react'
import { GestureResponderEvent, LayoutRectangle, Pressable, ViewStyle } from 'react-native'
import FastImage, { Source } from 'react-native-fast-image'

const PressableImage: React.FC<{
  source: Source | number
  onPress?: (event: GestureResponderEvent) => void
  style?: ViewStyle
  tintColor?: string
  disabled?: boolean
  hitSlop?: number
  padding?: number
  ripple?: boolean
}> = ({ source, onPress, style, tintColor, disabled, hitSlop, padding, ripple }) => {
  const [opacity, setOpacity] = useState(1)
  const [dimensions, setDimensions] = useState<LayoutRectangle | undefined>(undefined)

  disabled = disabled === undefined ? false : disabled
  padding = padding || 0
  ripple = ripple === undefined ? false : ripple
  style = {
    ...(style || {}),
    opacity,
    justifyContent: 'center',
    alignItems: 'center',
  }

  useEffect(() => {
    disabled ? setOpacity(0.3) : setOpacity(1)
  }, [disabled])

  return (
    <Pressable
      style={style}
      onPress={onPress}
      disabled={disabled}
      hitSlop={hitSlop}
      android_ripple={
        ripple
          ? {
              color: 'rgba(255,255,255,0.26)',
              radius: dimensions ? dimensions.width / 2 : undefined,
            }
          : undefined
      }
      onPressIn={() => {
        if (!disabled) {
          setOpacity(0.4)
        }
      }}
      onPressOut={() => {
        if (!disabled) {
          setOpacity(1)
        }
      }}
      onLayout={event => setDimensions(event.nativeEvent.layout)}>
      <FastImage
        style={{
          display: dimensions ? 'flex' : 'none',
          height: dimensions ? dimensions.height - padding : 0,
          width: dimensions ? dimensions.width - padding : 0,
        }}
        source={source}
        tintColor={tintColor || 'white'}
        resizeMode={FastImage.resizeMode.contain}
      />
    </Pressable>
  )
}

export default PressableImage
