import Button from '@app/components/Button'
import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import ListItem from '@app/components/ListItem'
import NothingHere from '@app/components/NothingHere'
import TextInput from '@app/components/TextInput'
import { useQuerySearchResults } from '@app/hooks/query'
import { useActiveServerRefresh } from '@app/hooks/settings'
import { useSetQueue } from '@app/hooks/trackplayer'
import { Album, Artist, SearchResults, Song } from '@app/models/library'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useFocusEffect, useNavigation } from '@react-navigation/native'
import equal from 'fast-deep-equal/es6/react'
import debounce from 'lodash.debounce'
import React, { useCallback, useMemo, useRef, useState } from 'react'
import {
  ActivityIndicator,
  InteractionManager,
  ScrollView,
  StyleSheet,
  TextInput as ReactTextInput,
  View,
} from 'react-native'
import { useSafeAreaInsets } from 'react-native-safe-area-context'

const SongItem = React.memo<{ item: Song }>(({ item }) => {
  const { setQueue, isReady, contextId } = useSetQueue('song', [item])

  return (
    <ListItem
      item={item}
      contextId={contextId}
      queueId={0}
      showArt={true}
      showStar={false}
      onPress={() => setQueue({ title: item.title, playTrack: 0 })}
      disabled={!isReady}
    />
  )
}, equal)

const ResultsCategory = React.memo<{
  name: string
  query: string
  items: (Artist | Album | Song)[]
  type: 'artist' | 'album' | 'song'
}>(({ name, query, type, items }) => {
  const navigation = useNavigation()

  if (items.length === 0) {
    return <></>
  }

  return (
    <>
      <Header>{name}</Header>
      {items.map(a =>
        type === 'song' ? (
          <SongItem key={a.id} item={a as Song} />
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
}, equal)

const Results = React.memo<{
  results: SearchResults
  query: string
}>(({ results, query }) => {
  return (
    <>
      <ResultsCategory name="Artists" query={query} type={'artist'} items={results.artists} />
      <ResultsCategory name="Albums" query={query} type={'album'} items={results.albums} />
      <ResultsCategory name="Songs" query={query} type={'song'} items={results.songs} />
    </>
  )
}, equal)

const Search = () => {
  const [query, setQuery] = useState('')
  const { data, isLoading } = useQuerySearchResults({ query, albumCount: 5, artistCount: 5, songCount: 5 })

  const [text, setText] = useState('')
  const searchBarRef = useRef<ReactTextInput>(null)
  const scrollRef = useRef<ScrollView>(null)
  const paddingTop = useSafeAreaInsets().top

  useFocusEffect(
    useCallback(() => {
      const task = InteractionManager.runAfterInteractions(() => {
        setTimeout(() => {
          if (text) {
            return
          }
          setText('')
          setQuery('')
          searchBarRef.current?.focus()
          scrollRef.current?.scrollTo({ y: 0, animated: true })
        }, 50)
      })
      return () => task.cancel()
    }, [text]),
  )

  useActiveServerRefresh(
    useCallback(() => {
      setText('')
      setQuery('')
    }, []),
  )

  const debouncedSetQuery = useMemo(
    () =>
      debounce((value: string) => {
        setQuery(value)
      }, 400),
    [],
  )

  const onChangeText = useCallback(
    (value: string) => {
      setText(value)
      debouncedSetQuery(value)
    },
    [setText, debouncedSetQuery],
  )

  const resultsCount =
    (data?.pages.reduce((acc, val) => (acc += val.artists.length), 0) || 0) +
    (data?.pages.reduce((acc, val) => (acc += val.albums.length), 0) || 0) +
    (data?.pages.reduce((acc, val) => (acc += val.songs.length), 0) || 0)

  return (
    <GradientScrollView ref={scrollRef} style={styles.scroll} contentContainerStyle={{ paddingTop }}>
      <View style={styles.content}>
        <View style={styles.inputBar}>
          <TextInput
            ref={searchBarRef}
            style={styles.textInput}
            placeholder="Search"
            value={text}
            onChangeText={onChangeText}
          />
          <ActivityIndicator animating={isLoading} size="small" color={colors.text.secondary} style={styles.activity} />
        </View>
        {data !== undefined && resultsCount > 0 ? (
          <Results results={data.pages[0]} query={text} />
        ) : (
          <NothingHere style={styles.noResults} />
        )}
      </View>
    </GradientScrollView>
  )
}

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
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
