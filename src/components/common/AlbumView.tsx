import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import React, { useEffect } from 'react'
import { ActivityIndicator, GestureResponderEvent, StyleSheet, Text, useWindowDimensions, View } from 'react-native'
import IconFA from 'react-native-vector-icons/FontAwesome'
import IconMat from 'react-native-vector-icons/MaterialIcons'
import { albumAtomFamily } from '../../state/music'
import { currentTrackAtom, useSetQueue } from '../../state/trackplayer'
import colors from '../../styles/colors'
import text, { Font } from '../../styles/text'
import AlbumArt from './AlbumArt'
import Button from './Button'
import GradientBackground from './GradientBackground'
import ImageGradientScrollView from './ImageGradientScrollView'
import PressableOpacity from './PressableOpacity'

const SongItem: React.FC<{
  id: string
  title: string
  artist?: string
  track?: number
  onPress: (event: GestureResponderEvent) => void
}> = ({ id, title, artist, onPress }) => {
  const currentTrack = useAtomValue(currentTrackAtom)

  return (
    <View style={songStyles.container}>
      <PressableOpacity onPress={onPress} style={songStyles.text}>
        <Text style={{ ...songStyles.title, color: currentTrack?.id === id ? colors.accent : colors.text.primary }}>
          {title}
        </Text>
        <Text style={songStyles.subtitle}>{artist}</Text>
      </PressableOpacity>
      <View style={songStyles.controls}>
        <PressableOpacity onPress={undefined}>
          <IconFA name="star-o" size={26} color={colors.text.primary} />
        </PressableOpacity>
        <PressableOpacity onPress={undefined} style={songStyles.more}>
          <IconMat name="more-vert" size={32} color="white" />
        </PressableOpacity>
      </View>
    </View>
  )
}

const songStyles = StyleSheet.create({
  container: {
    marginTop: 20,
    marginLeft: 10,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  text: {
    flex: 1,
    alignItems: 'flex-start',
  },
  title: {
    fontSize: 16,
    fontFamily: Font.semiBold,
  },
  subtitle: {
    fontSize: 14,
    fontFamily: Font.regular,
    color: colors.text.secondary,
  },
  controls: {
    flexDirection: 'row',
    alignItems: 'center',
    marginLeft: 10,
  },
  more: {
    marginLeft: 8,
  },
})

const AlbumDetails: React.FC<{
  id: string
}> = ({ id }) => {
  const album = useAtomValue(albumAtomFamily(id))
  const layout = useWindowDimensions()
  const setQueue = useSetQueue()

  const coverSize = layout.width - layout.width / 2.5

  if (!album) {
    return <Text style={text.paragraph}>No Album</Text>
  }

  return (
    <ImageGradientScrollView
      imageUri={album.coverArtThumbUri}
      imageKey={`${album.name}${album.artist}`}
      style={{
        flex: 1,
      }}
      contentContainerStyle={{
        alignItems: 'center',
        paddingTop: coverSize / 8,
      }}>
      <AlbumArt id={album.id} height={coverSize} width={coverSize} />
      <Text
        style={{
          ...text.title,
          marginTop: 12,
          width: layout.width - layout.width / 8,
          textAlign: 'center',
        }}>
        {album.name}
      </Text>

      <Text
        style={{
          ...text.itemSubtitle,
          fontSize: 14,
          marginTop: 4,
          marginBottom: 20,
          width: layout.width - layout.width / 8,
          textAlign: 'center',
        }}>
        {album.artist}
        {album.year ? ` • ${album.year}` : ''}
      </Text>

      <View
        style={{
          flexDirection: 'row',
        }}>
        <Button title="Play Album" onPress={() => setQueue(album.songs, album.name, album.songs[0].id)} />
      </View>

      <View
        style={{
          width: layout.width - layout.width / 20,
          marginTop: 20,
          marginBottom: 30,
        }}>
        {album.songs
          .sort((a, b) => {
            if (b.track && a.track) {
              return a.track - b.track
            } else {
              return a.title.localeCompare(b.title)
            }
          })
          .map(s => (
            <SongItem
              key={s.id}
              id={s.id}
              title={s.title}
              artist={s.artist}
              track={s.track}
              onPress={() => setQueue(album.songs, album.name, s.id)}
            />
          ))}
      </View>
    </ImageGradientScrollView>
  )
}

const AlbumViewFallback = () => {
  const layout = useWindowDimensions()

  const coverSize = layout.width - layout.width / 2.5

  return (
    <GradientBackground
      style={{
        alignItems: 'center',
        paddingTop: coverSize / 8 + coverSize / 2 - 18,
      }}>
      <ActivityIndicator size="large" color={colors.accent} />
    </GradientBackground>
  )
}

const AlbumView: React.FC<{
  id: string
  title: string
}> = ({ id, title }) => {
  const navigation = useNavigation()

  useEffect(() => {
    navigation.setOptions({ title })
  })

  return (
    <React.Suspense fallback={<AlbumViewFallback />}>
      <AlbumDetails id={id} />
    </React.Suspense>
  )
}

export default React.memo(AlbumView)
