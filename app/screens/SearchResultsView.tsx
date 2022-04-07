import GradientFlatList from '@app/components/GradientFlatList'
import ListItem from '@app/components/ListItem'
import { useQuerySearchResults } from '@app/hooks/query'
import { useSetQueue } from '@app/hooks/trackplayer'
import { Album, Artist, Song } from '@app/models/library'
import { Search3Params } from '@app/subsonic/params'
import { useNavigation } from '@react-navigation/native'
import React, { useEffect } from 'react'
import { StyleSheet } from 'react-native'

type SearchListItemType = Album | Song | Artist

const SongResultsListItem: React.FC<{ item: Song }> = ({ item }) => {
  const { setQueue, isReady, contextId } = useSetQueue('song', [item])

  return (
    <ListItem
      item={item}
      contextId={contextId}
      queueId={0}
      showArt={true}
      showStar={false}
      listStyle="small"
      onPress={() => setQueue({ title: item.title, playTrack: 0 })}
      style={styles.listItem}
      disabled={!isReady}
    />
  )
}

const OtherResultsListItem: React.FC<{ item: SearchListItemType }> = ({ item }) => {
  return (
    <ListItem
      item={item}
      contextId={item.id}
      queueId={0}
      showArt={true}
      showStar={false}
      listStyle="small"
      style={styles.listItem}
    />
  )
}

const ResultsListItem: React.FC<{ item: SearchListItemType }> = ({ item }) => {
  if (item.itemType === 'song') {
    return <SongResultsListItem item={item} />
  } else {
    return <OtherResultsListItem item={item} />
  }
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
