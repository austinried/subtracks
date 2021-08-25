import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { useFetchPaginatedList } from '@app/hooks/list'
import { AlbumListItem, Artist, Song } from '@app/models/music'
import { selectMusic } from '@app/state/music'
import { useStore } from '@app/state/store'
import { selectTrackPlayer } from '@app/state/trackplayer'
import { useNavigation } from '@react-navigation/native'
import React, { useCallback, useEffect } from 'react'
import { StyleSheet } from 'react-native'

type SearchListItemType = AlbumListItem | Song | Artist

const ResultsListItem: React.FC<{ item: SearchListItemType }> = ({ item }) => {
  const setQueue = useStore(selectTrackPlayer.setQueue)

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
  const fetchSearchResults = useStore(selectMusic.fetchSearchResults)
  const { list, refreshing, refresh, fetchNextPage } = useFetchPaginatedList<SearchListItemType>(
    useCallback(
      (size, offset) =>
        fetchSearchResults(query, type, size, offset).then(results => {
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
        }),
      [fetchSearchResults, query, type],
    ),
    100,
  )

  useEffect(() => {
    navigation.setOptions({
      title: `Search: "${query}"`,
    })
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <GradientFlatList
      data={list}
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
