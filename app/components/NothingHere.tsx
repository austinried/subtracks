import font from '@app/styles/font'
import React from 'react'
import { Text, View, StyleSheet } from 'react-native'
import Icon from 'react-native-vector-icons/MaterialCommunityIcons'

const NothingHere = React.memo<{
  height?: number
  width?: number
}>(({ height, width }) => {
  height = height || 200
  width = width || 200

  return (
    <View style={[styles.container, { height, width }]}>
      <Icon name="music-rest-quarter" color={styles.text.color} size={width / 2} />
      <Text style={[styles.text, { fontSize: width / 8 }]}>Nothing here...</Text>
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
