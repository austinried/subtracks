import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { useQuerySearchResults } from '@app/hooks/query'
import { Album, Artist, Song } from '@app/models/library'
import { useStore } from '@app/state/store'
import { Search3Params } from '@app/subsonic/params'
import { useNavigation } from '@react-navigation/native'
import React, { useEffect } from 'react'
import { StyleSheet } from 'react-native'

type SearchListItemType = Album | Song | Artist

const ResultsListItem: React.FC<{ item: SearchListItemType }> = ({ item }) => {
  const setQueue = useStore(store => store.setQueue)

  let onPress
  if (item.itemType === 'song') {
    onPress = () => setQueue([item], item.title, 'song', item.id, 0)
  }

  return (
    <ListItem
      item={item}
      contextId={item.id}
      queueId={0}
      showArt={true}
      showStar={false}
      listStyle="small"
      onPress={onPress}
      style={styles.listItem}
    />
  )
}

const SearchResultsRenderItem: React.FC<{ item: SearchListItemType }> = ({ item }) => <ResultsListItem item={item} />

const SearchResultsView: React.FC<{
  query: string
  type: 'album' | 'artist' | 'song'
}> = ({ query, type }) => {
  const navigation = useNavigation()

  const size = 100
  const params: Search3Params = { query }

  if (type === 'album') {
    params.albumCount = size
  } else if (type === 'artist') {
    params.artistCount = size
  } else {
    params.songCount = size
  }

  const { data, isLoading, refetch, fetchNextPage } = useQuerySearchResults(params)

  const items: (Artist | Album | Song)[] = []
  if (type === 'album') {
    data && items.push(...data.pages.flatMap(p => p.albums))
  } else if (type === 'artist') {
    data && items.push(...data.pages.flatMap(p => p.artists))
  } else {
    data && items.push(...data.pages.flatMap(p => p.songs))
  }

  useEffect(() => {
    navigation.setOptions({
      title: `Search: "${query}"`,
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <GradientFlatList
      data={items}
      renderItem={SearchResultsRenderItem}
      keyExtractor={(item, i) => i.toString()}
      onRefresh={refetch}
      refreshing={isLoading}
      overScrollMode="never"
      onEndReached={() => fetchNextPage}
      removeClippedSubviews={true}
      onEndReachedThreshold={2}
      contentMarginTop={6}
      windowSize={5}
    />
  )
}

const styles = StyleSheet.create({
  listItem: {
    paddingHorizontal: 10,
  },
})

export default SearchResultsView
