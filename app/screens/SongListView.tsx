import CoverArt from '@app/components/CoverArt'
import GradientBackground from '@app/components/GradientBackground'
import ImageGradientScrollView from '@app/components/ImageGradientScrollView'
import ListItem from '@app/components/ListItem'
import ListPlayerControls from '@app/components/ListPlayerControls'
import NothingHere from '@app/components/NothingHere'
import { useAlbumWithSongs, useCoverArtUri, usePlaylistWithSongs } from '@app/hooks/music'
import { AlbumWithSongs, PlaylistWithSongs, Song } from '@app/models/music'
import { useSetQueue } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import React, { useEffect } from 'react'
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native'

type SongListType = 'album' | 'playlist'

const SongListDetailsFallback = React.memo(() => (
  <GradientBackground style={styles.fallback}>
    <ActivityIndicator size="large" color={colors.accent} />
  </GradientBackground>
))

const Songs = React.memo<{
  songs: Song[]
  name: string
  type: SongListType
}>(({ songs, name, type }) => {
  const setQueue = useSetQueue()

  const _songs = [...songs]
  let typeName = ''

  if (type === 'album') {
    typeName = 'Album'
    _songs.sort((a, b) => {
      if (b.track && a.track) {
        return a.track - b.track
      } else {
        return a.title.localeCompare(b.title)
      }
    })
  } else {
    typeName = 'Playlist'
  }

  return (
    <>
      <ListPlayerControls style={styles.controls} songs={songs} typeName={typeName} queueName={name} />
      <View style={styles.songs}>
        {_songs.map((s, i) => (
          <ListItem
            key={i}
            item={s}
            subtitle={s.artist}
            onPress={() => setQueue(songs, name, i)}
            showArt={type === 'playlist'}
          />
        ))}
      </View>
    </>
  )
})

const SongListDetails = React.memo<{
  type: SongListType
  songList?: AlbumWithSongs | PlaylistWithSongs
  subtitle?: string
}>(({ songList, subtitle, type }) => {
  const coverArtUri = useCoverArtUri()

  if (!songList) {
    return <SongListDetailsFallback />
  }

  return (
    <ImageGradientScrollView imageUri={coverArtUri(songList.coverArt)} style={styles.container}>
      <View style={styles.content}>
        <CoverArt coverArt={songList.coverArt} style={styles.cover} imageSize="original" />
        <Text style={styles.title}>{songList.name}</Text>
        {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : <></>}
        {songList.songs.length > 0 ? (
          <Songs songs={songList.songs} name={songList.name} type={type} />
        ) : (
          <NothingHere height={300} width={250} />
        )}
      </View>
    </ImageGradientScrollView>
  )
})

const PlaylistView = React.memo<{
  id: string
}>(({ id }) => {
  const playlist = usePlaylistWithSongs(id)
  return <SongListDetails songList={playlist} subtitle={playlist?.comment} type="playlist" />
})

const AlbumView = React.memo<{
  id: string
}>(({ id }) => {
  const album = useAlbumWithSongs(id)
  return (
    <SongListDetails
      songList={album}
      subtitle={(album?.artist || '') + (album?.year ? ' • ' + album?.year : '')}
      type="album"
    />
  )
})

const SongListView = React.memo<{
  id: string
  title: string
  type: SongListType
}>(({ id, title, type }) => {
  const navigation = useNavigation()

  useEffect(() => {
    navigation.setOptions({ title })
  })

  return type === 'album' ? <AlbumView id={id} /> : <PlaylistView id={id} />
})

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    alignItems: 'center',
    paddingTop: 10,
    paddingHorizontal: 20,
  },
  controls: {
    marginTop: 20,
  },
  title: {
    fontSize: 24,
    fontFamily: font.bold,
    color: colors.text.primary,
    marginTop: 20,
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
    height: 220,
    width: 220,
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

export default SongListView
