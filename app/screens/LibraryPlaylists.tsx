import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { usePlaylistCover2x2Fetch } from '@app/components/PlaylistCover'
import { useFetchList } from '@app/hooks/list'
import { PlaylistListItem } from '@app/models/music'
import { selectMusic } from '@app/state/music'
import { useStore } from '@app/state/store'
import React from 'react'
import { StyleSheet } from 'react-native'

const PlaylistRenderItem: React.FC<{ item: PlaylistListItem }> = ({ item }) => {
  // technically violating the hook rules but this is really a constant branching because it is server dependent
  const CustomArt = item.coverArt != null ? null : usePlaylistCover2x2Fetch(item)
  return <ListItem item={item} showArt={true} showStar={false} listStyle="big" style={styles.listItem} CustomArt={CustomArt} />
}

const PlaylistsList = () => {
  const fetchPlaylists = useStore(selectMusic.fetchPlaylists)
  const { list, refreshing, refresh } = useFetchList(fetchPlaylists)

  return (
    <GradientFlatList
      data={list}
      renderItem={item => <PlaylistRenderItem item={item.item} />}
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
