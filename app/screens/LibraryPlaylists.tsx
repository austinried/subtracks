import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { useActiveListRefresh2 } from '@app/hooks/server'
import { PlaylistListItem } from '@app/models/music'
import { selectMusic } from '@app/state/music'
import { useStore } from '@app/state/store'
import React from 'react'
import { StyleSheet } from 'react-native'

const PlaylistRenderItem: React.FC<{ item: PlaylistListItem }> = ({ item }) => (
  <ListItem item={item} showArt={true} showStar={false} listStyle="big" />
)

const PlaylistsList = () => {
  const playlists = useStore(selectMusic.playlists)
  const updating = useStore(selectMusic.playlistsUpdating)
  const updatePlaylists = useStore(selectMusic.fetchPlaylists)

  useActiveListRefresh2(updatePlaylists)

  return (
    <GradientFlatList
      contentContainerStyle={styles.listContent}
      data={playlists}
      renderItem={PlaylistRenderItem}
      keyExtractor={item => item.id}
      onRefresh={updatePlaylists}
      refreshing={updating}
      overScrollMode="never"
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
