import { useStar } from '@app/hooks/query'
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
  disabled?: boolean
}>(({ id, type, size, style, disabled }) => {
  const { query, toggle } = useStar(id, type)

  return (
    <PressableOpacity onPress={() => toggle.mutate()} style={style} disabled={disabled}>
      <Star size={size} starred={!!query.data} />
    </PressableOpacity>
  )
})
