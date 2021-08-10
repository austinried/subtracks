import GradientScrollView from '@app/components/GradientScrollView'
import ListItem from '@app/components/ListItem'
import NowPlayingBar from '@app/components/NowPlayingBar'
import { mapTrackExtToSong, useSkipTo } from '@app/hooks/trackplayer'
import { useStore } from '@app/state/store'
import { selectTrackPlayer } from '@app/state/trackplayer'
import React from 'react'
import { StyleSheet, View } from 'react-native'

const NowPlayingQueue = React.memo<{}>(() => {
  const queue = useStore(selectTrackPlayer.queue)
  const skipTo = useSkipTo()

  return (
    <View style={styles.outerContainer}>
      <GradientScrollView style={styles.container}>
        <View style={styles.content}>
          {queue.map(mapTrackExtToSong).map((song, i) => (
            <ListItem
              key={i}
              item={song}
              onPress={() => skipTo(i)}
              showArt={true}
              subtitle={`${song.artist} • ${song.album}`}
            />
          ))}
        </View>
      </GradientScrollView>
      <NowPlayingBar />
    </View>
  )
})

const styles = StyleSheet.create({
  outerContainer: {
    flex: 1,
  },
  container: {
    flex: 1,
  },
  content: {
    alignItems: 'center',
    paddingTop: 10,
    paddingHorizontal: 20,
  },
})

export default NowPlayingQueue
