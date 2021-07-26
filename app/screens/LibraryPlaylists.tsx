import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { PlaylistListItem } from '@app/models/music'
import { playlistsAtom, playlistsUpdatingAtom, useUpdatePlaylists } from '@app/state/music'
import { useAtomValue } from 'jotai/utils'
import React, { useEffect } from 'react'
import { StyleSheet } from 'react-native'

const PlaylistRenderItem: React.FC<{ item: PlaylistListItem }> = ({ item }) => (
  <ListItem item={item} showArt={true} showStar={false} listStyle="big" />
)

const PlaylistsList = () => {
  const playlists = useAtomValue(playlistsAtom)
  const updating = useAtomValue(playlistsUpdatingAtom)
  const updatePlaylists = useUpdatePlaylists()

  useEffect(() => {
    if (playlists.length === 0) {
      updatePlaylists()
    }
  })

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
