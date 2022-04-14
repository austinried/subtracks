import FilterButton from '@app/components/FilterButton'
import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { withSuspenseMemo } from '@app/components/withSuspense'
import { useQueryArtists } from '@app/hooks/query'
import { Artist } from '@app/models/library'
import { ArtistFilterType } from '@app/models/settings'
import { useStore, useStoreDeep } from '@app/state/store'
import React, { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { StyleSheet, View } from 'react-native'

const ArtistRenderItem: React.FC<{ item: Artist }> = ({ item }) => (
  <ListItem item={item} showArt={true} showStar={false} listStyle="big" style={styles.listItem} />
)

const filterValues: ArtistFilterType[] = [
  'alphabeticalByName', //
  'starred',
  'random',
]

const ArtistFilterButton = withSuspenseMemo(() => {
  const { t } = useTranslation('resources.artist.lists')
  const filterType = useStoreDeep(store => store.settings.screens.library.artistsFilter.type)
  const setFilterType = useStore(store => store.setLibraryArtistFilterType)

  return (
    <FilterButton
      data={filterValues.map(value => ({ value, text: t(value) }))}
      value={filterType}
      onSelect={selection => setFilterType(selection as ArtistFilterType)}
      title={t('sort')}
    />
  )
})

const ArtistsList = () => {
  const filterType = useStore(store => store.settings.screens.library.artistsFilter.type)

  const { isLoading, data, refetch } = useQueryArtists()
  const [sortedList, setSortedList] = useState<Artist[]>([])

  useEffect(() => {
    if (!data) {
      setSortedList([])
      return
    }

    const list = Object.values(data.byId)
    switch (filterType) {
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
  }, [filterType, data])

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
      <ArtistFilterButton />
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
