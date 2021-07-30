import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { Artist } from '@app/models/music'
import { artistsAtom, artistsUpdatingAtom, useUpdateArtists } from '@app/state/music'
import { useActiveListRefresh } from '@app/state/server'
import { useAtomValue } from 'jotai/utils'
import React from 'react'
import { StyleSheet } from 'react-native'

const ArtistRenderItem: React.FC<{ item: Artist }> = ({ item }) => (
  <ListItem item={item} showArt={true} showStar={false} listStyle="big" />
)

const ArtistsList = () => {
  const artists = useAtomValue(artistsAtom)
  const updating = useAtomValue(artistsUpdatingAtom)
  const updateArtists = useUpdateArtists()

  useActiveListRefresh(artists, updateArtists)

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
