import colors from '@app/styles/colors'
import font from '@app/styles/font'
import React from 'react'
import { GestureResponderEvent, StyleProp, StyleSheet, Text, ViewStyle } from 'react-native'
import PressableOpacity from './PressableOpacity'

const Button: React.FC<{
  title?: string
  buttonStyle?: 'hollow' | 'highlight'
  onPress: (event: GestureResponderEvent) => void
  style?: StyleProp<ViewStyle>
  disabled?: boolean
}> = ({ title, buttonStyle, onPress, children, style, disabled }) => {
  return (
    <PressableOpacity
      onPress={onPress}
      disabled={disabled}
      style={[styles.container, buttonStyle !== undefined ? styles[buttonStyle] : {}, style]}>
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
    borderColor: colors.text.primary,
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
