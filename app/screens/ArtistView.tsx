import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import React, { useEffect } from 'react'
import { Text } from 'react-native'
import { artistInfoAtomFamily } from '@app/state/music'
import text from '@app/styles/text'
import ArtistArt from '@app/components/ArtistArt'
import GradientScrollView from '@app/components/GradientScrollView'

const ArtistDetails: React.FC<{ id: string }> = ({ id }) => {
  const artist = useAtomValue(artistInfoAtomFamily(id))

  if (!artist) {
    return <></>
  }

  return (
    <GradientScrollView
      style={{
        flex: 1,
      }}
      contentContainerStyle={{
        alignItems: 'center',
        // paddingTop: coverSize / 8,
      }}>
      <Text style={text.paragraph}>{artist.name}</Text>
      <ArtistArt id={artist.id} height={200} width={200} />
    </GradientScrollView>
  )
}

const ArtistView: React.FC<{
  id: string
  title: string
}> = ({ id, title }) => {
  const navigation = useNavigation()

  useEffect(() => {
    navigation.setOptions({ title })
  })

  return (
    <React.Suspense fallback={<Text>Loading...</Text>}>
      <ArtistDetails id={id} />
    </React.Suspense>
  )
}

export default React.memo(ArtistView)
