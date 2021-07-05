import React from 'react'
import { FlatList, Text, View } from 'react-native'
import { useAtomValue } from 'jotai/utils'
import { Artist } from '../models/music'
import { artistsAtom } from '../state/music'

const ArtistItem: React.FC<{ item: Artist }> = ({ item }) => (
  <View>
    <Text>{item.id}</Text>
    <Text
      style={{
        fontSize: 60,
        paddingBottom: 400,
      }}>
      {item.name}
    </Text>
  </View>
)

const List = () => {
  const artists = useAtomValue(artistsAtom)

  const renderItem: React.FC<{ item: Artist }> = ({ item }) => <ArtistItem item={item} />

  return <FlatList data={artists} renderItem={renderItem} keyExtractor={item => item.id} />
}

const ArtistsList = () => (
  <View>
    <React.Suspense fallback={<Text>Loading...</Text>}>
      <List />
    </React.Suspense>
  </View>
)

export default ArtistsList
