import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import NowPlayingBar from '@app/components/NowPlayingBar'
import { useSkipTo } from '@app/hooks/trackplayer'
import { Song } from '@app/models/music'
import { useStore } from '@app/state/store'
import { selectTrackPlayer } from '@app/state/trackplayer'
import { selectTrackPlayerMap } from '@app/state/trackplayermap'
import React from 'react'
import { StyleSheet, View } from 'react-native'

const SongRenderItem: React.FC<{
  item: {
    song: Song
    i: number
    onPress: () => void
  }
}> = ({ item }) => (
  <ListItem
    item={item.song}
    queueId={item.i}
    onPress={item.onPress}
    showArt={true}
    style={styles.listItem}
    subtitle={`${item.song.artist} • ${item.song.album}`}
  />
)

const NowPlayingQueue = React.memo<{}>(() => {
  const queue = useStore(selectTrackPlayer.queue)
  const mapTrackExtToSong = useStore(selectTrackPlayerMap.mapTrackExtToSong)
  const skipTo = useSkipTo()

  return (
    <View style={styles.container}>
      <GradientFlatList
        data={queue.map(mapTrackExtToSong).map((song, i) => ({ song, i, onPress: () => skipTo(i) }))}
        renderItem={SongRenderItem}
        keyExtractor={(item, i) => i.toString()}
        overScrollMode="never"
        windowSize={7}
        contentMarginTop={10}
      />
      <NowPlayingBar />
    </View>
  )
})

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  listItem: {
    paddingHorizontal: 20,
  },
})

export default NowPlayingQueue
