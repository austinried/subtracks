import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import React, { useEffect } from 'react'
import { Pressable } from 'react-native'
import { Text } from 'react-native'
import { Artist } from '@app/models/music'
import { artistsAtom, artistsUpdatingAtom, useUpdateArtists } from '@app/state/music'
import textStyles from '@app/styles/text'
import ArtistArt from '@app/components/ArtistArt'
import GradientFlatList from '@app/components/GradientFlatList'

const ArtistItem: React.FC<{ item: Artist }> = ({ item }) => {
  const navigation = useNavigation()

  return (
    <Pressable
      style={{
        flexDirection: 'row',
        alignItems: 'center',
        marginVertical: 6,
        marginLeft: 6,
      }}
      onPress={() => navigation.navigate('ArtistView', { id: item.id, title: item.name })}>
      <ArtistArt id={item.id} width={56} height={56} />
      <Text
        style={{
          ...textStyles.paragraph,
          marginLeft: 12,
        }}>
        {item.name}
      </Text>
    </Pressable>
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

export default ArtistsTab
