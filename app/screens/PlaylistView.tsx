import Button from '@app/components/Button'
import CoverArt from '@app/components/CoverArt'
import GradientBackground from '@app/components/GradientBackground'
import ImageGradientScrollView from '@app/components/ImageGradientScrollView'
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

  return (
    <ImageGradientScrollView
      imageUri={playlist.coverArtThumbUri}
      imageKey={`${playlist.id}${playlist.name}`}
      style={styles.container}>
      <View style={styles.content}>
        <CoverArt coverArtUri={playlist.coverArtUri} style={styles.cover} />
        <Text style={styles.title}>{playlist.name}</Text>
        {playlist.comment ? <Text style={styles.subtitle}>{playlist.comment}</Text> : <></>}
        <View style={styles.controls}>
          <Button title="Play Playlist" onPress={() => setQueue(playlist.songs, playlist.name, playlist.songs[0].id)} />
        </View>
        <View style={styles.songs}>
          {playlist.songs.map((s, index) => (
            <SongItem
              key={index}
              song={s}
              showArt={true}
              onPress={() => setQueue(playlist.songs, playlist.name, s.id)}
            />
          ))}
        </View>
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
  controls: {
    marginTop: 18,
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

export default React.memo(PlaylistView)
