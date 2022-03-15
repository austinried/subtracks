import FilterButton, { OptionData } from '@app/components/FilterButton'
import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { useFetchList2 } from '@app/hooks/list'
import { Artist } from '@app/models/music'
import { ArtistFilterType } from '@app/models/settings'
import { selectSettings } from '@app/state/settings'
import { Store, useStore } from '@app/state/store'
import React, { useEffect, useState } from 'react'
import { StyleSheet, View } from 'react-native'

const ArtistRenderItem: React.FC<{ item: Artist }> = ({ item }) => (
  <ListItem item={item} showArt={true} showStar={false} listStyle="big" style={styles.listItem} />
)

const filterOptions: OptionData[] = [
  { text: 'By Name', value: 'alphabeticalByName' },
  { text: 'Starred', value: 'starred' },
  { text: 'Random', value: 'random' },
]

const selectArtists = (store: Store) => store.entities.artists

const ArtistsList = () => {
  const fetchArtists = useStore(store => store.fetchLibraryArtists)
  const resetArtists = useStore(store => store.resetLibraryArtists)

  const { refreshing, refresh } = useFetchList2(fetchArtists, resetArtists)
  const artists = useStore(selectArtists)

  const filter = useStore(selectSettings.libraryArtistFilter)
  const setFilter = useStore(selectSettings.setLibraryArtistFiler)
  const [sortedList, setSortedList] = useState<Artist[]>([])

  useEffect(() => {
    const list = Object.values(artists)
    switch (filter.type) {
      case 'random':
        setSortedList([...list].sort(() => Math.random() - 0.5))
        break
      case 'starred':
        setSortedList([...list].filter(a => a.starred))
        break
      default:
        setSortedList([...list])
        break
    }
  }, [filter.type, artists])

  return (
    <View style={styles.container}>
      <GradientFlatList
        data={sortedList}
        renderItem={ArtistRenderItem}
        keyExtractor={item => item.id}
        onRefresh={refresh}
        refreshing={refreshing}
        overScrollMode="never"
        windowSize={3}
        contentMarginTop={6}
      />
      <FilterButton
        data={filterOptions}
        value={filter.type}
        onSelect={selection => {
          setFilter({
            ...filter,
            type: selection as ArtistFilterType,
          })
        }}
      />
    </View>
  )
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  listItem: {
    paddingHorizontal: 10,
  },
})

export default ArtistsList
