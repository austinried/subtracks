import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { useFetchList } from '@app/hooks/list'
import { PlaylistListItem } from '@app/models/music'
import { selectMusic } from '@app/state/music'
import { useStore } from '@app/state/store'
import React from 'react'
import { StyleSheet } from 'react-native'

const PlaylistRenderItem: React.FC<{ item: PlaylistListItem }> = ({ item }) => (
  <ListItem item={item} showArt={true} showStar={false} listStyle="big" />
)

const PlaylistsList = () => {
  const fetchPlaylists = useStore(selectMusic.fetchPlaylists)
  const { list, refreshing, refresh } = useFetchList(fetchPlaylists)

  return (
    <GradientFlatList
      contentContainerStyle={styles.listContent}
      data={list}
      renderItem={PlaylistRenderItem}
      keyExtractor={item => item.id}
      onRefresh={refresh}
      refreshing={refreshing}
      overScrollMode="never"
      windowSize={5}
    />
  )
}

const styles = StyleSheet.create({
  listContent: {
    minHeight: '100%',
    paddingHorizontal: 10,
    paddingTop: 6,
  },
})

export default PlaylistsList
