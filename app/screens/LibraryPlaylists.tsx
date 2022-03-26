import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { useFetchList2 } from '@app/hooks/list'
import { Playlist } from '@app/models/library'
import { useStore, useStoreDeep } from '@app/state/store'
import React from 'react'
import { StyleSheet } from 'react-native'

const PlaylistRenderItem: React.FC<{ item: Playlist }> = ({ item }) => (
  <ListItem item={item} showArt={true} showStar={false} listStyle="big" style={styles.listItem} />
)

const PlaylistsList = () => {
  const fetchPlaylists = useStore(store => store.fetchPlaylists)
  const { refreshing, refresh } = useFetchList2(fetchPlaylists)
  const playlists = useStoreDeep(store => store.library.playlists)

  return (
    <GradientFlatList
      data={Object.values(playlists)}
      renderItem={PlaylistRenderItem}
      keyExtractor={item => item.id}
      onRefresh={refresh}
      refreshing={refreshing}
      overScrollMode="never"
      windowSize={5}
      contentMarginTop={6}
    />
  )
}

const styles = StyleSheet.create({
  listItem: {
    paddingHorizontal: 10,
  },
})

export default PlaylistsList
