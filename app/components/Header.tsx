import colors from '@app/styles/colors'
import font from '@app/styles/font'
import React from 'react'
import { StyleSheet, Text, TextStyle } from 'react-native'

const Header: React.FC<{
  style?: TextStyle
}> = ({ children, style }) => {
  return <Text style={[styles.text, style]}>{children}</Text>
}

const styles = StyleSheet.create({
  text: {
    fontFamily: font.bold,
    fontSize: 24,
    color: colors.text.primary,
    marginTop: 18,
    marginBottom: 12,
  },
})

export default Header
