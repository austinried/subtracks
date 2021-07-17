import Button from '@app/components/Button'
import CoverArt from '@app/components/CoverArt'
import GradientBackground from '@app/components/GradientBackground'
import ImageGradientScrollView from '@app/components/ImageGradientScrollView'
import SongItem from '@app/components/SongItem'
import { albumAtomFamily } from '@app/state/music'
import { useSetQueue } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import React, { useEffect } from 'react'
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native'

const AlbumDetails: React.FC<{
  id: string
}> = ({ id }) => {
  const album = useAtomValue(albumAtomFamily(id))
  const setQueue = useSetQueue()

  if (!album) {
    return <></>
  }

  return (
    <ImageGradientScrollView
      imageUri={album.coverArtThumbUri}
      imageKey={`${album.name}${album.artist}`}
      style={styles.container}>
      <View style={styles.content}>
        <View style={styles.cover}>
          <CoverArt coverArtUri={album.coverArtUri} height="100%" width="100%" />
        </View>
        <Text style={styles.title}>{album.name}</Text>
        <Text style={styles.subtitle}>
          {album.artist}
          {album.year ? ` • ${album.year}` : ''}
        </Text>
        <View style={styles.controls}>
          <Button title="Play Album" onPress={() => setQueue(album.songs, album.name, album.songs[0].id)} />
        </View>
        <View style={styles.songs}>
          {album.songs
            .sort((a, b) => {
              if (b.track && a.track) {
                return a.track - b.track
              } else {
                return a.title.localeCompare(b.title)
              }
            })
            .map(s => (
              <SongItem key={s.id} song={s} onPress={() => setQueue(album.songs, album.name, s.id)} />
            ))}
        </View>
      </View>
    </ImageGradientScrollView>
  )
}

const AlbumViewFallback = () => (
  <GradientBackground style={styles.fallback}>
    <ActivityIndicator size="large" color={colors.accent} />
  </GradientBackground>
)

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

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    alignItems: 'center',
    paddingTop: 10,
    paddingHorizontal: 20,
  },
  title: {
    fontSize: 24,
    fontFamily: font.bold,
    color: colors.text.primary,
    marginTop: 12,
    textAlign: 'center',
  },
  subtitle: {
    fontFamily: font.regular,
    color: colors.text.secondary,
    fontSize: 14,
    marginTop: 4,
    marginBottom: 20,
    textAlign: 'center',
  },
  cover: {
    height: 220,
    width: 220,
  },
  controls: {
    flexDirection: 'row',
  },
  songs: {
    marginTop: 26,
    marginBottom: 30,
    width: '100%',
  },
  fallback: {
    alignItems: 'center',
    paddingTop: 100,
  },
})

export default React.memo(AlbumView)
