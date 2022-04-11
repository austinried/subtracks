import FilterButton, { OptionData } from '@app/components/FilterButton'
import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { useQueryArtists } from '@app/hooks/query'
import { Artist } from '@app/models/library'
import { ArtistFilterType } from '@app/models/settings'
import { useStore, useStoreDeep } from '@app/state/store'
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

const ArtistsList = () => {
  const filter = useStoreDeep(store => store.settings.screens.library.artistsFilter)
  const setFilter = useStore(store => store.setLibraryArtistFiler)

  const { isLoading, data, refetch } = useQueryArtists()
  const [sortedList, setSortedList] = useState<Artist[]>([])

  useEffect(() => {
    if (!data) {
      setSortedList([])
      return
    }

    const list = Object.values(data.byId)
    switch (filter.type) {
      case 'random':
        setSortedList([...list].sort(() => Math.random() - 0.5))
        break
      case 'starred':
        setSortedList([...list].filter(a => a.starred))
        break
      case 'alphabeticalByName':
        setSortedList(data.allIds.map(id => data.byId[id]))
        break
      default:
        setSortedList([...list])
        break
    }
  }, [filter.type, data])

  return (
    <View style={styles.container}>
      <GradientFlatList
        data={sortedList}
        renderItem={ArtistRenderItem}
        keyExtractor={item => item.id}
        onRefresh={refetch}
        refreshing={isLoading}
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
