import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { useActiveListRefresh2 } from '@app/hooks/server'
import { Artist } from '@app/models/music'
import { selectMusic } from '@app/state/music'
import { useStore } from '@app/state/store'
import React from 'react'
import { StyleSheet } from 'react-native'

const ArtistRenderItem: React.FC<{ item: Artist }> = ({ item }) => (
  <ListItem item={item} showArt={true} showStar={false} listStyle="big" />
)

const ArtistsList = () => {
  const artists = useStore(selectMusic.artists)
  const updating = useStore(selectMusic.artistsUpdating)
  const updateArtists = useStore(selectMusic.fetchArtists)

  useActiveListRefresh2(updateArtists)

  return (
    <GradientFlatList
      contentContainerStyle={styles.listContent}
      data={artists}
      renderItem={ArtistRenderItem}
      keyExtractor={item => item.id}
      onRefresh={updateArtists}
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

export default ArtistsList
