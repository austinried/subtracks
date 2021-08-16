import colors from '@app/styles/colors'
import font from '@app/styles/font'
import React from 'react'
import { StyleSheet, View, Text } from 'react-native'
import PressableOpacity from './PressableOpacity'

export type SettingsItemProps = {
  title: string
  subtitle?: string
  onPress?: () => void
}

const SettingsItem: React.FC<SettingsItemProps> = ({ title, subtitle, onPress, children }) => {
  return (
    <View style={styles.item}>
      <PressableOpacity style={styles.itemText} onPress={onPress}>
        <Text style={styles.itemTitle}>{title}</Text>
        {subtitle ? <Text style={styles.itemSubtitle}>{subtitle}</Text> : <></>}
      </PressableOpacity>
      {children}
    </View>
  )
}

const styles = StyleSheet.create({
  item: {
    marginBottom: 10,
    alignItems: 'stretch',
    flexDirection: 'row',
  },
  itemText: {
    flex: 1,
    paddingVertical: 10,
    alignSelf: 'stretch',
    alignItems: 'flex-start',
  },
  itemTitle: {
    fontFamily: font.regular,
    color: colors.text.primary,
    fontSize: 15,
  },
  itemSubtitle: {
    fontFamily: font.regular,
    color: colors.text.secondary,
    fontSize: 15,
  },
})

export default SettingsItem
