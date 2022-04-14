import colors from '@app/styles/colors'
import font from '@app/styles/font'
import React from 'react'
import { Text, StyleSheet } from 'react-native'
import { MenuOption, Menu, MenuTrigger, MenuOptions } from 'react-native-popup-menu'
import PressableOpacity from './PressableOpacity'
import Icon from 'react-native-vector-icons/MaterialCommunityIcons'

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
    <Text style={styles.optionText}>{text}</Text>
    {selected ? (
      <Icon name="checkbox-marked-circle" size={26} color={colors.accent} />
    ) : (
      <Icon name="checkbox-blank-circle-outline" size={26} color={colors.text.secondary} />
    )}
  </MenuOption>
))

const FilterButton = React.memo<{
  value?: string
  data: OptionData[]
  onSelect?: (selection: string) => void
}>(({ value, data, onSelect }) => {
  return (
    <Menu onSelect={onSelect}>
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
        {data.map(o => (
          <Option key={o.value} text={o.text} value={o.value} selected={o.value === value} />
        ))}
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
  optionsWrapper: {
    maxWidth: 130,
  },
  optionsContainer: {
    backgroundColor: colors.gradient.high,
    maxWidth: 130,
  },
  option: {
    flexDirection: 'row',
    paddingHorizontal: 6,
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: 40,
    paddingVertical: 8,
  },
  optionText: {
    flex: 1,
    color: colors.text.primary,
    fontFamily: font.semiBold,
    fontSize: 14,
    textAlign: 'right',
    marginRight: 6,
    marginLeft: 2,
  },
})

export default FilterButton
