import React, { useEffect, useState } from 'react'
import { GestureResponderEvent, LayoutRectangle, Pressable, ViewStyle } from 'react-native'
import FastImage, { Source } from 'react-native-fast-image'

const PressableImage: React.FC<{
  source: Source | number
  onPress?: (event: GestureResponderEvent) => void
  style?: ViewStyle
  tintColor?: string
  disabled?: boolean
}> = ({ source, onPress, style, tintColor, disabled }) => {
  const [opacity, setOpacity] = useState(1)
  const [dimensions, setDimensions] = useState<LayoutRectangle | undefined>(undefined)

  disabled = disabled === undefined ? false : disabled
  style = {
    ...(style || {}),
    opacity,
  }

  useEffect(() => {
    disabled ? setOpacity(0.3) : setOpacity(1)
  }, [disabled])

  return (
    <Pressable
      style={style}
      onPress={onPress}
      disabled={disabled}
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
          height: dimensions?.height,
          width: dimensions?.width,
        }}
        source={source}
        tintColor={tintColor || 'white'}
        resizeMode={FastImage.resizeMode.contain}
      />
    </Pressable>
  )
}

export default PressableImage
