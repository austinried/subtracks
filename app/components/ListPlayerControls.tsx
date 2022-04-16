import Button from '@app/components/Button'
import { Song } from '@app/models/library'
import colors from '@app/styles/colors'
import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { StyleProp, StyleSheet, View, ViewStyle } from 'react-native'
import Icon from 'react-native-vector-icons/Ionicons'
import IconMat from 'react-native-vector-icons/MaterialIcons'
import { withSuspenseMemo } from './withSuspense'

const ListPlayerControls = withSuspenseMemo<{
  songs: Song[]
  listType: 'album' | 'playlist'
  style?: StyleProp<ViewStyle>
  play: () => void
  shuffle: () => void
  disabled?: boolean
}>(({ listType, style, play, shuffle, disabled }) => {
  const [downloaded, setDownloaded] = useState(false)
  const { t } = useTranslation()

  return (
    <View style={[styles.controls, style]}>
      <View style={styles.controlsSide}>
        <Button
          disabled={true}
          buttonStyle={downloaded ? 'highlight' : 'hollow'}
          onPress={() => setDownloaded(!downloaded)}>
          {downloaded ? (
            <IconMat name="file-download-done" size={26} color={colors.text.primary} />
          ) : (
            <IconMat name="file-download" size={26} color={colors.text.primary} />
          )}
        </Button>
      </View>
      <View style={styles.controlsCenter}>
        <Button title={t(`resources.${listType}.actions.play`)} disabled={disabled} onPress={play} />
      </View>
      <View style={styles.controlsSide}>
        <Button disabled={disabled} onPress={shuffle}>
          <Icon name="shuffle" size={26} color="white" />
        </Button>
      </View>
    </View>
  )
})

const styles = StyleSheet.create({
  controls: {
    flexDirection: 'row',
  },
  controlsSide: {
    flex: 4,
    flexDirection: 'row',
    justifyContent: 'center',
  },
  controlsCenter: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    maxWidth: '65%',
  },
})

export default ListPlayerControls
