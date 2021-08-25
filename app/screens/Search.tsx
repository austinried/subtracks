import Button from '@app/components/Button'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import ListItem from '@app/components/ListItem'
import NothingHere from '@app/components/NothingHere'
import TextInput from '@app/components/TextInput'
import { useActiveServerRefresh } from '@app/hooks/server'
import { ListableItem, SearchResults, Song } from '@app/models/music'
import { selectMusic } from '@app/state/music'
import { useStore } from '@app/state/store'
import { selectTrackPlayer } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useFocusEffect, useNavigation } from '@react-navigation/native'
import debounce from 'lodash.debounce'
import React, { useCallback, useMemo, useRef, useState } from 'react'
import {
  ActivityIndicator,
  InteractionManager,
  ScrollView,
  StatusBar,
  StyleSheet,
  TextInput as ReactTextInput,
  View,
} from 'react-native'

const SongItem = React.memo<{ item: Song }>(({ item }) => {
  const setQueue = useStore(selectTrackPlayer.setQueue)

  return (
    <ListItem
      item={item}
      contextId={item.id}
      queueId={0}
      showArt={true}
      showStar={false}
      onPress={() => setQueue([item], item.title, 'song', item.id, 0)}
    />
  )
})

const ResultsCategory = React.memo<{
  name: string
  query: string
  items: ListableItem[]
}>(({ name, query, items }) => {
  const navigation = useNavigation()

  if (items.length === 0) {
    return <></>
  }

  return (
    <>
      <Header>{name}</Header>
      {items.map(a =>
        a.itemType === 'song' ? (
          <SongItem key={a.id} item={a} />
        ) : (
          <ListItem key={a.id} item={a} showArt={true} showStar={false} />
        ),
      )}
      {items.length === 5 && (
        <Button
          title="More..."
          buttonStyle="hollow"
          style={styles.more}
          onPress={() => navigation.navigate('results', { query, type: items[0].itemType })}
        />
      )}
    </>
  )
})

const Results = React.memo<{
  results: SearchResults
  query: string
}>(({ results, query }) => {
  return (
    <>
      <ResultsCategory name="Artists" query={query} items={results.artists} />
      <ResultsCategory name="Albums" query={query} items={results.albums} />
      <ResultsCategory name="Songs" query={query} items={results.songs} />
    </>
  )
})

const Search = () => {
  const fetchSearchResults = useStore(selectMusic.fetchSearchResults)
  const [results, setResults] = useState<SearchResults>({ artists: [], albums: [], songs: [] })
  const [refreshing, setRefreshing] = useState(false)
  const [text, setText] = useState('')
  const searchBarRef = useRef<ReactTextInput>(null)
  const scrollRef = useRef<ScrollView>(null)

  useFocusEffect(
    useCallback(() => {
      const task = InteractionManager.runAfterInteractions(() => {
        setTimeout(() => {
          setText('')
          setResults({ artists: [], albums: [], songs: [] })
          searchBarRef.current?.focus()
          scrollRef.current?.scrollTo({ y: 0, animated: true })
        }, 50)
      })
      return () => task.cancel()
    }, [searchBarRef, scrollRef]),
  )

  useActiveServerRefresh(
    useCallback(() => {
      setText('')
      setResults({ artists: [], albums: [], songs: [] })
    }, []),
  )

  const debouncedonUpdateSearch = useMemo(
    () =>
      debounce(async (query: string) => {
        setRefreshing(true)
        setResults(await fetchSearchResults(query))
        setRefreshing(false)
      }, 400),
    [fetchSearchResults],
  )

  const onChangeText = useCallback(
    (value: string) => {
      setText(value)
      debouncedonUpdateSearch(value)
    },
    [setText, debouncedonUpdateSearch],
  )

  const resultsCount = results.albums.length + results.artists.length + results.songs.length

  return (
    <GradientScrollView ref={scrollRef} style={styles.scroll} contentContainerStyle={styles.scrollContentContainer}>
      <View style={styles.content}>
        <View style={styles.inputBar}>
          <TextInput
            ref={searchBarRef}
            style={styles.textInput}
            placeholder="Search"
            value={text}
            onChangeText={onChangeText}
          />
          <ActivityIndicator
            animating={refreshing}
            size="small"
            color={colors.text.secondary}
            style={styles.activity}
          />
        </View>
        {resultsCount > 0 ? <Results results={results} query={text} /> : <NothingHere style={styles.noResults} />}
      </View>
    </GradientScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
  },
  scrollContentContainer: {
    paddingTop: StatusBar.currentHeight,
  },
  content: {
    paddingHorizontal: 20,
    paddingBottom: 20,
    alignItems: 'stretch',
  },
  inputBar: {
    justifyContent: 'center',
  },
  activity: {
    position: 'absolute',
    right: 16,
    bottom: 15,
  },
  noResults: {
    width: '100%',
  },
  itemText: {
    color: colors.text.primary,
    fontFamily: font.regular,
    fontSize: 14,
  },
  more: {
    marginTop: 5,
    marginBottom: 10,
  },
  textInput: {
    marginTop: 20,
    paddingHorizontal: 12,
    paddingRight: 46,
  },
})

export default Search
