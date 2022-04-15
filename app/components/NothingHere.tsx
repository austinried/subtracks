import font from '@app/styles/font'
import React from 'react'
import { useTranslation } from 'react-i18next'
import { Text, View, StyleSheet, ViewStyle } from 'react-native'
import Icon from 'react-native-vector-icons/MaterialCommunityIcons'
import { withSuspenseMemo } from './withSuspense'

const NothingHere = withSuspenseMemo<{
  height?: number
  width?: number
  style?: ViewStyle
}>(({ height, width, style }) => {
  const { t } = useTranslation('messages')
  height = height || 200
  width = width || 200

  return (
    <View style={[styles.container, { height, width }, style]}>
      <Icon name="music-rest-quarter" color={styles.text.color} size={width / 2} />
      <Text style={[styles.text, { fontSize: width / 8 }]} numberOfLines={3}>
        {t('nothingHere')}
      </Text>
    </View>
  )
})

const styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
    alignItems: 'center',
    opacity: 0.25,
  },
  text: {
    fontFamily: font.lightItalic,
    color: 'white',
  },
})

export default NothingHere
