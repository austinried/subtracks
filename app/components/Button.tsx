import colors from '@app/styles/colors'
import font from '@app/styles/font'
import React from 'react'
import { GestureResponderEvent, StyleSheet, Text } from 'react-native'
import PressableOpacity from './PressableOpacity'

const Button: React.FC<{
  title: string
  onPress: (event: GestureResponderEvent) => void
}> = ({ title, onPress }) => {
  return (
    <PressableOpacity onPress={onPress} style={styles.container}>
      <Text style={styles.text}>{title}</Text>
    </PressableOpacity>
  )
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.accent,
    paddingHorizontal: 24,
    minHeight: 42,
    justifyContent: 'center',
    borderRadius: 1000,
  },
  text: {
    fontSize: 15,
    fontFamily: font.bold,
    color: colors.text.primary,
  },
})

export default React.memo(Button)
