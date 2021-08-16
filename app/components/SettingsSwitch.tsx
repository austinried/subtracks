import colors from '@app/styles/colors'
import React from 'react'
import { Switch, StyleSheet } from 'react-native'
import SettingsItem, { SettingsItemProps } from './SettingsItem'

export type SettingsSwitchProps = SettingsItemProps & {
  value: boolean
  setValue: (value: boolean) => void
}

const SettingsSwitch = React.memo<SettingsSwitchProps>(props => {
  const { value, setValue } = props

  return (
    <SettingsItem onPress={() => setValue(!value)} {...props}>
      <Switch
        style={styles.switch}
        trackColor={{
          false: colors.accentLow,
          true: colors.accent,
        }}
        thumbColor={colors.text.primary}
        value={value}
        onValueChange={setValue}
      />
    </SettingsItem>
  )
})

const styles = StyleSheet.create({
  switch: {
    marginLeft: 20,
  },
})

export default SettingsSwitch
