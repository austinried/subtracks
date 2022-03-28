import { useStar } from '@app/hooks/library'
import colors from '@app/styles/colors'
import React from 'react'
import { PressableStateCallbackType, StyleProp, ViewStyle } from 'react-native'
import IconFA from 'react-native-vector-icons/FontAwesome'
import PressableOpacity from './PressableOpacity'

export const Star = React.memo<{
  starred: boolean
  size: number
}>(({ starred, size }) => {
  return (
    <IconFA name={starred ? 'star' : 'star-o'} color={starred ? colors.accent : colors.text.secondary} size={size} />
  )
})

export const PressableStar = React.memo<{
  id: string
  type: 'album' | 'artist' | 'song'
  size: number
  style?: StyleProp<ViewStyle> | ((state: PressableStateCallbackType) => StyleProp<ViewStyle>) | undefined
}>(({ id, type, size, style }) => {
  const { starred, toggleStar } = useStar(id, type)

  return (
    <PressableOpacity onPress={toggleStar} style={style}>
      <Star size={size} starred={starred} />
    </PressableOpacity>
  )
})
