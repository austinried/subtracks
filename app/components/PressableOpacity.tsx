import React, { useEffect, useState } from 'react'
import { LayoutRectangle, Pressable, PressableProps } from 'react-native'

type PressableOpacityProps = PressableProps & {
  ripple?: boolean
  rippleColor?: string
}

const PressableOpacity: React.FC<PressableOpacityProps> = props => {
  const [opacity, setOpacity] = useState(1)
  const [dimensions, setDimensions] = useState<LayoutRectangle | undefined>(undefined)

  useEffect(() => {
    props.disabled === true ? setOpacity(0.3) : setOpacity(1)
  }, [props.disabled])

  return (
    <Pressable
      {...props}
      style={[{ justifyContent: 'center', alignItems: 'center' }, props.style as any, { opacity }]}
      android_ripple={
        props.ripple
          ? {
              color: props.rippleColor || 'rgba(255,255,255,0.26)',
              radius: dimensions ? dimensions.width / 2 : undefined,
              borderless: true,
            }
          : undefined
      }
      onLayout={event => setDimensions(event.nativeEvent.layout)}
      onPressIn={() => {
        if (!props.disabled) {
          setOpacity(0.4)
        }
      }}
      onPressOut={() => {
        if (!props.disabled) {
          setOpacity(1)
        }
      }}>
      {props.children}
    </Pressable>
  )
}

export default React.memo(PressableOpacity)
