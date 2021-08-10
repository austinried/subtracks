import colors from '@app/styles/colors'
import React from 'react'
import IconFA from 'react-native-vector-icons/FontAwesome'

const Star = React.memo<{
  starred: boolean
  size: number
}>(({ starred, size }) => {
  return (
    <IconFA name={starred ? 'star' : 'star-o'} color={starred ? colors.accent : colors.text.secondary} size={size} />
  )
})

export default Star
