import GradientScrollView from '@app/components/GradientScrollView'
import Header from '@app/components/Header'
import ListItem from '@app/components/ListItem'
import { searchResultsAtom, useUpdateSearchResults } from '@app/state/music'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useAtomValue } from 'jotai/utils'
import React, { useState } from 'react'
import { StatusBar, StyleSheet, View, TextInput } from 'react-native'

const Search = () => {
  const [text, setText] = useState('')
  const updateSearch = useUpdateSearchResults()
  const results = useAtomValue(searchResultsAtom)

  const onSubmitEditing = () => {
    console.log(text)
    updateSearch(text)
  }

  return (
    <GradientScrollView style={styles.scroll} contentContainerStyle={styles.scrollContentContainer}>
      <View style={styles.content}>
        <TextInput
          style={styles.textInput}
          placeholder="Search"
          placeholderTextColor="grey"
          selectionColor={colors.text.secondary}
          value={text}
          onChangeText={setText}
          onSubmitEditing={onSubmitEditing}
        />
        <Header>Artists</Header>
        {results.artists.map(a => (
          <ListItem key={a.id} item={a} showArt={true} showStar={false} />
        ))}
        <Header>Albums</Header>
        {results.albums.map(a => (
          <ListItem key={a.id} item={a} showArt={true} showStar={false} subtitle={a.artist} />
        ))}
        <Header>Songs</Header>
        {results.songs.map(a => (
          <ListItem key={a.id} item={a} showArt={true} showStar={false} subtitle={a.artist} />
        ))}
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
  },
  textInput: {
    backgroundColor: '#515151',
    fontFamily: font.regular,
    fontSize: 18,
    color: colors.text.primary,
    marginTop: 20,
    paddingHorizontal: 12,
  },
  itemText: {
    color: colors.text.primary,
    fontFamily: font.regular,
    fontSize: 14,
  },
})

export default Search
