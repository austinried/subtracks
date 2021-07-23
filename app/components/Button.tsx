import colors from '@app/styles/colors'
import font from '@app/styles/font'
import React from 'react'
import { GestureResponderEvent, StyleSheet, Text } from 'react-native'
import PressableOpacity from './PressableOpacity'

const Button: React.FC<{
  title?: string
  buttonStyle?: 'hollow' | 'highlight'
  onPress: (event: GestureResponderEvent) => void
}> = ({ title, buttonStyle, onPress, children }) => {
  return (
    <PressableOpacity
      onPress={onPress}
      style={[styles.container, buttonStyle !== undefined ? styles[buttonStyle] : {}]}>
      {title ? <Text style={styles.text}>{title}</Text> : children}
    </PressableOpacity>
  )
}

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.accent,
    paddingHorizontal: 10,
    minHeight: 42,
    justifyContent: 'center',
    borderRadius: 1000,
  },
  hollow: {
    backgroundColor: 'transparent',
    borderColor: colors.text.secondary,
    borderWidth: 1.5,
  },
  highlight: {
    borderColor: colors.text.primary,
    borderWidth: 1.5,
  },
  text: {
    fontSize: 16,
    fontFamily: font.bold,
    color: colors.text.primary,
    paddingHorizontal: 14,
  },
})

export default Button
