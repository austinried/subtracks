import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import React, { useEffect } from 'react'
import { StyleSheet, Text } from 'react-native'
import { artistInfoAtomFamily } from '@app/state/music'
import ArtistArt from '@app/components/ArtistArt'
import GradientScrollView from '@app/components/GradientScrollView'
import font from '@app/styles/font'
import colors from '@app/styles/colors'

const ArtistDetails: React.FC<{ id: string }> = ({ id }) => {
  const artist = useAtomValue(artistInfoAtomFamily(id))

  if (!artist) {
    return <></>
  }

  return (
    <GradientScrollView style={styles.scroll} contentContainerStyle={styles.scrollContent}>
      <Text style={styles.title}>{artist.name}</Text>
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

const styles = StyleSheet.create({
  scroll: {
    flex: 1,
  },
  scrollContent: {
    alignItems: 'center',
  },
  title: {
    fontFamily: font.regular,
    fontSize: 16,
    color: colors.text.primary,
  },
})

export default React.memo(ArtistView)
