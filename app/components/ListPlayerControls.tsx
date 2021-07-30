import Button from '@app/components/Button'
import { Song } from '@app/models/music'
import { useSetQueue } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import React, { useState } from 'react'
import { StyleSheet, View, ViewStyle } from 'react-native'
import Icon from 'react-native-vector-icons/Ionicons'
import IconMat from 'react-native-vector-icons/MaterialIcons'

const ListPlayerControls = React.memo<{
  songs: Song[]
  typeName: string
  queueName: string
  style?: ViewStyle
}>(({ songs, typeName, queueName, style }) => {
  const [downloaded, setDownloaded] = useState(false)
  const setQueue = useSetQueue()

  return (
    <View style={[styles.controls, style]}>
      <View style={styles.controlsSide}>
        <Button buttonStyle={downloaded ? 'highlight' : 'hollow'} onPress={() => setDownloaded(!downloaded)}>
          {downloaded ? (
            <IconMat name="file-download-done" size={26} color={colors.text.primary} />
          ) : (
            <IconMat name="file-download" size={26} color={colors.text.primary} />
          )}
        </Button>
      </View>
      <View style={styles.controlsCenter}>
        <Button title={`Play ${typeName}`} onPress={() => setQueue(songs, queueName, undefined, false)} />
      </View>
      <View style={styles.controlsSide}>
        <Button onPress={() => setQueue(songs, queueName, undefined, true)}>
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
  },
})

export default ListPlayerControls
