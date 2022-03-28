import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { useFetchPaginatedList } from '@app/hooks/list'
import { Album, Artist, Song } from '@app/models/library'
import { useStore, useStoreDeep } from '@app/state/store'
import { Search3Params } from '@app/subsonic/params'
import { mapById } from '@app/util/state'
import { useNavigation } from '@react-navigation/native'
import React, { useCallback, useEffect } from 'react'
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
  const fetchSearchResults = useStore(store => store.fetchSearchResults)
  const { list, refreshing, refresh, fetchNextPage } = useFetchPaginatedList(
    useCallback(
      async (size, offset) => {
        const params: Search3Params = { query }
        if (type === 'album') {
          params.albumCount = size
          params.albumOffset = offset
        } else if (type === 'artist') {
          params.artistCount = size
          params.artistOffset = offset
        } else if (type === 'song') {
          params.songCount = size
          params.songOffset = offset
        } else {
          params.albumCount = 5
          params.artistCount = 5
          params.songCount = 5
        }

        const results = await fetchSearchResults(params)

        switch (type) {
          case 'album':
            return results.albums
          case 'artist':
            return results.artists
          case 'song':
            return results.songs
          default:
            return []
        }
      },
      [fetchSearchResults, query, type],
    ),
    100,
  )

  const items: SearchListItemType[] = useStoreDeep(
    useCallback(
      store => {
        switch (type) {
          case 'album':
            return mapById(store.library.albums, list)
          case 'artist':
            return mapById(store.library.artists, list)
          case 'song':
            return mapById(store.library.songs, list)
          default:
            return []
        }
      },
      [list, type],
    ),
  )

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
      onRefresh={refresh}
      refreshing={refreshing}
      overScrollMode="never"
      onEndReached={fetchNextPage}
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
