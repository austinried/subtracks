import CoverArt from '@app/components/CoverArt'
import GradientBackground from '@app/components/GradientBackground'
import HeaderBar from '@app/components/HeaderBar'
import ImageGradientScrollView from '@app/components/ImageGradientScrollView'
import ListItem from '@app/components/ListItem'
import ListPlayerControls from '@app/components/ListPlayerControls'
import NothingHere from '@app/components/NothingHere'
import { useCoverArtFile } from '@app/hooks/cache'
import { useAlbumWithSongs, usePlaylistWithSongs } from '@app/hooks/music'
import { AlbumWithSongs, PlaylistWithSongs, Song } from '@app/models/music'
import { useStore } from '@app/state/store'
import { selectTrackPlayer } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import { useNavigation } from '@react-navigation/native'
import React, { useEffect, useState } from 'react'
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
  itemId: string
}>(({ songs, name, type, itemId }) => {
  const setQueue = useStore(selectTrackPlayer.setQueue)

  const _songs = [...songs]
  let typeName = ''

  if (type === 'album') {
    typeName = 'Album'
    _songs
      .sort((a, b) => a.title.localeCompare(b.title)) //
      .sort((a, b) => (a.track || 0) - (b.track || 0))
  } else {
    typeName = 'Playlist'
  }

  return (
    <>
      <ListPlayerControls
        style={styles.controls}
        songs={_songs}
        typeName={typeName}
        queueName={name}
        queueContextId={itemId}
        queueContextType={type}
      />
      <View style={styles.songs}>
        {_songs.map((s, i) => (
          <ListItem
            key={i}
            item={s}
            contextId={itemId}
            queueId={i}
            subtitle={s.artist}
            onPress={() => setQueue(songs, name, type, itemId, i)}
            showArt={type === 'playlist'}
          />
        ))}
      </View>
    </>
  )
})

const SongListDetails = React.memo<{
  title: string
  type: SongListType
  songList?: AlbumWithSongs | PlaylistWithSongs
  subtitle?: string
}>(({ title, songList, subtitle, type }) => {
  const coverArtFile = useCoverArtFile(songList?.coverArt, 'thumbnail')
  const [headerColor, setHeaderColor] = useState<string | undefined>(undefined)

  if (!songList) {
    return <SongListDetailsFallback />
  }

  return (
    <View style={styles.container}>
      {songList.itemType === 'album' && (
        <HeaderBar headerStyle={{ backgroundColor: headerColor }} title={title} contextItem={songList} />
      )}
      <ImageGradientScrollView
        imagePath={coverArtFile?.file?.path}
        style={styles.container}
        onGetColor={setHeaderColor}>
        <View style={styles.content}>
          <CoverArt type="cover" size="original" coverArt={songList.coverArt} style={styles.cover} />
          <Text style={styles.title}>{songList.name}</Text>
          {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : <></>}
          {songList.songs.length > 0 ? (
            <Songs songs={songList.songs} name={songList.name} type={type} itemId={songList.id} />
          ) : (
            <NothingHere height={300} width={250} />
          )}
        </View>
      </ImageGradientScrollView>
    </View>
  )
})

const PlaylistView = React.memo<{
  id: string
  title: string
}>(({ id, title }) => {
  const playlist = usePlaylistWithSongs(id)
  return <SongListDetails title={title} songList={playlist} subtitle={playlist?.comment} type="playlist" />
})

const AlbumView = React.memo<{
  id: string
  title: string
}>(({ id, title }) => {
  const album = useAlbumWithSongs(id)
  return (
    <SongListDetails
      title={title}
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

  return type === 'album' ? <AlbumView id={id} title={title} /> : <PlaylistView id={id} title={title} />
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
    height: 240,
    width: 240,
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
