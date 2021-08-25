import CoverArt from '@app/components/CoverArt'
import GradientBackground from '@app/components/GradientBackground'
import HeaderBar from '@app/components/HeaderBar'
import ImageGradientFlatList from '@app/components/ImageGradientFlatList'
import ListItem from '@app/components/ListItem'
import ListPlayerControls from '@app/components/ListPlayerControls'
import { useCoverArtFile } from '@app/hooks/cache'
import { useAlbumWithSongs, usePlaylistWithSongs } from '@app/hooks/music'
import { AlbumWithSongs, PlaylistWithSongs, Song } from '@app/models/music'
import { useStore } from '@app/state/store'
import { selectTrackPlayer } from '@app/state/trackplayer'
import colors from '@app/styles/colors'
import font from '@app/styles/font'
import React, { useState } from 'react'
import { ActivityIndicator, StyleSheet, Text, View } from 'react-native'

type SongListType = 'album' | 'playlist'

const SongListDetailsFallback = React.memo(() => (
  <GradientBackground style={styles.fallback}>
    <ActivityIndicator size="large" color={colors.accent} />
  </GradientBackground>
))

const SongRenderItem: React.FC<{
  item: {
    song: Song
    contextId?: string
    queueId?: number
    subtitle?: string
    onPress?: () => void
    showArt?: boolean
  }
}> = ({ item }) => (
  <ListItem
    item={item.song}
    contextId={item.contextId}
    queueId={item.queueId}
    subtitle={item.subtitle}
    onPress={item.onPress}
    showArt={item.showArt}
    style={styles.listItem}
  />
)

const SongListDetails = React.memo<{
  title: string
  type: SongListType
  songList?: AlbumWithSongs | PlaylistWithSongs
  subtitle?: string
}>(({ title, songList, subtitle, type }) => {
  const coverArtFile = useCoverArtFile(songList?.coverArt, 'thumbnail')
  const [headerColor, setHeaderColor] = useState<string | undefined>(undefined)
  const setQueue = useStore(selectTrackPlayer.setQueue)

  if (!songList) {
    return <SongListDetailsFallback />
  }

  const _songs = [...songList.songs]
  let typeName = ''

  if (type === 'album') {
    typeName = 'Album'
    if (_songs.some(s => s.track === undefined)) {
      _songs.sort((a, b) => a.title.localeCompare(b.title))
    } else {
      _songs.sort((a, b) => {
        const aVal = (a.track as number) + (a.discNumber !== undefined ? a.discNumber * 10000 : 0)
        const bVal = (b.track as number) + (b.discNumber !== undefined ? b.discNumber * 10000 : 0)
        return aVal - bVal
      })
    }
  } else {
    typeName = 'Playlist'
  }

  return (
    <View style={styles.container}>
      <HeaderBar
        headerStyle={{ backgroundColor: headerColor }}
        title={title}
        contextItem={songList.itemType === 'album' ? songList : undefined}
      />
      <ImageGradientFlatList
        data={_songs.map((s, i) => ({
          song: s,
          contextId: songList.id,
          queueId: i,
          subtitle: s.artist,
          onPress: () => setQueue(_songs, songList.name, type, songList.id, i),
          showArt: songList.itemType === 'playlist',
        }))}
        renderItem={SongRenderItem}
        keyExtractor={(item, i) => i.toString()}
        backgroundProps={{
          imagePath: coverArtFile?.file?.path,
          style: styles.container,
          onGetColor: setHeaderColor,
        }}
        overScrollMode="never"
        windowSize={7}
        contentMarginTop={26}
        ListHeaderComponent={
          <View style={styles.content}>
            <CoverArt type="cover" size="original" coverArt={songList.coverArt} style={styles.cover} />
            <Text style={styles.title}>{songList.name}</Text>
            {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : <></>}
            {songList.songs.length > 0 && (
              <ListPlayerControls
                style={styles.controls}
                songs={_songs}
                typeName={typeName}
                queueName={songList.name}
                queueContextId={songList.id}
                queueContextType={type}
              />
            )}
          </View>
        }
      />
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
  listItem: {
    paddingHorizontal: 20,
  },
})

export default SongListView
