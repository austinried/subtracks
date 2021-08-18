import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { useFetchList } from '@app/hooks/list'
import { Artist } from '@app/models/music'
import { selectMusic } from '@app/state/music'
import { useStore } from '@app/state/store'
import React from 'react'
import { StyleSheet } from 'react-native'

const ArtistRenderItem: React.FC<{ item: Artist }> = ({ item }) => (
  <ListItem item={item} showArt={true} showStar={false} listStyle="big" />
)

const ArtistsList = () => {
  const fetchArtists = useStore(selectMusic.fetchArtists)
  const { list, refreshing, refresh } = useFetchList(fetchArtists)

  return (
    <GradientFlatList
      contentContainerStyle={styles.listContent}
      data={list}
      renderItem={ArtistRenderItem}
      keyExtractor={item => item.id}
      onRefresh={refresh}
      refreshing={refreshing}
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
