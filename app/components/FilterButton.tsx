import colors from '@app/styles/colors'
import font from '@app/styles/font'
import React from 'react'
import { Text, StyleSheet, View } from 'react-native'
import { MenuOption, Menu, MenuTrigger, MenuOptions, renderers } from 'react-native-popup-menu'
import PressableOpacity from './PressableOpacity'
import Icon from 'react-native-vector-icons/MaterialCommunityIcons'
import { ScrollView } from 'react-native-gesture-handler'

const { SlideInMenu } = renderers

export type OptionData = {
  value: string
  text: string
}

const Option = React.memo<{
  text: string
  value: string
  selected?: boolean
}>(({ text, value, selected }) => (
  <MenuOption style={styles.option} value={value}>
    {selected ? (
      <Icon name="checkbox-marked-circle" size={32} color={colors.accent} style={styles.icon} />
    ) : (
      <Icon name="checkbox-blank-circle-outline" size={32} color={colors.text.secondary} style={styles.icon} />
    )}
    <Text style={styles.optionText} numberOfLines={1} adjustsFontSizeToFit={true} minimumFontScale={0.6}>
      {text}
    </Text>
  </MenuOption>
))

const FilterButton = React.memo<{
  value?: string
  data: OptionData[]
  onSelect?: (selection: string) => void
  title: string
}>(({ value, data, onSelect, title }) => {
  return (
    <Menu onSelect={onSelect} renderer={SlideInMenu}>
      <MenuTrigger
        customStyles={{
          triggerOuterWrapper: styles.filterOuterWrapper,
          triggerWrapper: styles.filterWrapper,
          triggerTouchable: { style: styles.filter },
          TriggerTouchableComponent: PressableOpacity,
        }}>
        <Icon name="filter-variant" color="white" size={30} />
      </MenuTrigger>
      <MenuOptions
        customStyles={{
          optionsWrapper: styles.optionsWrapper,
          optionsContainer: styles.optionsContainer,
        }}>
        <ScrollView style={styles.optionsScroll} overScrollMode="never">
          <View style={styles.header}>
            <Text style={styles.headerText} numberOfLines={2} ellipsizeMode="clip">
              {title}
            </Text>
          </View>
          {data.map(o => (
            <Option key={o.value} text={o.text} value={o.value} selected={o.value === value} />
          ))}
        </ScrollView>
      </MenuOptions>
    </Menu>
  )
})

const styles = StyleSheet.create({
  filterOuterWrapper: {
    position: 'absolute',
    bottom: 20,
    right: 20,
  },
  filterWrapper: {},
  filter: {
    borderRadius: 32,
    width: 50,
    height: 50,
    elevation: 5,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: colors.accent,
  },
  optionsScroll: {
    maxHeight: 260,
  },
  optionsWrapper: {
    overflow: 'hidden',
  },
  optionsContainer: {
    backgroundColor: 'rgba(45, 45, 45, 0.95)',
  },
  header: {
    paddingHorizontal: 20,
    // paddingVertical: 10,
    marginTop: 16,
    marginBottom: 6,
  },
  headerText: {
    fontFamily: font.bold,
    fontSize: 20,
    color: colors.text.primary,
  },
  option: {
    paddingVertical: 8,
    paddingHorizontal: 20,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-start',
  },
  optionText: {
    fontFamily: font.semiBold,
    fontSize: 16,
    color: colors.text.primary,
  },
  icon: {
    marginRight: 14,
    width: 32,
    height: 32,
    justifyContent: 'center',
    alignItems: 'center',
    // backgroundColor: 'red',
  },
})

export default FilterButton
