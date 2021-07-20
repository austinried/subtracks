import Button from '@app/components/Button'
import CoverArt from '@app/components/CoverArt'
import GradientBackground from '@app/components/GradientBackground'
import ImageGradientScrollView from '@app/components/ImageGradientScrollView'
import NothingHere from '@app/components/NothingHere'
import SongItem from '@app/components/SongItem'
import { playlistAtomFamily } from '@app/state/music'
import { useSetQueue } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import { useAtomValue } from 'jotai/utils'
import React, { useEffect } from 'react'
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native'

const PlaylistDetails: React.FC<{
  id: string
}> = ({ id }) => {
  const playlist = useAtomValue(playlistAtomFamily(id))
  const setQueue = useSetQueue()

  if (!playlist) {
    return <></>
  }

  const Songs = () => (
    <>
      <View style={styles.controls}>
        <Button title="Play Playlist" onPress={() => setQueue(playlist.songs, playlist.name)} />
      </View>
      <View style={styles.songs}>
        {playlist.songs.map((s, i) => (
          <SongItem key={i} song={s} showArt={true} onPress={() => setQueue(playlist.songs, playlist.name, i)} />
        ))}
      </View>
    </>
  )

  return (
    <ImageGradientScrollView
      imageUri={playlist.coverArtThumbUri}
      imageKey={`${playlist.id}${playlist.name}`}
      style={styles.container}>
      <View style={styles.content}>
        <CoverArt coverArtUri={playlist.coverArtUri} style={styles.cover} />
        <Text style={styles.title}>{playlist.name}</Text>
        {playlist.comment ? <Text style={styles.subtitle}>{playlist.comment}</Text> : <></>}
        {playlist.songs.length > 0 ? <Songs /> : <NothingHere height={350} width={250} />}
      </View>
    </ImageGradientScrollView>
  )
}

const PlaylistViewFallback = () => (
  <GradientBackground style={styles.fallback}>
    <ActivityIndicator size="large" color={colors.accent} />
  </GradientBackground>
)

const PlaylistView: React.FC<{
  id: string
  title: string
}> = ({ id, title }) => {
  const navigation = useNavigation()

  useEffect(() => {
    navigation.setOptions({ title })
  })

  return (
    <React.Suspense fallback={<PlaylistViewFallback />}>
      <PlaylistDetails id={id} />
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
    paddingHorizontal: 10,
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
    textAlign: 'center',
  },
  cover: {
    height: 160,
    width: 160,
  },
  songsContainer: {
    width: '100%',
    marginTop: 18,
    alignItems: 'center',
  },
  controls: {
    flexDirection: 'row',
    marginTop: 20,
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
  nothingContainer: {
    height: 400,
    backgroundColor: 'green',
    justifyContent: 'center',
    alignItems: 'center',
  },
})

export default React.memo(PlaylistView)
