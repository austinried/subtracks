import ArtistArt from '@app/components/ArtistArt'
import GradientFlatList from '@app/components/GradientFlatList'
import PressableOpacity from '@app/components/PressableOpacity'
import { Artist } from '@app/models/music'
import { artistsAtom, artistsUpdatingAtom, useUpdateArtists } from '@app/state/music'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import React, { useEffect } from 'react'
import { StyleSheet, Text } from 'react-native'

const ArtistItem: React.FC<{ item: Artist }> = ({ item }) => {
  const navigation = useNavigation()

  return (
    <PressableOpacity
      style={styles.item}
      onPress={() => navigation.navigate('ArtistView', { id: item.id, title: item.name })}>
      <ArtistArt id={item.id} width={70} height={70} />
      <Text style={styles.title}>{item.name}</Text>
    </PressableOpacity>
  )
}

const ArtistItemLoader: React.FC<{ item: Artist }> = props => (
  <React.Suspense fallback={<Text>Loading...</Text>}>
    <ArtistItem {...props} />
  </React.Suspense>
)

const ArtistsList = () => {
  const artists = useAtomValue(artistsAtom)
  const updating = useAtomValue(artistsUpdatingAtom)
  const updateArtists = useUpdateArtists()

  useEffect(() => {
    if (artists.length === 0) {
      updateArtists()
    }
  })

  const renderItem: React.FC<{ item: Artist }> = ({ item }) => <ArtistItemLoader item={item} />

  return (
    <GradientFlatList
      data={artists}
      renderItem={renderItem}
      keyExtractor={item => item.id}
      onRefresh={updateArtists}
      refreshing={updating}
      overScrollMode="never"
    />
  )
}

const ArtistsTab = () => <ArtistsList />

const styles = StyleSheet.create({
  item: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-start',
    marginVertical: 6,
    marginLeft: 10,
  },
  title: {
    fontFamily: font.semiBold,
    fontSize: 16,
    color: colors.text.primary,
    marginLeft: 14,
  },
})

export default ArtistsTab
