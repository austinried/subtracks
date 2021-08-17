import Button from '@app/components/Button'
import { Song } from '@app/models/music'
import { useStore } from '@app/state/store'
import { QueueContextType, selectTrackPlayer } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import React, { useState } from 'react'
import { StyleProp, StyleSheet, View, ViewStyle } from 'react-native'
import Icon from 'react-native-vector-icons/Ionicons'
import IconMat from 'react-native-vector-icons/MaterialIcons'

const ListPlayerControls = React.memo<{
  songs: Song[]
  typeName: string
  queueName: string
  queueContextType: QueueContextType
  queueContextId: string
  style?: StyleProp<ViewStyle>
}>(({ songs, typeName, queueName, queueContextType, queueContextId, style }) => {
  const [downloaded, setDownloaded] = useState(false)
  const setQueue = useStore(selectTrackPlayer.setQueue)

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
        <Button
          title={`Play ${typeName}`}
          onPress={() => setQueue(songs, queueName, queueContextType, queueContextId, undefined, false)}
        />
      </View>
      <View style={styles.controlsSide}>
        <Button onPress={() => setQueue(songs, queueName, queueContextType, queueContextId, undefined, true)}>
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
