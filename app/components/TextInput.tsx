import colors from '@app/styles/colors'
import font from '@app/styles/font'
import React from 'react'
import { TextInput as ReactTextInput, StyleSheet, KeyboardTypeOptions, StyleProp, TextStyle } from 'react-native'

const TextInput = React.forwardRef<
  ReactTextInput,
  {
    style?: StyleProp<TextStyle>
    value?: string
    placeholder?: string
    onChangeText?: (text: string) => void
    onSubmitEditing?: () => void
    keyboardType?: KeyboardTypeOptions
  }
>(({ style, value, placeholder, onChangeText, onSubmitEditing, keyboardType }, ref) => {
  return (
    <ReactTextInput
      ref={ref}
      style={[style, styles.textInput]}
      placeholder={placeholder}
      placeholderTextColor="grey"
      selectionColor={colors.text.secondary}
      value={value}
      onChangeText={onChangeText}
      onSubmitEditing={onSubmitEditing}
      keyboardType={keyboardType}
    />
  )
})

const styles = StyleSheet.create({
  textInput: {
    width: '100%',
    backgroundColor: '#515151',
    fontFamily: font.regular,
    fontSize: 18,
    color: colors.text.primary,
  },
})

export default TextInput
